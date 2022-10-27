package com.example.android.planner5d.webservice

import com.example.android.planner5d.models.PlannerProjectPaging

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
