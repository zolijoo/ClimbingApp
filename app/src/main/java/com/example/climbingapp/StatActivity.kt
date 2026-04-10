package com.example.climbingapp

import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import androidx.activity.ComponentActivity
import java.io.File
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.TextView
import kotlin.math.max
import kotlin.math.round

class StatActivity : ComponentActivity() {

    private var whiteCount: Int = 0
    private var blueCount: Int = 0
    private var yellowCount: Int = 0
    private var greenCount: Int = 0
    private var redCount: Int = 0
    private var blackCount: Int = 0
    private var slowCount: Int = 0
    private var fastCount: Int = 0
    private var unclassifiedCount: Int = 0
    private var whiteFCount: Int = 0
    private var blueFCount: Int = 0
    private var yellowFCount: Int = 0
    private var greenFCount: Int = 0
    private var redFCount: Int = 0
    private var blackFCount: Int = 0
    private var wAttemptsCount: Int = 0
    private var bAttemptsCount: Int = 0
    private var yAttemptsCount: Int = 0
    private var gAttemptsCount: Int = 0
    private var rAttemptsCount: Int = 0
    private var blAttemptsCount: Int = 0
    private var wAttemptsFCount: Int = 0
    private var bAttemptsFCount: Int = 0
    private var yAttemptsFCount: Int = 0
    private var gAttemptsFCount: Int = 0
    private var rAttemptsFCount: Int = 0
    private var blAttemptsFCount: Int = 0
    private var flashes: Int = 0

    private lateinit var overallBoulders: TextView
    private lateinit var overallSent: TextView
    private lateinit var overallAttempts: TextView
    private lateinit var overallFast: TextView
    private lateinit var overallSlow: TextView
    private lateinit var overallUnclassified: TextView
    private lateinit var whiteSent: TextView
    private lateinit var whiteFailed: TextView
    private lateinit var whiteAttempts: TextView
    private lateinit var whiteFAttempts: TextView
    private lateinit var blueSent: TextView
    private lateinit var blueFailed: TextView
    private lateinit var blueAttempts: TextView
    private lateinit var blueFAttempts: TextView
    private lateinit var yellowSent: TextView
    private lateinit var yellowFailed: TextView
    private lateinit var yellowAttempts: TextView
    private lateinit var yellowFAttempts: TextView
    private lateinit var greenSent: TextView
    private lateinit var greenFailed: TextView
    private lateinit var greenAttempts: TextView
    private lateinit var greenFAttempts: TextView
    private lateinit var redSent: TextView
    private lateinit var redFailed: TextView
    private lateinit var redAttempts: TextView
    private lateinit var redFAttempts: TextView
    private lateinit var blackSent: TextView
    private lateinit var blackFailed: TextView
    private lateinit var blackAttempts: TextView
    private lateinit var blackFAttempts: TextView
    private lateinit var flashesView: TextView
    private lateinit var averageAttempts: TextView
    private lateinit var overallSuccessRateView: TextView
    private lateinit var overallFailed: TextView
    private lateinit var whiteSuccessRateView: TextView
    private lateinit var blueSuccessRateView: TextView
    private lateinit var yellowSuccessRateView: TextView
    private lateinit var greenSuccessRateView: TextView
    private lateinit var redSuccessRateView: TextView
    private lateinit var blackSuccessRateView: TextView

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
        setContentView(R.layout.stat_activity)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        overallBoulders = findViewById(R.id.overallView)
        overallSent = findViewById(R.id.overallSentView)
        overallAttempts = findViewById(R.id.overallAttemptsView)
        overallFast = findViewById(R.id.overallFastView)
        overallSlow = findViewById(R.id.overallSlowView)
        overallUnclassified = findViewById(R.id.overallUnclassifiedView)
        whiteSent = findViewById(R.id.whiteView)
        whiteFailed = findViewById(R.id.whiteFView)
        whiteAttempts = findViewById(R.id.whiteAttemptsView)
        whiteFAttempts = findViewById(R.id.whiteFAttemptsView)
        blueSent = findViewById(R.id.blueView)
        blueFailed = findViewById(R.id.blueFView)
        blueAttempts = findViewById(R.id.blueAttemptsView)
        blueFAttempts = findViewById(R.id.blueFAttemptsView)
        yellowSent = findViewById(R.id.yellowView)
        yellowFailed = findViewById(R.id.yellowFView)
        yellowAttempts = findViewById(R.id.yellowAttemptsView)
        yellowFAttempts = findViewById(R.id.yellowFAttemptsView)
        greenSent = findViewById(R.id.greenView)
        greenFailed = findViewById(R.id.greenFView)
        greenAttempts = findViewById(R.id.greenAttemptsView)
        greenFAttempts = findViewById(R.id.greenFAttemptsView)
        redSent = findViewById(R.id.redView)
        redFailed = findViewById(R.id.redFView)
        redAttempts = findViewById(R.id.redAttemptsView)
        redFAttempts = findViewById(R.id.redFAttemptsView)
        blackSent = findViewById(R.id.blackView)
        blackFailed = findViewById(R.id.blackFView)
        blackAttempts = findViewById(R.id.blackAttemptsView)
        blackFAttempts = findViewById(R.id.blackFAttemptsView)
        flashesView = findViewById(R.id.overallFlashesView)
        averageAttempts = findViewById(R.id.averageAttemptsView)
        overallSuccessRateView = findViewById(R.id.overallSuccessRateView)
        overallFailed = findViewById(R.id.overallFailedView)
        whiteSuccessRateView = findViewById(R.id.whiteSuccessRateView)
        blueSuccessRateView = findViewById(R.id.blueSuccessRateView)
        yellowSuccessRateView = findViewById(R.id.yellowSuccessRateView)
        greenSuccessRateView = findViewById(R.id.greenSuccessRateView)
        redSuccessRateView = findViewById(R.id.redSuccessRateView)
        blackSuccessRateView = findViewById(R.id.blackSuccessRateView)
        getStats()
    }
    private fun getStats(){
        val sessionFolder = File(filesDir, "sessions")
        if (sessionFolder.exists() && sessionFolder.isDirectory) {
            val sessionFiles = sessionFolder.listFiles()
            if (!sessionFiles.isNullOrEmpty()) {
                for (file in sessionFiles) {
                    if (file.isFile && file.extension == "csv") {
                        file.bufferedReader().useLines { lines ->
                            lines.drop(2).forEach { line ->
                                val columns = line.split(",")
                                if (columns.size >= 6) {
                                    val difficulty = columns[0]
                                    val attempts = columns[2].toIntOrNull() ?: 0
                                    val sent = columns[3].toIntOrNull() ?: 0
                                    val label = columns[5]
                                    if ((difficulty == "White") and (sent == 1)){
                                        whiteCount += 1
                                        wAttemptsCount += attempts
                                    } else if ((difficulty == "White") and (sent == 0)){
                                        whiteFCount += 1
                                        wAttemptsFCount += attempts
                                    }
                                    if ((difficulty == "Blue") and (sent == 1)){
                                        blueCount += 1
                                        bAttemptsCount += attempts
                                    } else if ((difficulty == "Blue") and (sent == 0)){
                                        blueFCount += 1
                                        bAttemptsFCount += attempts
                                    }
                                    if ((difficulty == "Yellow") and (sent == 1)){
                                        yellowCount += 1
                                        yAttemptsCount += attempts
                                    } else if ((difficulty == "Yellow") and (sent == 0)){
                                        yellowFCount += 1
                                        yAttemptsFCount += attempts
                                    }
                                    if ((difficulty == "Green") and (sent == 1)){
                                        greenCount += 1
                                        gAttemptsCount += attempts
                                    } else if ((difficulty == "Green") and (sent == 0)){
                                        greenFCount += 1
                                        gAttemptsFCount += attempts
                                    }
                                    if ((difficulty == "Red") and (sent == 1)){
                                        redCount += 1
                                        rAttemptsCount += attempts
                                    } else if ((difficulty == "Red") and (sent == 0)){
                                        redFCount += 1
                                        rAttemptsFCount += attempts
                                    }
                                    if ((difficulty == "Black") and (sent == 1)){
                                        blackCount += 1
                                        blAttemptsCount += attempts
                                    } else if ((difficulty == "Black") and (sent == 0)){
                                        blackFCount += 1
                                        blAttemptsFCount += attempts
                                    }
                                    if (label == "Fast"){
                                        fastCount += 1
                                    } else if (label == "Slow") {
                                        slowCount += 1
                                    }
                                    else if (label != "Label"){
                                        unclassifiedCount += 1
                                    }
                                    if (attempts == 1){
                                        flashes += 1
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        listStats()
    }
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun listStats(){
        val overallBouldersCount = whiteCount + whiteFCount + blueCount + blueFCount + yellowCount + yellowFCount +
                greenCount + greenFCount + redCount + redFCount + blackCount + blackFCount
        val overallAttemptsCount = wAttemptsCount + bAttemptsCount + yAttemptsCount + gAttemptsCount + rAttemptsCount + blAttemptsCount +
                wAttemptsFCount + bAttemptsFCount + yAttemptsFCount + gAttemptsFCount + rAttemptsFCount + blAttemptsFCount
        val overallSentCount = whiteCount + blueCount + yellowCount + greenCount + redCount + blackCount
        val overallFailedCount = whiteFCount + blueFCount + yellowFCount + greenFCount + redFCount + blackFCount
        var overallSuccessRateCount = 0.00
        var whiteSuccessRate = 0.00
        var blueSuccessRate = 0.00
        var yellowSuccessRate = 0.00
        var greenSuccessRate = 0.00
        var redSuccessRate = 0.00
        var blackSuccessRate = 0.00
        if (overallBouldersCount != 0) {
            overallSuccessRateCount = (overallSentCount.toDouble() / overallBouldersCount) * 100
        }
        if ((whiteCount != 0) or (whiteFCount != 0)) {
            whiteSuccessRate = (whiteCount.toDouble() / (whiteCount.toDouble() + whiteFCount)) * 100
        }
        if ((blueCount != 0) or (blueFCount != 0)) {
            blueSuccessRate = (blueCount.toDouble() / (blueCount.toDouble() + blueFCount)) * 100
        }
        if ((yellowCount != 0) or (yellowFCount != 0)) {
            yellowSuccessRate = (yellowCount.toDouble() / (yellowCount + yellowFCount)) * 100
        }
        if ((greenCount != 0) or (greenFCount != 0)) {
            greenSuccessRate = (greenCount.toDouble() / (greenCount + greenFCount)) * 100
        }
        if ((redCount != 0) or (redFCount != 0)) {
            redSuccessRate = (redCount.toDouble() / (redCount + redFCount)) * 100
        }
        if ((blackCount != 0) or (blackFCount != 0)) {
            blackSuccessRate = (blackCount.toDouble() / (blackCount + blackFCount)) * 100
        }

        overallBoulders.text = overallBouldersCount.toString()
        overallSent.text = overallSentCount.toString()
        overallAttempts.text = overallAttemptsCount.toString()
        overallFast.text = fastCount.toString()
        overallSlow.text = slowCount.toString()
        overallUnclassified.text = unclassifiedCount.toString()
        whiteSent.text = whiteCount.toString()
        whiteFailed.text = whiteFCount.toString()
        whiteAttempts.text = wAttemptsCount.toString()
        whiteFAttempts.text = wAttemptsFCount.toString()
        blueSent.text = blueCount.toString()
        blueFailed.text = blueFCount.toString()
        blueAttempts.text = bAttemptsCount.toString()
        blueFAttempts.text = bAttemptsFCount.toString()
        yellowSent.text = yellowCount.toString()
        yellowFailed.text = yellowFCount.toString()
        yellowAttempts.text = yAttemptsCount.toString()
        yellowFAttempts.text = yAttemptsFCount.toString()
        greenSent.text = greenCount.toString()
        greenFailed.text = greenFCount.toString()
        greenAttempts.text = gAttemptsCount.toString()
        greenFAttempts.text = gAttemptsFCount.toString()
        redSent.text = redCount.toString()
        redFailed.text = redFCount.toString()
        redAttempts.text = rAttemptsCount.toString()
        redFAttempts.text = rAttemptsFCount.toString()
        blackSent.text = blackCount.toString()
        blackFailed.text = blackFCount.toString()
        blackAttempts.text = blAttemptsCount.toString()
        blackFAttempts.text = blAttemptsFCount.toString()
        flashesView.text = flashes.toString()
        averageAttempts.text = String.format("%.2f", (overallAttemptsCount.toDouble() / overallBouldersCount))
        overallSuccessRateView.text = String.format("%.2f", overallSuccessRateCount) + "%"
        overallFailed.text = overallFailedCount.toString()
        whiteSuccessRateView.text = String.format("%.2f",whiteSuccessRate) + "%"
        blueSuccessRateView.text = String.format("%.2f",blueSuccessRate) + "%"
        yellowSuccessRateView.text = String.format("%.2f",yellowSuccessRate) + "%"
        greenSuccessRateView.text = String.format("%.2f",greenSuccessRate) + "%"
        redSuccessRateView.text = String.format("%.2f",redSuccessRate) + "%"
        blackSuccessRateView.text = String.format("%.2f",blackSuccessRate) + "%"

    }
}
