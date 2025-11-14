package com.example.appcarreras.ui.cars

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appcarreras.R
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.data.entity.CocheEntity
import com.example.appcarreras.databinding.ActivityAddCarBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCarBinding
    private val db by lazy { DatabaseProvider.getDatabase(this) }
    private val carDao by lazy { db.cocheDao() }

    private var torneoId: Long = -1L
    private var carreraId: Int? = null
    private var cocheId: Int? = null
    private var isEditMode = false

    private val searchCarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val marca = data.getStringExtra(SearchCarActivity.EXTRA_MARCA) ?: ""
                val modelo = data.getStringExtra(SearchCarActivity.EXTRA_MODELO) ?: ""
                val color = data.getStringExtra(SearchCarActivity.EXTRA_COLOR) ?: ""
                val dorsal = data.getIntExtra(SearchCarActivity.EXTRA_DORSAL, -1)

                if (marca.isNotEmpty() && modelo.isNotEmpty() && color.isNotEmpty() && dorsal != -1) {
                    binding.etMarca.setText(marca)
                    binding.etModelo.setText(modelo)
                    binding.etColor.setText(color)
                    binding.etDorsal.setText(dorsal.toString())
                }
            }
        }
    }

    private val selectTournamentCarsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Los coches se han agregado, cerrar esta actividad
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbarAddCar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddCar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Obtener id del torneo desde el intent
        torneoId = intent.getLongExtra("TORNEO_ID", -1L)
        carreraId = if (intent.hasExtra(EXTRA_CARRERA_ID)) {
            intent.getIntExtra(EXTRA_CARRERA_ID, -1).takeIf { it != -1 }
        } else {
            null
        }
        cocheId = if (intent.hasExtra(EXTRA_COCHE_ID)) {
            intent.getIntExtra(EXTRA_COCHE_ID, -1).takeIf { it != -1 }
        } else {
            null
        }
        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT_MODE, false)

        if (torneoId == -1L) {
            mostrarError(getString(R.string.error_torneo_not_found))
            finish()
            return
        }

        if (isEditMode) {
            supportActionBar?.title = getString(R.string.title_edit_car)
            cargarDatosCoche()
            // Ocultar botón de búsqueda en modo edición
            binding.btnSearchExistingCar.visibility = View.GONE
        } else {
            supportActionBar?.title = getString(R.string.title_register_new_car)
            if (carreraId != null) {
                supportActionBar?.title = getString(R.string.title_add_race_car)
            }
        }

        // Acción del botón "Save Car"
        binding.btnSaveCar.setOnClickListener {
            guardarCar()
        }

        // Acción del botón "Buscar Coche Existente" (solo visible en modo creación)
        binding.btnSearchExistingCar.setOnClickListener {
            val intent = Intent(this, SearchCarActivity::class.java)
            intent.putExtra(SearchCarActivity.EXTRA_TORNEO_ID_EXCLUIR, torneoId)
            searchCarLauncher.launch(intent)
        }

        // Acción del botón "Agregar Coches del Torneo" (solo visible en modo creación y cuando hay carreraId)
        binding.btnAddTournamentCars.setOnClickListener {
            if (carreraId != null) {
                val intent = Intent(this, SelectTournamentCarsActivity::class.java)
                intent.putExtra(SelectTournamentCarsActivity.EXTRA_TORNEO_ID, torneoId)
                intent.putExtra(SelectTournamentCarsActivity.EXTRA_CARRERA_ID, carreraId!!)
                selectTournamentCarsLauncher.launch(intent)
            }
        }

        // Ocultar botón si no hay carreraId o está en modo edición
        if (carreraId == null || isEditMode) {
            binding.btnAddTournamentCars.visibility = View.GONE
        }
    }

    private fun cargarDatosCoche() {
        if (cocheId == null) return
        lifecycleScope.launch(Dispatchers.IO) {
            val coche = carDao.obtenerCochePorId(cocheId!!)
            withContext(Dispatchers.Main) {
                coche?.let {
                    binding.etMarca.setText(it.marca)
                    binding.etModelo.setText(it.modelo)
                    binding.etColor.setText(it.color)
                    binding.etDorsal.setText(it.dorsal.toString())
                }
            }
        }
    }

    private fun guardarCar() {
        val marca = binding.etMarca.text.toString().trim()
        val modelo = binding.etModelo.text.toString().trim()
        val color = binding.etColor.text.toString().trim()
        val dorsalText = binding.etDorsal.text.toString().trim()

        // Resetear errores
        binding.layoutDorsal.error = null
        binding.tvErrorDorsal.visibility = View.GONE

        if (marca.isEmpty() || modelo.isEmpty() || color.isEmpty() || dorsalText.isEmpty()) {
            mostrarError(getString(R.string.error_complete_all_fields))
            return
        }

        val dorsal = dorsalText.toIntOrNull()
        if (dorsal == null || dorsal <= 0) {
            binding.layoutDorsal.error = getString(R.string.error_invalid_number)
            return
        }

        // Mostrar estado de carga
        mostrarCargando(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Verificar que el dorsal no esté en uso por otro coche en TODO el torneo
                val cocheActual = if (isEditMode && cocheId != null) {
                    carDao.obtenerCochePorId(cocheId!!)
                } else {
                    null
                }
                
                // Verificar si el dorsal existe en todo el torneo (incluyendo todas las carreras)
                val existeDorsal = if (isEditMode && cocheId != null) {
                    // Si estamos editando, excluir el coche actual de la verificación
                    carDao.existeDorsalEnTodoElTorneoExcluyendo(torneoId, dorsal, cocheId!!)
                } else {
                    // Si estamos creando, verificar en todo el torneo
                    carDao.existeDorsalEnTodoElTorneo(torneoId, dorsal)
                }
                
                if (existeDorsal) {
                    withContext(Dispatchers.Main) {
                        binding.layoutDorsal.error = ""
                        binding.tvErrorDorsal.visibility = View.VISIBLE
                        binding.tvErrorDorsal.text = getString(R.string.error_dorsal_already_registered, dorsal)
                        mostrarCargando(false)
                    }
                } else {
                    if (isEditMode && cocheId != null) {
                        // Modo edición: actualizar coche existente
                        val cocheExistente = carDao.obtenerCochePorId(cocheId!!)
                        cocheExistente?.let {
                            val cocheActualizado = it.copy(
                                marca = marca,
                                modelo = modelo,
                                color = color,
                                dorsal = dorsal
                            )
                            carDao.actualizarCoche(cocheActualizado)
                            withContext(Dispatchers.Main) {
                                mostrarCargando(false)
                                mostrarExito(getString(R.string.message_car_updated))
                                finish()
                            }
                        }
                    } else {
                        // Modo creación: insertar nuevo coche
                        val nuevoCar = CocheEntity(
                            torneoId = torneoId,
                            marca = marca,
                            modelo = modelo,
                            color = color,
                            dorsal = dorsal,
                            carreraId = carreraId
                        )
                        carDao.insertarCoche(nuevoCar)

                        withContext(Dispatchers.Main) {
                            mostrarCargando(false)
                            mostrarExito(getString(R.string.message_car_added))
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarCargando(false)
                    mostrarError(getString(R.string.error_database))
                }
            }
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.btnSaveCar.isEnabled = !mostrar
        binding.btnSaveCar.text = if (mostrar) {
            getString(R.string.button_saving)
        } else {
            getString(R.string.button_save_car)
        }
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
        const val EXTRA_CARRERA_ID = "CARRERA_ID"
        const val EXTRA_COCHE_ID = "COCHE_ID"
        const val EXTRA_IS_EDIT_MODE = "IS_EDIT_MODE"
    }
}
