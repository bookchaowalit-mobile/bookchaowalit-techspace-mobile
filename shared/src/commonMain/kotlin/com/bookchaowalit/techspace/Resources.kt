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
        require(!isVisiblyBlank(id)) { "id must not be blank" }
        require(!isVisiblyBlank(title)) { "title must not be blank" }
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
    val authority = rest.substringBefore('/').substringBefore('?').substringBefore('#')
    val host = authority.substringAfterLast('@').substringBefore(':')
    return host.isNotEmpty() && host.none { it.isWhitespace() || it.isISOControl() }
}

/** Zero-width / invisible format characters that keyboards and pastes leave behind. */
private fun isInvisible(c: Char): Boolean =
    c == '\u200B' || c == '\u200C' || c == '\u200D' || c == '\u2060' || c == '\uFEFF' || c == '\u00AD'

/** Blank check that also treats zero-width characters (U+200B, U+2060, BOM, ...) as blank. */
fun isVisiblyBlank(text: String): Boolean = text.all { it.isWhitespace() || isInvisible(it) }

/** Splits on any Unicode whitespace (incl. NBSP), dropping invisible characters. */
internal fun words(text: String): List<String> =
    text.filterNot(::isInvisible).splitWhere { it.isWhitespace() }

private fun String.splitWhere(isSeparator: (Char) -> Boolean): List<String> {
    val out = mutableListOf<String>()
    val current = StringBuilder()
    for (c in this) {
        if (isSeparator(c)) {
            if (current.isNotEmpty()) out += current.toString()
            current.clear()
        } else {
            current.append(c)
        }
    }
    if (current.isNotEmpty()) out += current.toString()
    return out
}

/** Normalises a user-entered tag: trims, lowercases, collapses spaces to '-', strips leading '#'s. */
fun normalizeTag(raw: String): String =
    words(raw).joinToString(" ").trimStart('#').lowercase().let(::words).joinToString("-")

/** Estimated reading time in whole minutes (at least 1) at [wordsPerMinute]. */
fun readingMinutes(wordCount: Int, wordsPerMinute: Int = 200): Int {
    require(wordsPerMinute > 0) { "wordsPerMinute must be positive" }
    if (wordCount <= 0) return 1
    return wordCount / wordsPerMinute + if (wordCount % wordsPerMinute == 0) 0 else 1
}
