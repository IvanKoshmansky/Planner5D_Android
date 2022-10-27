package com.example.android.planner5d.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.android.planner5d.LocalRepository
import com.example.android.planner5d.PAGE_SIZE
import com.example.android.planner5d.main.viewpaging.GalleryPagingSource
import com.example.android.planner5d.models.PlannerProject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// TODO: карточки проектов желательно разместить через GridLayoutManager
// каждый новый проект можно открывать в новой активити и там уже при необходимости
// делать навигацию по этажам
// TODO: на фрагменте для просмотра планировки нужно сделать поддержку жестов:
// растянуть-сжать и движением пальцем
// плюс две FAB кнопки: увеличить и уменьшить

// ViewModel, которая используется для обзора проектов

@HiltViewModel
class MainViewModel @Inject constructor(
    val localRepository: LocalRepository
) : ViewModel() {

    private var pager = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE
        ),
        pagingSourceFactory = { GalleryPagingSource(localRepository) }
    )

    val galleryItems: Flow<PagingData<PlannerProject>> = pager.flow.cachedIn(viewModelScope)

    fun onItemClicked(id: String) {
    }
}
