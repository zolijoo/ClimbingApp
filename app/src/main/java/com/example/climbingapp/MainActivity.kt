package com.example.climbingapp

import android.os.Bundle
import android.app.AlertDialog
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.view.View
import android.os.Environment
import android.widget.Button
import androidx.activity.ComponentActivity
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.EditText
import android.widget.Switch
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import java.io.FileOutputStream
import java.io.OutputStream





class MainActivity : ComponentActivity() {

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { fileUpload(it) }
    }
    private var selectedModel = "LSTM"

    private val modelMap = mapOf(
        "LSTM" to "lstm.tflite",
        "GRU" to "gru.tflite",
        "1dConv" to "conv.tflite"
    )
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission if not granted
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        val modelSpinner: Spinner = findViewById(R.id.modelSelectorSpinner)
        val modelOptions = listOf("LSTM", "GRU", "1dConv")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modelOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = spinnerAdapter

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModel = modelOptions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Default
                selectedModel = "LSTM"
            }
        }

        val startSessionButton: Button = findViewById(R.id.startSessionB)
        startSessionButton.setOnClickListener {
                startSession()
        }
        val filesButton: Button = findViewById(R.id.filesB)
        filesButton.setOnClickListener {
            fileView()
        }
        val uploadButton: Button = findViewById(R.id.uploadB)
        uploadButton.setOnClickListener{
            filePickerLauncher.launch("*/*")
        }
    }

    private fun startSession() {
        val modelFileName = modelMap[selectedModel]
        val intent = Intent(this, SessionActivity::class.java)
        intent.putExtra("model",modelFileName)
        startActivity(intent)
    }
    private fun fileView(){
        val sessionFolder = File(filesDir, "sessions")

        if (sessionFolder.exists() && sessionFolder.isDirectory) {
            val sessionFiles = sessionFolder.listFiles()

            if (sessionFiles != null && sessionFiles.isNotEmpty()) {
                val fileNames = sessionFiles.map { it.name }.toTypedArray()

                // Show a dialog with the list of saved session files
                AlertDialog.Builder(this)
                    .setTitle("Select a Session File")
                    .setItems(fileNames) { _, which ->
                        val selectedFile = sessionFiles[which]
                        showFileOptionsDialog(selectedFile)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this, "No sessions saved", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No sessions folder found", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showFileOptionsDialog(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Options for ${file.name}")
            .setMessage("Choose action:")
            .setPositiveButton("Download") { _, _ -> downloadFile(file) }
            .setNegativeButton("Delete") { _, _ -> deleteFile(file) }
            .setNeutralButton("Cancel", null)
            .show()
    }
    private fun deleteFile(file: File) {
        if (file.exists() && file.delete()) {
            Toast.makeText(this, "${file.name} deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to delete ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun downloadFile(file: File) {

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val destination = File(downloadsDir, file.name)

        try {
            file.inputStream().use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(this, "${file.name} saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to download ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun fileUpload(uri: Uri){
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val fName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        } ?: "uploaded_${System.currentTimeMillis()}.csv" // Default if name retrieval fails

        if (!fName.endsWith(".csv", ignoreCase = true)) {
            Toast.makeText(this, "Please select a CSV file.", Toast.LENGTH_SHORT).show()
            return
        }
        val sessionsDir = File(filesDir, "sessions")

        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs()
        }

        val destinationFile = File(sessionsDir, fName)

        try {
            inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "$fName uploaded successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to upload file", Toast.LENGTH_SHORT).show()
        }
    }


}
