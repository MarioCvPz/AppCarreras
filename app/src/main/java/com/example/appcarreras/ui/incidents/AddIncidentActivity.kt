package com.example.appcarreras.ui.incidents

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appcarreras.R
import com.google.android.material.button.MaterialButtonToggleGroup
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.data.entity.IncidenciaEntity
import com.example.appcarreras.databinding.ActivityAddIncidentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddIncidentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddIncidentBinding

    private var torneoId: Long = -1L
    private var carreraId: Int = -1
    private var incidenciaId: Int? = null
    private var isEditMode = false
    private var penaltyLaps = 0
    private var selectedDorsal: Int? = null
    private var selectedIncidentType: String = ""

    private val db by lazy { DatabaseProvider.getDatabase(this) }
    private val cocheDao by lazy { db.cocheDao() }
    private val incidenciaDao by lazy { db.incidenciaDao() }
    private lateinit var dorsalAdapter: DorsalSelectorAdapter
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddIncidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        torneoId = intent.getLongExtra(EXTRA_TORNEO_ID, -1L)
        carreraId = intent.getIntExtra(EXTRA_CARRERA_ID, -1)
        incidenciaId = if (intent.hasExtra(EXTRA_INCIDENCIA_ID)) {
            intent.getIntExtra(EXTRA_INCIDENCIA_ID, -1).takeIf { it != -1 }
        } else {
            null
        }
        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT_MODE, false)

        if (torneoId == -1L || carreraId == -1) {
            Toast.makeText(this, R.string.error_missing_race_information, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupPenaltyControls()
        setupIncidentTypeSelector()
        
        if (isEditMode) {
            supportActionBar?.title = getString(R.string.title_edit_incident)
        }
        
        setupDorsalSelector()
        
        binding.btnSaveIncident.setOnClickListener { guardarIncidencia() }
    }

    private fun cargarDatosIncidencia() {
        if (incidenciaId == null) return
        lifecycleScope.launch(Dispatchers.IO) {
            val incidencia = incidenciaDao.obtenerIncidenciaPorId(incidenciaId!!)
            withContext(Dispatchers.Main) {
                incidencia?.let {
                    // Obtener el coche para mostrar el dorsal
                    val coche = cocheDao.obtenerCochePorId(it.cocheId)
                    coche?.let { c ->
                        selectedDorsal = c.dorsal
                        dorsalAdapter.setSelectedDorsal(c.dorsal)
                    }
                    selectedIncidentType = it.tipoIncidencia
                    // Seleccionar el toggle button correspondiente
                    val toggleId = when (it.tipoIncidencia) {
                        "Salida de pista" -> binding.toggleSalidaPista.id
                        "Avería" -> binding.toggleAveria.id
                        "Choque" -> binding.toggleChoque.id
                        "Otros" -> binding.toggleOtros.id
                        else -> null
                    }
                    toggleId?.let { id ->
                        binding.toggleGroupIncidentType.check(id)
                    }
                    penaltyLaps = it.vueltasPenalizacion
                    updatePenaltyLabel()
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarAddIncident)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddIncident.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupDorsalSelector() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Solo obtener coches creados específicamente para esta carrera
            val cochesCarrera = cocheDao.obtenerCochesPorCarrera(carreraId)
            
            // Ordenar por dorsal
            val coches = cochesCarrera.sortedBy { it.dorsal }
            
            val dorsales = coches.map { it.dorsal }
            
            withContext(Dispatchers.Main) {
                if (dorsales.isEmpty()) {
                    binding.recyclerDorsales.visibility = View.GONE
                    binding.tvNoCarsError.visibility = View.VISIBLE
                    binding.btnSaveIncident.isEnabled = false
                } else {
                    binding.recyclerDorsales.visibility = View.VISIBLE
                    binding.tvNoCarsError.visibility = View.GONE
                    binding.btnSaveIncident.isEnabled = true
                    
                    dorsalAdapter = DorsalSelectorAdapter(dorsales) { dorsal ->
                        selectedDorsal = dorsal
                    }
                    binding.recyclerDorsales.layoutManager = GridLayoutManager(this@AddIncidentActivity, 4)
                    binding.recyclerDorsales.adapter = dorsalAdapter
                    
                    // Si estamos en modo edición, cargar los datos después de inicializar el adapter
                    if (isEditMode) {
                        cargarDatosIncidencia()
                    }
                }
            }
        }
    }

    private fun setupIncidentTypeSelector() {
        binding.toggleGroupIncidentType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val tipo = when (checkedId) {
                    binding.toggleSalidaPista.id -> "Salida de pista"
                    binding.toggleAveria.id -> "Avería"
                    binding.toggleChoque.id -> "Choque"
                    binding.toggleOtros.id -> "Otros"
                    else -> return@addOnButtonCheckedListener
                }
                seleccionarTipoIncidencia(tipo)
            }
        }
    }
    
    private fun seleccionarTipoIncidencia(tipo: String) {
        selectedIncidentType = tipo
        
        // Asignar valores automáticos según el tipo
        penaltyLaps = when (tipo) {
            "Salida de pista" -> 3
            "Avería" -> 5
            "Choque" -> 2
            "Otros" -> 0
            else -> 0
        }
        updatePenaltyLabel()
    }

    private fun setupPenaltyControls() {
        updatePenaltyLabel()
        binding.btnDecreasePenalty.setOnClickListener {
            if (penaltyLaps > 0) {
                penaltyLaps -= 1
                updatePenaltyLabel()
            }
        }
        binding.btnIncreasePenalty.setOnClickListener {
            penaltyLaps += 1
            updatePenaltyLabel()
        }
    }

    private fun updatePenaltyLabel() {
        binding.tvPenaltyValue.text = penaltyLaps.toString()
    }

    private fun guardarIncidencia() {
        var isValid = true

        if (selectedDorsal == null) {
            binding.tvNoCarsError.visibility = View.VISIBLE
            binding.tvNoCarsError.text = getString(R.string.error_car_required)
            isValid = false
        } else {
            binding.tvNoCarsError.visibility = View.GONE
        }

        if (selectedIncidentType.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_action_required), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) return

        lifecycleScope.launch(Dispatchers.IO) {
            // Buscar el coche en esta carrera específica
            val coche = cocheDao.obtenerCochePorDorsalEnCarrera(carreraId, selectedDorsal!!)
            
            if (coche == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddIncidentActivity, getString(R.string.error_car_not_found), Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            if (isEditMode && incidenciaId != null) {
                // Modo edición: actualizar incidencia existente (mantener hora/minuto original)
                val incidenciaExistente = incidenciaDao.obtenerIncidenciaPorId(incidenciaId!!)
                incidenciaExistente?.let {
                    val incidenciaActualizada = it.copy(
                        cocheId = coche.idCoche,
                        tipoIncidencia = selectedIncidentType,
                        // Mantener la hora y minuto originales al editar
                        vueltasPenalizacion = penaltyLaps
                    )
                    incidenciaDao.actualizarIncidencia(incidenciaActualizada)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddIncidentActivity, "Incidencia actualizada correctamente", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } else {
                // Modo creación: insertar nueva incidencia con hora/minuto actual
                val calendario = Calendar.getInstance()
                val incidencia = IncidenciaEntity(
                    torneoId = torneoId.toInt(),
                    carreraId = carreraId,
                    cocheId = coche.idCoche,
                    tipoIncidencia = selectedIncidentType,
                    hora = calendario.get(Calendar.HOUR_OF_DAY),
                    minuto = calendario.get(Calendar.MINUTE),
                    vueltasPenalizacion = penaltyLaps,
                )

                incidenciaDao.insertarIncidencia(incidencia)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddIncidentActivity, R.string.message_incident_saved, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_TORNEO_ID = "EXTRA_TORNEO_ID"
        const val EXTRA_CARRERA_ID = "EXTRA_CARRERA_ID"
        const val EXTRA_INCIDENCIA_ID = "EXTRA_INCIDENCIA_ID"
        const val EXTRA_IS_EDIT_MODE = "EXTRA_IS_EDIT_MODE"
    }
}