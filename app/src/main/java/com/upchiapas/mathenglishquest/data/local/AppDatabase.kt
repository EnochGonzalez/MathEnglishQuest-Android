package com.upchiapas.mathenglishquest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.upchiapas.mathenglishquest.data.local.dao.UsuarioDao
import com.upchiapas.mathenglishquest.data.local.entity.Usuario

@Database(entities = [Usuario::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
}