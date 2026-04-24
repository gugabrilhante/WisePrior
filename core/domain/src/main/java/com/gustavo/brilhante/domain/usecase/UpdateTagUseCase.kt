package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TagRepository
import com.gustavo.brilhante.model.Tag
import javax.inject.Inject

class UpdateTagUseCase @Inject constructor(private val repository: TagRepository) {
    suspend operator fun invoke(tag: Tag) = repository.updateTag(tag)
}
