package com.example.appsampahkita.ui.daftar_sampah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.appsampahkita.data.repository.TransaksiSampahRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DaftarSampahViewModel @Inject constructor(
    private val repository: TransaksiSampahRepository
) : ViewModel() {

    // State untuk query pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // State untuk daftar transaksi sampah
    val transaksiSampahList = combine(
        repository.getAllTransaksiSampah(), // Ambil semua data
        _searchQuery // Amati perubahan query pencarian
    ) { allTransaksi, query ->
        if (query.isBlank()) {
            allTransaksi // Jika query kosong, tampilkan semua
        } else {
            // Jika ada query, lakukan filter di sisi klien (untuk contoh sederhana)
            // Untuk aplikasi besar, filter ini lebih baik dilakukan di DAO dengan query SQL
            allTransaksi.filter {
                it.noKendaraan.contains(query, ignoreCase = true) ||
                        it.kodeSuratJalan.contains(query, ignoreCase = true)
            }
        }
    }.asLiveData() // Konversi Flow menjadi LiveData untuk diobservasi di UI

    // Untuk menampilkan status loading/error/no data
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        // Initial load atau setup observer
        viewModelScope.launch {
            repository.getAllTransaksiSampah().collect { list ->
                if (list.isEmpty() && _searchQuery.value.isBlank()) {
                    _uiState.value = UiState.Empty
                } else if (list.isEmpty() && _searchQuery.value.isNotBlank()) {
                    _uiState.value = UiState.NoSearchResults
                }
                else {
                    _uiState.value = UiState.Success
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Fungsi untuk mengubah status 'Menuju TPA' (jika ada logic khusus)
    fun updateStatusMenujuTPA(transaksiId: Long) {
        viewModelScope.launch {
            val transaksi = repository.getTransaksiSampahById(transaksiId)
            transaksi?.let {
                // Implementasikan logic perubahan status di sini
                // Contoh: it.statusKeterangkutan = "Menuju TPA"
                // repository.updateTransaksiSampah(it)
                // Toast.makeText(getApplication(), "Status diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        object Empty : UiState() // Jika tidak ada data sama sekali
        object NoSearchResults : UiState() // Jika tidak ada hasil pencarian
        data class Error(val message: String) : UiState()
    }
}