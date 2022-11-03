package com.example.android.planner5d.webservice

import android.graphics.PointF
import com.example.android.planner5d.models.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//
// структуры ответов от сервера в формате JSON
//

// структура описания одного проекта в галерее
data class ApiPlannerProjectItem (
    val name: String?,   // имя проекта
    val cdate: String?,  // дата обновления проекта
    val key: String,     // идентификатор проекта
    val img: String?     // ссылка на изображение в галерее
)

// с поддержкой пагинации
data class ApiPlannerProjectResponsePaging (
    val items: List<ApiPlannerProjectItem>,
    val page: Int,  // номер страницы (поддержка пагинации)
    val pages: Int  // общее количество страниц
) {
    // как основная модель данных в приложении
    fun asDomainModel() = items.map {
        PlannerProjectPaging(
            name = it.name,
            cdate = it.cdate,
            key = it.key,
            img = it.img,
            page = this.page,
            pages = this.pages
        )
    }
}

//
// структуры описания проекта Planner5D (от наибольшего уровня вложенности к наименьшему)
//

// двумерная координата, Point
data class ApiPoint (
    val x: Double,
    val y: Double
)

// стена
data class ApiWall (
    @Json(name = "w")
    val width: Double,          // ширина (толщина) стены
    @Json(name = "items")
    val coords: List<ApiPoint>  // список из координат для построения стены
)

// элемент этажа
sealed class ApiFloorItem {

    // комната
    @JsonClass(generateAdapter = true)
    data class ApiRoom (
        //val className: String,   // "Room"
        val x: Double,
        val y: Double,
        @Json(name = "items")
        val walls: List<ApiWall>,  // список стен
        @Json(name = "sX")
        val scaleX: Double,
        @Json(name = "sY")
        val scaleY: Double
    ) : ApiFloorItem()

    // дверь
    @JsonClass(generateAdapter = true)
    data class ApiDoor (
        //val className: String,// "Door"
        val id: String,         // id модели
        @Json(name = "a")
        val angle: Double,      // угол поворота модели
        val x: Double,          // координаты x,y,z
        val y: Double,
        val z: Double,
        @Json(name = "sX")
        val scaleX: Double,     // масштаб по осям в процентах
        @Json(name = "sY")
        val scaleY: Double,
        @Json(name = "sZ")
        val scaleZ: Double,
        @Json(name = "fX")
        val flipX: Int,         // отразить по горизонтали
        @Json(name = "fY")
        val flipY: Int,         // отразить по вертикали
    ) : ApiFloorItem()

    // окно
    @JsonClass(generateAdapter = true)
    data class ApiWindow (
        //val className: String,// "Window"
        val id: String,         // id модели
        @Json(name = "a")
        val angle: Double,      // угол поворота модели
        val x: Double,          // координаты x,y,z
        val y: Double,
        val z: Double,
        @Json(name = "sX")
        val scaleX: Double,     // масштаб по осям в процентах
        @Json(name = "sY")
        val scaleY: Double,
        @Json(name = "sZ")
        val scaleZ: Double,
        @Json(name = "fX")
        val flipX: Int,         // отразить по горизонтали
        @Json(name = "fY")
        val flipY: Int,         // отразить по вертикали
    ) : ApiFloorItem()

    object Unknown : ApiFloorItem()
}

// этаж
data class ApiFloor (
    val className: String,         // "Floor"
    val items: List<ApiFloorItem>  // комната/дверь/окно/прочее (3D объект)
)

// данные проекта
data class ApiProjectData (
    val className: String,     // "Project"
    val width: Double,         // ширина (предположительно в мм)
    val height: Double,        // длина (предположительно в мм)
    val items: List<ApiFloor>  // массив этажей
)

// проект
data class ApiPlannerProject (
    val name: String,         // название проекта
    val data: ApiProjectData  // данные проекта
)

// проекты - полный ответ от сервера
data class ApiPlannerProjectsResponse (
    val items: List<ApiPlannerProject>
) {
    // преоборазование в основную модель данных в приложении
    fun asDomainObject(): FloorPlan {
        var result = FloorPlan.fillEmpty()
        if (this.items.isNotEmpty()) {
            this.items[0].also { plannerProject ->
                // имя проекта
                val projectName = plannerProject.name
                plannerProject.data.also { projectData ->
                    // размеры проекта
                    val projectWidth = projectData.width
                    val projectHeight = projectData.height
                    if (projectData.items.isNotEmpty()) {
                        projectData.items[0].also { floor ->
                            if (floor.items.isNotEmpty()) {
                                // перебор объектов первого этажа для преобразования 3D -> план 2D
                                val floorItems = mutableListOf<FloorItem>()
                                floor.items.forEach { apiFloorItem ->
                                    val floorItem = parseFloorItem(apiFloorItem)
                                    // добавляем только not null
                                    floorItem?.let {
                                        floorItems.add(it)
                                    }
                                }
                                result = FloorPlan(
                                    projectName = projectName,
                                    width = projectWidth,
                                    height = projectHeight,
                                    floorItems = listOf(*floorItems.toTypedArray())
                                )
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    // преобразование элемента этажа из 3D формата API в 2D формат плана первого этажа
    private fun parseFloorItem(apiFloorItem: ApiFloorItem): FloorItem? {
        var result: FloorItem? = null
        when (apiFloorItem) {
            is ApiFloorItem.ApiRoom -> {
                // комната: преобразовать координаты вершин ломаной линии в координаты
                // относительно общего начала координат с учетом масштаба
                val startingPoint = PointF(apiFloorItem.x.toFloat(), apiFloorItem.y.toFloat())
                val scaleX = apiFloorItem.scaleX.toFloat()
                val scaleY = apiFloorItem.scaleY.toFloat()
                var wallWidth = 0.0f
                val wallsList = mutableListOf<WallItem>()
                for (apiWall in apiFloorItem.walls) {
                    wallWidth = apiWall.width.toFloat()
                    val coordsList = mutableListOf<PointF>()
                    for (apiCoords in apiWall.coords) {
                        val point = PointF(
                            startingPoint.x + apiCoords.x.toFloat() * scaleX / 100.0f,
                            startingPoint.y + apiCoords.y.toFloat() * scaleY / 100.0f
                        )
                        coordsList.add(point)
                    }
                    wallsList.add(WallItem(wallWidth, coordsList))
                }
                result = FloorItem.RoomItem(wallsList)
            }
            is ApiFloorItem.ApiDoor -> {
                result = parseMeshObject(apiFloorItem)
            }
            is ApiFloorItem.ApiWindow -> {
                result = parseMeshObject(apiFloorItem)
            }
            else -> {}
        }
        return result
    }

    // преобразование для двери или окна (стандартная 3D - модель)
    private fun parseMeshObject(apiFloorItem: ApiFloorItem): FloorItem? {
        // преобразовать с учетом масштаба и дефолтных размеров двери и окна
        var result: FloorItem? = null
        var angle = 0.0f
        val startingPoint = PointF(0.0f,0.0f)
        var length = 0.0f
        var width = 0.0f

        when (apiFloorItem) {
            is ApiFloorItem.ApiDoor -> {
                angle = apiFloorItem.angle.toFloat()
                startingPoint.x = apiFloorItem.x.toFloat()
                startingPoint.y = apiFloorItem.y.toFloat()
                length = PLAN_DOOR_LINE_LENGTH_DEFAULT * apiFloorItem.scaleX.toFloat() / 100.0f
                width = PLAN_DOOR_LINE_WIDTH_DEFAULT
                result = FloorItem.DoorItem(angle, startingPoint, length, width)
            }
            is ApiFloorItem.ApiWindow -> {
                angle = apiFloorItem.angle.toFloat()
                startingPoint.x = apiFloorItem.x.toFloat()
                startingPoint.y = apiFloorItem.y.toFloat()
                length = PLAN_WINDOW_LINE_LENGTH_DEFAULT * apiFloorItem.scaleX.toFloat() / 100.0f
                width = PLAN_WINDOW_LINE_WIDTH_DEFAULT
                result = FloorItem.WindowItem(angle, startingPoint, length, width)
            }
            else -> {}
        }

        return result
    }
}
