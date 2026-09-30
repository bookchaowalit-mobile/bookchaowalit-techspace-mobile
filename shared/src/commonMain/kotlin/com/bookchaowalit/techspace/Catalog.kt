package com.bookchaowalit.techspace

data class ResourceFilter(
    val query: String = "",
    val kinds: Set<ResourceKind> = emptySet(),
    val levels: Set<Level> = emptySet(),
    /** All of these tags must be present (after normalisation). */
    val tags: Set<String> = emptySet(),
)

enum class SortOrder { RELEVANCE, NEWEST, SHORTEST }

/** In-memory catalog with search, filtering, tag counts and bookmarks. */
class Catalog(resources: List<TechResource>) {
    val resources: List<TechResource> = resources.distinctBy { it.id }

    private val bookmarks = linkedSetOf<String>()

    /**
     * Relevance score for [query]: title hits weigh 3, tag hits 2, summary hits 1,
     * summed over each whitespace-separated term. 0 means no match.
     */
    fun score(resource: TechResource, query: String): Int {
        val terms = query.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return 0
        val title = resource.title.lowercase()
        val summary = resource.summary.lowercase()
        val tags = resource.tags.map(::normalizeTag)
        return terms.sumOf { term ->
            (if (term in title) 3 else 0) +
                (if (tags.any { term in it }) 2 else 0) +
                (if (term in summary) 1 else 0)
        }
    }

    fun search(filter: ResourceFilter, order: SortOrder = SortOrder.RELEVANCE): List<TechResource> {
        // Blank tags (e.g. an empty chip) are ignored instead of matching nothing.
        val wantedTags = filter.tags.map(::normalizeTag).filter { it.isNotEmpty() }.toSet()
        val hasQuery = filter.query.isNotBlank()
        val matches = resources.filter { r ->
            (filter.kinds.isEmpty() || r.kind in filter.kinds) &&
                (filter.levels.isEmpty() || r.level in filter.levels) &&
                r.tags.map(::normalizeTag).containsAll(wantedTags) &&
                (!hasQuery || score(r, filter.query) > 0)
        }
        return when (order) {
            SortOrder.RELEVANCE ->
                if (hasQuery) {
                    matches.sortedWith(compareByDescending<TechResource> { score(it, filter.query) }.then(byTitle))
                } else {
                    matches.sortedWith(byTitle)
                }
            SortOrder.NEWEST -> matches.sortedWith(compareByDescending<TechResource> { it.publishedEpochDay }.then(byTitle))
            SortOrder.SHORTEST -> matches.sortedWith(compareBy<TechResource> { it.wordCount }.then(byTitle))
        }
    }

    /** Case-insensitive title order ("apple" next to "Apple", not after "Zebra"), then id for stability. */
    private val byTitle: Comparator<TechResource> =
        compareBy<TechResource, String>(String.CASE_INSENSITIVE_ORDER) { it.title }.thenBy { it.id }

    /** Tag -> number of resources, most used first (ties alphabetical). */
    fun tagCounts(): List<Pair<String, Int>> =
        resources.flatMap { r -> r.tags.map(::normalizeTag).filter { it.isNotEmpty() }.toSet() }
            .groupingBy { it }.eachCount()
            .toList()
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })

    /** Toggles a bookmark; returns true if the resource is now bookmarked. Unknown ids are rejected. */
    fun toggleBookmark(id: String): Boolean {
        require(resources.any { it.id == id }) { "unknown resource $id" }
        return if (id in bookmarks) {
            bookmarks.remove(id); false
        } else {
            bookmarks.add(id); true
        }
    }

    /** Bookmarked resources in the order they were bookmarked. */
    fun bookmarked(): List<TechResource> = bookmarks.mapNotNull { id -> resources.firstOrNull { it.id == id } }
}
