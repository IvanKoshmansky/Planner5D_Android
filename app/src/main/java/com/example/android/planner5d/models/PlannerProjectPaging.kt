package com.example.android.planner5d.models

import com.example.android.planner5d.localdb.DBPlannerProject

data class PlannerProjectPaging (
    val name: String?,   // имя проекта
    val cdate: String?,  // дата обновления проекта
    val key: String?,    // идентификатор проекта
    val img: String?,    // ссылка на изображение в галерее
    val page: Int,       // номер страницы (поддержка пагинации)
    val pages: Int       // общее количество страниц
)

fun List<PlannerProjectPaging>.asDatabaseModel() = map {
    DBPlannerProject(
        name = it.name,
        cdate = it.cdate,
        key = it.key,
        img = it.img,
        page = it.page,
        pages = it.pages
    )
}

fun List<PlannerProjectPaging>.asViewModel() = map {
    PlannerProject(it.name, it.cdate, it.key, it.img)
}

