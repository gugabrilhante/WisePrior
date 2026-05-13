package com.gustavo.brilhante.domain.system

interface TestEnvironmentProvider {
    fun isTesting(): Boolean
}
