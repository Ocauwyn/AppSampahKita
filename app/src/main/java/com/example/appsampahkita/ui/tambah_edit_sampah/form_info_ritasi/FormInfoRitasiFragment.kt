package com.example.appsampahkita.ui.tambah_edit_sampah.form_info_ritasi

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle // Tambahkan import ini
import androidx.lifecycle.lifecycleScope // Tambahkan import ini
import androidx.lifecycle.repeatOnLifecycle // Tambahkan import ini
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.appsampahkita.R
import com.example.appsampahkita.databinding.FragmentFormInfoRitasiBinding
import com.example.appsampahkita.ui.tambah_edit_sampah.AddEditSampahViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch // Tambahkan import ini

@AndroidEntryPoint
class FormInfoRitasiFragment : Fragment() {

    private var _binding: FragmentFormInfoRitasiBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: AddEditSampahViewModel by activityViewModels()
    private val args: FormInfoRitasiFragmentArgs by navArgs()

    private var selectedDateMillis: Long = System.currentTimeMillis() // Untuk menyimpan tanggal yang dipilih

    private var isEditModeForSection: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormInfoRitasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cek apakah ini mode edit per bagian
        isEditModeForSection = args.editMode != null && args.editMode == "ritasi"
        Log.d("FormRitasi", "FormInfoRitasiFragment: isEditModeForSection = $isEditModeForSection") // LOG INI
        if (isEditModeForSection) {
            Log.d("FormRitasi", "FormInfoRitasiFragment: Navigated for section edit. Current Transaksi ID: ${args.transaksiId}") // LOG INI
        }


        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        sharedViewModel.currentTransaksiData.observe(viewLifecycleOwner) { data ->
            if (data == null) {
                Log.d("FormRitasi", "Observed data is NULL. Is it still loading or not found?") // LOG INI
            } else {
                Log.d("FormRitasi", "Observed data is NOT NULL. Populating UI with: $data") // LOG INI
                // Isi field jika ada data yang sudah dimuat
                binding.etIdRitasiTpa.setText(data.idRitasiTpa)
                binding.etTanggal.setText(formatDate(data.tanggalRitasi))
                selectedDateMillis = data.tanggalRitasi
                binding.etVolumeSampah.setText(DecimalFormat("#.##").format(data.volumeSampahTon))
                binding.etJamMasuk.setText(formatTime(data.jamMasuk))
                binding.etJamKeluar.setText(formatTime(data.jamKeluar))
                binding.etPetugasPencatat.setText(data.petugasPencatat)
                binding.etBruto.setText(DecimalFormat("#.##").format(data.brutoKg))
                binding.etTarra.setText(DecimalFormat("#.##").format(data.tarraKg))
                binding.etNettoTonase.setText(DecimalFormat("#.##").format(data.nettoTonaseKg))

                // Jika ini mode edit per bagian, langsung simpan
                if (isEditModeForSection) {
                    binding.llButtons.visibility = View.GONE // Sembunyikan tombol navigasi langkah
                    binding.fabSaveEditSection.visibility = View.VISIBLE // Tampilkan tombol simpan khusus
                    Log.d("FormRitasi", "Edit mode for section active. Hiding step buttons, showing FAB.") // LOG INI
                }
            }
        }

        // Tambahkan observasi operationResult untuk debugging tambahan
        sharedViewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddEditSampahViewModel.OperationResult.Success -> {
                    Log.d("FormRitasi", "Operation Result: Success - ${result.message}") // LOG INI
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_SHORT).show()
                    if (isEditModeForSection) {
                        findNavController().popBackStack(R.id.detailSampahFragment, false)
                    }
                }
                is AddEditSampahViewModel.OperationResult.Error -> {
                    Log.e("FormRitasi", "Operation Result: Error - ${result.message}") // LOG INI
                    Snackbar.make(binding.root, "Error: ${result.message}", Snackbar.LENGTH_LONG).show()
                }
                AddEditSampahViewModel.OperationResult.Loading -> {
                    Log.d("FormRitasi", "Operation Result: Loading") // LOG INI
                    // Tampilkan progress bar jika diperlukan
                }
                AddEditSampahViewModel.OperationResult.Idle -> {
                    Log.d("FormRitasi", "Operation Result: Idle") // LOG INI
                }
            }
        }
    }

    private fun setupListeners() {
        // Tombol Sebelumnya
        binding.btnSebelumnya.setOnClickListener {
            saveDataToViewModel() // Simpan data saat ini sebelum kembali
            findNavController().navigate(R.id.action_formInfoRitasiFragment_to_formLokasiSumberFragment)
        }

        // Tombol Selanjutnya
        binding.btnSelanjutnya.setOnClickListener {
            if (validateInputs()) {
                saveDataToViewModel()
                val action = FormInfoRitasiFragmentDirections.actionFormInfoRitasiFragmentToFormSuratTugasFragment()
                findNavController().navigate(action)
            } else {
                Snackbar.make(binding.root, "Mohon lengkapi semua field yang wajib diisi.", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Date Picker untuk Tanggal
        binding.etTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        // Listener untuk menghitung Netto secara otomatis
        binding.etBruto.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) calculateNetto()
        }
        binding.etTarra.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) calculateNetto()
        }

        // Tombol Simpan (khusus untuk mode edit per bagian)
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

        if (binding.etTanggal.text.isNullOrBlank()) {
            binding.tilTanggal.error = "Tanggal wajib diisi."
            isValid = false
        } else {
            binding.tilTanggal.error = null
        }

        if (binding.etVolumeSampah.text.isNullOrBlank() || binding.etVolumeSampah.text.toString().toDoubleOrNull() == null) {
            binding.tilVolumeSampah.error = "Volume sampah wajib diisi dengan angka."
            isValid = false
        } else {
            binding.tilVolumeSampah.error = null
        }

        val bruto = binding.etBruto.text.toString().toDoubleOrNull()
        val tarra = binding.etTarra.text.toString().toDoubleOrNull()

        if (bruto == null || bruto < 0) {
            binding.tilBruto.error = "Bruto wajib diisi dengan angka positif."
            isValid = false
        } else {
            binding.tilBruto.error = null
        }

        if (tarra == null || tarra < 0) {
            binding.tilTarra.error = "Tarra wajib diisi dengan angka positif."
            isValid = false
        } else {
            binding.tilTarra.error = null
        }

        if (bruto != null && tarra != null && tarra > bruto) {
            binding.tilTarra.error = "Tarra tidak boleh lebih besar dari bruto."
            isValid = false
        } else if (tarra != null) {
            binding.tilTarra.error = null
        }

        return isValid
    }

    private fun saveDataToViewModel() {
        val bruto = binding.etBruto.text.toString().toDoubleOrNull() ?: 0.0
        val tarra = binding.etTarra.text.toString().toDoubleOrNull() ?: 0.0
        val netto = if (bruto >= tarra) bruto - tarra else 0.0

        sharedViewModel.updateRitasiData(
            idRitasiTpa = binding.etIdRitasiTpa.text.toString(),
            tanggalRitasi = selectedDateMillis,
            volumeSampahTon = binding.etVolumeSampah.text.toString().toDoubleOrNull() ?: 0.0,
            jamMasuk = sharedViewModel.currentTransaksiData.value?.jamMasuk, // Pertahankan nilai dari ViewModel
            jamKeluar = sharedViewModel.currentTransaksiData.value?.jamKeluar, // Pertahankan nilai dari ViewModel
            petugasPencatat = sharedViewModel.currentTransaksiData.value?.petugasPencatat ?: "Admin", // Pertahankan nilai dari ViewModel
            brutoKg = bruto,
            tarraKg = tarra,
            nettoTonaseKg = netto,
            zonaPembuangan = sharedViewModel.currentTransaksiData.value?.zonaPembuangan // Pertahankan nilai dari ViewModel
        )
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDateMillis = newCalendar.timeInMillis
                binding.etTanggal.setText(formatDate(selectedDateMillis))
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun calculateNetto() {
        val bruto = binding.etBruto.text.toString().toDoubleOrNull() ?: 0.0
        val tarra = binding.etTarra.text.toString().toDoubleOrNull() ?: 0.0
        val netto = if (bruto >= tarra) bruto - tarra else 0.0
        binding.etNettoTonase.setText(DecimalFormat("#.##").format(netto))
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd/MMM/yyyy", Locale("id", "ID"))
        return format.format(date)
    }

    private fun formatTime(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "-"
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH.mm", Locale("id", "ID"))
        return "${format.format(date)} WIB"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}