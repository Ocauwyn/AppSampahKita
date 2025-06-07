package com.example.appsampahkita.ui.detail_sampah

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsampahkita.data.local.entity.TransaksiSampahEntity
import com.example.appsampahkita.data.repository.TransaksiSampahRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailSampahViewModel @Inject constructor(
    private val repository: TransaksiSampahRepository,
    private val savedStateHandle: SavedStateHandle // Untuk mendapatkan argumen dari Navigation
) : ViewModel() {

    // Menggunakan StateFlow untuk mengelola status loading dan data
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    // LiveData untuk menyimpan data transaksi sampah yang sedang ditampilkan
    private val _transaksiSampah = MutableLiveData<TransaksiSampahEntity?>()
    val transaksiSampah: LiveData<TransaksiSampahEntity?> = _transaksiSampah

    init {
        // Ambil transaksiId dari Safe Args
        val transaksiId = savedStateHandle.get<Long>("transaksiId") ?: -1L
        if (transaksiId != -1L) {
            loadTransaksiSampah(transaksiId)
        } else {
            _uiState.value = DetailUiState.Error("ID Transaksi tidak valid.")
        }
    }

    private fun loadTransaksiSampah(id: Long) {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getTransaksiSampahById(id)
                _transaksiSampah.value = data
                if (data != null) {
                    _uiState.value = DetailUiState.Success(data)
                } else {
                    _uiState.value = DetailUiState.NotFound
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Gagal memuat data: ${e.message}")
            }
        }
    }

    fun deleteTransaksiSampah(id: Long) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Deleting
            try {
                repository.deleteTransaksiSampahById(id)
                _uiState.value = DetailUiState.DeleteSuccess // Beri tahu UI bahwa penghapusan berhasil
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Gagal menghapus data: ${e.message}")
            }
        }
    }

    // Sealed class untuk mengelola berbagai state UI
    sealed class DetailUiState {
        object Loading : DetailUiState()
        data class Success(val data: TransaksiSampahEntity) : DetailUiState()
        object NotFound : DetailUiState() // Jika data dengan ID tertentu tidak ditemukan
        object Deleting : DetailUiState()
        object DeleteSuccess : DetailUiState()
        data class Error(val message: String) : DetailUiState()
    }
}