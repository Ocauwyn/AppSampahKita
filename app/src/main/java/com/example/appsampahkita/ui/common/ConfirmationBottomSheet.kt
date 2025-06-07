package com.example.appsampahkita.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.example.appsampahkita.databinding.DialogConfirmationBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogConfirmationBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var onConfirmListener: (() -> Unit)? = null

    companion object {
        const val TAG = "ConfirmationBottomSheet"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_CONFIRM_TEXT = "arg_confirm_text"
        private const val ARG_CANCEL_TEXT = "arg_cancel_text"
        private const val ARG_ILLUSTRATION_RES_ID = "arg_illustration_res_id"

        fun newInstance(
            title: String,
            message: String,
            confirmButtonText: String,
            cancelButtonText: String,
            @DrawableRes illustrationResId: Int? = null // Resource ID untuk ilustrasi
        ): ConfirmationBottomSheet {
            val fragment = ConfirmationBottomSheet()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
                putString(ARG_CONFIRM_TEXT, confirmButtonText)
                putString(ARG_CANCEL_TEXT, cancelButtonText)
                illustrationResId?.let { putInt(ARG_ILLUSTRATION_RES_ID, it) }
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfirmationBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            binding.tvTitle.text = it.getString(ARG_TITLE)
            binding.tvMessage.text = it.getString(ARG_MESSAGE)
            binding.btnConfirm.text = it.getString(ARG_CONFIRM_TEXT)
            binding.btnCancel.text = it.getString(ARG_CANCEL_TEXT)
            val illustrationResId = it.getInt(ARG_ILLUSTRATION_RES_ID, 0)
            if (illustrationResId != 0) {
                binding.ivIllustration.setImageResource(illustrationResId)
                binding.ivIllustration.visibility = View.VISIBLE
            } else {
                binding.ivIllustration.visibility = View.GONE
            }
        }

        binding.btnConfirm.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss() // Tutup bottom sheet setelah konfirmasi
        }

        binding.btnCancel.setOnClickListener {
            dismiss() // Tutup bottom sheet saat dibatalkan
        }
    }

    fun setOnConfirmListener(listener: () -> Unit) {
        onConfirmListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}