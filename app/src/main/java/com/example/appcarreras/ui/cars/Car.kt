package com.example.appcarreras.ui.cars

data class Car(
    val id: Int,
    val name: String,
    val team: String,
    var status: CarStatus
)

enum class CarStatus { GREEN, YELLOW, RED }
