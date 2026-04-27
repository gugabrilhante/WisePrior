package com.gustavo.brilhante.data.repository

import com.gustavo.brilhante.common.IoDispatcher
import com.gustavo.brilhante.data.mapper.toEntity
import com.gustavo.brilhante.data.mapper.toModel
import com.gustavo.brilhante.domain.repository.TagRepository
import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.storage.datasources.TagDataSource
import com.gustavo.brilhante.storage.datasources.TaskDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDataSource: TagDataSource,
    private val taskDataSource: TaskDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TagRepository {

    override fun getTags(): Flow<List<Tag>> =
        tagDataSource.allTags
            .map { entities -> entities.map { it.toModel() } }
            .flowOn(ioDispatcher)

    override suspend fun addTag(tag: Tag): Long = withContext(ioDispatcher) {
        tagDataSource.insertTag(tag.toEntity())
    }

    override suspend fun updateTag(tag: Tag) = withContext(ioDispatcher) {
        tagDataSource.updateTag(tag.toEntity())
    }

    override suspend fun deleteTag(tag: Tag) = withContext(ioDispatcher) {
        // Remove the tag id from all tasks that reference it
        val allTasks = taskDataSource.allTasks.first()
        allTasks.forEach { taskEntity ->
            if (taskEntity.tagIds.contains(tag.id)) {
                val updatedTagIds = taskEntity.tagIds.filter { it != tag.id }
                taskDataSource.updateTask(taskEntity.copy(tagIds = updatedTagIds))
            }
        }
        // Now delete the tag
        tagDataSource.deleteTag(tag.toEntity())
    }
}
