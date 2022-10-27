package com.example.android.planner5d.localdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

private const val DATABASE_NAME = "local_db_debug_19"

@Database(entities = [DBPlannerProject::class], version = 1)
abstract class LocalDatabase: RoomDatabase() {
    abstract val databaseDao: DatabaseDao
}

private lateinit var INSTANCE: LocalDatabase

fun getDatabase(context: Context): LocalDatabase {
    synchronized(LocalDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                LocalDatabase::class.java,
                DATABASE_NAME).build()
        }
    }
    return INSTANCE
}
