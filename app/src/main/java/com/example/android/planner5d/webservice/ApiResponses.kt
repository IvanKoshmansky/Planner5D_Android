package com.example.android.planner5d.webservice

import com.example.android.planner5d.models.PlannerProjectPaging
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
    val coords: List<ApiPoint>  // список из двух координат (начало и конец отрезка)
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
    val className: String,          // "Floor"
    val items: List<ApiFloorItem>,  // комната/дверь/окно/прочее (3D объект)
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
)
