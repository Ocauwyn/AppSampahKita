// file: app/src/main/java/com.example.appsampahkita.ui.tambah_edit_sampah/AddEditSampahActivity.kt
package com.example.appsampahkita.ui.tambah_edit_sampah

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.appsampahkita.R
import com.example.appsampahkita.databinding.ActivityAddEditSampahBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditSampahActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditSampahBinding
    private lateinit var navController: NavController
    private val viewModel: AddEditSampahViewModel by viewModels() // Shared ViewModel for fragments

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditSampahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.add_edit_nav_host_fragment) as NavHostFragment

        // Dapatkan NavGraph dari file nav_graph.xml
        val graph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph)

        // Set NavController untuk menggunakan nested graph (add_edit_flow_nav_graph) sebagai start destination
        // Ini adalah cara yang benar untuk Activity yang me-host nested graph
        graph.setStartDestination(R.id.add_edit_flow_nav_graph) // Set start destination ke nested graph
        navHostFragment.navController.setGraph(graph, intent.extras) // Pass arguments from intent to graph

        navController = navHostFragment.navController // Inisialisasi navController

        // Setup Toolbar
        // AppBarConfiguration harus mencakup root graph atau set destination yang ingin tombol back tampil
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.formLokasiSumberFragment)) // setOf dengan ID fragment awal di nested graph
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        // Observe current destination to update stepper
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateStepper(destination.id)
            // Toolbar title akan diatur oleh ViewModel's isEditMode observer, atau bisa diatur di sini
            // updateToolbarTitle(destination.label.toString())
        }

        // Observe ViewModel's edit mode to adjust toolbar title
        viewModel.isEditMode.observe(this) { isEdit ->
            if (isEdit) {
                binding.toolbar.title = "Ubah Data Sampah"
            } else {
                binding.toolbar.title = "Tambah Data Sampah"
            }
        }
    }

    private fun updateStepper(destinationId: Int) {
        val greenPrimary = getColor(R.color.green_primary)
        val blackColor = getColor(R.color.black)
        val transparent = Color.TRANSPARENT
        val grayOutline = getColor(R.color.darker_gray)

        // Reset all stepper indicators
        binding.tvStep1.setBackgroundResource(R.drawable.rounded_grey_outline)
        binding.tvStep1.setTextColor(blackColor)
        binding.tvStep2.setBackgroundResource(R.drawable.rounded_grey_outline)
        binding.tvStep2.setTextColor(blackColor)
        binding.tvStep3.setBackgroundResource(R.drawable.rounded_grey_outline)
        binding.tvStep3.setTextColor(blackColor)

        // Reset lines to default
        binding.line12.setBackgroundColor(getColor(R.color.green_light))
        binding.line23.setBackgroundColor(getColor(R.color.green_light))

        when (destinationId) {
            R.id.formLokasiSumberFragment -> {
                binding.tvStep1.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.tvStep1.setTextColor(Color.WHITE)
            }
            R.id.formInfoRitasiFragment -> {
                binding.tvStep1.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.tvStep1.setTextColor(Color.WHITE)
                binding.line12.setBackgroundColor(greenPrimary) // Line to green
                binding.tvStep2.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.tvStep2.setTextColor(Color.WHITE)
            }
            R.id.formSuratTugasFragment -> {
                binding.tvStep1.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.tvStep1.setTextColor(Color.WHITE)
                binding.line12.setBackgroundColor(greenPrimary)
                binding.tvStep2.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.tvStep2.setTextColor(Color.WHITE)
                binding.line23.setBackgroundColor(greenPrimary)
                binding.tvStep3.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.tvStep3.setTextColor(Color.WHITE)
            }
        }
    }
}