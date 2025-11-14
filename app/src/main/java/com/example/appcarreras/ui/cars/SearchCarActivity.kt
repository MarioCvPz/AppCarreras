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
        supportActionBar?.title = "Buscar Coche Existente"
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
                Toast.makeText(this, "Selecciona un coche primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarTodosLosCoches() {
        if (torneoIdExcluir == -1L) {
            Toast.makeText(this, "Error: torneo no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val cochesEntities = cocheDao.buscarCochesExcluyendoTorneo(torneoIdExcluir)
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
        }
    }

    private fun buscarCoches(query: String) {
        if (torneoIdExcluir == -1L) return

        lifecycleScope.launch(Dispatchers.IO) {
            val cochesEntities = if (query.isEmpty()) {
                cocheDao.buscarCochesExcluyendoTorneo(torneoIdExcluir)
            } else {
                cocheDao.buscarCochesPorTexto(torneoIdExcluir, query)
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
        }
    }

    private fun mostrarEstadoVacio(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerSearchCars.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    companion object {
        const val EXTRA_TORNEO_ID_EXCLUIR = "EXTRA_TORNEO_ID_EXCLUIR"
        const val EXTRA_MARCA = "EXTRA_MARCA"
        const val EXTRA_MODELO = "EXTRA_MODELO"
        const val EXTRA_COLOR = "EXTRA_COLOR"
        const val EXTRA_DORSAL = "EXTRA_DORSAL"
    }
}

