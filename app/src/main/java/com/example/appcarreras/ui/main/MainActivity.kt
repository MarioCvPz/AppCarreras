package com.example.appcarreras.ui.main

import android.os.Bundle
import android.view.Menu
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.ui.torneo.Campeonato
import com.example.appcarreras.ui.torneo.CampeonatoAdapter
import com.example.appcarreras.R
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.data.entity.TorneoEntity
import com.example.appcarreras.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CampeonatoAdapter

    // Base de datos
    private val db by lazy { DatabaseProvider.getDatabase(this) }
    private val torneoDao by lazy { db.torneoDao() }
    private val cocheDao by lazy { db.cocheDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        // Inicializar RecyclerView
        adapter = CampeonatoAdapter(this, mutableListOf())
        binding.recyclerCampeonatos.layoutManager = LinearLayoutManager(this)
        binding.recyclerCampeonatos.adapter = adapter

        // Cargar torneos desde la base de datos
        cargarTorneosDesdeBD()

        // FAB para añadir campeonato
        binding.fabAdd.setOnClickListener { mostrarDialogoNuevoCampeonato() }
    }

    override fun onResume() {
        super.onResume()
        cargarTorneosDesdeBD()
    }

    /** Carga todos los torneos guardados en la base de datos y los muestra en el RecyclerView */
    private fun cargarTorneosDesdeBD() {
        lifecycleScope.launch(Dispatchers.IO) {
            val torneos = torneoDao.obtenerTorneos()

            // Convertimos TorneoEntity -> Campeonato (para el adaptador actual)
            val listaCampeonatos = convertirATorneosConConteo(torneos)

            withContext(Dispatchers.Main) {
                adapter.actualizarLista(listaCampeonatos)
            }
        }
    }

    /** Muestra el diálogo para crear un nuevo torneo y guardarlo en la BD */
    private fun mostrarDialogoNuevoCampeonato() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_campeonato, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreCampeonato)

        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setPositiveButton("Agregar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val botonPositivo = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonPositivo.setTextColor(resources.getColor(R.color.orange, null))
            botonPositivo.setOnClickListener {
                val nombre = etNombre.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        // 1️⃣ Guardamos en la base de datos
                        torneoDao.insertarTorneo(TorneoEntity(nombre = nombre))

                        // 2️⃣ Volvemos a cargar la lista actualizada
                        val torneos = torneoDao.obtenerTorneos()
                        val listaCampeonatos = convertirATorneosConConteo(torneos)

                        withContext(Dispatchers.Main) {
                            adapter.actualizarLista(listaCampeonatos)
                        }
                    }

                    dialog.dismiss()
                } else {
                    etNombre.error = "El nombre no puede estar vacío"
                }
            }

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.white, null))
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_campeonatos, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Buscar torneo..."

        searchView?.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarTorneos(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarTorneos(newText.orEmpty())
                return true
            }
        })

        return true
    }

    private fun filtrarTorneos(nombre: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val torneosFiltrados = if (nombre.isEmpty()) {
                torneoDao.obtenerTorneos()
            } else {
                torneoDao.buscarTorneosPorNombre(nombre)
            }

            val listaCampeonatos = convertirATorneosConConteo(torneosFiltrados)

            withContext(Dispatchers.Main) {
                adapter.actualizarLista(listaCampeonatos)
            }
        }
    }

    private suspend fun convertirATorneosConConteo(torneos: List<TorneoEntity>): List<Campeonato> {
        return torneos.map {
            val numCoches = cocheDao.contarCochesPorTorneo(it.idTorneo.toLong())
            Campeonato(it.nombre, numCoches, it.idTorneo)
        }
    }
}