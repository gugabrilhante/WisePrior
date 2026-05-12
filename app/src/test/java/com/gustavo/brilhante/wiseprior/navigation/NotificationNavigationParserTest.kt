package com.gustavo.brilhante.wiseprior.navigation

import android.content.Intent
import com.gustavo.brilhante.notifications.EXTRA_TASK_ID
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationNavigationParserTest {

    private val parser = NotificationNavigationParser()

    @Test
    fun `getTaskId returns id when intent has positive task id`() {
        val intent = mockk<Intent>()
        every { intent.getLongExtra(EXTRA_TASK_ID, -1L) } returns 123L

        val result = parser.getTaskId(intent)

        assertEquals(123L, result)
    }

    @Test
    fun `getTaskId returns null when intent has no task id`() {
        val intent = mockk<Intent>()
        every { intent.getLongExtra(EXTRA_TASK_ID, -1L) } returns -1L

        val result = parser.getTaskId(intent)

        assertNull(result)
    }

    @Test
    fun `getTaskId returns null when intent has zero task id`() {
        val intent = mockk<Intent>()
        every { intent.getLongExtra(EXTRA_TASK_ID, -1L) } returns 0L

        val result = parser.getTaskId(intent)

        assertNull(result)
    }

    @Test
    fun `getTaskId returns null when intent is null`() {
        val result = parser.getTaskId(null)

        assertNull(result)
    }
}
