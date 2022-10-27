package com.example.android.planner5d.main.viewpaging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.android.planner5d.LocalRepository
import com.example.android.planner5d.PAGE_SIZE
import com.example.android.planner5d.models.*
import timber.log.Timber

//
// источник данных для пейджера
//

class GalleryPagingSource (private val localRepository: LocalRepository) : PagingSource<Int, PlannerProject>() {

    // основная функция подгрузки галереи из репозитория
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlannerProject> {
        // стартовая страница
        // нумерация в пейджере с нуля, в репозитории с единицы
        val startPage = (params.key ?: 0) + 1
        // количество страниц для загрузки (loadSize задается в строках)
        val pagesCount = if ((params.loadSize % PAGE_SIZE != 0) || (params.loadSize < PAGE_SIZE)) {
            params.loadSize / PAGE_SIZE + 1
        } else {
            params.loadSize / PAGE_SIZE }

        val cachedGallery = localRepository.getCachedGallery(startPage, pagesCount)
        var nextKey: Int? = null
        var prevKey: Int? = null
        if (cachedGallery.isNotEmpty()) {
            // номер последующей страницы или null если страница отсутствует
            if (cachedGallery.size == params.loadSize) {
                // все запрашиваемые данные загрузились
                if (cachedGallery.last().page < cachedGallery.last().pages) {
                    // не последняя страница
                    nextKey = cachedGallery.last().page
                }
            }
            // номер предыдущей страницы или null если страница не существует
            if (cachedGallery.first().page >= 2) {
                prevKey = cachedGallery.first().page - 2
            }
        } else {
            // в репозитории отсутствуют требуемые данные
            prevKey = startPage
        }

        return LoadResult.Page(
            data = cachedGallery.asViewModel(),
            prevKey = prevKey,
            nextKey = nextKey
        )
    }

    override fun getRefreshKey(state: PagingState<Int, PlannerProject>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}
