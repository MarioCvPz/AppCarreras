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
            Toast.makeText(this, "Error: torneo no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (isEditMode) {
            supportActionBar?.title = getString(R.string.title_edit_car)
            cargarDatosCoche()
            // Ocultar botón de búsqueda en modo edición
            binding.btnSearchExistingCar.visibility = View.GONE
        } else if (carreraId != null) {
            supportActionBar?.title = getString(R.string.title_add_race_car)
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
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val dorsal = dorsalText.toIntOrNull()
        if (dorsal == null) {
            binding.layoutDorsal.error = "Introduce un número válido"
            return
        }

        // Mostrar animación de "Saving..."
        binding.btnSaveCar.isEnabled = false
        binding.btnSaveCar.text = "Saving..."

        lifecycleScope.launch(Dispatchers.IO) {
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
                    binding.tvErrorDorsal.text =
                        "Dorsal $dorsal is already registered in this tournament. Please use a different number."
                    binding.btnSaveCar.isEnabled = true
                    binding.btnSaveCar.text = "Save Car"
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
                            Toast.makeText(
                                this@AddCarActivity,
                                "Car updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
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
                        Toast.makeText(
                            this@AddCarActivity,
                            "Car added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }

        }
    }

    companion object {
        const val EXTRA_CARRERA_ID = "CARRERA_ID"
        const val EXTRA_COCHE_ID = "COCHE_ID"
        const val EXTRA_IS_EDIT_MODE = "IS_EDIT_MODE"
    }
}
