package com.example.climbingapp.models

import android.content.Context
import android.widget.Toast
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class ModelLoader (context: Context,
                   val modelFileName: String){
    private lateinit var tflite: Interpreter
    private val appContext = context.applicationContext
    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        val fileDescriptor = appContext.assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    fun loadModel() {
        val tfliteModel = loadModelFile(modelFileName)
        tflite = Interpreter(tfliteModel)
    }
    fun getInterpreter(): Interpreter = tflite
}