package com.gustavo.brilhante.tasklist.presentation

import javax.inject.Inject

class LayoutModeResolver @Inject constructor() {
    fun isExpanded(screenWidthDp: Int): Boolean {
        return screenWidthDp >= 600
    }
}
