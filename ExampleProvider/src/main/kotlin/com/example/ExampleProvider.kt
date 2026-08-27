package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType

class ExampleProvider : MainAPI() {

    override var mainUrl = "https://topasnew24.ru/"

    override var name = "TopasNew24"

    override val supportedTypes = setOf(TvType.Movie)

    override var lang = "ru"

    override val hasMainPage = false

    override suspend fun search(query: String): List<SearchResponse> {
        return emptyList()
    }
}
