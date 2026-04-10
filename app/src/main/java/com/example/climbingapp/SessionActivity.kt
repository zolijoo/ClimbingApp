package com.example.climbingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.climbingapp.adapters.RecordAdapter
import com.example.climbingapp.models.ModelLoader
import com.example.climbingapp.models.RecordItem
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionActivity : AppCompatActivity() {
    private var file: File? = null
    private val difficulties = mutableListOf<String>()
    private val values = mutableListOf<Int>()
    private val attemptsList = mutableListOf<Int>()
    private val sentList = mutableListOf<Int>()
    private val times = mutableListOf<String>()
    private val labels = mutableListOf<String>()
    private val T = 5
    private lateinit var tflite: Interpreter

    private lateinit var recyclerView: RecyclerView

    private val recordList = mutableListOf<RecordItem>()
    private val adapter = RecordAdapter(recordList)
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
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            uri?.let {
                saveCsvToUri(it)
            } ?: run {
                Toast.makeText(this, "Save cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        val modelFileName:String = intent.getStringExtra("model").toString()
        val stopSessionButton: Button = findViewById(R.id.stopSessionB)
        val whiteButton: Button = findViewById(R.id.whiteButton)
        val blueButton: Button = findViewById(R.id.blueButton)
        val yellowButton: Button = findViewById(R.id.yellowButton)
        val greenButton: Button = findViewById(R.id.greenButton)
        val redButton: Button = findViewById(R.id.redButton)
        val blackButton: Button = findViewById(R.id.blackButton)
        val sentSwitch: Switch = findViewById(R.id.sentSwitch)
        val attemptsInput: EditText = findViewById(R.id.attemptsInput)
        val predictButton: Button = findViewById(R.id.predictButton)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        stopSessionButton.setOnClickListener {
                showSaveDialog()
        }
        whiteButton.setOnClickListener {
            val attemptsText = attemptsInput.text.toString()

            if (attemptsText.isEmpty()) {
                Toast.makeText(this, "Please enter the number of attempts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val attempts = attemptsText.toIntOrNull()
            if (attempts == null || attempts < 0) {
                Toast.makeText(this, "Please enter a valid non-negative number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addEntry("White", 1, attemptsInput, sentSwitch)
        }
        blueButton.setOnClickListener {
            val attemptsText = attemptsInput.text.toString()

            if (attemptsText.isEmpty()) {
                Toast.makeText(this, "Please enter a valid number of attempts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val attempts = attemptsText.toIntOrNull()
            if (attempts == null || attempts < 0) {
                Toast.makeText(this, "Please enter a valid non-negative number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addEntry("Blue", 2, attemptsInput, sentSwitch)
        }
        yellowButton.setOnClickListener {
            val attemptsText = attemptsInput.text.toString()

            if (attemptsText.isEmpty()) {
                Toast.makeText(this, "Please enter a valid number of attempts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val attempts = attemptsText.toIntOrNull()
            if (attempts == null || attempts < 0) {
                Toast.makeText(this, "Please enter a valid non-negative number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addEntry("Yellow", 4, attemptsInput, sentSwitch)
        }
        greenButton.setOnClickListener {
            val attemptsText = attemptsInput.text.toString()

            if (attemptsText.isEmpty()) {
                Toast.makeText(this, "Please enter a valid number of attempts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val attempts = attemptsText.toIntOrNull()
            if (attempts == null || attempts < 0) {
                Toast.makeText(this, "Please enter a valid non-negative number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addEntry("Green", 8, attemptsInput, sentSwitch)
        }
        redButton.setOnClickListener {
            val attemptsText = attemptsInput.text.toString()

            if (attemptsText.isEmpty()) {
                Toast.makeText(this, "Please enter a valid number of attempts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val attempts = attemptsText.toIntOrNull()
            if (attempts == null || attempts < 0) {
                Toast.makeText(this, "Please enter a valid non-negative number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addEntry("Red", 12, attemptsInput, sentSwitch)
        }
        blackButton.setOnClickListener {
            val attemptsText = attemptsInput.text.toString()

            if (attemptsText.isEmpty()) {
                Toast.makeText(this, "Please enter a valid number of attempts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val attempts = attemptsText.toIntOrNull()
            if (attempts == null || attempts < 0) {
                Toast.makeText(this, "Please enter a valid non-negative number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addEntry("Black", 20, attemptsInput, sentSwitch)
        }
        predictButton.setOnClickListener {
            classifySessionData()
        }
        val modelLoader = ModelLoader(this,modelFileName)
        modelLoader.loadModel()
        tflite = modelLoader.getInterpreter()
    }
    private fun showSaveDialog() {

        try {
            val sessionFolder = File(filesDir, "sessions")
            if (!sessionFolder.exists()) {
                sessionFolder.mkdir()
            }

            val fileName = "session_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            val file = File(sessionFolder, fileName)

            val csvWriter = FileWriter(file)
            val currentDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
            csvWriter.append(currentDate)
            csvWriter.append("Difficulty,Value,Attempts,Sent,Time,Label\n")

            for (i in difficulties.indices) {
                val label = labels.getOrElse(i) { "" }
                csvWriter.append("${difficulties[i]},${values[i]},${attemptsList[i]},${sentList[i]},${times[i]},$label\n")
            }

            csvWriter.flush()
            csvWriter.close()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving internally", Toast.LENGTH_SHORT).show()
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save Session")
        builder.setMessage("Do you want to save the session CSV file in your Downloads folder?")

        builder.setPositiveButton("Yes") { _, _ ->
            stopSession(true)
        }

        builder.setNegativeButton("No") { _, _ ->
            stopSession(false)
        }

        builder.create().show()
    }
    private fun stopSession(saveFile: Boolean) {
        if (saveFile) {
            createFile()
        } else {
            Toast.makeText(this, "Session Ended", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    private fun createFile() {
        val fileName = "session_${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        }.csv"
        createFileLauncher.launch(fileName)
    }
    private fun saveCsvToUri(uri: Uri) {
        val currentDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.append(currentDate)
                    writer.append("Difficulty,Value,Attempts,Sent,Time,Label\n")

                    for (i in difficulties.indices) {
                        val label = labels.getOrElse(i) { "" }
                        writer.append("${difficulties[i]},${values[i]},${attemptsList[i]},${sentList[i]},${times[i]},$label\n")
                    }
                }
            }
            Toast.makeText(this, "Session Saved", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to Save", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
    private fun addEntry(difficulty: String, value: Int, attemptsInput: EditText, sentSwitch: Switch) {

        val attempts = attemptsInput.text.toString().toIntOrNull() ?: 0
        if (attempts < 0) {
            Toast.makeText(this, "Please enter a non-negative attempt number", Toast.LENGTH_SHORT).show()
            return
        }
        val sent = if (sentSwitch.isChecked) 1 else 0
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val record = RecordItem(
            difficulty = difficulty,
            value = value,
            attempts = attempts,
            sent = sent,
            time = currentTime
        )
        adapter.addRecord(record)
        recyclerView.scrollToPosition(recordList.size - 1)

        difficulties.add(difficulty)
        values.add(value)
        attemptsList.add(attempts)
        sentList.add(sent)
        times.add(currentTime)
    }

    private fun numerizeDifficulty(difficulty: String): Float {
        return when (difficulty) {
            "White" -> 0f
            "Blue" -> 1f
            "Yellow" -> 2f
            "Green" -> 3f
            "Red" -> 4f
            else -> 5f
        }
    }
    private fun timeToSeconds(time: String): Float {
        val parts = time.split(":").map { it.toInt() }
        return (parts[0] * 3600 + parts[1] * 60 + parts[2]).toFloat()
    }
    private fun classifySessionData() {
        if (difficulties.size < T) {
            Toast.makeText(this, "Not enough data to classify", Toast.LENGTH_SHORT).show()
            return
        }

        val votes = MutableList(difficulties.size) { mutableListOf<Float>() }
        val numWindows = difficulties.size - T + 1

        for (startIndex in 0 until numWindows) {
            classifyWindow(startIndex, votes)
        }

        labels.clear()
        for (i in votes.indices) {
            val avg = votes[i].average()
            val label = if (avg > 0.5) "Fast" else "Slow"
            labels.add(label)
        }
        Toast.makeText(this, "Prediction complete.", Toast.LENGTH_SHORT).show()
    }
    private fun classifyWindow(startIndex: Int, votes: MutableList<MutableList<Float>>) {
        // Prepare input data array for the window
        val inputArray = Array(1) { Array(T) { FloatArray(5) } }

        for (i in 0 until T) {
            val index = startIndex + i
            inputArray[0][i][0] = numerizeDifficulty(difficulties[index])
            inputArray[0][i][1] = values[index].toFloat()
            inputArray[0][i][2] = attemptsList[index].toFloat()
            inputArray[0][i][3] = sentList[index].toFloat()
            inputArray[0][i][4] = timeToSeconds(times[index])
        }

        val inputByteBuffer = ByteBuffer.allocateDirect(4 * T * 5).apply {
            order(ByteOrder.nativeOrder())
        }
        for (i in 0 until T) {
            inputByteBuffer.putFloat(inputArray[0][i][0])
            inputByteBuffer.putFloat(inputArray[0][i][1])
            inputByteBuffer.putFloat(inputArray[0][i][2])
            inputByteBuffer.putFloat(inputArray[0][i][3])
            inputByteBuffer.putFloat(inputArray[0][i][4])
        }
        inputByteBuffer.rewind()

        val outputBuffer = ByteBuffer.allocateDirect(4).apply {
            order(ByteOrder.nativeOrder())
        }

        tflite.run(inputByteBuffer, outputBuffer)
        outputBuffer.rewind()

        val prediction = outputBuffer.float
        for (i in 0 until T) {
            votes[startIndex + i].add(prediction)
        }
    }
}