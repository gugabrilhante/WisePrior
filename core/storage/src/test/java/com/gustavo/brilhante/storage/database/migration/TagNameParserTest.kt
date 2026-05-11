package com.gustavo.brilhante.storage.database.migration

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TagNameParserTest {

    @Test
    fun `parse empty string returns empty list`() {
        assertThat(TagNameParser.parse("")).isEmpty()
    }

    @Test
    fun `parse null string returns empty list`() {
        assertThat(TagNameParser.parse(null)).isEmpty()
    }

    @Test
    fun `parse blank string returns empty list`() {
        assertThat(TagNameParser.parse("   ")).isEmpty()
    }

    @Test
    fun `parse single tag returns single element list`() {
        assertThat(TagNameParser.parse("tag1")).containsExactly("tag1")
    }

    @Test
    fun `parse multiple tags returns list of tags`() {
        assertThat(TagNameParser.parse("tag1,tag2,tag3")).containsExactly("tag1", "tag2", "tag3")
    }

    @Test
    fun `parse tags with spaces trims them`() {
        assertThat(TagNameParser.parse(" tag1 ,  tag2  ")).containsExactly("tag1", "tag2")
    }

    @Test
    fun `parse duplicated tags returns unique tags`() {
        assertThat(TagNameParser.parse("tag1,tag1,tag2")).containsExactly("tag1", "tag2")
    }

    @Test
    fun `parse malformed commas ignores empty entries`() {
        assertThat(TagNameParser.parse("tag1,,tag2, ,tag3,")).containsExactly("tag1", "tag2", "tag3")
    }
}
