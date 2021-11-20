package com.android.andersenrickandmorty.sources

import android.net.Uri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.models.EpisodeModel
import com.android.andersenrickandmorty.retrofit.RetrofitApi

typealias OnPageLoadedListenerEpisode = (episodes: List<EpisodeModel>) -> Unit

class EpisodePagingSource(
    private val retrofitApi: RetrofitApi,
    private val loadedListener: OnPageLoadedListenerEpisode
) :
    PagingSource<Int, EpisodeModel>() {

    private var previous: Int? = null
    private var next: Int? = null
    private val key = "page"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EpisodeModel> {

        val list = mutableListOf<EpisodeModel>()
        defineSourceAndFillList(list)
        list.sortBy { it.id }
        return try {
            if (DataBase.online && !DataBase.useEpisodeFilters
                && !DataBase.useEpisodeForDetails && Thread.getAllStackTraces().size < 210
            ) {
                val nextPage: Int = params.key ?: FIRST_PAGE_INDEX
                val response = retrofitApi.getAllEpisodes(nextPage)
                var nextPageNumber: Int? = null
                if (response.info.next != null) {
                    val uri = Uri.parse(response.info.next)
                    val nextPageQuery = uri.getQueryParameter(key)
                    nextPageNumber = nextPageQuery?.toInt()
                }
                var prevPageNumber: Int? = null
                if (response.info.prev != null) {
                    val uri = Uri.parse(response.info.prev)
                    val prevPageQuery = uri.getQueryParameter(key)
                    prevPageNumber = prevPageQuery?.toInt()
                }
                loadedListener(response.results)

                previous = prevPageNumber
                next = nextPageNumber
                LoadResult.Page(
                    data = response.results,
                    prevKey = prevPageNumber,
                    nextKey = nextPageNumber
                )
            } else {
                loadedListener(list)
                return LoadResult.Page(
                    data = list,
                    prevKey = previous,
                    nextKey = next
                )
            }
        } catch (e: Exception) {
            loadedListener(list)
            return LoadResult.Page(
                data = list,
                prevKey = previous,
                nextKey = next
            )
        }
    }

    private fun defineSourceAndFillList(workSet: MutableList<EpisodeModel>) {
        when {
            DataBase.useEpisodeFilters -> {
                workSet.addAll(DataBase.filteredDataEpisode)
            }
            DataBase.useEpisodeForDetails -> {
                workSet.addAll(DataBase.detailsDataEpisode)
            }
            else -> {
                workSet.addAll(DataBase.episodesList)
            }
        }
    }

    override val keyReuseSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<Int, EpisodeModel>): Int? {
        return state.anchorPosition
    }

    companion object {
        const val FIRST_PAGE_INDEX = 1
    }
}