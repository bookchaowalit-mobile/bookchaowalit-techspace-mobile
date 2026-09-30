package com.bookchaowalit.techspace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogTest {
    private val kotlinGuide = TechResource(
        "1", "Kotlin Coroutines Guide", "https://example.com/coroutines", ResourceKind.ARTICLE, Level.INTERMEDIATE,
        setOf("Kotlin", "#Concurrency"), "Structured concurrency explained", wordCount = 1200, publishedEpochDay = 100,
    )
    private val composeVideo = TechResource(
        "2", "Compose in 10 minutes", "https://example.com/compose", ResourceKind.VIDEO, Level.BEGINNER,
        setOf("kotlin", "android ui"), "A tour of Jetpack Compose", wordCount = 0, publishedEpochDay = 300,
    )
    private val swiftTool = TechResource(
        "3", "SwiftLint", "http://example.org/swiftlint", ResourceKind.TOOL, Level.BEGINNER,
        setOf("swift", "lint"), "Linter that also mentions kotlin", wordCount = 400, publishedEpochDay = 200,
    )
    private val catalog = Catalog(listOf(kotlinGuide, composeVideo, swiftTool, kotlinGuide.copy(title = "dup")))

    @Test
    fun rejectsInvalidResources() {
        assertFailsWith<IllegalArgumentException> { kotlinGuide.copy(url = "javascript:alert(1)") }
        assertFailsWith<IllegalArgumentException> { kotlinGuide.copy(url = "https://") }
        assertFailsWith<IllegalArgumentException> { kotlinGuide.copy(title = "  ") }
        assertFailsWith<IllegalArgumentException> { kotlinGuide.copy(wordCount = -1) }
    }

    @Test
    fun deduplicatesById() {
        assertEquals(listOf("1", "2", "3"), catalog.resources.map { it.id })
        assertEquals("Kotlin Coroutines Guide", catalog.resources.first().title)
    }

    @Test
    fun normalizesTagsAndReadingTime() {
        assertEquals("android-ui", normalizeTag("  Android   UI "))
        assertEquals("concurrency", normalizeTag("#Concurrency"))
        assertEquals(6, readingMinutes(1200))
        assertEquals(2, readingMinutes(201))
        assertEquals(1, readingMinutes(0))
        assertFailsWith<IllegalArgumentException> { readingMinutes(10, 0) }
    }

    @Test
    fun relevanceRanksTitleOverTagOverSummary() {
        assertEquals(5, catalog.score(kotlinGuide, "kotlin")) // title 3 + tag 2
        assertEquals(2, catalog.score(composeVideo, "kotlin")) // tag only
        assertEquals(1, catalog.score(swiftTool, "kotlin")) // summary only
        assertEquals(0, catalog.score(swiftTool, "   "))
        val ids = catalog.search(ResourceFilter(query = "Kotlin")).map { it.id }
        assertEquals(listOf("1", "2", "3"), ids)
    }

    @Test
    fun filtersByKindLevelAndAllTags() {
        assertEquals(listOf("2", "3"), catalog.search(ResourceFilter(levels = setOf(Level.BEGINNER))).map { it.id }.sorted())
        assertEquals(listOf("3"), catalog.search(ResourceFilter(kinds = setOf(ResourceKind.TOOL))).map { it.id })
        assertEquals(listOf("2"), catalog.search(ResourceFilter(tags = setOf("Kotlin", "Android UI"))).map { it.id })
        assertTrue(catalog.search(ResourceFilter(query = "rust")).isEmpty())
    }

    @Test
    fun sortsByNewestAndShortest() {
        assertEquals(listOf("2", "3", "1"), catalog.search(ResourceFilter(), SortOrder.NEWEST).map { it.id })
        assertEquals(listOf("2", "3", "1"), catalog.search(ResourceFilter(), SortOrder.SHORTEST).map { it.id })
    }

    @Test
    fun countsTags() {
        val counts = catalog.tagCounts()
        assertEquals("kotlin" to 2, counts.first())
        assertEquals(5, counts.size)
    }

    @Test
    fun togglesBookmarksInOrder() {
        assertTrue(catalog.toggleBookmark("3"))
        assertTrue(catalog.toggleBookmark("1"))
        assertEquals(listOf("3", "1"), catalog.bookmarked().map { it.id })
        assertFalse(catalog.toggleBookmark("3"))
        assertEquals(listOf("1"), catalog.bookmarked().map { it.id })
        assertFailsWith<IllegalArgumentException> { catalog.toggleBookmark("nope") }
    }
}
