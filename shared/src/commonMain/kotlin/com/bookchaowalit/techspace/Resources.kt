package com.bookchaowalit.techspace

enum class ResourceKind { ARTICLE, VIDEO, TOOL, COURSE }

enum class Level { BEGINNER, INTERMEDIATE, ADVANCED }

/** A curated tech resource (article, video, tool or course). */
data class TechResource(
    val id: String,
    val title: String,
    val url: String,
    val kind: ResourceKind,
    val level: Level,
    val tags: Set<String>,
    val summary: String = "",
    val wordCount: Int = 0,
    val publishedEpochDay: Long = 0,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(title.isNotBlank()) { "title must not be blank" }
        require(isHttpUrl(url)) { "url must be http(s): $url" }
        require(wordCount >= 0) { "wordCount must be >= 0" }
    }
}

internal fun isHttpUrl(url: String): Boolean {
    val lower = url.trim().lowercase()
    val rest = when {
        lower.startsWith("https://") -> lower.removePrefix("https://")
        lower.startsWith("http://") -> lower.removePrefix("http://")
        else -> return false
    }
    val host = rest.substringBefore('/').substringBefore('?').substringBefore('#')
    return host.isNotEmpty() && ' ' !in host
}

/** Normalises a user-entered tag: trims, lowercases, collapses spaces to '-', strips '#'. */
fun normalizeTag(raw: String): String =
    raw.trim().removePrefix("#").lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString("-")

/** Estimated reading time in whole minutes (at least 1) at [wordsPerMinute]. */
fun readingMinutes(wordCount: Int, wordsPerMinute: Int = 200): Int {
    require(wordsPerMinute > 0) { "wordsPerMinute must be positive" }
    if (wordCount <= 0) return 1
    return (wordCount + wordsPerMinute - 1) / wordsPerMinute
}
