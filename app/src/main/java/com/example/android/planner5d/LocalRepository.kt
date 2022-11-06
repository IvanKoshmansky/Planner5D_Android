package com.example.android.planner5d

import com.example.android.planner5d.localdb.LocalDatabase
import com.example.android.planner5d.localdb.asDomainModel
import com.example.android.planner5d.models.FloorPlan
import com.example.android.planner5d.models.PlannerProjectPaging
import com.example.android.planner5d.models.asDatabaseModel
import com.example.android.planner5d.webservice.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
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

    sealed class RoomPlanFromRepo {
        data class RoomPlanLoading (val percent: Int): RoomPlanFromRepo()
        data class RoomPlanOk (val roomPlan: FloorPlan) : RoomPlanFromRepo()
        data class RoomPlanError (val e: Exception) : RoomPlanFromRepo()
    }

    private suspend fun getRoomPlanFromServer(projectKey: String): RoomPlanFromRepo {
        Timber.d("debug_regex: начата загрузка плана с сервера")
        var floorPlan: FloorPlan? = null
        try {
            val apiResponse = apiService.getProjectInfo(projectKey).await()
            floorPlan = apiResponse.asDomainObject()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //delay(5000)
        Timber.d("debug_regex: загружен план $floorPlan")
        return if (floorPlan != null) {
            RoomPlanFromRepo.RoomPlanOk(floorPlan)
        } else {
            RoomPlanFromRepo.RoomPlanError(java.lang.Exception("unable to get room plan"))
        }
    }

    // StateFlow требует значение по умолчанию (здесь - пустой план)
    private val _floorStateFlow = MutableStateFlow<RoomPlanFromRepo>(
        RoomPlanFromRepo.RoomPlanOk(FloorPlan.fillEmpty())
    )
    val floorStateFlow: StateFlow<RoomPlanFromRepo> = _floorStateFlow

    fun setupFloorState(projectKey: String, externalScope: CoroutineScope) {
        externalScope.launch {
            // поскольку подгрузка идет не внутри билдера Flow {}
            // то flowOn() для переключения диспетчера не нужна (можно использовать withContext())
            // первый шаг - состояние загрузки
            _floorStateFlow.value = RoomPlanFromRepo.RoomPlanLoading(50)
            Timber.d("debug_regex: stateFlow <- состояние загрузки 50%")
            withContext(Dispatchers.IO) {
                // блок withContext() не создает новую корутину
                // он выполняет последовательно код в новом контексте (при смене диспетчера в новом потоке)
                // и затем возвращается к предыдущему контексту
                // второй шаг - загрузка завершена
                _floorStateFlow.value = getRoomPlanFromServer(projectKey)
                Timber.d("debug_regex: stateFlow <- новый план 100%")
            }
        }
    }
}
