package com.example.appsampahkita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaksi_sampah")
data class TransaksiSampahEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Info Lokasi Sumber Sampah
    val kodeSuratJalan: String,
    val lokasiSumberSampah: String,
    val alamatLokasi: String,
    val jenisPengirimLokasi: String,
    val statusKeterangkutan: String,
    val volumeTerangkutKg: Double,

    // Info Ritasi
    val idRitasiTpa: String,
    val tanggalRitasi: Long, // Menggunakan timestamp Long
    val volumeSampahTon: Double,
    val jamMasuk: Long? = null, // Optional
    val jamKeluar: Long? = null, // Optional
    val petugasPencatat: String,
    val brutoKg: Double,
    val tarraKg: Double,
    val nettoTonaseKg: Double, // Calculated: bruto - tarra
    val zonaPembuangan: String? = null, // Optional

    // Info Surat Tugas
    val noKendaraan: String,
    val jenisKendaraan: String,
    val jenisPengirimSuratTugas: String,
    val namaPengemudi: String,
    val crew1: String,
    val crew2: String,
    val crew3: String,
    val crew4: String,
    val crew5: String
)