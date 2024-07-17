package com.inky.fitnesscalendar.di

import android.content.Context
import com.inky.fitnesscalendar.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) = AppDatabase.getInstance(context)

    @Provides
    fun provideActivityDao(database: AppDatabase) = database.activityDao()

    @Provides
    fun provideRecordingDao(database: AppDatabase) = database.recordingDao()

    @Provides
    fun provideActivityTypeDao(database: AppDatabase) = database.activityTypeDao()

    @Provides
    fun filterHistoryDao(database: AppDatabase) = database.filterHistoryDao()

    @Provides
    fun dayDao(database: AppDatabase) = database.dayDao()

    @Provides
    fun placeDao(database: AppDatabase) = database.placeDao()
}