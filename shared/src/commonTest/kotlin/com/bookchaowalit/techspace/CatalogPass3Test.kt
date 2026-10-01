package com.bookchaowalit.techspace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogPass3Test {
    private fun res(id: String, title: String, tags: Set<String> = emptySet()) =
        TechResource(id, title, "https://example.com/$id", ResourceKind.ARTICLE, Level.BEGINNER, tags)

    @Test
    fun invisibleOnlyTitleIsRejected() {
        assertFailsWith<IllegalArgumentException> { res("a", "​") }
        assertFailsWith<IllegalArgumentException> { res("a", "﻿⁠") }
        assertFailsWith<IllegalArgumentException> { res("​", "Title") }
    }

    @Test
    fun invisibleOnlyQueryShowsEverything() {
        val catalog = Catalog(listOf(res("a", "Kotlin"), res("b", "Swift")))
        assertEquals(listOf("a", "b"), catalog.search(ResourceFilter(query = "​")).map { it.id })
    }

    @Test
    fun nonBreakingSpacesInTagsAndQueriesActAsSpaces() {
        assertEquals("machine-learning", normalizeTag("Machine Learning"))
        assertEquals("", normalizeTag("​"))
        assertEquals("kotlin", normalizeTag("﻿kotlin"))
        val catalog = Catalog(listOf(res("a", "Intro to Kotlin", setOf("jvm")), res("b", "Swift")))
        assertEquals(listOf("a"), catalog.search(ResourceFilter(query = "kotlin intro")).map { it.id })
        assertEquals(listOf("a"), catalog.search(ResourceFilter(tags = setOf("​"))).filter { it.id == "a" }.map { it.id })
        assertEquals(2, catalog.search(ResourceFilter(tags = setOf("​"))).size)
    }

    @Test
    fun urlHostMustBeARealHost() {
        assertTrue(isHttpUrl("https://example.com:8080/x"))
        assertTrue(isHttpUrl("https://user@example.com/"))
        assertFalse(isHttpUrl("https://:8080/"))
        assertFalse(isHttpUrl("https://user@/path"))
        assertFalse(isHttpUrl("https://exa\tmple.com/"))
    }

    @Test
    fun readingMinutesDoesNotOverflow() {
        assertEquals(Int.MAX_VALUE / 200 + 1, readingMinutes(Int.MAX_VALUE))
    }
}
