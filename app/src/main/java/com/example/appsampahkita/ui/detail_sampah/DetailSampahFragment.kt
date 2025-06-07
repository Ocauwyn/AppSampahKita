package com.example.appsampahkita.ui.detail_sampah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.appsampahkita. R
import com.example.appsampahkita.data.local.entity.TransaksiSampahEntity
import com.example.appsampahkita.databinding.FragmentDetailSampahBinding
import com.example.appsampahkita.ui.common.ConfirmationBottomSheet
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DetailSampahFragment : Fragment() {

    private var _binding: FragmentDetailSampahBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailSampahViewModel by viewModels()
    private val args: DetailSampahFragmentArgs by navArgs() // Untuk mengambil argumen dari Navigation

    private var currentTransaksiId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailSampahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentTransaksiId = args.transaksiId // Ambil ID dari Safe Args

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // Kembali ke layar sebelumnya
        }
    }

    private fun setupListeners() {
        binding.cardDeleteData.setOnClickListener {
            showDeleteConfirmationBottomSheet()
        }

        binding.ivEditSuratTugas.setOnClickListener {
            navigateToEditForm(currentTransaksiId, "suratTugas")
        }
        binding.ivEditRitasi.setOnClickListener {
            navigateToEditForm(currentTransaksiId, "ritasi")
        }
        binding.ivEditLokasiSumber.setOnClickListener {
            navigateToEditForm(currentTransaksiId, "lokasiSumber")
        }
    }

    private fun navigateToEditForm(transaksiId: Long, editMode: String) {
        val action = when (editMode) {
            "ritasi" -> DetailSampahFragmentDirections.actionDetailSampahFragmentToUbahDataInfoRitasiFragment(transaksiId, editMode)
            "suratTugas" -> DetailSampahFragmentDirections.actionDetailSampahFragmentToUbahDataSuratTugasFragment(transaksiId, editMode)
            "lokasiSumber" -> DetailSampahFragmentDirections.actionDetailSampahFragmentToUbahDataLokasiSumberFragment(transaksiId, editMode)
            else -> return // Should not happen
        }
        findNavController().navigate(action)
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Amati data transaksi sampah yang dimuat
                launch {
                    viewModel.transaksiSampah.observe(viewLifecycleOwner) { transaksi ->
                        transaksi?.let { bindDataToViews(it) }
                    }
                }

                // Amati UI State
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBarDetail.visibility = View.GONE
                        binding.tvDetailError.visibility = View.GONE
                        binding.scrollView.visibility = View.VISIBLE // Tampilkan konten default

                        when (state) {
                            DetailSampahViewModel.DetailUiState.Loading -> {
                                binding.progressBarDetail.visibility = View.VISIBLE
                                binding.scrollView.visibility = View.GONE
                            }
                            DetailSampahViewModel.DetailUiState.NotFound -> {
                                binding.tvDetailError.text = "Data sampah tidak ditemukan."
                                binding.tvDetailError.visibility = View.VISIBLE
                                binding.scrollView.visibility = View.GONE
                            }
                            is DetailSampahViewModel.DetailUiState.Error -> {
                                binding.tvDetailError.text = "Error: ${state.message}"
                                binding.tvDetailError.visibility = View.VISIBLE
                                binding.scrollView.visibility = View.GONE
                                Snackbar.make(binding.root, "Error: ${state.message}", Snackbar.LENGTH_LONG).show()
                            }
                            DetailSampahViewModel.DetailUiState.Deleting -> {
                                Snackbar.make(binding.root, "Menghapus data...", Snackbar.LENGTH_SHORT).show()
                            }
                            DetailSampahViewModel.DetailUiState.DeleteSuccess -> {
                                Snackbar.make(binding.root, "Data berhasil dihapus!", Snackbar.LENGTH_SHORT).show()
                                findNavController().popBackStack(R.id.daftarSampahFragment, false) // Kembali ke daftar dan hapus detail dari back stack
                            }
                            is DetailSampahViewModel.DetailUiState.Success -> {
                                // Data berhasil dimuat, bindDataToViews sudah dipanggil dari observasi transaksiSampah
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindDataToViews(transaksi: TransaksiSampahEntity) {
        // Info Surat Tugas
        binding.tvIdSuratJalanDetail.text = "#${transaksi.kodeSuratJalan}"
        binding.tvNoKendaraanDetail.text = transaksi.noKendaraan
        binding.tvJenisPengirimDetail.text = transaksi.jenisPengirimSuratTugas
        binding.tvJenisKendaraanDetail.text = transaksi.jenisKendaraan
        binding.tvNamaPengemudiDetail.text = transaksi.namaPengemudi
        binding.tvCrew1Detail.text = transaksi.crew1
        binding.tvCrew2Detail.text = transaksi.crew2
        binding.tvCrew3Detail.text = transaksi.crew3
        binding.tvCrew4Detail.text = transaksi.crew4
        binding.tvCrew5Detail.text = transaksi.crew5

        // Info Ritasi
        binding.tvIdRitasiTpaDetail.text = "#${transaksi.idRitasiTpa}"
        binding.tvTanggalRitasiDetail.text = formatDate(transaksi.tanggalRitasi)
        binding.tvVolumeSampahDetail.text = "${transaksi.volumeSampahTon} ton"
        binding.tvJamMasukDetail.text = formatTime(transaksi.jamMasuk)
        binding.tvJamKeluarDetail.text = formatTime(transaksi.jamKeluar)
        binding.tvPetugasPencatatDetail.text = transaksi.petugasPencatat
        binding.tvBrutoDetail.text = "${transaksi.brutoKg} Kg"
        binding.tvTarraDetail.text = "${transaksi.tarraKg} Kg"
        binding.tvNettoTonaseDetail.text = "${transaksi.nettoTonaseKg} Kg"
        binding.tvZonaPembuanganDetail.text = transaksi.zonaPembuangan ?: "-"

        // Info Lokasi Sumber Sampah
        binding.tvKodeSuratJalanLokasiDetail.text = "#${transaksi.kodeSuratJalan}"
        binding.tvLokasiSumberSampahDetail.text = transaksi.lokasiSumberSampah
        binding.tvStatusKeterangkutanDetail.text = transaksi.statusKeterangkutan
        binding.tvAlamatLokasiDetail.text = transaksi.alamatLokasi
        binding.tvJenisPengirimLokasiDetail.text = transaksi.jenisPengirimLokasi
        binding.tvVolumeTerangkutDetail.text = "${transaksi.volumeTerangkutKg} Kg"
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return format.format(date)
    }

    private fun formatTime(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "-"
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH.mm", Locale("id", "ID"))
        return "${format.format(date)} WIB"
    }

    private fun showDeleteConfirmationBottomSheet() {
        val bottomSheet = ConfirmationBottomSheet.newInstance(
            title = "Hapus Data Ini?",
            message = "Data transaksi sampah ini akan dihapus permanen. Tindakan ini tidak bisa dibatalkan.",
            confirmButtonText = "Ya, Hapus sekarang",
            cancelButtonText = "Batal",
            illustrationResId = R.drawable.city_illustration_delete_bottom_sheet // Anda perlu membuat ilustrasi ini
        )
        bottomSheet.setOnConfirmListener {
            viewModel.deleteTransaksiSampah(currentTransaksiId)
        }
        bottomSheet.show(childFragmentManager, ConfirmationBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}