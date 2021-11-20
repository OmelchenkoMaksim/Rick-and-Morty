package com.android.andersenrickandmorty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.room.RickRepository

class PagingViewModelFactory(
    private val rickRepository: RickRepository,
    private val database: DataBase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PagingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PagingViewModel(rickRepository, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}