package com.lotoproba.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lotoproba.app.data.model.AiEvaluationRecord
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.data.model.LotoSource
import com.lotoproba.app.data.model.ProposedCombination

@Database(
    entities = [
        LotoDraw::class,
        LotoSource::class,
        ProposedCombination::class,
        AiEvaluationRecord::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LotoDatabase : RoomDatabase() {

    abstract fun lotoDao(): LotoDao

    companion object {
        @Volatile
        private var INSTANCE: LotoDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ai_evaluations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sourceId` TEXT NOT NULL,
                        `dateGenerated` TEXT NOT NULL,
                        `targetDate` TEXT NOT NULL,
                        `bankerN1` INTEGER NOT NULL,
                        `nap2PairsCsv` TEXT NOT NULL,
                        `nap3TriosCsv` TEXT NOT NULL,
                        `perm5Csv` TEXT NOT NULL,
                        `aiReasoning` TEXT NOT NULL,
                        `confidenceScore` INTEGER NOT NULL,
                        `evaluated` INTEGER NOT NULL,
                        `actualDrawDate` TEXT,
                        `actualWinningNumbersCsv` TEXT,
                        `verdictSummary` TEXT,
                        `explanationWhy` TEXT,
                        `actionAdviceNextDraw` TEXT,
                        `matchedCount` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Dédoublonnage : conserver une seule entrée par couple (sourceId, dateString)
                db.execSQL(
                    """
                    DELETE FROM loto_draws
                    WHERE id NOT IN (
                        SELECT MAX(id)
                        FROM loto_draws
                        GROUP BY sourceId, dateString
                    )
                    """.trimIndent()
                )
                db.execSQL("DROP INDEX IF EXISTS `index_loto_draws_sourceId_dateString_drawName`")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_loto_draws_sourceId_dateString` ON `loto_draws` (`sourceId`, `dateString`)"
                )
            }
        }

        fun getDatabase(context: Context): LotoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotoDatabase::class.java,
                    "loto_proba_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
