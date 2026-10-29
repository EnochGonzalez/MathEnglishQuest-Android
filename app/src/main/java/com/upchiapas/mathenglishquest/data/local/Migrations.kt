package com.upchiapas.mathenglishquest.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Versión 2 -> 3: agrega la tabla de evidencias SIN tocar las cuentas que ya existen. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `evidencias` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`usuarioId` INTEGER NOT NULL, " +
                "`rutaImagen` TEXT NOT NULL, " +
                "`fecha` INTEGER NOT NULL, " +
                "`dificultad` TEXT NOT NULL, " +
                "`categorias` TEXT NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_evidencias_usuarioId` ON `evidencias` (`usuarioId`)")
    }
}
