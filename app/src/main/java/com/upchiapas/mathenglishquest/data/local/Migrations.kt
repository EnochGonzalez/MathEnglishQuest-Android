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

/**
 * Versión 3 -> 4: cada evidencia guarda el ejercicio. La tabla se reconstruye (en vez de usar ALTER TABLE con DEFAULT)
 * para que el esquema quede exactamente como Room lo espera. Las fotos ya guardadas se conservan.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE `evidencias_nueva` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`usuarioId` INTEGER NOT NULL, " +
                "`rutaImagen` TEXT NOT NULL, " +
                "`fecha` INTEGER NOT NULL, " +
                "`dificultad` TEXT NOT NULL, " +
                "`categorias` TEXT NOT NULL, " +
                "`ejercicio` TEXT NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO `evidencias_nueva` (`id`, `usuarioId`, `rutaImagen`, `fecha`, `dificultad`, `categorias`, `ejercicio`) " +
                "SELECT `id`, `usuarioId`, `rutaImagen`, `fecha`, `dificultad`, `categorias`, '' FROM `evidencias`"
        )
        db.execSQL("DROP TABLE `evidencias`")
        db.execSQL("ALTER TABLE `evidencias_nueva` RENAME TO `evidencias`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_evidencias_usuarioId` ON `evidencias` (`usuarioId`)")
    }
}
