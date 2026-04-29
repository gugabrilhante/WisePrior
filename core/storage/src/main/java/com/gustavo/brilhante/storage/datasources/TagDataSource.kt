package com.gustavo.brilhante.storage.datasources

import com.gustavo.brilhante.storage.dao.TagDao
import com.gustavo.brilhante.storage.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagDataSource @Inject constructor(private val tagDao: TagDao) {
    val allTags: Flow<List<TagEntity>> = tagDao.getAllTags()
    suspend fun insertTag(tag: TagEntity): Long = tagDao.insertTag(tag)
    suspend fun updateTag(tag: TagEntity) = tagDao.updateTag(tag)
    suspend fun deleteTagTransactional(tag: TagEntity) = tagDao.deleteTagAndRemoveFromTasks(tag)
}
