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
                // Versión 2 cambió la tabla de usuarios. Todavía no hay datos reales que conservar,
                // así que se recrea. Antes de publicar la app hay que escribir migraciones de verdad.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
