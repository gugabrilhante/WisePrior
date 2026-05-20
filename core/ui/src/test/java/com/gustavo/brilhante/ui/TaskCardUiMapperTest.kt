package com.gustavo.brilhante.ui

import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.model.Task
import com.gustavo.brilhante.designsystem.R as DesignR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskCardUiMapperTest {

    private fun task(
        id: Long = 1L,
        title: String = "Task",
        notes: String = "",
        isCompleted: Boolean = false,
        isFlagged: Boolean = false,
        isUrgent: Boolean = false,
        priority: Priority = Priority.NONE,
        tagIds: List<Long> = emptyList(),
        createdAt: Long = 1000L
    ) = Task(
        id = id, title = title, notes = notes, isCompleted = isCompleted,
        isFlagged = isFlagged, isUrgent = isUrgent, priority = priority, tagIds = tagIds,
        createdAt = createdAt
    )

    private fun tag(id: Long, name: String = "Tag $id") = Tag(id = id, name = name, color = 0L)

    // ── Pass-through fields ───────────────────────────────────────────────────

    @Test
    fun `given a task, map passes through id and title`() {
        val result = TaskCardUiMapper.map(task(id = 42L, title = "My Task"), emptyList(), null)

        assertEquals(42L, result.id)
        assertEquals("My Task", result.title)
    }

    @Test
    fun `given a task with notes, map passes through notes`() {
        val result = TaskCardUiMapper.map(task(notes = "Detail notes"), emptyList(), null)

        assertEquals("Detail notes", result.notes)
    }

    @Test
    fun `given a formatted due date, map passes it through unchanged`() {
        val result = TaskCardUiMapper.map(task(), emptyList(), "Jan 15")

        assertEquals("Jan 15", result.formattedDueDate)
    }

    @Test
    fun `given no formatted due date, formattedDueDate is null`() {
        val result = TaskCardUiMapper.map(task(), emptyList(), null)

        assertNull(result.formattedDueDate)
    }

    // ── Completion state ──────────────────────────────────────────────────────

    @Test
    fun `given completed task, contentAlpha is 0_6f`() {
        val result = TaskCardUiMapper.map(task(isCompleted = true), emptyList(), null)

        assertEquals(0.6f, result.contentAlpha)
    }

    @Test
    fun `given incomplete task, contentAlpha is 1f`() {
        val result = TaskCardUiMapper.map(task(isCompleted = false), emptyList(), null)

        assertEquals(1f, result.contentAlpha)
    }

    @Test
    fun `given completed task, isTitleStrikethrough is true`() {
        val result = TaskCardUiMapper.map(task(isCompleted = true), emptyList(), null)

        assertTrue(result.isTitleStrikethrough)
    }

    @Test
    fun `given incomplete task, isTitleStrikethrough is false`() {
        val result = TaskCardUiMapper.map(task(isCompleted = false), emptyList(), null)

        assertFalse(result.isTitleStrikethrough)
    }

    @Test
    fun `given completed task, checkboxDescriptionRes is mark_incomplete`() {
        val result = TaskCardUiMapper.map(task(isCompleted = true), emptyList(), null)

        assertEquals(R.string.task_card_mark_incomplete, result.checkboxDescriptionRes)
    }

    @Test
    fun `given incomplete task, checkboxDescriptionRes is mark_complete`() {
        val result = TaskCardUiMapper.map(task(isCompleted = false), emptyList(), null)

        assertEquals(R.string.task_card_mark_complete, result.checkboxDescriptionRes)
    }

    @Test
    fun `completed and incomplete tasks produce different checkboxDescriptionRes values`() {
        val completed = TaskCardUiMapper.map(task(isCompleted = true), emptyList(), null)
        val incomplete = TaskCardUiMapper.map(task(isCompleted = false), emptyList(), null)

        assertTrue(completed.checkboxDescriptionRes != incomplete.checkboxDescriptionRes)
    }

    // ── Priority — NONE ───────────────────────────────────────────────────────

    @Test
    fun `given NONE priority, hasPriority is false`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.NONE), emptyList(), null)

        assertFalse(result.hasPriority)
    }

    @Test
    fun `given NONE priority, priorityColorRes is null`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.NONE), emptyList(), null)

        assertNull(result.priorityColorRes)
    }

    @Test
    fun `given NONE priority, priorityTextRes is null`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.NONE), emptyList(), null)

        assertNull(result.priorityTextRes)
    }

    // ── Priority — HIGH ───────────────────────────────────────────────────────

    @Test
    fun `given HIGH priority, hasPriority is true`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.HIGH), emptyList(), null)

        assertTrue(result.hasPriority)
    }

    @Test
    fun `given HIGH priority, priorityColorRes is priority_high color`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.HIGH), emptyList(), null)

        assertEquals(DesignR.color.priority_high, result.priorityColorRes)
    }

    @Test
    fun `given HIGH priority, priorityTextRes is priority_high string`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.HIGH), emptyList(), null)

        assertEquals(R.string.priority_high, result.priorityTextRes)
    }

    // ── Priority — MEDIUM ─────────────────────────────────────────────────────

    @Test
    fun `given MEDIUM priority, priorityColorRes is priority_medium color`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.MEDIUM), emptyList(), null)

        assertEquals(DesignR.color.priority_medium, result.priorityColorRes)
    }

    @Test
    fun `given MEDIUM priority, priorityTextRes is priority_medium string`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.MEDIUM), emptyList(), null)

        assertEquals(R.string.priority_medium, result.priorityTextRes)
    }

    // ── Priority — LOW ────────────────────────────────────────────────────────

    @Test
    fun `given LOW priority, priorityColorRes is priority_low color`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.LOW), emptyList(), null)

        assertEquals(DesignR.color.priority_low, result.priorityColorRes)
    }

    @Test
    fun `given LOW priority, priorityTextRes is priority_low string`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.LOW), emptyList(), null)

        assertEquals(R.string.priority_low, result.priorityTextRes)
    }

    @Test
    fun `each non-none priority uses a distinct color resource`() {
        val low = TaskCardUiMapper.map(task(priority = Priority.LOW), emptyList(), null)
        val medium = TaskCardUiMapper.map(task(priority = Priority.MEDIUM), emptyList(), null)
        val high = TaskCardUiMapper.map(task(priority = Priority.HIGH), emptyList(), null)

        assertTrue(low.priorityColorRes != medium.priorityColorRes)
        assertTrue(medium.priorityColorRes != high.priorityColorRes)
        assertTrue(low.priorityColorRes != high.priorityColorRes)
    }

    @Test
    fun `each non-none priority uses a distinct text resource`() {
        val low = TaskCardUiMapper.map(task(priority = Priority.LOW), emptyList(), null)
        val medium = TaskCardUiMapper.map(task(priority = Priority.MEDIUM), emptyList(), null)
        val high = TaskCardUiMapper.map(task(priority = Priority.HIGH), emptyList(), null)

        assertTrue(low.priorityTextRes != medium.priorityTextRes)
        assertTrue(medium.priorityTextRes != high.priorityTextRes)
        assertTrue(low.priorityTextRes != high.priorityTextRes)
    }

    // ── hasExpandableContent ──────────────────────────────────────────────────

    @Test
    fun `given plain task with no expandable attributes, hasExpandableContent is false`() {
        val result = TaskCardUiMapper.map(task(), emptyList(), null)

        assertFalse(result.hasExpandableContent)
    }

    @Test
    fun `given task with non-none priority, hasExpandableContent is true`() {
        val result = TaskCardUiMapper.map(task(priority = Priority.LOW), emptyList(), null)

        assertTrue(result.hasExpandableContent)
    }

    @Test
    fun `given flagged task, hasExpandableContent is true`() {
        val result = TaskCardUiMapper.map(task(isFlagged = true), emptyList(), null)

        assertTrue(result.hasExpandableContent)
    }

    @Test
    fun `given urgent task, hasExpandableContent is true`() {
        val result = TaskCardUiMapper.map(task(isUrgent = true), emptyList(), null)

        assertTrue(result.hasExpandableContent)
    }

    @Test
    fun `given task with non-blank notes, hasExpandableContent is true`() {
        val result = TaskCardUiMapper.map(task(notes = "Some detail"), emptyList(), null)

        assertTrue(result.hasExpandableContent)
    }

    @Test
    fun `given task with exactly two tags, hasExpandableContent is false`() {
        val allTags = listOf(tag(1L), tag(2L))

        val result = TaskCardUiMapper.map(task(tagIds = listOf(1L, 2L)), allTags, null)

        assertFalse(result.hasExpandableContent)
    }

    @Test
    fun `given task with three tags, hasExpandableContent is true`() {
        val allTags = listOf(tag(1L), tag(2L), tag(3L))

        val result = TaskCardUiMapper.map(task(tagIds = listOf(1L, 2L, 3L)), allTags, null)

        assertTrue(result.hasExpandableContent)
    }

    @Test
    fun `given task with checklist items, hasExpandableContent is true`() {
        val task = task().copy(checklistItems = listOf(ChecklistItem(text = "Step 1")))

        val result = TaskCardUiMapper.map(task, emptyList(), null)

        assertTrue(result.hasExpandableContent)
    }

    @Test
    fun `given task with checklist items, checklistItems are mapped to uiModel`() {
        val items = listOf(
            ChecklistItem(id = 1L, text = "Buy milk", isChecked = false),
            ChecklistItem(id = 2L, text = "Buy eggs", isChecked = true)
        )
        val task = task(isCompleted = false).copy(checklistItems = items)

        val result = TaskCardUiMapper.map(task, emptyList(), null)

        assertEquals(2, result.checklistItems.size)
        assertEquals(1L, result.checklistItems[0].id)
        assertEquals("Buy milk", result.checklistItems[0].text)
        assertFalse(result.checklistItems[0].isChecked)
        assertFalse(result.checklistItems[0].isDisplayChecked)

        assertEquals(2L, result.checklistItems[1].id)
        assertEquals("Buy eggs", result.checklistItems[1].text)
        assertTrue(result.checklistItems[1].isChecked)
        assertTrue(result.checklistItems[1].isDisplayChecked)
    }

    @Test
    fun `given completed task, all checklist items have isDisplayChecked as true`() {
        val items = listOf(
            ChecklistItem(id = 1L, text = "Item", isChecked = false)
        )
        val task = task(isCompleted = true).copy(checklistItems = items)

        val result = TaskCardUiMapper.map(task, emptyList(), null)

        assertTrue(result.checklistItems[0].isDisplayChecked)
    }

    // ── Tag filtering ─────────────────────────────────────────────────────────

    @Test
    fun `given available tags, map filters to only tags referenced by task tagIds`() {
        val allTags = listOf(tag(1L, "Work"), tag(2L, "Personal"), tag(3L, "Urgent"))

        val result = TaskCardUiMapper.map(task(tagIds = listOf(1L, 3L)), allTags, null)

        assertEquals(2, result.tags.size)
        assertTrue(result.tags.any { it.id == 1L })
        assertTrue(result.tags.any { it.id == 3L })
        assertFalse(result.tags.any { it.id == 2L })
    }

    @Test
    fun `given task tagId that matches no available tag, result tags list is empty`() {
        val allTags = listOf(tag(1L), tag(2L))

        val result = TaskCardUiMapper.map(task(tagIds = listOf(99L)), allTags, null)

        assertTrue(result.tags.isEmpty())
    }

    @Test
    fun `given empty available tags list, result tags list is always empty`() {
        val result = TaskCardUiMapper.map(task(tagIds = listOf(1L, 2L)), emptyList(), null)

        assertTrue(result.tags.isEmpty())
    }

    @Test
    fun `given task with no tagIds, result tags list is empty`() {
        val allTags = listOf(tag(1L), tag(2L))

        val result = TaskCardUiMapper.map(task(tagIds = emptyList()), allTags, null)

        assertTrue(result.tags.isEmpty())
    }
}
