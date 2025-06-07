package com.example.appsampahkita.ui.tambah_edit_sampah.form_lokasi_sumber

import android.os.Bundle
import android.util.Log // Import Log di sini
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.appsampahkita.R
import com.example.appsampahkita.databinding.FragmentFormLokasiSumberBinding
import com.example.appsampahkita.ui.tambah_edit_sampah.AddEditSampahViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class FormLokasiSumberFragment : Fragment() {

    private var _binding: FragmentFormLokasiSumberBinding? = null
    private val binding get() = _binding!!

    // Mengambil ViewModel yang di-scope ke Activity
    private val sharedViewModel: AddEditSampahViewModel by activityViewModels()
    private val args: FormLokasiSumberFragmentArgs by navArgs()

    private var isEditModeForSection: Boolean = false // Tambahan untuk edit per bagian

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormLokasiSumberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cek apakah ini mode edit per bagian
        isEditModeForSection = args.editMode != null && args.editMode == "lokasiSumber"
        Log.d("FormLokasiSumber", "FormLokasiSumberFragment: isEditModeForSection = $isEditModeForSection") // LOG INI
        if (isEditModeForSection) {
            Log.d("FormLokasiSumber", "FormLokasiSumberFragment: Navigated for section edit. Current Transaksi ID: ${args.transaksiId}") // LOG INI
        }

        setupDropdowns()
        observeViewModel()
        setupListeners()

        // Hide "Sebelumnya" button for the first step
        binding.btnSebelumnya.visibility = View.GONE
    }

    private fun setupDropdowns() {
        val jenisPengirimOptions = arrayOf("Pemerintah", "Swasta", "Perorangan")
        val statusKeterangkutanOptions = arrayOf("Belum", "Dalam Proses", "Sudah")

        val jenisPengirimAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, jenisPengirimOptions)
        binding.actvJenisPengirim.setAdapter(jenisPengirimAdapter)

        val statusKeterangkutanAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, statusKeterangkutanOptions)
        binding.actvStatusKeterangkutan.setAdapter(statusKeterangkutanAdapter)
    }

    private fun observeViewModel() {
        sharedViewModel.currentTransaksiData.observe(viewLifecycleOwner) { data ->
            if (data == null) {
                Log.d("FormLokasiSumber", "Observed data is NULL. Is it still loading or not found?") // LOG INI
            } else {
                Log.d("FormLokasiSumber", "Observed data is NOT NULL. Populating UI with: $data") // LOG INI
                // Isi field jika ada data yang sudah dimuat (mode edit atau kembali dari langkah berikutnya)
                binding.etKodeSuratJalan.setText(data.kodeSuratJalan)
                binding.etLokasiSumberSampah.setText(data.lokasiSumberSampah)
                binding.etAlamatLokasi.setText(data.alamatLokasi)
                binding.actvJenisPengirim.setText(data.jenisPengirimLokasi, false) // false to prevent filtering
                binding.actvStatusKeterangkutan.setText(data.statusKeterangkutan, false)
                binding.etVolumeTerangkut.setText(DecimalFormat("#.##").format(data.volumeTerangkutKg))

                // Jika ini mode edit per bagian, langsung simpan
                if (isEditModeForSection) {
                    binding.llButtons.visibility = View.GONE // Sembunyikan tombol navigasi langkah
                    binding.fabSaveEditSection.visibility = View.VISIBLE // Tampilkan tombol simpan khusus
                    Log.d("FormLokasiSumber", "Edit mode for section active. Hiding step buttons, showing FAB.") // LOG INI
                }
            }
        }

        sharedViewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddEditSampahViewModel.OperationResult.Success -> {
                    Log.d("FormLokasiSumber", "Operation Result: Success - ${result.message}") // LOG INI
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_SHORT).show()
                    if (isEditModeForSection) {
                        // Kembali ke detail setelah edit per bagian
                        findNavController().popBackStack(R.id.detailSampahFragment, false)
                    }
                    // Jika ini bagian dari alur tambah, tidak perlu navigasi di sini, akan dilanjutkan di langkah terakhir
                }
                is AddEditSampahViewModel.OperationResult.Error -> {
                    Log.e("FormLokasiSumber", "Operation Result: Error - ${result.message}") // LOG INI
                    Snackbar.make(binding.root, "Error: ${result.message}", Snackbar.LENGTH_LONG).show()
                }
                AddEditSampahViewModel.OperationResult.Loading -> {
                    Log.d("FormLokasiSumber", "Operation Result: Loading") // LOG INI
                    // Tampilkan progress bar jika diperlukan
                }
                AddEditSampahViewModel.OperationResult.Idle -> {
                    Log.d("FormLokasiSumber", "Operation Result: Idle") // LOG INI
                }
            }
        }
    }

    private fun setupListeners() {
        // Tombol Selanjutnya (untuk alur tambah data)
        binding.btnSelanjutnya.setOnClickListener {
            if (validateInputs()) {
                saveDataToViewModel()
                val action = FormLokasiSumberFragmentDirections.actionFormLokasiSumberFragmentToFormInfoRitasiFragment()
                findNavController().navigate(action)
            } else {
                Snackbar.make(binding.root, "Mohon lengkapi semua field yang wajib diisi.", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Tombol Simpan (khusus untuk mode edit per bagian)
        binding.fabSaveEditSection.setOnClickListener {
            if (validateInputs()) {
                saveDataToViewModel()
                sharedViewModel.saveOrUpdateTransaksi() // Panggil fungsi simpan/update dari ViewModel
            } else {
                Snackbar.make(binding.root, "Mohon lengkapi semua field yang wajib diisi.", Snackbar.LENGTH_SHORT).show()
            }
        }

        // ... (observe operationResult tetap sama, sudah saya pindahkan ke observeViewModel) ...
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etLokasiSumberSampah.text.isNullOrBlank()) {
            binding.tilLokasiSumberSampah.error = "Lokasi sumber sampah wajib diisi."
            isValid = false
        } else {
            binding.tilLokasiSumberSampah.error = null
        }

        if (binding.etAlamatLokasi.text.isNullOrBlank()) {
            binding.tilAlamatLokasi.error = "Alamat lokasi wajib diisi."
            isValid = false
        } else {
            binding.tilAlamatLokasi.error = null
        }

        if (binding.actvJenisPengirim.text.isNullOrBlank()) {
            binding.tilJenisPengirim.error = "Jenis pengirim wajib dipilih."
            isValid = false
        } else {
            binding.tilJenisPengirim.error = null
        }

        if (binding.actvStatusKeterangkutan.text.isNullOrBlank()) {
            binding.tilStatusKeterangkutan.error = "Status keterangkutan wajib dipilih."
            isValid = false
        } else {
            binding.tilStatusKeterangkutan.error = null
        }

        if (binding.etVolumeTerangkut.text.isNullOrBlank() || binding.etVolumeTerangkut.text.toString().toDoubleOrNull() == null) {
            binding.tilVolumeTerangkut.error = "Volume terangkut wajib diisi dengan angka."
            isValid = false
        } else {
            binding.tilVolumeTerangkut.error = null
        }

        return isValid
    }

    private fun saveDataToViewModel() {
        sharedViewModel.updateLokasiSumberData(
            kodeSuratJalan = binding.etKodeSuratJalan.text.toString(), // Ini akan di-generate/diisi di langkah terakhir
            lokasiSumberSampah = binding.etLokasiSumberSampah.text.toString(),
            alamatLokasi = binding.etAlamatLokasi.text.toString(),
            jenisPengirimLokasi = binding.actvJenisPengirim.text.toString(),
            statusKeterangkutan = binding.actvStatusKeterangkutan.text.toString(),
            volumeTerangkutKg = binding.etVolumeTerangkut.text.toString().toDoubleOrNull() ?: 0.0
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}