package com.example.appsampahkita.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appsampahkita.data.local.dao.TransaksiSampahDao
import com.example.appsampahkita.data.local.entity.TransaksiSampahEntity

@Database(entities = [TransaksiSampahEntity::class], version = 1, exportSchema = false)
abstract class SampahKitaDatabase : RoomDatabase() {
    abstract fun transaksiSampahDao(): TransaksiSampahDao
}