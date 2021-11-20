package com.android.andersenrickandmorty.sources

import android.net.Uri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.DataBase.useCharacterFilters
import com.android.andersenrickandmorty.common.DataBase.useCharacterForDetails
import com.android.andersenrickandmorty.models.CharacterModel
import com.android.andersenrickandmorty.retrofit.RetrofitApi

// can take date into pager and after in our base
typealias OnPageLoadedListenerCharacter = (characters: List<CharacterModel>) -> Unit

class CharacterPagingSource(
    private val retrofitApi: RetrofitApi,
    private val onPageLoadedListener: OnPageLoadedListenerCharacter
) : PagingSource<Int, CharacterModel>() {

    private var previous: Int? = null
    private var next: Int? = null
    private val key = "page"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterModel> {

        val list = mutableListOf<CharacterModel>()
        defineSourceAndFillList(list)
        list.sortBy { it.id }

        // check thread count needs because https://rickandmortyapi.com
        // not response if have too many queries
        return try {
            if (online && !useCharacterFilters && !useCharacterForDetails
                && Thread.getAllStackTraces().size < 210
            ) {

                val nextPage: Int = params.key ?: FIRST_PAGE_INDEX
                val response = retrofitApi.getAllCharacters(nextPage)
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
                onPageLoadedListener(response.results)

                previous = prevPageNumber
                next = nextPageNumber
                LoadResult.Page(
                    data = response.results,
                    prevKey = prevPageNumber,
                    nextKey = nextPageNumber
                )
            } else {
                onPageLoadedListener(list)
                return LoadResult.Page(
                    data = list,
                    prevKey = previous,
                    nextKey = next
                )
            }
        } catch (e: Exception) {
            onPageLoadedListener(list)

            return LoadResult.Page(
                data = list,
                prevKey = previous,
                nextKey = next
            )
        }
    }

    private fun defineSourceAndFillList(workSet: MutableList<CharacterModel>) {
        when {
            useCharacterFilters -> {
                workSet.addAll(DataBase.filteredDataCharacter)
            }
            useCharacterForDetails -> {
                workSet.addAll(DataBase.detailsDataCharacter)
            }
            else -> {
                workSet.addAll(DataBase.charactersList)
            }
        }
    }

    // very important parameter
    override val keyReuseSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<Int, CharacterModel>): Int? {
        return state.anchorPosition
    }

    companion object {
        const val FIRST_PAGE_INDEX = 1
    }
}