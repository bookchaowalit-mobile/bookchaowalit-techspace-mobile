package com.bookchaowalit.techspace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogEdgeCaseTest {
    private fun r(id: String, title: String, tags: Set<String> = setOf("kotlin"), words: Int = 100, day: Long = 0, summary: String = "") =
        TechResource(id, title, "https://example.com/$id", ResourceKind.ARTICLE, Level.BEGINNER, tags, summary, words, day)

    @Test
    fun blankFilterTagsAreIgnored() {
        val c = Catalog(listOf(r("1", "One"), r("2", "Two", tags = setOf("swift"))))
        assertEquals(2, c.search(ResourceFilter(tags = setOf("", "  "))).size)
        assertEquals(listOf("1"), c.search(ResourceFilter(tags = setOf(" ", "#Kotlin"))).map { it.id })
    }

    @Test
    fun blankResourceTagsAreNotCounted() {
        val c = Catalog(listOf(r("1", "One", tags = setOf(" ", "Kotlin")), r("2", "Two", tags = setOf("#kotlin", "KMP"))))
        assertEquals(listOf("kotlin" to 2, "kmp" to 1), c.tagCounts())
    }

    @Test
    fun tagNormalisation() {
        assertEquals("kotlin", normalizeTag("##Kotlin"))
        assertEquals("jetpack-compose", normalizeTag("  Jetpack \t Compose "))
        assertEquals("", normalizeTag(" # "))
    }

    @Test
    fun titleOrderIsCaseInsensitiveAndStable() {
        val c = Catalog(listOf(r("3", "Zebra"), r("2", "apple"), r("1", "Apple")))
        assertEquals(listOf("1", "2", "3"), c.search(ResourceFilter()).map { it.id })
        assertEquals(listOf("1", "2", "3"), c.search(ResourceFilter(), SortOrder.SHORTEST).map { it.id })
    }

    @Test
    fun queryOfOnlyWhitespaceMatchesEverything() {
        val c = Catalog(listOf(r("1", "One"), r("2", "Two")))
        assertEquals(2, c.search(ResourceFilter(query = "   ")).size)
        assertEquals(0, c.score(c.resources[0], " \t "))
    }

    @Test
    fun multiTermScoresAdd() {
        val res = r("1", "Kotlin coroutines", tags = setOf("async"), summary = "structured concurrency")
        val c = Catalog(listOf(res))
        assertEquals(3 + 3 + 1, c.score(res, "KOTLIN coroutines concurrency"))
        assertEquals(0, c.score(res, "swift"))
    }

    @Test
    fun urlValidation() {
        for (bad in listOf("ftp://x.com", "https://", "https:///path", "https:// x.com", "example.com", "")) {
            assertFalse(isHttpUrl(bad), bad)
        }
        assertTrue(isHttpUrl("HTTPS://Example.com?q=1"))
        assertFailsWith<IllegalArgumentException> { r("1", "  ") }
        assertFailsWith<IllegalArgumentException> { r("1", "x", words = -1) }
    }

    @Test
    fun readingMinutesBoundaries() {
        assertEquals(1, readingMinutes(0))
        assertEquals(1, readingMinutes(200))
        assertEquals(2, readingMinutes(201))
        assertFailsWith<IllegalArgumentException> { readingMinutes(10, 0) }
    }

    @Test
    fun bookmarksSurviveToggleCycles() {
        val c = Catalog(listOf(r("1", "One"), r("2", "Two")))
        assertTrue(c.toggleBookmark("2"))
        assertTrue(c.toggleBookmark("1"))
        assertFalse(c.toggleBookmark("2"))
        assertTrue(c.toggleBookmark("2"))
        assertEquals(listOf("1", "2"), c.bookmarked().map { it.id })
        assertFailsWith<IllegalArgumentException> { c.toggleBookmark("nope") }
    }
}
