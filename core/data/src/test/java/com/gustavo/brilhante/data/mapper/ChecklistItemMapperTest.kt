package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.ChecklistItem
import com.gustavo.brilhante.storage.entity.ChecklistItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ChecklistItemMapperTest {

    @Test
    fun givenEntity_whenToModel_thenAllFieldsMapped() {
        val entity = ChecklistItemEntity(id = 1L, taskId = 10L, text = "Buy milk", isChecked = true)

        val model = entity.toModel()

        assertEquals(1L, model.id)
        assertEquals("Buy milk", model.text)
        assertEquals(true, model.isChecked)
    }

    @Test
    fun givenModel_whenToEntity_thenAllFieldsMappedWithCorrectTaskId() {
        val model = ChecklistItem(id = 5L, text = "Eggs", isChecked = false)

        val entity = model.toEntity(taskId = 42L)

        assertEquals(5L, entity.id)
        assertEquals(42L, entity.taskId)
        assertEquals("Eggs", entity.text)
        assertEquals(false, entity.isChecked)
    }

    @Test
    fun givenNewModel_whenToEntityWithZeroId_thenEntityHasZeroId() {
        val model = ChecklistItem(id = 0L, text = "New item", isChecked = false)

        val entity = model.toEntity(taskId = 1L)

        assertEquals(0L, entity.id)
    }
}
