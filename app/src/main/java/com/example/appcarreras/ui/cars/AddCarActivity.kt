package com.example.appcarreras.ui.cars

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
        } else if (carreraId != null) {
            supportActionBar?.title = getString(R.string.title_add_race_car)
        }

        // Acción del botón "Save Car"
        binding.btnSaveCar.setOnClickListener {
            guardarCar()
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
            // Si estamos editando, verificar que el dorsal no esté en uso por otro coche
            val cocheActual = if (isEditMode && cocheId != null) {
                carDao.obtenerCochePorId(cocheId!!)
            } else {
                null
            }
            
            val existeEnTorneo = carDao.existeDorsalEnTorneo(torneoId, dorsal)
            val carreraActual = carreraId
            val existeEnCarrera = if (carreraActual != null) {
                carDao.existeDorsalEnCarrera(carreraActual, dorsal) || existeEnTorneo
            } else {
                existeEnTorneo
            }
            
            // Si estamos editando y el dorsal es el mismo del coche actual, permitir
            val esMismoCoche = cocheActual?.dorsal == dorsal
            
            if (existeEnCarrera && !esMismoCoche) {

                withContext(Dispatchers.Main) {
                    binding.layoutDorsal.error = ""
                    binding.tvErrorDorsal.visibility = View.VISIBLE
                    binding.tvErrorDorsal.text =
                        "Dorsal $dorsal is already registered. Please use a different number."
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
