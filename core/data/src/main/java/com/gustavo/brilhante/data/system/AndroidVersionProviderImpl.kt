package com.gustavo.brilhante.data.system

import android.os.Build
import com.gustavo.brilhante.domain.system.AndroidVersionProvider
import javax.inject.Inject

class AndroidVersionProviderImpl @Inject constructor() : AndroidVersionProvider {
    override val sdkInt: Int get() = Build.VERSION.SDK_INT
}
