package com.hady.stonesforever

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hady.stonesforever.data.dao.BatchMovementDao
import com.hady.stonesforever.data.entity.BatchMovementEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "batch_movement_db"
            ).fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideBatchMovementDao(db: AppDatabase): BatchMovementDao {
        return db.batchMovementDao()
    }
}

@Database(
    entities = [BatchMovementEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batchMovementDao(): BatchMovementDao
}
