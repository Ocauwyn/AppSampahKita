package com.example.appsampahkita.ui.daftar_sampah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appsampahkita.data.local.entity.TransaksiSampahEntity
import com.example.appsampahkita.databinding.ItemTransaksiSampahBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaksiSampahAdapter(
    private val onItemClick: (TransaksiSampahEntity) -> Unit,
    private val onMenujuTPAClick: (TransaksiSampahEntity) -> Unit // Untuk tombol 'Menuju TPA'
) : ListAdapter<TransaksiSampahEntity, TransaksiSampahAdapter.TransaksiSampahViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiSampahViewHolder {
        val binding = ItemTransaksiSampahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransaksiSampahViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransaksiSampahViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TransaksiSampahViewHolder(private val binding: ItemTransaksiSampahBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnLihatDetail.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
            binding.btnMenujuTpa.setOnClickListener {
                onMenujuTPAClick(getItem(adapterPosition))
            }
        }

        fun bind(transaksi: TransaksiSampahEntity) {
            binding.apply {
                tvIdSuratJalan.text = "#${transaksi.kodeSuratJalan}"
                tvNoKendaraan.text = transaksi.noKendaraan
                tvVolume.text = "${transaksi.volumeSampahTon} ton" // Atau gunakan volumeTerangkutKg
                tvIdRitasi.text = "#${transaksi.idRitasiTpa}"
                tvTanggal.text = formatDate(transaksi.tanggalRitasi)
                tvSumberSampah.text = transaksi.lokasiSumberSampah
                // Jika ada status di entity, bisa ditampilkan juga:
                // tvStatus.text = transaksi.statusKeterangkutan
            }
        }

        private fun formatDate(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) // Format Indonesia
            return format.format(date)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TransaksiSampahEntity>() {
        override fun areItemsTheSame(oldItem: TransaksiSampahEntity, newItem: TransaksiSampahEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TransaksiSampahEntity, newItem: TransaksiSampahEntity) =
            oldItem == newItem
    }
}