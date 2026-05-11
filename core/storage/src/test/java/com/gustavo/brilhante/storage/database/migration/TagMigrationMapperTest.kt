package com.gustavo.brilhante.storage.database.migration

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TagMigrationMapperTest {

    private val nameToId = mapOf(
        "Work" to 1L,
        "Personal" to 2L,
        "Urgent" to 3L
    )

    @Test
    fun `mapToIdString with null tags returns empty string`() {
        assertThat(TagMigrationMapper.mapToIdString(null, nameToId)).isEqualTo("")
    }

    @Test
    fun `mapToIdString with empty tags returns empty string`() {
        assertThat(TagMigrationMapper.mapToIdString("", nameToId)).isEqualTo("")
    }

    @Test
    fun `mapToIdString with blank tags returns empty string`() {
        assertThat(TagMigrationMapper.mapToIdString("  ", nameToId)).isEqualTo("")
    }

    @Test
    fun `mapToIdString with valid tags returns comma separated ids`() {
        assertThat(TagMigrationMapper.mapToIdString("Work,Personal", nameToId)).isEqualTo("1,2")
    }

    @Test
    fun `mapToIdString with spaces trims tags`() {
        assertThat(TagMigrationMapper.mapToIdString(" Work , Personal ", nameToId)).isEqualTo("1,2")
    }

    @Test
    fun `mapToIdString with unknown tags ignores them`() {
        assertThat(TagMigrationMapper.mapToIdString("Work,Unknown,Personal", nameToId)).isEqualTo("1,2")
    }

    @Test
    fun `mapToIdString with all unknown tags returns empty string`() {
        assertThat(TagMigrationMapper.mapToIdString("Unknown1,Unknown2", nameToId)).isEqualTo("")
    }

    @Test
    fun `mapToIdString with duplicated tags returns unique ids`() {
        assertThat(TagMigrationMapper.mapToIdString("Work,Work,Personal", nameToId)).isEqualTo("1,2")
    }
}
