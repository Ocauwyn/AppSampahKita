package com.example.appsampahkita.ui.daftar_sampah

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appsampahkita.R
import com.example.appsampahkita.databinding.FragmentDaftarSampahBinding
import com.example.appsampahkita.ui.daftar_sampah.adapter.TransaksiSampahAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DaftarSampahFragment : Fragment() {

    private var _binding: FragmentDaftarSampahBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DaftarSampahViewModel by viewModels()
    private lateinit var transaksiSampahAdapter: TransaksiSampahAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDaftarSampahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        transaksiSampahAdapter = TransaksiSampahAdapter(
            onItemClick = { transaksi ->
                // Navigasi ke DetailSampahFragment
                val action = DaftarSampahFragmentDirections.actionDaftarSampahFragmentToDetailSampahFragment(transaksi.id)
                findNavController().navigate(action)
            },
            onMenujuTPAClick = { transaksi ->
                // Implementasi logika untuk tombol "Menuju TPA"
                viewModel.updateStatusMenujuTPA(transaksi.id)
                Snackbar.make(binding.root, "Status 'Menuju TPA' untuk ${transaksi.kodeSuratJalan} diperbarui!", Snackbar.LENGTH_SHORT).show()
            }
        )
        binding.rvTransaksiSampah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transaksiSampahAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddSampah.setOnClickListener {
            // Navigasi ke AddEditSampahActivity untuk menambah data baru
            val action = DaftarSampahFragmentDirections.actionDaftarSampahFragmentToAddEditSampahActivity(-1L) // -1L menandakan mode tambah
            findNavController().navigate(action)
        }

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text.toString())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Amati daftar transaksi sampah
                launch {
                    viewModel.transaksiSampahList.observe(viewLifecycleOwner) { list ->
                        transaksiSampahAdapter.submitList(list)
                    }
                }

                // Amati UI State (loading, empty, no search results)
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.visibility = View.GONE
                        binding.tvNoData.visibility = View.GONE
                        binding.rvTransaksiSampah.visibility = View.VISIBLE

                        when (state) {
                            DaftarSampahViewModel.UiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.rvTransaksiSampah.visibility = View.GONE
                            }
                            DaftarSampahViewModel.UiState.Empty -> {
                                binding.tvNoData.text = "Tidak ada data sampah masuk."
                                binding.tvNoData.visibility = View.VISIBLE
                                binding.rvTransaksiSampah.visibility = View.GONE
                            }
                            DaftarSampahViewModel.UiState.NoSearchResults -> {
                                binding.tvNoData.text = "Tidak ada hasil untuk pencarian Anda."
                                binding.tvNoData.visibility = View.VISIBLE
                                binding.rvTransaksiSampah.visibility = View.GONE
                            }
                            is DaftarSampahViewModel.UiState.Error -> {
                                binding.tvNoData.text = "Error: ${state.message}"
                                binding.tvNoData.visibility = View.VISIBLE
                                Snackbar.make(binding.root, "Error: ${state.message}", Snackbar.LENGTH_LONG).show()
                            }
                            DaftarSampahViewModel.UiState.Success -> {
                                // Data berhasil dimuat, tampilkan RecyclerView (default visible)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}