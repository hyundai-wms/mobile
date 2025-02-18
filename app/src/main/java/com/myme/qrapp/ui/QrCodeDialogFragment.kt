package com.myme.qrapp.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.myme.qrapp.databinding.FragmentQrCodeDialogBinding

class QrCodeDialogFragment(private val qrCodeValue: String) : DialogFragment() {

    private var _binding: FragmentQrCodeDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentQrCodeDialogBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)

        binding.qrCodeTextView.text = qrCodeValue  // QR 코드 정보를 표시

        binding.closeButton.setOnClickListener {
            dismiss()  // 다이얼로그 닫기
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
