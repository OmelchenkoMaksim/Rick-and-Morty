package com.android.andersenrickandmorty.sources

import android.net.Uri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.models.LocationModel
import com.android.andersenrickandmorty.retrofit.RetrofitApi

typealias OnPageLoadedListenerLocation = (locations: List<LocationModel>) -> Unit

class LocationPagingSource(
    private val retrofitApi: RetrofitApi,
    private val listener: OnPageLoadedListenerLocation
) :
    PagingSource<Int, LocationModel>() {

    private var previous: Int? = null
    private var next: Int? = null
    private val key = "page"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LocationModel> {

        val list = mutableListOf<LocationModel>()
        defineSourceAndFillList(list)
        list.sortBy { it.id }

        return try {
            if (DataBase.online && !DataBase.useLocationFilters
                && Thread.getAllStackTraces().size < 210
            ) {
                val nextPage: Int = params.key ?: FIRST_PAGE_INDEX
                val response = retrofitApi.getAllLocations(nextPage)
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
                listener(response.results)

                previous = prevPageNumber
                next = nextPageNumber
                LoadResult.Page(
                    data = response.results,
                    prevKey = prevPageNumber,
                    nextKey = nextPageNumber
                )
            } else {
                listener(list)
                return LoadResult.Page(
                    data = list,
                    prevKey = previous,
                    nextKey = next
                )
            }
        } catch (e: Exception) {
            listener(list)
            return LoadResult.Page(
                data = list,
                prevKey = previous,
                nextKey = next
            )
        }
    }

    private fun defineSourceAndFillList(workSet: MutableList<LocationModel>) {
        when {
            DataBase.useLocationFilters -> {
                workSet.addAll(DataBase.filteredDataLocation)
            }
            else -> {
                workSet.addAll(DataBase.locationsList)
            }
        }
    }

    override val keyReuseSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<Int, LocationModel>): Int? {
        return state.anchorPosition
    }

    companion object {
        const val FIRST_PAGE_INDEX = 1
    }
}