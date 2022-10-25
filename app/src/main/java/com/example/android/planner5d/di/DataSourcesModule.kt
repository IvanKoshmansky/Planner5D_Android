package com.example.android.planner5d.di

import android.content.Context
import com.example.android.planner5d.localdb.*
import com.example.android.planner5d.webservice.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourcesModuleProvides {
    @Singleton
    @Provides
    fun provideLocalDatabase(@ApplicationContext appContext: Context): LocalDatabase {
        return getDatabase(appContext)
    }

    @Singleton
    @Provides
    fun provideApiService(): ApiService {
        return getApiService()
    }
}
