package com.example.appsampahkita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appsampahkita.data.local.entity.TransaksiSampahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaksiSampahDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksiSampah(transaksi: TransaksiSampahEntity): Long // Mengembalikan ID yang diinsert

    @Update
    suspend fun updateTransaksiSampah(transaksi: TransaksiSampahEntity)

    @Query("DELETE FROM transaksi_sampah WHERE id = :transaksiId")
    suspend fun deleteTransaksiSampahById(transaksiId: Long)

    @Query("SELECT * FROM transaksi_sampah ORDER BY tanggalRitasi DESC")
    fun getAllTransaksiSampah(): Flow<List<TransaksiSampahEntity>>

    @Query("SELECT * FROM transaksi_sampah WHERE id = :transaksiId")
    suspend fun getTransaksiSampahById(transaksiId: Long): TransaksiSampahEntity?

    @Query("SELECT * FROM transaksi_sampah WHERE noKendaraan LIKE '%' || :query || '%' ORDER BY tanggalRitasi DESC")
    fun searchTransaksiSampahByNoKendaraan(query: String): Flow<List<TransaksiSampahEntity>>
}