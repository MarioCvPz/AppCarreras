package com.example.appcarreras

data class Car(
    val id: Int,
    val name: String,
    val team: String,
    var status: CarStatus
)

enum class CarStatus { GREEN, YELLOW, RED }
