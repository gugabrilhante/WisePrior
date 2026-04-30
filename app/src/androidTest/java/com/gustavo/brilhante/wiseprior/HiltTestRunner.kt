package com.gustavo.brilhante.wiseprior

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)

    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
    }
}
