package com.upchiapas.mathenglishquest.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "math_english_quest.db"
            )
                // De la 2 a la 3 se conservan los datos. Solo si alguien viene de la versión 1 (que ya no
                // existe en ningún celular real) se recrea. Cada cambio futuro necesita su propia migración.
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
