package com.example.appcarreras.ui.cars

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.R
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.data.entity.CocheEntity
import com.example.appcarreras.databinding.ActivitySelectTournamentCarsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectTournamentCarsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectTournamentCarsBinding
    private val db by lazy { DatabaseProvider.getDatabase(this) }
    private val cocheDao by lazy { db.cocheDao() }
    
    private lateinit var adapter: SelectTournamentCarsAdapter
    private val cochesList = mutableListOf<CocheEntity>()
    private val cochesSeleccionados = mutableSetOf<Int>() // IDs de coches seleccionados
    
    private var torneoId: Long = -1L
    private var carreraId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectTournamentCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        torneoId = intent.getLongExtra(EXTRA_TORNEO_ID, -1L)
        carreraId = intent.getIntExtra(EXTRA_CARRERA_ID, -1)

        if (torneoId == -1L || carreraId == -1) {
            mostrarError(getString(R.string.error_invalid_torneo))
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupAddButton()
        
        cargarCochesDelTorneo()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarSelectCars)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_select_tournament_cars)
        binding.toolbarSelectCars.setNavigationOnClickListener { 
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = SelectTournamentCarsAdapter(cochesList, cochesSeleccionados) { cocheId ->
            // Toggle selección
            if (cochesSeleccionados.contains(cocheId)) {
                cochesSeleccionados.remove(cocheId)
            } else {
                cochesSeleccionados.add(cocheId)
            }
            adapter.notifyDataSetChanged()
            actualizarBotonAgregar()
        }
        
        binding.recyclerSelectCars.layoutManager = LinearLayoutManager(this)
        binding.recyclerSelectCars.adapter = adapter
        
        // Inicializar lista filtrada
        adapter.filter("")
    }

    private fun setupSearch() {
        binding.etSearchCar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                adapter.filter(query)
            }
        })
    }

    private fun setupAddButton() {
        binding.btnAddSelectedCars.isEnabled = false
        binding.btnAddSelectedCars.setOnClickListener {
            if (cochesSeleccionados.isNotEmpty()) {
                agregarCochesALaCarrera()
            } else {
                mostrarError(getString(R.string.error_select_cars_first))
            }
        }
    }

    private fun cargarCochesDelTorneo() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Obtener TODOS los coches del torneo (sin filtrar por carrera)
                // Esto incluye coches del nivel torneo y coches de todas las carreras
                val todosLosCoches = cocheDao.obtenerTodosLosCochesDelTorneo(torneoId.toInt())
                
                // Eliminar duplicados por idCoche (por si hay algún problema)
                val cochesUnicos = todosLosCoches.distinctBy { it.idCoche }
                
                android.util.Log.d("SelectTournamentCars", "Coches encontrados: ${cochesUnicos.size}, torneoId: $torneoId")
                
                withContext(Dispatchers.Main) {
                    cochesList.clear()
                    cochesList.addAll(cochesUnicos)
                    // Actualizar el adapter y la lista filtrada
                    adapter.filter("")
                    mostrarEstadoVacio(cochesUnicos.isEmpty())
                    android.util.Log.d("SelectTournamentCars", "Lista actualizada, tamaño: ${cochesList.size}, filtrados: ${adapter.itemCount}")
                }
            } catch (e: Exception) {
                android.util.Log.e("SelectTournamentCars", "Error cargando coches", e)
                withContext(Dispatchers.Main) {
                    mostrarError(getString(R.string.error_database))
                    e.printStackTrace()
                }
            }
        }
    }

    private fun agregarCochesALaCarrera() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Actualizar cada coche seleccionado para asignarlo a esta carrera
                cochesSeleccionados.forEach { cocheId ->
                    val coche = cocheDao.obtenerCochePorId(cocheId)
                    coche?.let {
                        val cocheActualizado = it.copy(carreraId = carreraId)
                        cocheDao.actualizarCoche(cocheActualizado)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    mostrarExito(getString(R.string.message_cars_added_to_race, cochesSeleccionados.size))
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarError(getString(R.string.error_database))
                }
            }
        }
    }

    private fun actualizarBotonAgregar() {
        binding.btnAddSelectedCars.isEnabled = cochesSeleccionados.isNotEmpty()
        val count = cochesSeleccionados.size
        binding.btnAddSelectedCars.text = if (count > 0) {
            getString(R.string.button_add_selected_cars_count, count)
        } else {
            getString(R.string.button_add_selected_cars)
        }
    }

    private fun mostrarEstadoVacio(isEmpty: Boolean) {
        binding.emptyState.text = getString(R.string.empty_no_cars_available)
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerSelectCars.visibility = if (isEmpty) View.GONE else View.VISIBLE
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

    private fun mostrarError(mensaje: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            mensaje,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).setBackgroundTint(
            androidx.core.content.ContextCompat.getColor(this, R.color.red)
        ).show()
    }

    companion object {
        const val EXTRA_TORNEO_ID = "EXTRA_TORNEO_ID"
        const val EXTRA_CARRERA_ID = "EXTRA_CARRERA_ID"
    }
}

