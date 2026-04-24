package com.gustavo.brilhante.domain.repository

import com.gustavo.brilhante.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getTags(): Flow<List<Tag>>
    suspend fun addTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
}
