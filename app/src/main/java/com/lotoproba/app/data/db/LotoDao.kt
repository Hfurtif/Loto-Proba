package com.lotoproba.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lotoproba.app.data.model.AiEvaluationRecord
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.data.model.LotoSource
import com.lotoproba.app.data.model.ProposedCombination
import kotlinx.coroutines.flow.Flow

@Dao
interface LotoDao {

    // --- LotoDraw queries ---

    @Query("SELECT * FROM loto_draws ORDER BY dateString DESC")
    fun getAllDrawsFlow(): Flow<List<LotoDraw>>

    @Query("SELECT * FROM loto_draws WHERE sourceId = :sourceId ORDER BY dateString DESC")
    fun getDrawsBySourceFlow(sourceId: String): Flow<List<LotoDraw>>

    @Query("SELECT * FROM loto_draws WHERE sourceId = :sourceId ORDER BY dateString DESC")
    suspend fun getDrawsBySource(sourceId: String): List<LotoDraw>

    @Query("SELECT * FROM loto_draws WHERE sourceId = :sourceId AND dateString = :dateString LIMIT 1")
    suspend fun getDrawByDate(sourceId: String, dateString: String): LotoDraw?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDraw(draw: LotoDraw): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDraws(draws: List<LotoDraw>): List<Long>

    @Delete
    suspend fun deleteDraw(draw: LotoDraw)

    @Query("DELETE FROM loto_draws WHERE sourceId = :sourceId")
    suspend fun deleteAllDrawsForSource(sourceId: String)

    @Query("DELETE FROM loto_draws")
    suspend fun clearAllDraws()

    @Query("SELECT COUNT(*) FROM loto_draws WHERE sourceId = :sourceId")
    fun countDrawsFlow(sourceId: String): Flow<Int>

    // --- LotoSource queries ---

    @Query("SELECT * FROM loto_sources ORDER BY name ASC")
    fun getAllSourcesFlow(): Flow<List<LotoSource>>

    @Query("SELECT * FROM loto_sources WHERE id = :id LIMIT 1")
    suspend fun getSourceById(id: String): LotoSource?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: LotoSource)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<LotoSource>)

    @Update
    suspend fun updateSource(source: LotoSource)

    @Delete
    suspend fun deleteSource(source: LotoSource)

    // --- ProposedCombination queries ---

    @Query("SELECT * FROM proposed_combinations WHERE sourceId = :sourceId AND targetDateString = :targetDateString ORDER BY rank ASC")
    fun getCombinationsByDateFlow(sourceId: String, targetDateString: String): Flow<List<ProposedCombination>>

    @Query("SELECT * FROM proposed_combinations WHERE sourceId = :sourceId ORDER BY targetDateString DESC, rank ASC")
    fun getAllCombinationsFlow(sourceId: String): Flow<List<ProposedCombination>>

    @Query("SELECT * FROM proposed_combinations WHERE sourceId = :sourceId AND evaluated = 1 ORDER BY targetDateString DESC")
    fun getEvaluatedCombinationsFlow(sourceId: String): Flow<List<ProposedCombination>>

    @Query("SELECT * FROM proposed_combinations WHERE evaluated = 0")
    suspend fun getPendingCombinations(): List<ProposedCombination>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombinations(combinations: List<ProposedCombination>): List<Long>

    @Update
    suspend fun updateCombination(combination: ProposedCombination)

    @Query("DELETE FROM proposed_combinations WHERE sourceId = :sourceId AND targetDateString = :targetDateString")
    suspend fun deleteCombinationsForDate(sourceId: String, targetDateString: String)

    // --- AiEvaluationRecord queries ---

    @Query("SELECT * FROM ai_evaluations WHERE sourceId = :sourceId ORDER BY timestamp DESC")
    fun getAllAiEvaluationsFlow(sourceId: String): Flow<List<AiEvaluationRecord>>

    @Query("SELECT * FROM ai_evaluations WHERE sourceId = :sourceId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAiEvaluation(sourceId: String): AiEvaluationRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiEvaluation(record: AiEvaluationRecord): Long

    @Update
    suspend fun updateAiEvaluation(record: AiEvaluationRecord)

    @Delete
    suspend fun deleteAiEvaluation(record: AiEvaluationRecord)
}
