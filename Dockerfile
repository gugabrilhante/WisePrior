# syntax=docker/dockerfile:1
#
# WisePrior — reproducible Android build image
#
# Supports: assembleDebug · testDebugUnitTest · jacocoFullReport
#
# UI tests (connectedDebugAndroidTest) require a hardware device or a KVM-enabled
# host and are intentionally excluded from this image.
#
# ── Quick start ────────────────────────────────────────────────────────────────
#
#   Build the image:
#     DOCKER_BUILDKIT=1 docker build -t wiseprior-builder .
#
#   Run with a persistent Gradle cache volume (fast incremental rebuilds):
#     docker run --rm \
#       -v wiseprior-gradle:/root/.gradle \
#       wiseprior-builder
#
#   Extract the APK and coverage report:
#     docker run --rm \
#       -v wiseprior-gradle:/root/.gradle \
#       -v "$(pwd)/out":/out \
#       wiseprior-builder \
#       sh -c "cp -r app/build/outputs/apk/debug /out/apk \
#           && cp -r build/reports/jacoco /out/coverage"
#

# ── Base image ─────────────────────────────────────────────────────────────────
# Eclipse Temurin 21 matches the JDK used in GitHub Actions CI.
FROM eclipse-temurin:21-jdk-jammy

# ── System tools ───────────────────────────────────────────────────────────────
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        wget  \
        unzip \
        curl  \
        git   \
    && rm -rf /var/lib/apt/lists/*

# ── Android SDK environment ────────────────────────────────────────────────────
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

# Pin the command-line tools archive for a fully reproducible image.
ARG CMDLINE_TOOLS_VERSION=11076708
ARG ANDROID_COMPILE_SDK=36
ARG ANDROID_BUILD_TOOLS=36.0.0

# ── Install Android command-line tools ────────────────────────────────────────
RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && wget -q \
        "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" \
        -O /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d "${ANDROID_HOME}/cmdline-tools" \
    && mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm /tmp/cmdline-tools.zip

# ── Accept SDK licenses and install required packages ─────────────────────────
# This layer is cached until the ARG values above change.
RUN yes | sdkmanager --licenses > /dev/null \
    && sdkmanager \
        "platform-tools" \
        "build-tools;${ANDROID_BUILD_TOOLS}" \
        "platforms;android-${ANDROID_COMPILE_SDK}"

# ── Gradle wrapper bootstrap ───────────────────────────────────────────────────
# Copy only the wrapper files first so that the Gradle distribution download is
# cached independently of any source changes.
WORKDIR /app

COPY gradlew gradle.properties settings.gradle build.gradle ./
COPY gradle/ gradle/

RUN chmod +x gradlew \
    && sed -i '/org.gradle.java.home/d' gradle.properties

# Download and cache the Gradle distribution (gradle-9.1.0-bin.zip).
# The BuildKit cache mount makes this layer reusable across image rebuilds.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --version --no-daemon

# ── Dependency pre-fetch ───────────────────────────────────────────────────────
# Copy module build scripts before source files.  Because these files change
# less often than source code, Docker can reuse the dependency-download layer
# on subsequent builds when only source files are modified.
COPY app/build.gradle         app/build.gradle
COPY core/common/build.gradle     core/common/build.gradle
COPY core/data/build.gradle       core/data/build.gradle
COPY core/designsystem/build.gradle core/designsystem/build.gradle
COPY core/domain/build.gradle     core/domain/build.gradle
COPY core/model/build.gradle      core/model/build.gradle
COPY core/notifications/build.gradle core/notifications/build.gradle
COPY core/storage/build.gradle    core/storage/build.gradle
COPY core/ui/build.gradle         core/ui/build.gradle
COPY feature/taskeditor/build.gradle feature/taskeditor/build.gradle
COPY feature/tasklist/build.gradle   feature/tasklist/build.gradle

# Resolve and cache all compile + test dependencies without compiling source.
# The BuildKit cache mount persists the Gradle home across image rebuilds,
# so repeated builds only re-download dependencies that actually changed.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies \
        --no-daemon \
        --no-configuration-cache \
        --quiet \
    || true

# ── Full source copy ───────────────────────────────────────────────────────────
COPY . .

# Re-apply the gradle.properties fix after the full COPY (the source tree may
# have overwritten the earlier edit) and ensure the wrapper is still executable.
RUN sed -i '/org.gradle.java.home/d' gradle.properties \
    && chmod +x gradlew

# ── Build · Unit Tests · Coverage ─────────────────────────────────────────────
# --no-daemon avoids JVM process leaks; the BuildKit cache keeps dependencies warm.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew \
        assembleDebug \
        testDebugUnitTest \
        jacocoFullReport \
        --no-daemon \
        --stacktrace

# Default command — re-run the full build when the container is invoked without
# arguments, or pass a specific task: docker run wiseprior-builder ./gradlew test
CMD ["./gradlew", "assembleDebug", "testDebugUnitTest", "--no-daemon", "--stacktrace"]
