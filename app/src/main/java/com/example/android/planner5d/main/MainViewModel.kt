package com.example.android.planner5d.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.example.android.planner5d.paint.ViewPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

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

    val floorViewState = localRepository.floorStateFlow

    private val _needToRefreshAdapter = MutableLiveData<Boolean>()
    val needToRefreshAdapter
        get() = _needToRefreshAdapter
    fun needToRefreshAdapterReset() {
        _needToRefreshAdapter.value = false
    }

    val viewPort = ViewPort.createDefault()

    private val _updateViewPort = MutableLiveData<Boolean>()
    val updateViewPort: LiveData<Boolean> = _updateViewPort

    fun viewPortUpdated() {
        _updateViewPort.value = false
    }

    fun setupFloorState(projectKey: String) {
        Timber.d("debug_regex: обращение к репозиторию")
        viewPort.setDefault()  // значение по-умолчанию
        localRepository.setupFloorState(projectKey, viewModelScope)  // асинхронный запрос, результат в floorStateFlow
    }

    // переход на второй фрагмент
    private val _navigateToFloorFragment = MutableLiveData<String?>()

    val navigateToFloorFragment
    get() = _navigateToFloorFragment

    fun onItemClicked(id: String) {
        _navigateToFloorFragment.value = id
    }

    fun navigateToFloorFragmentDone() {
        _navigateToFloorFragment.value = null
    }

    // увеличить масштаб
    fun zoomIn() {
        viewPort.zoomIn()
        _updateViewPort.value = true
    }

    // уменьшить масштаб
    fun zoomOut() {
        viewPort.zoomOut()
        _updateViewPort.value = true
    }

    fun clearLocalGallery() {
        viewModelScope.launch {
            localRepository.clearLocalGallery()
            _needToRefreshAdapter.value = true
        }
    }
}
