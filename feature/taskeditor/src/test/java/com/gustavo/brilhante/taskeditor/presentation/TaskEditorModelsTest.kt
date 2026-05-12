package com.gustavo.brilhante.taskeditor.presentation

import com.gustavo.brilhante.model.RecurrenceRule
import com.gustavo.brilhante.model.RecurrenceUnit
import com.gustavo.brilhante.ui.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskEditorModelsTest {

    @Test
    fun `TaskEditorArgsResolver resolves mode correctly`() {
        assertEquals(TaskEditorMode.NEW, TaskEditorArgsResolver.resolveMode(null))
        assertEquals(TaskEditorMode.NEW, TaskEditorArgsResolver.resolveMode(-1L))
        assertEquals(TaskEditorMode.NEW, TaskEditorArgsResolver.resolveMode(0L))
        assertEquals(TaskEditorMode.EDIT, TaskEditorArgsResolver.resolveMode(1L))
        assertEquals(TaskEditorMode.EDIT, TaskEditorArgsResolver.resolveMode(100L))
    }

    @Test
    fun `TaskEditorArgsResolver resolves id correctly`() {
        assertEquals(-1L, TaskEditorArgsResolver.resolveId(null))
        assertEquals(-1L, TaskEditorArgsResolver.resolveId(-1L))
        assertEquals(1L, TaskEditorArgsResolver.resolveId(1L))
    }

    @Test
    fun `RecurrenceUiMapper maps recurring rule correctly`() {
        val unitOptions = listOf(
            RecurrenceUnitOptionUiModel(RecurrenceUnit.DAYS, UiText.DynamicString("Days")),
            RecurrenceUnitOptionUiModel(RecurrenceUnit.WEEKS, UiText.DynamicString("Weeks"))
        )
        val rule = RecurrenceRule(RecurrenceUnit.DAYS, 2)
        
        val result = RecurrenceUiMapper.map(rule, unitOptions)
        
        assertTrue(result.isRecurring)
        assertEquals(2, result.interval)
        assertEquals(RecurrenceUnit.DAYS, result.unit)
        assertEquals("2", result.intervalLabel)
        assertTrue(result.canDecrement)
        assertEquals("Days", (result.selectedUnitLabel as UiText.DynamicString).value)
        assertEquals(unitOptions, result.unitOptions)
    }

    @Test
    fun `RecurrenceUiMapper maps non-recurring rule correctly`() {
        val unitOptions = emptyList<RecurrenceUnitOptionUiModel>()
        val rule = RecurrenceRule.NONE
        
        val result = RecurrenceUiMapper.map(rule, unitOptions)
        
        assertFalse(result.isRecurring)
        assertEquals(1, result.interval)
        assertEquals(RecurrenceUnit.NONE, result.unit)
        assertEquals("1", result.intervalLabel)
        assertFalse(result.canDecrement)
        assertEquals("", (result.selectedUnitLabel as UiText.DynamicString).value)
    }

    @Test
    fun `RecurrenceUiMapper handles interval of 1`() {
        val rule = RecurrenceRule(RecurrenceUnit.WEEKS, 1)
        val result = RecurrenceUiMapper.map(rule, emptyList())
        assertFalse(result.canDecrement)
    }
}
