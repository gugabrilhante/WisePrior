package com.gustavo.brilhante.tasklist.presentation

import com.gustavo.brilhante.common.DateFormatter
import com.gustavo.brilhante.domain.usecase.AddTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTagUseCase
import com.gustavo.brilhante.domain.usecase.DeleteTaskUseCase
import com.gustavo.brilhante.domain.usecase.GetTagsUseCase
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTagUseCase
import com.gustavo.brilhante.domain.usecase.UpdateTaskUseCase
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.notifications.NotificationScheduler
import com.gustavo.brilhante.tasklist.model.TaskCollection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskListTagEditorTest {

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val getTagsUseCase: GetTagsUseCase = mockk()
    private val addTagUseCase: AddTagUseCase = mockk(relaxed = true)
    private val updateTagUseCase: UpdateTagUseCase = mockk(relaxed = true)
    private val deleteTagUseCase: DeleteTagUseCase = mockk(relaxed = true)
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val dateFormatter: DateFormatter = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getTasksUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(tags: List<Tag> = emptyList()): TaskListViewModel {
        every { getTagsUseCase() } returns MutableStateFlow(tags)
        return TaskListViewModel(
            getTasksUseCase, deleteTaskUseCase, updateTaskUseCase, getTagsUseCase,
            addTagUseCase, updateTagUseCase, deleteTagUseCase,
            notificationScheduler, dateFormatter
        )
    }

    // ── show / dismiss ────────────────────────────────────────────────────────

    @Test
    fun `given idle state, when showAddTag called, then showTagEditor is true and editingTag is null`() {
        val viewModel = buildViewModel()

        viewModel.showAddTag()

        val state = viewModel.uiState.value
        assertTrue(state.showTagEditor)
        assertNull(state.editingTag)
    }

    @Test
    fun `given a tag, when showEditTag called, then showTagEditor is true and editingTag matches that tag`() {
        val viewModel = buildViewModel()
        val tag = Tag(id = 1L, name = "Work", color = 0xFF3B82F6L)

        viewModel.showEditTag(tag)

        val state = viewModel.uiState.value
        assertTrue(state.showTagEditor)
        assertEquals(tag, state.editingTag)
    }

    @Test
    fun `given editor open, when dismissTagEditor called, then showTagEditor is false and editingTag is null`() {
        val viewModel = buildViewModel()
        viewModel.showEditTag(Tag(id = 1L, name = "Work", color = 0L))

        viewModel.dismissTagEditor()

        val state = viewModel.uiState.value
        assertFalse(state.showTagEditor)
        assertNull(state.editingTag)
    }

    // ── saveTag — validation ──────────────────────────────────────────────────

    @Test
    fun `given blank name, when saveTag called, then addTagUseCase is never invoked`() = runTest {
        val viewModel = buildViewModel()

        viewModel.saveTag("   ", 0xFF0000L)

        coVerify(exactly = 0) { addTagUseCase(any()) }
    }

    // ── saveTag — new tag ─────────────────────────────────────────────────────

    @Test
    fun `given fewer than max tags, when saveTag called, then addTagUseCase invoked with trimmed name`() = runTest {
        val viewModel = buildViewModel(tags = emptyList())

        viewModel.saveTag("  Personal  ", 0xFF22C55EL)

        coVerify(exactly = 1) {
            addTagUseCase(match { it.name == "Personal" && it.color == 0xFF22C55EL })
        }
        assertFalse(viewModel.uiState.value.showTagEditor)
    }

    @Test
    fun `given tag count at maximum, when saveTag called for new tag, then error is set and tag is not added`() = runTest {
        val maxTags = (1L..5L).map { Tag(id = it, name = "Tag $it", color = 0L) }
        val viewModel = buildViewModel(tags = maxTags)

        viewModel.saveTag("Overflow tag", 0xFF0000L)

        coVerify(exactly = 0) { addTagUseCase(any()) }
        assertEquals("Maximum of 5 tags reached", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.showTagEditor)
    }

    // ── saveTag — update existing ─────────────────────────────────────────────

    @Test
    fun `given editing tag, when saveTag called, then updateTagUseCase invoked with updated fields`() = runTest {
        val original = Tag(id = 7L, name = "Old name", color = 0L)
        val viewModel = buildViewModel(tags = listOf(original))
        viewModel.showEditTag(original)

        viewModel.saveTag("New name", 0xFF9900L)

        coVerify(exactly = 1) {
            updateTagUseCase(original.copy(name = "New name", color = 0xFF9900L))
        }
        assertFalse(viewModel.uiState.value.showTagEditor)
    }

    @Test
    fun `given editing tag, when saveTag called with whitespace, then name is trimmed before update`() = runTest {
        val original = Tag(id = 8L, name = "Old", color = 0L)
        val viewModel = buildViewModel(tags = listOf(original))
        viewModel.showEditTag(original)

        viewModel.saveTag("  Trimmed  ", 0L)

        coVerify { updateTagUseCase(match { it.name == "Trimmed" }) }
    }

    // ── deleteTag ─────────────────────────────────────────────────────────────

    @Test
    fun `given a tag, when deleteTag called, then deleteTagUseCase invoked and editor dismissed`() = runTest {
        val tag = Tag(id = 2L, name = "Delete me", color = 0L)
        val viewModel = buildViewModel(tags = listOf(tag))
        viewModel.showEditTag(tag)

        viewModel.deleteTag(tag)

        coVerify(exactly = 1) { deleteTagUseCase(tag) }
        assertFalse(viewModel.uiState.value.showTagEditor)
    }

    @Test
    fun `given active ByTag collection, when that tag deleted, then selection falls back to All`() = runTest {
        val tag = Tag(id = 3L, name = "Active tag", color = 0L)
        val viewModel = buildViewModel(tags = listOf(tag))
        viewModel.onCollectionSelected(TaskCollection.ByTag(tag.id))
        assertEquals(TaskCollection.ByTag(tag.id), viewModel.uiState.value.selectedCollection)

        viewModel.deleteTag(tag)

        assertEquals(TaskCollection.All, viewModel.uiState.value.selectedCollection)
    }

    @Test
    fun `given active ByTag collection for another tag, when unrelated tag deleted, then selection is unchanged`() = runTest {
        val deletedTag = Tag(id = 10L, name = "Gone", color = 0L)
        val activeTag  = Tag(id = 20L, name = "Active", color = 0L)
        val viewModel = buildViewModel(tags = listOf(deletedTag, activeTag))
        viewModel.onCollectionSelected(TaskCollection.ByTag(activeTag.id))

        viewModel.deleteTag(deletedTag)

        assertEquals(TaskCollection.ByTag(activeTag.id), viewModel.uiState.value.selectedCollection)
    }

    // ── addTagUseCase not called when updating ────────────────────────────────

    @Test
    fun `given editing tag, when saveTag called, then addTagUseCase is never invoked`() = runTest {
        val original = Tag(id = 5L, name = "Existing", color = 0L)
        val viewModel = buildViewModel(tags = listOf(original))
        viewModel.showEditTag(original)

        viewModel.saveTag("Renamed", 0L)

        coVerify(exactly = 0) { addTagUseCase(any()) }
    }
}
