package com.example.climbingapp.models

import android.util.Log
import org.tensorflow.lite.Interpreter
import android.widget.Toast
import java.io.File
import java.io.FileWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DataClassifier (private val interpreter: Interpreter,
                      private val file: File){
    private val difficulties = mutableListOf<String>()
    private val values = mutableListOf<Int>()
    private val attemptsList = mutableListOf<Int>()
    private val sentList = mutableListOf<Int>()
    private val times = mutableListOf<String>()
    private val labels = mutableListOf<String>()
    private val t = 5

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

    fun ClassifyData(): String{
        difficulties.clear()
        values.clear()
        attemptsList.clear()
        sentList.clear()
        times.clear()
        labels.clear()
        val outputShape = interpreter.getOutputTensor(0).shape()
        Log.d("MODEL_DEBUG", "Output shape: ${outputShape.joinToString()}")
        file.bufferedReader().useLines { lines ->
            lines.drop(2).forEach { line ->
                val columns = line.split(",")
                if (columns.size >= 6) {
                    difficulties.add(columns[0])
                    values.add(columns[1].toIntOrNull() ?: 0)
                    attemptsList.add(columns[2].toIntOrNull() ?: 0)
                    sentList.add(columns[3].toIntOrNull() ?: 0)
                    times.add(columns[4])
                }
            }
        }
        if (difficulties.size < t) {
            return "Not enough data to classify"
        }

        val votes = MutableList(difficulties.size) { mutableListOf<Float>() }
        val numWindows = difficulties.size - t + 1

        for (startIndex in 0 until numWindows) {
            classifyWindow(startIndex, votes)
        }

        labels.clear()
        for (i in votes.indices) {
            val avg = votes[i].average()
            val label = if (avg > 0.5) "Fast" else "Slow"
            labels.add(label)
        }

        val csvWriter = FileWriter(file)
        csvWriter.append("Difficulty,Value,Attempts,Sent,Time,Label\n")
        for (i in difficulties.indices) {
            csvWriter.append("${difficulties[i]},${values[i]},${attemptsList[i]},${sentList[i]},${times[i]},${labels[i]}\n")
        }
        csvWriter.flush()
        csvWriter.close()
        return "Classification complete."
    }

    private fun classifyWindow(startIndex: Int, votes: MutableList<MutableList<Float>>) {
        // Prepare input data array for the window
        val inputArray = Array(1) { Array(t) { FloatArray(5) } }

        for (i in 0 until t) {
            val index = startIndex + i
            inputArray[0][i][0] = numerizeDifficulty(difficulties[index])
            inputArray[0][i][1] = values[index].toFloat()
            inputArray[0][i][2] = attemptsList[index].toFloat()
            inputArray[0][i][3] = sentList[index].toFloat()
            inputArray[0][i][4] = timeToSeconds(times[index])
        }

        Log.d("MODEL_DEBUG", "---- Window starting at $startIndex ----")
        for (i in 0 until t) {
            Log.d("MODEL_DEBUG", inputArray[0][i].joinToString())
        }

        val inputByteBuffer = ByteBuffer.allocateDirect(4 * t * 5).apply {
            order(ByteOrder.nativeOrder())
        }
        for (i in 0 until t) {
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

        interpreter.run(inputByteBuffer, outputBuffer)
        outputBuffer.rewind()

        val prediction = outputBuffer.float
        for (i in 0 until t) {
            votes[startIndex + i].add(prediction)
        }
    }
}