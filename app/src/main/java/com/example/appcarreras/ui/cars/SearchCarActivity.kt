package com.example.appcarreras.ui.cars

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.R
import com.example.appcarreras.data.dao.CocheDao
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.databinding.ActivitySearchCarBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchCarBinding
    private val db by lazy { DatabaseProvider.getDatabase(this) }
    private val cocheDao by lazy { db.cocheDao() }
    
    private lateinit var adapter: SearchCarAdapter
    private val cochesList = mutableListOf<CocheDao.CocheBusqueda>()
    private var cocheSeleccionado: CocheDao.CocheBusqueda? = null
    private var torneoIdExcluir: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        torneoIdExcluir = intent.getLongExtra(EXTRA_TORNEO_ID_EXCLUIR, -1L)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupAddButton()
        
        cargarTodosLosCoches()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarSearchCar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_search_existing_car)
        binding.toolbarSearchCar.setNavigationOnClickListener { 
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = SearchCarAdapter(cochesList) { coche ->
            cocheSeleccionado = if (cocheSeleccionado == coche) {
                // Si ya está seleccionado, deseleccionar
                adapter.setSelectedCoche(null)
                null
            } else {
                // Seleccionar nuevo coche
                adapter.setSelectedCoche(coche)
                coche
            }
            binding.btnAddCar.isEnabled = cocheSeleccionado != null
        }
        
        binding.recyclerSearchCars.layoutManager = LinearLayoutManager(this)
        binding.recyclerSearchCars.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearchCar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                buscarCoches(query)
            }
        })
    }

    private fun setupAddButton() {
        binding.btnAddCar.isEnabled = false
        binding.btnAddCar.setOnClickListener {
            cocheSeleccionado?.let { coche ->
                val resultIntent = android.content.Intent().apply {
                    putExtra(EXTRA_MARCA, coche.marca)
                    putExtra(EXTRA_MODELO, coche.modelo)
                    putExtra(EXTRA_COLOR, coche.color)
                    putExtra(EXTRA_DORSAL, coche.dorsal)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: run {
                mostrarError(getString(R.string.error_select_car_first))
            }
        }
    }

    private fun cargarTodosLosCoches() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Mostrar TODOS los coches de TODOS los torneos, sin excluir ninguno
                val cochesEntities = cocheDao.obtenerTodosLosCoches()
                // Convertir a CocheBusqueda y eliminar duplicados
                val coches = cochesEntities
                    .map { CocheDao.CocheBusqueda.fromEntity(it) }
                    .distinctBy { "${it.marca}_${it.modelo}_${it.color}_${it.dorsal}" }
                    .sortedWith(compareBy({ it.marca }, { it.modelo }, { it.dorsal }))
                
                withContext(Dispatchers.Main) {
                    cochesList.clear()
                    cochesList.addAll(coches)
                    adapter.notifyDataSetChanged()
                    mostrarEstadoVacio(coches.isEmpty())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarError(getString(R.string.error_database))
                    e.printStackTrace() // Para debugging
                }
            }
        }
    }

    private fun buscarCoches(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Buscar en TODOS los coches de TODOS los torneos
                val cochesEntities = if (query.isEmpty()) {
                    // Si no hay query, mostrar todos los coches
                    cocheDao.obtenerTodosLosCoches()
                } else {
                    // Si hay query, buscar por texto en todos los coches
                    cocheDao.obtenerTodosLosCoches().filter {
                        it.marca.contains(query, ignoreCase = true) ||
                        it.modelo.contains(query, ignoreCase = true) ||
                        it.color.contains(query, ignoreCase = true) ||
                        it.dorsal.toString().contains(query, ignoreCase = true)
                    }
                }
                
                // Convertir a CocheBusqueda y eliminar duplicados
                val coches = cochesEntities
                    .map { CocheDao.CocheBusqueda.fromEntity(it) }
                    .distinctBy { "${it.marca}_${it.modelo}_${it.color}_${it.dorsal}" }
                    .sortedWith(compareBy({ it.marca }, { it.modelo }, { it.dorsal }))
                
                withContext(Dispatchers.Main) {
                    cochesList.clear()
                    cochesList.addAll(coches)
                    adapter.notifyDataSetChanged()
                    mostrarEstadoVacio(coches.isEmpty())
                    
                    // Si había un coche seleccionado, verificar si sigue en la lista
                    cocheSeleccionado?.let { seleccionado ->
                        val sigueEnLista = coches.any { 
                            it.marca == seleccionado.marca && 
                            it.modelo == seleccionado.modelo && 
                            it.color == seleccionado.color && 
                            it.dorsal == seleccionado.dorsal 
                        }
                        if (!sigueEnLista) {
                            cocheSeleccionado = null
                            adapter.setSelectedCoche(null)
                            binding.btnAddCar.isEnabled = false
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarError(getString(R.string.error_database))
                }
            }
        }
    }

    private fun mostrarEstadoVacio(isEmpty: Boolean) {
        binding.emptyState.text = getString(R.string.empty_no_cars_found)
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerSearchCars.visibility = if (isEmpty) View.GONE else View.VISIBLE
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
        const val EXTRA_TORNEO_ID_EXCLUIR = "EXTRA_TORNEO_ID_EXCLUIR"
        const val EXTRA_MARCA = "EXTRA_MARCA"
        const val EXTRA_MODELO = "EXTRA_MODELO"
        const val EXTRA_COLOR = "EXTRA_COLOR"
        const val EXTRA_DORSAL = "EXTRA_DORSAL"
    }
}

