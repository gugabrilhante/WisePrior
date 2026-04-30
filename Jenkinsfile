pipeline {

    agent any

    options {
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        ansiColor('xterm')
    }

    environment {
        // Disable the Gradle daemon in CI — each build gets a clean JVM.
        GRADLE_OPTS = '-Dorg.gradle.daemon=false -Xmx4g -XX:+HeapDumpOnOutOfMemoryError'
        // AVD name reused across stages; emulator PID tracked for clean teardown.
        AVD_NAME    = 'ci_emulator_api34'
    }

    stages {

        // ── Stage 1: Checkout ─────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm

                // gradle.properties carries an org.gradle.java.home entry that
                // points to a local Android Studio JDK.  Remove it so Gradle
                // uses the JDK supplied by the Jenkins agent instead.
                sh "sed -i '/org.gradle.java.home/d' gradle.properties"

                sh './gradlew --version'
            }
        }

        // ── Stage 2: Build ────────────────────────────────────────────────────
        stage('Build') {
            steps {
                sh './gradlew assembleDebug --stacktrace'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/outputs/apk/debug/*.apk',
                                     fingerprint: true,
                                     allowEmptyArchive: false
                }
            }
        }

        // ── Stage 3: Unit Tests ───────────────────────────────────────────────
        stage('Unit Tests') {
            steps {
                sh './gradlew testDebugUnitTest --stacktrace --continue'
            }
            post {
                always {
                    junit testResults: '**/build/test-results/testDebugUnitTest/**/*.xml',
                          allowEmptyResults: false

                    publishHTML(target: [
                        allowMissing         : false,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : 'build/reports/allTests',
                        reportFiles          : 'index.html',
                        reportName           : 'Unit Test Report'
                    ])
                }
            }
        }

        // ── Stage 4: UI / Instrumented Tests ──────────────────────────────────
        //
        // Requires the Jenkins agent to have:
        //   • Android SDK with emulator + system-images;android-34;google_apis;x86_64
        //   • KVM enabled (Linux) or HAXM installed (macOS/Windows)
        //   • ANDROID_HOME set in the agent environment
        //
        stage('UI Tests') {
            steps {
                script {
                    // ── Boot emulator ─────────────────────────────────────────
                    sh """
                        # Create a fresh AVD (--force overwrites if already exists).
                        avdmanager create avd \\
                            --name    "${AVD_NAME}" \\
                            --package "system-images;android-34;google_apis;x86_64" \\
                            --device  "pixel_6" \\
                            --force

                        # Start emulator in the background.
                        emulator -avd ${AVD_NAME} \\
                            -no-window      \\
                            -no-audio       \\
                            -no-boot-anim   \\
                            -gpu swiftshader_indirect \\
                            -memory 2048 &

                        # Wait until the device is fully booted.
                        adb wait-for-device
                        timeout 300 sh -c \\
                            'until adb shell getprop sys.boot_completed 2>/dev/null | grep -q 1; do sleep 5; done'

                        # Disable animations to avoid timing flakiness in Compose tests.
                        adb shell settings put global window_animation_scale    0.0
                        adb shell settings put global transition_animation_scale 0.0
                        adb shell settings put global animator_duration_scale   0.0
                    """

                    // ── Grant notification permission up-front ─────────────────
                    // Avoids a system dialog blocking Compose UI interactions.
                    sh """
                        adb shell pm grant com.gustavo.brilhante.wiseprior \\
                            android.permission.POST_NOTIFICATIONS 2>/dev/null || true
                    """

                    // ── Run instrumented tests ─────────────────────────────────
                    sh './gradlew connectedDebugAndroidTest --stacktrace'
                }
            }
            post {
                always {
                    // Kill the emulator regardless of test outcome.
                    sh 'adb emu kill || true'

                    junit testResults: '**/build/outputs/androidTest-results/**/*.xml',
                          allowEmptyResults: true
                }
            }
        }

        // ── Stage 5: Code Coverage ────────────────────────────────────────────
        stage('Code Coverage') {
            steps {
                // jacocoFullReport is a custom aggregation task defined in the
                // root build.gradle; it merges exec files from all submodules.
                sh './gradlew jacocoFullReport --stacktrace'
            }
            post {
                always {
                    // Publish the HTML report so developers can browse coverage
                    // results directly from the Jenkins build page.
                    publishHTML(target: [
                        allowMissing         : false,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : 'build/reports/jacoco/jacocoFullReport/html',
                        reportFiles          : 'index.html',
                        reportName           : 'JaCoCo Coverage Report'
                    ])

                    // If the Jenkins instance has the JaCoCo plugin installed,
                    // this step enforces a coverage gate and draws trend charts.
                    jacoco(
                        execPattern        : '**/build/jacoco/*.exec',
                        classPattern       : '**/build/intermediates/javac/debug/classes',
                        sourcePattern      : '**/src/main/java',
                        exclusionPattern   : [
                            '**/*_HiltModules*',
                            '**/*_Factory*',
                            '**/Hilt_*',
                            '**/*_Impl*',
                            '**/ComposableSingletons*',
                            '**/R.class',
                            '**/R$*.class',
                            '**/BuildConfig.*'
                        ].join(','),
                        minimumLineCoverage: '70'
                    )
                }
            }
        }
    }

    post {
        always {
            // Remove workspace to keep the agent disk clean.
            cleanWs()
        }
        success {
            echo "✅ Pipeline passed — build #${env.BUILD_NUMBER} is green."
        }
        failure {
            echo "❌ Pipeline failed — check the stage logs above for details."
        }
        unstable {
            echo "⚠️  Pipeline unstable — some tests may have failed."
        }
    }
}
