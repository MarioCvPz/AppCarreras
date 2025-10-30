package com.example.appcarreras.ui.racedetail

data class RaceIncident(
    val id: Int,
    val carNumber: Int,
    val carName: String,
    val incidentType: String,
    val time: String,
    val penaltyLaps: Int,
)