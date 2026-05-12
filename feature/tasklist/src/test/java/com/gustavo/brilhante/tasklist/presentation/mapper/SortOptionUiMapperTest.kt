package com.gustavo.brilhante.tasklist.presentation.mapper

import com.gustavo.brilhante.model.TaskSortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SortOptionUiMapperTest {

    private val mapper = SortOptionUiMapper()

    @Test
    fun `map should return all sort options with correct selection`() {
        val selectedOption = TaskSortOption.CREATED_DESC
        
        val result = mapper.map(selectedOption)

        assertEquals(3, result.size)
        assertTrue(result.find { it.option == TaskSortOption.CREATED_DESC }?.isSelected == true)
        assertTrue(result.find { it.option == TaskSortOption.CREATED_ASC }?.isSelected == false)
        assertTrue(result.find { it.option == TaskSortOption.SMART_PRIORITY }?.isSelected == false)
    }
}
