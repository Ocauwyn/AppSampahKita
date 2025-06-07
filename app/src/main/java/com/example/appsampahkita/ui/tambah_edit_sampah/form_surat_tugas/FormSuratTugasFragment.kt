package com.example.appsampahkita.ui.tambah_edit_sampah.form_surat_tugas

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
import com.example.appsampahkita.databinding.FragmentFormSuratTugasBinding
import com.example.appsampahkita.ui.tambah_edit_sampah.AddEditSampahViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FormSuratTugasFragment : Fragment() {

    private var _binding: FragmentFormSuratTugasBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: AddEditSampahViewModel by activityViewModels()
    private val args: FormSuratTugasFragmentArgs by navArgs() // Untuk mengambil argumen dari Navigation

    private var isEditModeForSection: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormSuratTugasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cek apakah ini mode edit per bagian
        isEditModeForSection = args.editMode != null && args.editMode == "suratTugas"
        Log.d("FormSuratTugas", "FormSuratTugasFragment: isEditModeForSection = $isEditModeForSection") // LOG INI
        if (isEditModeForSection) {
            Log.d("FormSuratTugas", "FormSuratTugasFragment: Navigated for section edit. Current Transaksi ID: ${args.transaksiId}") // LOG INI
        }

        setupDropdowns()
        observeViewModel()
        setupListeners()
    }

    private fun setupDropdowns() {
        val jenisKendaraanOptions = arrayOf("LH", "Dump Truck", "Arm Roll") // Contoh opsi
        val jenisPengirimOptions = arrayOf("Pemerintah", "Swasta", "Perorangan")
        val namaPengemudiOptions = arrayOf("Asep", "Budi", "Cici") // Contoh nama
        val crewOptions = arrayOf("Rendi", "Dedi", "Adam", "Raden", "Ridwan", "Ani", "Bambang", "None") // Contoh nama

        binding.actvJenisKendaraan.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, jenisKendaraanOptions))
        binding.actvJenisPengirimSt.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, jenisPengirimOptions))
        binding.actvNamaPengemudi.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, namaPengemudiOptions))
        binding.actvCrew1.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, crewOptions))
        binding.actvCrew2.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, crewOptions))
        binding.actvCrew3.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, crewOptions))
        binding.actvCrew4.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, crewOptions))
        binding.actvCrew5.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_item, crewOptions))
    }

    private fun observeViewModel() {
        sharedViewModel.currentTransaksiData.observe(viewLifecycleOwner) { data ->
            if (data == null) {
                Log.d("FormSuratTugas", "Observed data is NULL. Is it still loading or not found?") // LOG INI
            } else {
                Log.d("FormSuratTugas", "Observed data is NOT NULL. Populating UI with: $data") // LOG INI
                // Isi field jika ada data yang sudah dimuat
                binding.etIdSuratJalanSt.setText(data.kodeSuratJalan)
                binding.etNoKendaraan.setText(data.noKendaraan)
                binding.actvJenisKendaraan.setText(data.jenisKendaraan, false)
                binding.actvJenisPengirimSt.setText(data.jenisPengirimSuratTugas, false)
                binding.actvNamaPengemudi.setText(data.namaPengemudi, false)
                binding.actvCrew1.setText(data.crew1, false)
                binding.actvCrew2.setText(data.crew2, false)
                binding.actvCrew3.setText(data.crew3, false)
                binding.actvCrew4.setText(data.crew4, false)
                binding.actvCrew5.setText(data.crew5, false)

                // Jika ini mode edit per bagian
                if (isEditModeForSection) {
                    binding.llButtons.visibility = View.GONE // Sembunyikan tombol navigasi langkah
                    binding.fabSaveEditSection.visibility = View.VISIBLE // Tampilkan tombol simpan khusus
                    Log.d("FormSuratTugas", "Edit mode for section active. Hiding step buttons, showing FAB.") // LOG INI
                }
            }
        }

        sharedViewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddEditSampahViewModel.OperationResult.Success -> {
                    Log.d("FormSuratTugas", "Operation Result: Success - ${result.message}") // LOG INI
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_SHORT).show()
                    // Navigasi kembali setelah berhasil menyimpan
                    if (isEditModeForSection) {
                        // Untuk mode edit per bagian (dari Detail ke Edit), pop back ke Detail
                        findNavController().popBackStack(R.id.detailSampahFragment, false)
                    } else {
                        // UNTUK MODE TAMBAH BARU: Selesaikan Activity ini
                        requireActivity().finish() // <--- PERUBAHAN UTAMA DI SINI
                    }
                }
                is AddEditSampahViewModel.OperationResult.Error -> {
                    Log.e("FormSuratTugas", "Operation Result: Error - ${result.message}") // LOG INI
                    Snackbar.make(binding.root, "Error: ${result.message}", Snackbar.LENGTH_LONG).show()
                }
                AddEditSampahViewModel.OperationResult.Loading -> {
                    Log.d("FormSuratTugas", "Operation Result: Loading") // LOG INI
                    // Tampilkan progress bar
                }
                AddEditSampahViewModel.OperationResult.Idle -> {
                    Log.d("FormSuratTugas", "Operation Result: Idle") // LOG INI
                }
            }
        }
    }

    private fun setupListeners() {
        // Tombol Batal
        binding.btnBatal.setOnClickListener {
            // Kembali ke layar sebelumnya (bisa daftar utama atau detail)
            if (isEditModeForSection) {
                findNavController().popBackStack() // Kembali dari edit ke detail
            } else {
                // Untuk alur tambah baru, batal berarti menutup Activity
                requireActivity().finish() // <--- PERUBAHAN UTAMA DI SINI JUGA UNTUK BATAL
            }
        }

        // Tombol Simpan (utama)
        binding.btnSimpan.setOnClickListener {
            if (validateInputs()) {
                saveDataToViewModel()
                sharedViewModel.saveOrUpdateTransaksi() // Panggil fungsi simpan/update dari ViewModel
            } else {
                Snackbar.make(binding.root, "Mohon lengkapi semua field yang wajib diisi.", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Tombol Simpan (khusus untuk mode edit per bagian)
        // Pastikan fab_save_edit_section ada di layout XML Anda
        binding.fabSaveEditSection.setOnClickListener {
            if (validateInputs()) {
                saveDataToViewModel()
                sharedViewModel.saveOrUpdateTransaksi()
            } else {
                Snackbar.make(binding.root, "Mohon lengkapi semua field yang wajib diisi.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etNoKendaraan.text.isNullOrBlank()) {
            binding.tilNoKendaraan.error = "No. Kendaraan wajib diisi."
            isValid = false
        } else {
            binding.tilNoKendaraan.error = null
        }

        if (binding.actvJenisKendaraan.text.isNullOrBlank()) {
            binding.tilJenisKendaraan.error = "Jenis kendaraan wajib dipilih."
            isValid = false
        } else {
            binding.tilJenisKendaraan.error = null
        }

        if (binding.actvJenisPengirimSt.text.isNullOrBlank()) {
            binding.tilJenisPengirimSt.error = "Jenis pengirim wajib dipilih."
            isValid = false
        } else {
            binding.tilJenisPengirimSt.error = null
        }

        if (binding.actvNamaPengemudi.text.isNullOrBlank()) {
            binding.tilNamaPengemudi.error = "Nama pengemudi wajib dipilih."
            isValid = false
        } else {
            binding.tilNamaPengemudi.error = null
        }

        if (binding.actvCrew1.text.isNullOrBlank()) {
            binding.tilCrew1.error = "Crew 1 wajib diisi."
            isValid = false
        } else {
            binding.tilCrew1.error = null
        }

        // Crew 2-5 opsional, tidak perlu validasi wajib

        return isValid
    }

    private fun saveDataToViewModel() {
        // Generate atau gunakan kode surat jalan yang sudah ada
        val kodeSuratJalan = sharedViewModel.currentTransaksiData.value?.kodeSuratJalan.let {
            // Jika kodeSuratJalan dari ViewModel kosong/null atau "otomatis", generate yang baru
            if (it.isNullOrBlank() || it == "otomatis") generateRandomSuratJalanId() else it
        }

        sharedViewModel.updateSuratTugasData(
            noKendaraan = binding.etNoKendaraan.text.toString(),
            jenisKendaraan = binding.actvJenisKendaraan.text.toString(),
            jenisPengirimSuratTugas = binding.actvJenisPengirimSt.text.toString(),
            namaPengemudi = binding.actvNamaPengemudi.text.toString(),
            crew1 = binding.actvCrew1.text.toString(),
            crew2 = binding.actvCrew2.text.toString(),
            crew3 = binding.actvCrew3.text.toString(),
            crew4 = binding.actvCrew4.text.toString(),
            crew5 = binding.actvCrew5.text.toString()
        )

        // Ini bagian penting untuk mengisi `kodeSuratJalan` dan `idRitasiTpa`
        // Logic ini hanya dijalankan jika dalam mode TAMBAH BARU (bukan edit)
        if (sharedViewModel.isEditMode.value == false) {
            val generatedRitasiId = generateRandomRitasiId()
            // Perbarui data lokasi sumber dengan kode surat jalan yang dihasilkan
            sharedViewModel.updateLokasiSumberData(
                kodeSuratJalan = kodeSuratJalan, // Update kode surat jalan di data lokasi sumber
                lokasiSumberSampah = sharedViewModel.currentTransaksiData.value?.lokasiSumberSampah ?: "",
                alamatLokasi = sharedViewModel.currentTransaksiData.value?.alamatLokasi ?: "",
                jenisPengirimLokasi = sharedViewModel.currentTransaksiData.value?.jenisPengirimLokasi ?: "Pemerintah",
                statusKeterangkutan = sharedViewModel.currentTransaksiData.value?.statusKeterangkutan ?: "Belum",
                volumeTerangkutKg = sharedViewModel.currentTransaksiData.value?.volumeTerangkutKg ?: 0.0
            )
            // Perbarui data ritasi dengan ID ritasi yang dihasilkan
            sharedViewModel.updateRitasiData(
                idRitasiTpa = generatedRitasiId, // Update ID ritasi di data ritasi
                tanggalRitasi = sharedViewModel.currentTransaksiData.value?.tanggalRitasi ?: System.currentTimeMillis(),
                volumeSampahTon = sharedViewModel.currentTransaksiData.value?.volumeSampahTon ?: 0.0,
                jamMasuk = sharedViewModel.currentTransaksiData.value?.jamMasuk,
                jamKeluar = sharedViewModel.currentTransaksiData.value?.jamKeluar,
                petugasPencatat = sharedViewModel.currentTransaksiData.value?.petugasPencatat ?: "Admin",
                brutoKg = sharedViewModel.currentTransaksiData.value?.brutoKg ?: 0.0,
                tarraKg = sharedViewModel.currentTransaksiData.value?.tarraKg ?: 0.0,
                nettoTonaseKg = sharedViewModel.currentTransaksiData.value?.nettoTonaseKg ?: 0.0,
                zonaPembuangan = sharedViewModel.currentTransaksiData.value?.zonaPembuangan
            )
        }
    }

    private fun generateRandomSuratJalanId(): String {
        return "#${(10000000..99999999).random()}"
    }

    private fun generateRandomRitasiId(): String {
        return "#${(1000000..9999999).random()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}