package com.example.android.planner5d

import com.example.android.planner5d.localdb.LocalDatabase
import com.example.android.planner5d.localdb.asDomainModel
import com.example.android.planner5d.models.PlannerProjectPaging
import com.example.android.planner5d.models.asDatabaseModel
import com.example.android.planner5d.webservice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

//
// репозиторий является медиатором между двумя источниками данных: localdb и apiservice
//

class LocalRepository @Inject constructor (val localDatabase: LocalDatabase, val apiService: ApiService) {

    // асинхронный запрос на сервер
    private fun serverRequest(startPage: Int, count: Int): List<PlannerProjectPaging> {
        val plannerProjectsPaging = mutableListOf<PlannerProjectPaging>()  // итоговый пустой список
        for (page in startPage until startPage + count) {
            // пройти по страницам
            try {
                val apiResponse = apiService.getGallery(page.toString())
                plannerProjectsPaging += apiResponse.asDomainModel()  // конкатенация списков
                if (apiResponse.page == apiResponse.pages) {
                    break  // больше страниц нет
                }
            } catch (e: Exception) {
                e.printStackTrace()
                break  // ошибка
            }
        }
        return plannerProjectsPaging
    }

    // номер страницы начинается с 1, count - количество страниц
    // в случае отсутствия страницы с заданным номером как на сервере так и в кэше, возвращается пустой список
    suspend fun getCachedGallery(startPage: Int, count: Int): List<PlannerProjectPaging> {
        return withContext(Dispatchers.IO) {
            var fetched = false  // данные были подгружены с сервера в БД
            // загрузить из кэша
            var fromDB = localDatabase.databaseDao.getGallery(startPage, count)
            if (fromDB.isNotEmpty()) {
                // в кэше есть данные
                if (fromDB.size < count * PAGE_SIZE) {
                    // данных в кэше недостаточно
                    val lastPage = fromDB.last().page
                    val pagesTotal = fromDB.last().pages
                    if (lastPage < pagesTotal) {
                        // должны быть еще данные
                        var startPageToLoad = lastPage + 1
                        var pagesToLoad = startPage + count - lastPage
                        // контроль границ
                        if (startPageToLoad > lastPage) startPageToLoad = lastPage
                        if (startPageToLoad + pagesToLoad > pagesTotal) {
                            pagesToLoad = pagesTotal - startPageToLoad
                        }
                        // запрос недостающих данных на сервере
                        val fromServer = serverRequest(startPageToLoad, pagesToLoad)
                        // вставка в БД
                        localDatabase.databaseDao.insertProjects(*fromServer.asDatabaseModel().toTypedArray())
                        fetched = true
                    }
                }
            } else {
                // кэш пуст - получить все данные с сервера
                val fromServer = serverRequest(startPage, count)
                // вставка в БД
                localDatabase.databaseDao.insertProjects(*fromServer.asDatabaseModel().toTypedArray())
                fetched = true
            }
            if (fetched) {
                fromDB = localDatabase.databaseDao.getGallery(startPage, count)
            }
            // преобразование из формата БД в основной формат
            val result = fromDB.asDomainModel()
            result
        }
    }
}
