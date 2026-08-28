package com.example

import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import org.jsoup.Jsoup
import java.net.URI

class ExampleProvider : MainAPI() {

    override var mainUrl = "https://topasnew24.ru/"

    override var name = "TopasNew24"

    override val supportedTypes = setOf(TvType.Movie)

    override var lang = "ru"

    override val hasMainPage = false

    /**
     * Wandelt relative Links in absolute URLs um.
     * Beispiel:
     * /film/test -> https://topasnew24.ru/film/test
     */
    private fun absoluteUrl(link: String): String {
        return try {
            URI(mainUrl).resolve(link).toString()
        } catch (e: Exception) {
            link
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {

        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")

        val searchUrl = "$mainUrl?s=$encodedQuery"

        val document = Jsoup
            .connect(searchUrl)
            .userAgent(
                "Mozilla/5.0 (Linux; Android 10) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0 Mobile Safari/537.36"
            )
            .timeout(15000)
            .get()

        return document
            .select("article, .post, .item")
            .mapNotNull { element ->

                val link = element
                    .select("a[href]")
                    .firstOrNull()
                    ?.attr("href")
                    ?.trim()
                    ?: return@mapNotNull null

                val absoluteLink = absoluteUrl(link)

                val title = element
                    .select(
                        "h1, h2, h3, " +
                        ".entry-title, " +
                        ".post-title, " +
                        ".title"
                    )
                    .firstOrNull()
                    ?.text()
                    ?.trim()
                    ?: element
                        .select("a[href]")
                        .firstOrNull()
                        ?.text()
                        ?.trim()
                        ?: absoluteLink
                            .substringAfterLast("/")
                            .substringBefore("?")
                            .replace("-", " ")
                            .replace("_", " ")
                            .trim()

                if (title.isBlank()) {
                    return@mapNotNull null
                }

                newMovieSearchResponse(
                    title = title,
                    url = absoluteLink,
                    type = TvType.Movie
                )
            }
    }

    override suspend fun load(url: String): LoadResponse {

        val document = Jsoup
            .connect(url)
            .userAgent(
                "Mozilla/5.0 (Linux; Android 10) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0 Mobile Safari/537.36"
            )
            .timeout(15000)
            .get()

        val title = document
            .select("h1")
            .firstOrNull()
            ?.text()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: document
                .select("title")
                .firstOrNull()
                ?.text()
                ?.trim()
                ?: "TopasNew24"

        val description = document
            .select(
                "article p, " +
                ".entry-content p, " +
                ".post-content p, " +
                ".content p"
            )
            .firstOrNull()
            ?.text()
            ?.trim()

        return newMovieLoadResponse(
            name = title,
            url = url,
            type = TvType.Movie,
            dataUrl = url
        ) {
            this.plot = description
        }
    }
}
