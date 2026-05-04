package com.gustavo.brilhante.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TaskTest {

    @Test
    fun `test task equality and copy`() {
        val task1 = Task(id = 1, title = "Test Task")
        val task2 = Task(id = 1, title = "Test Task")
        val task3 = task1.copy(title = "Updated Task")

        assertEquals(task1, task2)
        assertNotEquals(task1, task3)
        assertEquals("Updated Task", task3.title)
        assertEquals(task1.id, task3.id)
    }

    @Test
    fun `test default values`() {
        val task = Task(title = "Default Task")
        assertEquals(0L, task.id)
        assertEquals("", task.notes)
        assertEquals(Priority.NONE, task.priority)
        assertEquals(false, task.isCompleted)
    }
}
