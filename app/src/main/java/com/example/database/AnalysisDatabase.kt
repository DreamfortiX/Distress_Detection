package com.example.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [AnalysisEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AnalysisDatabase : RoomDatabase() {
    
    abstract fun analysisDao(): AnalysisDao
    
    companion object {
        @Volatile
        private var INSTANCE: AnalysisDatabase? = null
        
        fun getDatabase(context: Context): AnalysisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnalysisDatabase::class.java,
                    "analysis_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Database created successfully
        }
    }
}
