package com.example.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AnalysisDao {
    
    @Query("SELECT * FROM analysis_history ORDER BY analysisDate DESC")
    fun getAllAnalyses(): LiveData<List<AnalysisEntity>>

    // Synchronous full history for debug/logging use (called from coroutines)
    @Query("SELECT * FROM analysis_history ORDER BY analysisDate DESC")
    suspend fun getAllAnalysesSync(): List<AnalysisEntity>
    
    @Query("SELECT * FROM analysis_history ORDER BY analysisDate DESC LIMIT :limit")
    fun getRecentAnalyses(limit: Int): LiveData<List<AnalysisEntity>>
    
    // Synchronous recent analyses for debug/logging use (called from coroutines)
    @Query("SELECT * FROM analysis_history ORDER BY analysisDate DESC LIMIT :limit")
    suspend fun getRecentAnalysesSync(limit: Int): List<AnalysisEntity>
    
    @Query("SELECT * FROM analysis_history WHERE id = :id")
    suspend fun getAnalysisById(id: Long): AnalysisEntity?
    
    @Query("SELECT * FROM analysis_history WHERE mediaType = :mediaType ORDER BY analysisDate DESC")
    fun getAnalysesByMediaType(mediaType: String): LiveData<List<AnalysisEntity>>
    
    @Query("SELECT * FROM analysis_history WHERE dominantEmotion = :emotion ORDER BY analysisDate DESC")
    fun getAnalysesByEmotion(emotion: String): LiveData<List<AnalysisEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyses(analyses: List<AnalysisEntity>): List<Long>
    
    @Update
    suspend fun updateAnalysis(analysis: AnalysisEntity)
    
    @Delete
    suspend fun deleteAnalysis(analysis: AnalysisEntity)
    
    @Query("DELETE FROM analysis_history WHERE id = :id")
    suspend fun deleteAnalysisById(id: Long)
    
    @Query("DELETE FROM analysis_history")
    suspend fun deleteAllAnalyses()
    
    @Query("SELECT COUNT(*) FROM analysis_history")
    fun getAnalysisCount(): LiveData<Int>
    
    // Synchronous count for debug/logging use (called from coroutines)
    @Query("SELECT COUNT(*) FROM analysis_history")
    suspend fun getCount(): Int
    
    @Query("SELECT * FROM analysis_history ORDER BY analysisDate DESC LIMIT 1")
    suspend fun getLatestAnalysis(): AnalysisEntity?
}
