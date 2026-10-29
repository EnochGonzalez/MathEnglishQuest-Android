package com.upchiapas.mathenglishquest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.upchiapas.mathenglishquest.data.local.dao.EvidenciaDao
import com.upchiapas.mathenglishquest.data.local.dao.UsuarioDao
import com.upchiapas.mathenglishquest.data.local.entity.Evidencia
import com.upchiapas.mathenglishquest.data.local.entity.Usuario

@Database(entities = [Usuario::class, Evidencia::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun evidenciaDao(): EvidenciaDao
}
