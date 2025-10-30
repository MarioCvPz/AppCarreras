package com.example.appcarreras

data class Car(
    val name: String,
    val team: String,
    val status: CarStatus
)

enum class CarStatus { GREEN, YELLOW, RED }
