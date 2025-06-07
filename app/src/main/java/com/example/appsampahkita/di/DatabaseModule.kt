package com.example.appsampahkita.di

import android.content.Context
import androidx.room.Room
import com.example.appsampahkita.data.local.SampahKitaDatabase
import com.example.appsampahkita.data.local.dao.TransaksiSampahDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SampahKitaDatabase {
        return Room.databaseBuilder(
            context,
            SampahKitaDatabase::class.java,
            "sampah_kita_db" // Nama database
        ).fallbackToDestructiveMigration() // Hapus ini di produksi dan gunakan migrasi yang tepat
            .build()
    }

    @Provides
    @Singleton
    fun provideTransaksiSampahDao(database: SampahKitaDatabase): TransaksiSampahDao {
        return database.transaksiSampahDao()
    }
}