package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse

class ExampleProvider : MainAPI() {

    override var mainUrl = "https://topasnew24.ru/"
    override var name = "TopasNew24"
    override val supportedTypes = setOf(TvType.Movie)
    override var lang = "ru"
    override val hasMainPage = false

    override suspend fun search(query: String): List<SearchResponse> {
    val url = "$mainUrl?s=${java.net.URLEncoder.encode(query, "UTF-8")}"
    val document = app.get(url).document

    return document.select("article, .post, .item").mapNotNull { element ->
        val link = element.selectFirst("a[href]") ?: return@mapNotNull null

        val title = element.selectFirst(
            "h1, h2, h3, .entry-title, .post-title, .title"
        )?.text()?.trim()
            ?: link.text().trim()

        if (title.isBlank()) return@mapNotNull null

        newMovieSearchResponse(
            title,
            fixUrl(link.attr("href")),
            TvType.Movie
        )
    }
}

override suspend fun load(url: String): LoadResponse {
    val document = app.get(url).document

    val title = document.selectFirst("h1")?.text()?.trim()
        ?: "TopasNew24"

    val description = document.selectFirst(
        "article p, .entry-content p"
    )?.text()?.trim()

    return newMovieLoadResponse(
        title,
        url,
        TvType.Movie,
        url
    ) {
        this.plot = description
    }
}
}
