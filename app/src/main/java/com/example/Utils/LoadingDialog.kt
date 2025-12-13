package com.example.Utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import com.example.myapplication.R
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class LoadingDialog : DialogFragment() {

    private var message: String = "Processing..."

    companion object {
        fun newInstance(message: String = "Processing..."): LoadingDialog {
            return LoadingDialog().apply {
                this.message = message
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_loading, null)

        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val messageText = view.findViewById<TextView>(R.id.message_text)
        messageText.text = message

        builder.setView(view)
        builder.setCancelable(false)

        return builder.create()
    }
}