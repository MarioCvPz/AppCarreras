package com.example.appcarreras

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
        if (torneoId == -1L) {
            Toast.makeText(this, "Error: torneo no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Acción del botón "Save Car"
        binding.btnSaveCar.setOnClickListener {
            guardarCar()
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
            val existe = carDao.existeDorsalEnTorneo(torneoId, dorsal)

            if (existe) {
                withContext(Dispatchers.Main) {
                    binding.layoutDorsal.error = ""
                    binding.tvErrorDorsal.visibility = View.VISIBLE
                    binding.tvErrorDorsal.text =
                        "Dorsal $dorsal is already registered. Please use a different number."
                    binding.btnSaveCar.isEnabled = true
                    binding.btnSaveCar.text = "Save Car"
                }
            } else {
                val nuevoCar = CocheEntity(
                    torneoId = torneoId,
                    marca = marca,
                    modelo = modelo,
                    color = color,
                    dorsal = dorsal
                )
                carDao.insertarCoche(nuevoCar)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddCarActivity, "Car added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
