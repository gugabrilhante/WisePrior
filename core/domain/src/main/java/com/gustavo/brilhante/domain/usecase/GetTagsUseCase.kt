package com.gustavo.brilhante.domain.usecase

import com.gustavo.brilhante.domain.repository.TagRepository
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(private val repository: TagRepository) {
    operator fun invoke() = repository.getTags()
}
