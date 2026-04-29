package com.gustavo.brilhante.data.mapper

import com.gustavo.brilhante.model.Tag
import com.gustavo.brilhante.storage.entity.TagEntity

fun TagEntity.toModel() = Tag(id = id, name = name, color = color)

fun Tag.toEntity() = TagEntity(id = id, name = name, color = color)
