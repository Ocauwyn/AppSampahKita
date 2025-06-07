package com.example.appsampahkita.data.repository

import android.util.Log // Import Log di sini
import com.example.appsampahkita.data.local.dao.TransaksiSampahDao
import com.example.appsampahkita.data.local.entity.TransaksiSampahEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransaksiSampahRepository @Inject constructor(
    private val transaksiSampahDao: TransaksiSampahDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    // CREATE
    suspend fun insertTransaksiSampah(transaksi: TransaksiSampahEntity): Long {
        return withContext(ioDispatcher) {
            transaksiSampahDao.insertTransaksiSampah(transaksi)
        }
    }

    // READ All
    fun getAllTransaksiSampah(): Flow<List<TransaksiSampahEntity>> {
        return transaksiSampahDao.getAllTransaksiSampah()
    }

    // READ One
    suspend fun getTransaksiSampahById(transaksiId: Long): TransaksiSampahEntity? {
        return withContext(ioDispatcher) {
            Log.d("Repo", "Attempting to get data from DAO for ID: $transaksiId") // LOG INI
            val dataFromDao = transaksiSampahDao.getTransaksiSampahById(transaksiId)
            if (dataFromDao != null) {
                Log.d("Repo", "Data received from DAO: $dataFromDao") // LOG INI
            } else {
                Log.w("Repo", "No data returned from DAO for ID: $transaksiId") // LOG INI (warning jika null)
            }
            dataFromDao
        }
    }

    // UPDATE
    suspend fun updateTransaksiSampah(transaksi: TransaksiSampahEntity) {
        withContext(ioDispatcher) {
            transaksiSampahDao.updateTransaksiSampah(transaksi)
        }
    }

    // DELETE
    suspend fun deleteTransaksiSampahById(transaksiId: Long) {
        withContext(ioDispatcher) {
            transaksiSampahDao.deleteTransaksiSampahById(transaksiId)
        }
    }

    // SEARCH
    fun searchTransaksiSampahByNoKendaraan(query: String): Flow<List<TransaksiSampahEntity>> {
        return transaksiSampahDao.searchTransaksiSampahByNoKendaraan(query)
    }
}