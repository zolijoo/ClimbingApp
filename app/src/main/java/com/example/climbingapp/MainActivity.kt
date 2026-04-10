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
import android.widget.Button
import androidx.activity.ComponentActivity
import java.io.File
import java.io.IOException
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.core.content.ContextCompat
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.climbingapp.models.DataClassifier
import com.example.climbingapp.models.ModelLoader
import com.example.climbingapp.models.YearMonth
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.FileOutputStream
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.collections.get
import kotlin.text.toFloat


class MainActivity : ComponentActivity() {

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNotEmpty()) {
                fileUpload(uris)
            }
        }
    private var selectedModel = "LSTM"
    private var selectedChart = "Monthly"

    private val modelMap = mapOf(
        "LSTM" to "lstm.tflite",
        "GRU" to "gru.tflite",
        "1dConv" to "conv.tflite",
        "BiLSTM" to "bilstm.tflite",
        "Transformer" to "transformer.tflite"
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
    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            uri?.let {
                selectedUri = it
                pendingFile?.let { file -> copyFileToUri(file, uri) }
            } ?: run {
                Toast.makeText(this, "Save cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    private var pendingFile: File? = null
    private var selectedUri: Uri? = null
    val yearMonths = mutableListOf<YearMonth>()
    private lateinit var barChart: BarChart
    private lateinit var yearSpinner: Spinner
    private lateinit var chartSpinner: Spinner

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        barChart = findViewById(R.id.barChart)
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.axisRight.isEnabled = false
        val modelSpinner: Spinner = findViewById(R.id.modelSelectorSpinner)
        val modelOptions = listOf("LSTM", "GRU", "1dConv", "BiLSTM", "Transformer")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modelOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = spinnerAdapter
        yearSpinner = findViewById(R.id.yearSpinner)
        chartSpinner = findViewById(R.id.chartSpinner)

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModel = modelOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
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
            filePickerLauncher.launch(arrayOf("text/csv"))
        }
        val statButton: Button = findViewById(R.id.statB)
        statButton.setOnClickListener {
            viewStats()
        }
        val classifyButton :Button = findViewById(R.id.classifyB)
        classifyButton.setOnClickListener {
            showFileList()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshChart()
    }

    private fun refreshChart(){
        getDates()
        val chartOptions = listOf("Monthly", "Yearly")
        val chartSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chartOptions)
        chartSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        chartSpinner.adapter = chartSpinnerAdapter
        chartSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedChart = chartOptions[position]
                updateChart()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedChart = "Monthly"
            }
        }
    }

    private fun startSession() {
        val modelFileName = modelMap[selectedModel]
        val intent = Intent(this, SessionActivity::class.java)
        intent.putExtra("model",modelFileName)
        startActivity(intent)
    }
    private fun viewStats() {
        val intent = Intent(this, StatActivity::class.java)
        startActivity(intent)
    }
    private fun showFileList() {
        val sessionFolder = File(filesDir, "sessions")

        if (sessionFolder.exists() && sessionFolder.isDirectory) {
            val sessionFiles = sessionFolder.listFiles()

            if (sessionFiles != null && sessionFiles.isNotEmpty()) {
                val fileNames = sessionFiles.map { it.name }.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Choose which file to classify")
                    .setItems(fileNames) { _, which ->
                        val selectedFile = sessionFiles[which]
                        classifyData(selectedFile)
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
    private fun fileView(){
        val sessionFolder = File(filesDir, "sessions")

        if (sessionFolder.exists() && sessionFolder.isDirectory) {
            val sessionFiles = sessionFolder.listFiles()

            if (sessionFiles != null && sessionFiles.isNotEmpty()) {
                val fileNames = sessionFiles.map { it.name }.toTypedArray()

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
    private fun classifyData(file: File){
        val modelFileName = modelMap[selectedModel] ?: "lstm.tflite"
        val modelLoader = ModelLoader(this,modelFileName)
        modelLoader.loadModel()
        val classifier = DataClassifier(modelLoader.getInterpreter(), file)
        val result = classifier.ClassifyData()
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
    }
    private fun deleteFile(file: File) {
        if (file.exists() && file.delete()) {
            Toast.makeText(this, "${file.name} deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to delete ${file.name}", Toast.LENGTH_SHORT).show()
        }
        refreshChart()
    }
    private fun downloadFile(file: File) {
        pendingFile = file
        createFileLauncher.launch(file.name)
    }
    private fun copyFileToUri(sourceFile: File, uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(this, "${sourceFile.name} saved", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save ${sourceFile.name}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun fileUpload(uris: List<Uri>){
        val sessionsDir = File(filesDir, "sessions")
        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs()
        }
        for (uri in uris) {
            val inputStream = contentResolver.openInputStream(uri) ?: continue

            val fName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            } ?: "uploaded_${System.currentTimeMillis()}.csv"

            if (!fName.endsWith(".csv", ignoreCase = true)) {
                Toast.makeText(this, "$fName is not a CSV file.", Toast.LENGTH_SHORT).show()
                continue
            }

            val destinationFile = File(sessionsDir, fName)

            try {
                inputStream.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to upload $fName", Toast.LENGTH_SHORT).show()
            }
        }
        Toast.makeText(this, "Upload completed!", Toast.LENGTH_SHORT).show()
        refreshChart()
    }
    private fun getDates() {
        yearMonths.clear()
        val sessionFolder = File(filesDir, "sessions")
        if (sessionFolder.exists() && sessionFolder.isDirectory) {
            val sessionFiles = sessionFolder.listFiles()
            if (!sessionFiles.isNullOrEmpty()) {
                for (file in sessionFiles) {
                    if (file.isFile && file.extension == "csv") {
                        file.bufferedReader().useLines { lines ->
                            val firstLine = lines.firstOrNull()
                            if (firstLine != null) {
                                val columns = firstLine.split(",")
                                val date = columns[0]
                                val split = date.split(".")
                                val year = split.getOrNull(0)?.toIntOrNull()
                                val month = split.getOrNull(1)?.toIntOrNull()
                                if (year != null && month != null) {
                                    yearMonths.add(YearMonth(year, month))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun getMonthlyCounts(year: Int): List<Int> {
        val counts = yearMonths.groupingBy { it }.eachCount()
        return (1..12).map { month ->
            counts[YearMonth(year, month)] ?: 0
        }
    }
    fun getYearlyCounts(): List<Int> {
        val counts = yearMonths.groupingBy { it.year }.eachCount()
        return getAvailableYears().map { year ->
            counts[year] ?: 0
        }
    }


    fun getAvailableYears(): List<Int> {
        return yearMonths.map { it.year }.distinct().sorted()
    }
    fun updateMonthlyChart (data: List<Int>) {
        val entries = data.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Sessions")
        val barData = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        barChart.data = barData
        val months = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        barChart.xAxis.apply {
            axisMinimum = -0.5f
            axisMaximum = 11.5f
            valueFormatter = IndexAxisValueFormatter(months)
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            setDrawGridLines(false)
            setDrawLabels(true)
            labelCount = 12
        }
        barChart.axisLeft.apply {
            setDrawLabels(true)
            setDrawGridLines(true)
            axisMinimum = 0f
            granularity = 1f
            textColor = Color.WHITE
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        barChart.legend.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.setTouchEnabled(false)
        barChart.isDragEnabled = false
        barChart.setScaleEnabled(false)
        barChart.invalidate()
    }
    fun updateYearlyChart(){
        val years = getAvailableYears()
        val yearLabels = years.map { it.toString() }
        val data = getYearlyCounts()
        val entries = data.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Sessions")
        val barData = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        barChart.data = barData
        barChart.xAxis.apply {
            axisMinimum = -0.5f
            axisMaximum = years.size - 0.5f
            valueFormatter = IndexAxisValueFormatter(yearLabels)
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            setDrawGridLines(false)
            setDrawLabels(true)
            labelCount = years.size
        }
        barChart.axisLeft.apply {
            setDrawLabels(true)
            setDrawGridLines(true)
            axisMinimum = 0f
            granularity = 1f
            textColor = Color.WHITE
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        barChart.legend.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.setTouchEnabled(false)
        barChart.isDragEnabled = false
        barChart.setScaleEnabled(false)
        barChart.invalidate()
    }
    fun updateChart() {
        val years = getAvailableYears()
        if (selectedChart == "Monthly") {
            val yearSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
            yearSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpinner.adapter = yearSpinnerAdapter
            yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    val selectedYear = parent.getItemAtPosition(position) as Int
                    val monthlyData = getMonthlyCounts(selectedYear)
                    updateMonthlyChart(monthlyData)
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
        else {
            updateYearlyChart()
        }
    }
}
