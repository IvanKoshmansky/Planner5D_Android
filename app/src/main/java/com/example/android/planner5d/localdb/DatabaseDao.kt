package com.example.android.planner5d.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DatabaseDao {

    // запросить проекты из галереи для репозитория
    @Query("select * from gallery_table where page >= :startPage and page < :startPage + :count order by page")
    fun getGallery(startPage: Int, count: Int): List<DBPlannerProject>

    // вставка новых данных
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProjects(vararg projects: DBPlannerProject)

    // очистить таблицу с проектами из галереи
    @Query("delete from gallery_table")
    fun clearGallery()
}
