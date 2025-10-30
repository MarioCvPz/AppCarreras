package com.example.appcarreras.ui.torneo

data class Campeonato(
    val nombre: String,
    var numCoches: Int = 0,  // número de coches inscritos
    val idTorneo: Int
)
