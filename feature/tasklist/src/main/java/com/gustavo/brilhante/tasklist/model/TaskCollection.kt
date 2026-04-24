package com.gustavo.brilhante.tasklist.model

sealed class TaskCollection {
    data object All : TaskCollection()
    data object Today : TaskCollection()
    data object Scheduled : TaskCollection()
    data object Flagged : TaskCollection()
    data object Completed : TaskCollection()

    /** Filter tasks by a specific Tag id. Future: extend to CustomList the same way. */
    data class ByTag(val tagId: Long) : TaskCollection()
}
