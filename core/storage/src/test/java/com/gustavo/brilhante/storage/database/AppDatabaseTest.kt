package com.gustavo.brilhante.storage.database

import com.gustavo.brilhante.storage.database.AppDatabase.Companion.MIGRATION_3_4
import com.gustavo.brilhante.storage.database.AppDatabase.Companion.MIGRATION_4_5
import com.gustavo.brilhante.storage.database.AppDatabase.Companion.MIGRATION_5_6
import com.gustavo.brilhante.storage.database.AppDatabase.Companion.MIGRATION_6_7
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDatabaseTest {

    @Test
    fun `database name is correct`() {
        assertEquals("wiseprior_database", AppDatabase.databaseName)
    }

    @Test
    fun `migrations have correct versions`() {
        assertEquals(3, MIGRATION_3_4.startVersion)
        assertEquals(4, MIGRATION_3_4.endVersion)

        assertEquals(4, MIGRATION_4_5.startVersion)
        assertEquals(5, MIGRATION_4_5.endVersion)

        assertEquals(5, MIGRATION_5_6.startVersion)
        assertEquals(6, MIGRATION_5_6.endVersion)

        assertEquals(6, MIGRATION_6_7.startVersion)
        assertEquals(7, MIGRATION_6_7.endVersion)
    }
}
