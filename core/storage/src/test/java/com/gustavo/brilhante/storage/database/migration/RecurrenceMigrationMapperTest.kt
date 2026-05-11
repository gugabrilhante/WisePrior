package com.gustavo.brilhante.storage.database.migration

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecurrenceMigrationMapperTest {

    @Test
    fun `map DAILY returns DAYS`() {
        assertThat(RecurrenceMigrationMapper.map("DAILY")).isEqualTo("DAYS")
    }

    @Test
    fun `map WEEKLY returns WEEKS`() {
        assertThat(RecurrenceMigrationMapper.map("WEEKLY")).isEqualTo("WEEKS")
    }

    @Test
    fun `map MONTHLY returns MONTHS`() {
        assertThat(RecurrenceMigrationMapper.map("MONTHLY")).isEqualTo("MONTHS")
    }

    @Test
    fun `map NONE returns NONE`() {
        assertThat(RecurrenceMigrationMapper.map("NONE")).isEqualTo("NONE")
    }

    @Test
    fun `map null returns NONE`() {
        assertThat(RecurrenceMigrationMapper.map(null)).isEqualTo("NONE")
    }

    @Test
    fun `map invalid value returns NONE`() {
        assertThat(RecurrenceMigrationMapper.map("INVALID")).isEqualTo("NONE")
    }
}
