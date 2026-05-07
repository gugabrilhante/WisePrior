package com.gustavo.brilhante.data.logging

import android.util.Log
import com.gustavo.brilhante.domain.logging.Logger
import javax.inject.Inject

class AndroidLogger @Inject constructor() : Logger {
    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
