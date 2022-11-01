package com.example.android.planner5d

import com.example.android.planner5d.localdb.LocalDatabase
import com.example.android.planner5d.localdb.asDomainModel
import com.example.android.planner5d.models.PlannerProjectPaging
import com.example.android.planner5d.models.RoomPlan
import com.example.android.planner5d.models.asDatabaseModel
import com.example.android.planner5d.webservice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

//
// репозиторий является медиатором между двумя источниками данных: localdb и apiservice
//

class LocalRepository @Inject constructor (val localDatabase: LocalDatabase, val apiService: ApiService) {

    private suspend fun serverRequest(startPage: Int, count: Int): List<PlannerProjectPaging> {
        val plannerProjectsPaging = mutableListOf<PlannerProjectPaging>()
        for (reqPage in startPage until startPage + count) {
            try {
                val apiResponse = apiService.getGallery("$reqPage").await()
                if (apiResponse.items.isNotEmpty()) {
                    if (apiResponse.page == reqPage) {
                        plannerProjectsPaging += apiResponse.asDomainModel()  // конкатенация списков
                        if (apiResponse.page == apiResponse.pages) {
                            break
                        }
                    }
                } else {
                    break
                }
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }
        }
        return plannerProjectsPaging
    }

    // номер страницы начинается с 1, count - количество страниц
    // в случае отсутствия страницы с заданным номером как на сервере так и в кэше, возвращается пустой список
    suspend fun getCachedGallery(startPage: Int, count: Int): List<PlannerProjectPaging> {
        return withContext(Dispatchers.IO) {
            var result = listOf<PlannerProjectPaging>()
            var fetched = false
            var fromDB = localDatabase.databaseDao.getGallery(startPage, count)
            if (fromDB.isNotEmpty()) {
                if (fromDB.size == count * PAGE_SIZE) {
                    result = fromDB.asDomainModel()
                } else {
                    val lastPage = fromDB.last().page
                    val pagesTotal = fromDB.last().pages
                    if (lastPage < pagesTotal) {
                        var startPageToLoad = lastPage + 1
                        var pagesToLoad = startPage + count - lastPage
                        if (startPageToLoad > lastPage) startPageToLoad = lastPage
                        if (startPageToLoad + pagesToLoad > pagesTotal) {
                            pagesToLoad = pagesTotal - startPageToLoad
                        }
                        val fromServer = serverRequest(startPageToLoad, pagesToLoad)
                        if (fromServer.isNotEmpty()) {
                            localDatabase.databaseDao.insertProjects(*fromServer.asDatabaseModel().toTypedArray())
                            fetched = true
                        }
                    }
                }
            } else {
                val fromServer = serverRequest(startPage, count)
                if (fromServer.isNotEmpty()) {
                    localDatabase.databaseDao.insertProjects(*fromServer.asDatabaseModel().toTypedArray())
                    fetched = true
                }
            }
            if (fetched) {
                fromDB = localDatabase.databaseDao.getGallery(startPage, count)
                result = fromDB.asDomainModel()
            }
            result
        }
    }

    sealed class RoomPlanOrError {
        data class RoomPlanOk (val roomPlan: RoomPlan) : RoomPlanOrError()
        data class RoomPlanError (val e: Exception) : RoomPlanOrError()
    }

    suspend fun getCurrentRoomPlan(projectKey: String): RoomPlanOrError {
        try {
            val apiResponse = apiService.getProjectInfo("63ec7dfa77fb62b4c96f9f191410c07f").await()
            val roomPlan = RoomPlan(apiResponse.items[0].name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RoomPlanOrError.RoomPlanOk(RoomPlan.fillEmpty())
    }
}
