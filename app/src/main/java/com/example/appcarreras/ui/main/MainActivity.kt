package com.example.appcarreras.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
        adapter = CampeonatoAdapter(
            this,
            mutableListOf(),
            onEditClick = { campeonato -> mostrarDialogoEditarCampeonato(campeonato) },
            onDeleteClick = { campeonato -> mostrarDialogoEliminarCampeonato(campeonato) }
        )
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
            try {
                val torneos = torneoDao.obtenerTorneos()

                // Convertimos TorneoEntity -> Campeonato (para el adaptador actual)
                val listaCampeonatos = convertirATorneosConConteo(torneos)

                withContext(Dispatchers.Main) {
                    adapter.actualizarLista(listaCampeonatos)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarError(getString(R.string.error_database))
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            mensaje,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).setBackgroundTint(
            androidx.core.content.ContextCompat.getColor(this, R.color.red)
        ).show()
    }

    private fun mostrarExito(mensaje: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            mensaje,
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).setBackgroundTint(
            androidx.core.content.ContextCompat.getColor(this, R.color.trophy_green)
        ).show()
    }

    /** Muestra el diálogo para crear un nuevo torneo y guardarlo en la BD */
    private fun mostrarDialogoNuevoCampeonato() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_campeonato, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreCampeonato)

        // referencias a opciones
        val optRed = dialogView.findViewById<FrameLayout>(R.id.optRed)
        val optYellow = dialogView.findViewById<FrameLayout>(R.id.optYellow)
        val optGreen = dialogView.findViewById<FrameLayout>(R.id.optGreen)
        val optBlue = dialogView.findViewById<FrameLayout>(R.id.optBlue)
        val optPurple = dialogView.findViewById<FrameLayout>(R.id.optPurple)
        val optOrange = dialogView.findViewById<FrameLayout>(R.id.optOrange)

        val colorMap = mapOf(
            optRed to ContextCompat.getColor(this, R.color.trophy_red),
            optYellow to ContextCompat.getColor(this, R.color.trophy_yellow),
            optGreen to ContextCompat.getColor(this, R.color.trophy_green),
            optBlue to ContextCompat.getColor(this, R.color.trophy_blue),
            optPurple to ContextCompat.getColor(this, R.color.trophy_purple),
            optOrange to ContextCompat.getColor(this, R.color.trophy_orange)
        )

        val opciones = colorMap.keys.toList()

        var selectedColor = colorMap[optYellow] ?: ContextCompat.getColor(this, R.color.trophy_yellow)

        fun marcarSeleccion(vSeleccionado: View) {
            opciones.forEach { it.alpha = if (it == vSeleccionado) 1f else 0.5f }
        }

        opciones.forEach { v ->
            v.setOnClickListener {
                selectedColor = colorMap[v] ?: ContextCompat.getColor(this, R.color.trophy_yellow)
                Log.d("COLOR_DEBUG", "Color seleccionado: $selectedColor")
                marcarSeleccion(v)
            }
        }
        marcarSeleccion(optYellow)

        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.button_add), null)
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create()

        dialog.setOnShowListener {
            val btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnOk.setTextColor(resources.getColor(R.color.orange, null))
            btnOk.setOnClickListener {
                val nombre = etNombre.text.toString().trim()
                if (nombre.isEmpty()) {
                    etNombre.error = getString(R.string.error_name_empty)
                    return@setOnClickListener
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        torneoDao.insertarTorneo(TorneoEntity(nombre = nombre, colorIcono = selectedColor))
                        val torneos = torneoDao.obtenerTorneos()
                        val listaCampeonatos = convertirATorneosConConteo(torneos)
                        withContext(Dispatchers.Main) { 
                            adapter.actualizarLista(listaCampeonatos)
                            mostrarExito(getString(R.string.success_operation_completed))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            mostrarError(getString(R.string.error_database))
                        }
                    }
                }
                dialog.dismiss()
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

        searchView?.queryHint = getString(R.string.hint_search_torneo)

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
            // Contar todos los coches del torneo (incluyendo los de todas las carreras)
            val numCoches = cocheDao.contarTodosLosCochesDelTorneo(it.idTorneo.toLong())
            Campeonato(
                nombre = it.nombre,
                numCoches = numCoches,
                idTorneo = it.idTorneo,
                colorIcono = it.colorIcono // 👈 ahora sí pasa el color elegido
            )
        }
    }

    /** Muestra el diálogo para editar un torneo existente */
    private fun mostrarDialogoEditarCampeonato(campeonato: Campeonato) {
        lifecycleScope.launch(Dispatchers.IO) {
            val torneo = torneoDao.obtenerTorneoPorId(campeonato.idTorneo)
            withContext(Dispatchers.Main) {
                if (torneo == null) return@withContext

                val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_campeonato, null)
                val etNombre = dialogView.findViewById<EditText>(R.id.etNombreCampeonato)
                etNombre.setText(torneo.nombre)

                // referencias a opciones
                val optRed = dialogView.findViewById<FrameLayout>(R.id.optRed)
                val optYellow = dialogView.findViewById<FrameLayout>(R.id.optYellow)
                val optGreen = dialogView.findViewById<FrameLayout>(R.id.optGreen)
                val optBlue = dialogView.findViewById<FrameLayout>(R.id.optBlue)
                val optPurple = dialogView.findViewById<FrameLayout>(R.id.optPurple)
                val optOrange = dialogView.findViewById<FrameLayout>(R.id.optOrange)

                val colorMap = mapOf(
                    optRed to ContextCompat.getColor(this@MainActivity, R.color.trophy_red),
                    optYellow to ContextCompat.getColor(this@MainActivity, R.color.trophy_yellow),
                    optGreen to ContextCompat.getColor(this@MainActivity, R.color.trophy_green),
                    optBlue to ContextCompat.getColor(this@MainActivity, R.color.trophy_blue),
                    optPurple to ContextCompat.getColor(this@MainActivity, R.color.trophy_purple),
                    optOrange to ContextCompat.getColor(this@MainActivity, R.color.trophy_orange)
                )

                val opciones = colorMap.keys.toList()
                var selectedColor = torneo.colorIcono

                fun marcarSeleccion(vSeleccionado: View) {
                    opciones.forEach { it.alpha = if (it == vSeleccionado) 1f else 0.5f }
                }

                // Seleccionar el color actual
                val colorActual = colorMap.entries.find { it.value == torneo.colorIcono }?.key
                if (colorActual != null) {
                    marcarSeleccion(colorActual)
                } else {
                    marcarSeleccion(optYellow)
                }

                opciones.forEach { v ->
                    v.setOnClickListener {
                        selectedColor = colorMap[v] ?: ContextCompat.getColor(this@MainActivity, R.color.trophy_yellow)
                        marcarSeleccion(v)
                    }
                }

                val dialog = AlertDialog.Builder(this@MainActivity, R.style.CustomDialogTheme)
                    .setTitle(R.string.title_edit_torneo)
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.button_save), null)
                    .setNegativeButton(getString(R.string.dialog_cancel), null)
                    .create()

                dialog.setOnShowListener {
                    val btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    btnOk.setTextColor(resources.getColor(R.color.orange, null))
                    btnOk.setOnClickListener {
                        val nombre = etNombre.text.toString().trim()
                        if (nombre.isEmpty()) {
                            etNombre.error = getString(R.string.error_name_empty)
                            return@setOnClickListener
                        }

                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                torneoDao.actualizarTorneo(torneo.copy(nombre = nombre, colorIcono = selectedColor))
                                val torneos = torneoDao.obtenerTorneos()
                                val listaCampeonatos = convertirATorneosConConteo(torneos)
                                withContext(Dispatchers.Main) { 
                                    adapter.actualizarLista(listaCampeonatos)
                                    mostrarExito(getString(R.string.success_operation_completed))
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    mostrarError(getString(R.string.error_database))
                                }
                            }
                        }
                        dialog.dismiss()
                    }

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.white, null))
                }

                dialog.show()
            }
        }
    }

    /** Muestra el diálogo de confirmación para eliminar un torneo */
    private fun mostrarDialogoEliminarCampeonato(campeonato: Campeonato) {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle(R.string.dialog_delete_torneo_title)
            .setMessage(getString(R.string.dialog_delete_torneo_message, campeonato.nombre))
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val torneo = torneoDao.obtenerTorneoPorId(campeonato.idTorneo)
                        torneo?.let {
                            torneoDao.eliminarTorneo(it)
                            val torneos = torneoDao.obtenerTorneos()
                            val listaCampeonatos = convertirATorneosConConteo(torneos)
                            withContext(Dispatchers.Main) { 
                                adapter.actualizarLista(listaCampeonatos)
                                mostrarExito(getString(R.string.success_operation_completed))
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            mostrarError(getString(R.string.error_database))
                        }
                    }
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
}