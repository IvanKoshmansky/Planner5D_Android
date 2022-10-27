package com.example.android.planner5d.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.planner5d.models.PlannerProjectPaging

//
// таблица с описанием проекта в галерее проектов
//

@Entity(tableName = "gallery_table")
data class DBPlannerProject constructor (

    // первичный ключ (заполняется автоматически)
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,

    // имя проекта в галерее
    val name: String?,

    // дата обновления проекта (по дате можно сделать сортировку прямо по строке в формате "2022-06-22 04:50:11")
    val cdate: String?,

    // ID проекта
    val key: String?,

    // ссылка на изображение (предпросмотр)
    val img: String?,

    // номер страницы для пейджинга (всего приходит по 16 проектов на одной странице)
    val page: Int,

    // общее количество страниц для пейджинга
    val pages: Int
)

fun List<DBPlannerProject>.asDomainModel() = map {
    PlannerProjectPaging(
        name = it.name,
        cdate = it.cdate,
        key = it.key ?: "",
        img = it.img,
        page = it.page,
        pages = it.pages
    )
}
