package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.LotoDraw
import com.example.data.model.LotoSource
import com.example.data.model.ProposedCombination

@Database(
    entities = [
        LotoDraw::class,
        LotoSource::class,
        ProposedCombination::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LotoDatabase : RoomDatabase() {

    abstract fun lotoDao(): LotoDao

    companion object {
        @Volatile
        private var INSTANCE: LotoDatabase? = null

        fun getDatabase(context: Context): LotoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotoDatabase::class.java,
                    "loto_proba_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
