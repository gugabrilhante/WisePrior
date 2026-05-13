package com.gustavo.brilhante.data.system

import com.gustavo.brilhante.domain.system.TestEnvironmentProvider
import javax.inject.Inject

class TestEnvironmentProviderImpl @Inject constructor() : TestEnvironmentProvider {
    override fun isTesting(): Boolean {
        return try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
