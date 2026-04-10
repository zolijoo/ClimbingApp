package com.example.climbingapp.models

data class RecordItem(
    val difficulty: String,
    val value: Int,
    val attempts: Int,
    val sent: Int,
    val time: String)
