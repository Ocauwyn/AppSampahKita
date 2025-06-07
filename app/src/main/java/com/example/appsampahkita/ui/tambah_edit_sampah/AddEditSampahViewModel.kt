package com.example.appsampahkita.ui.tambah_edit_sampah

import android.util.Log // Pastikan ini di-import
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsampahkita.data.local.entity.TransaksiSampahEntity
import com.example.appsampahkita.data.repository.TransaksiSampahRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditSampahViewModel @Inject constructor(
    private val repository: TransaksiSampahRepository,
    private val state: SavedStateHandle // SavedStateHandle yang di-inject Hilt
) : ViewModel() {

    // State untuk menyimpan data form sementara
    private val _currentTransaksiData = MutableLiveData<TransaksiSampahEntity?>(null)
    val currentTransaksiData: LiveData<TransaksiSampahEntity?> = _currentTransaksiData

    // State untuk mode (Add atau Edit)
    private val _isEditMode = MutableLiveData(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    // State untuk ID transaksi jika dalam mode edit
    private val _transaksiId = MutableLiveData<Long>(-1L)
    val transaksiId: LiveData<Long> = _transaksiId

    // State untuk hasil operasi (sukses/gagal)
    private val _operationResult = MutableLiveData<OperationResult>()
    val operationResult: LiveData<OperationResult> = _operationResult

    // Enum untuk mengidentifikasi mode edit spesifik (jika mengedit per bagian)
    enum class EditSection {
        LOKASI_SUMBER, RITASI, SURAT_TUGAS, NONE
    }
    private val _editSection = MutableLiveData(EditSection.NONE)
    val editSection: LiveData<EditSection> = _editSection

    init {
        // Coba ambil transaksiId dan editMode langsung dari 'state' (SavedStateHandle)
        // Ini adalah upaya pertama untuk mendapatkan argumen yang benar
        var idFromArgs = state.get<Long>("transaksiId") ?: -1L
        var editModeFromArgs = state.get<String>("editMode")

        // Log.d("AddEditVM", "ViewModel init: Initial check - idFromArgs=$idFromArgs, editModeFromArgs=$editModeFromArgs")

        // Perbaikan logika inisialisasi utama di sini:
        if (idFromArgs != -1L) { // Jika ID yang valid ditemukan
            _transaksiId.value = idFromArgs
            _isEditMode.value = true // Jika ada ID valid, ini pasti mode edit

            _editSection.value = when (editModeFromArgs) {
                "lokasiSumber" -> EditSection.LOKASI_SUMBER
                "ritasi" -> EditSection.RITASI
                "suratTugas" -> EditSection.SURAT_TUGAS
                else -> EditSection.NONE
            }
            Log.d("AddEditVM", "ViewModel init: Detected EDIT mode. Transaksi ID: $idFromArgs, Edit Section: ${editModeFromArgs}")

            // Selalu panggil loadExistingTransaksi jika dalam mode EDIT dan ID sudah valid
            loadExistingTransaksi(idFromArgs)
            Log.d("AddEditVM", "ViewModel init: Calling loadExistingTransaksi for ID: $idFromArgs")

        } else { // Ini adalah mode ADD (idFromArgs adalah -1L)
            _transaksiId.value = -1L
            _isEditMode.value = false

            // Hanya buat data kosong jika _currentTransaksiData belum diinisialisasi
            if (_currentTransaksiData.value == null) {
                _currentTransaksiData.value = createEmptyTransaksi()
                Log.d("AddEditVM", "ViewModel init: New empty data created for ADD mode. Initial data: ${_currentTransaksiData.value}")
            } else {
                Log.d("AddEditVM", "ViewModel init: Existing empty data found for ADD mode. Skipping createEmptyTransaksi. Data: ${_currentTransaksiData.value}")
            }
        }
    }

    private fun createEmptyTransaksi(): TransaksiSampahEntity {
        val currentTimestamp = System.currentTimeMillis()
        val randomSuratJalanId = generateRandomSuratJalanId()
        val randomRitasiId = generateRandomRitasiId()

        return TransaksiSampahEntity(
            kodeSuratJalan = randomSuratJalanId,
            lokasiSumberSampah = "",
            alamatLokasi = "",
            jenisPengirimLokasi = "Pemerintah",
            statusKeterangkutan = "Belum",
            volumeTerangkutKg = 0.0,
            idRitasiTpa = randomRitasiId,
            tanggalRitasi = currentTimestamp,
            volumeSampahTon = 0.0,
            petugasPencatat = "Admin",
            brutoKg = 0.0,
            tarraKg = 0.0,
            nettoTonaseKg = 0.0,
            noKendaraan = "",
            jenisKendaraan = "LH",
            jenisPengirimSuratTugas = "Pemerintah",
            namaPengemudi = "",
            crew1 = "", crew2 = "", crew3 = "", crew4 = "", crew5 = ""
        )
    }

    private fun generateRandomSuratJalanId(): String {
        return "#${(10000000..99999999).random()}"
    }

    private fun generateRandomRitasiId(): String {
        return "#${(1000000..9999999).random()}"
    }


    private fun loadExistingTransaksi(id: Long) {
        _operationResult.value = OperationResult.Loading
        viewModelScope.launch {
            try {
                Log.d("AddEditVM", "Attempting to load data from repository for ID: $id")
                val existingData = repository.getTransaksiSampahById(id)
                _currentTransaksiData.value = existingData
                if (existingData != null) {
                    Log.d("AddEditVM", "Data loaded successfully: $existingData")
                    _operationResult.value = OperationResult.Idle
                } else {
                    Log.e("AddEditVM", "Data not found for ID: $id")
                    _operationResult.value = OperationResult.Error("Data tidak ditemukan untuk diedit.")
                }
                Log.d("AddEditVM", "_currentTransaksiData.value after load: ${_currentTransaksiData.value}")
            } catch (e: Exception) {
                Log.e("AddEditVM", "Error loading data for ID $id: ${e.message}", e)
                _operationResult.value = OperationResult.Error("Gagal memuat data: ${e.message}")
            }
        }
    }

    fun updateLokasiSumberData(
        kodeSuratJalan: String,
        lokasiSumberSampah: String,
        alamatLokasi: String,
        jenisPengirimLokasi: String,
        statusKeterangkutan: String,
        volumeTerangkutKg: Double
    ) {
        _currentTransaksiData.value = _currentTransaksiData.value?.copy(
            kodeSuratJalan = kodeSuratJalan,
            lokasiSumberSampah = lokasiSumberSampah,
            alamatLokasi = alamatLokasi,
            jenisPengirimLokasi = jenisPengirimLokasi,
            statusKeterangkutan = statusKeterangkutan,
            volumeTerangkutKg = volumeTerangkutKg
        )
        Log.d("AddEditVM", "updateLokasiSumberData called. currentTransaksiData: ${_currentTransaksiData.value}")
    }

    fun updateRitasiData(
        idRitasiTpa: String,
        tanggalRitasi: Long,
        volumeSampahTon: Double,
        jamMasuk: Long?,
        jamKeluar: Long?,
        petugasPencatat: String,
        brutoKg: Double,
        tarraKg: Double,
        nettoTonaseKg: Double,
        zonaPembuangan: String?
    ) {
        _currentTransaksiData.value = _currentTransaksiData.value?.copy(
            idRitasiTpa = idRitasiTpa,
            tanggalRitasi = tanggalRitasi,
            volumeSampahTon = volumeSampahTon,
            jamMasuk = jamMasuk,
            jamKeluar = jamKeluar,
            petugasPencatat = petugasPencatat,
            brutoKg = brutoKg,
            tarraKg = tarraKg,
            nettoTonaseKg = nettoTonaseKg,
            zonaPembuangan = zonaPembuangan
        )
        Log.d("AddEditVM", "updateRitasiData called. currentTransaksiData: ${_currentTransaksiData.value}")
    }

    fun updateSuratTugasData(
        noKendaraan: String,
        jenisKendaraan: String,
        jenisPengirimSuratTugas: String,
        namaPengemudi: String,
        crew1: String,
        crew2: String,
        crew3: String,
        crew4: String,
        crew5: String
    ) {
        _currentTransaksiData.value = _currentTransaksiData.value?.copy(
            noKendaraan = noKendaraan,
            jenisKendaraan = jenisKendaraan,
            jenisPengirimSuratTugas = jenisPengirimSuratTugas,
            namaPengemudi = namaPengemudi,
            crew1 = crew1,
            crew2 = crew2,
            crew3 = crew3,
            crew4 = crew4,
            crew5 = crew5
        )
        Log.d("AddEditVM", "updateSuratTugasData called. currentTransaksiData: ${_currentTransaksiData.value}")
    }

    fun saveOrUpdateTransaksi() {
        val dataToSave = _currentTransaksiData.value
        if (dataToSave == null) {
            _operationResult.value = OperationResult.Error("Data transaksi kosong, tidak dapat disimpan.")
            return
        }

        viewModelScope.launch {
            _operationResult.value = OperationResult.Loading
            try {
                if (isEditMode.value == true) {
                    Log.d("AddEditVM", "Saving (UPDATE) data: $dataToSave")
                    repository.updateTransaksiSampah(dataToSave)
                    _operationResult.value = OperationResult.Success("Data berhasil diperbarui!")
                } else {
                    Log.d("AddEditVM", "Saving (INSERT) data: $dataToSave")
                    val newId = repository.insertTransaksiSampah(dataToSave)
                    _operationResult.value = OperationResult.Success("Data baru berhasil ditambahkan! ID: $newId")
                }
            } catch (e: Exception) {
                Log.e("AddEditVM", "Error saving data: ${e.message}", e)
                _operationResult.value = OperationResult.Error("Gagal menyimpan data: ${e.message}")
            } finally {
                _operationResult.value = OperationResult.Idle // Reset status setelah operasi
            }
        }
    }

    // Sealed class untuk mengelola hasil operasi
    sealed class OperationResult {
        object Loading : OperationResult()
        data class Success(val message: String) : OperationResult()
        data class Error(val message: String) : OperationResult()
        object Idle : OperationResult()
    }
}