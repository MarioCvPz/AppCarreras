package com.example.appcarreras.ui.incidents

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appcarreras.R
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.data.entity.IncidenciaEntity
import com.example.appcarreras.databinding.ActivityAddIncidentBinding
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddIncidentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddIncidentBinding

    private var torneoId: Long = -1L
    private var carreraId: Int = -1
    private var penaltyLaps = 0

    private val db by lazy { DatabaseProvider.getDatabase(this) }
    private val cocheDao by lazy { db.cocheDao() }
    private val incidenciaDao by lazy { db.incidenciaDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddIncidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        torneoId = intent.getLongExtra(EXTRA_TORNEO_ID, -1L)
        carreraId = intent.getIntExtra(EXTRA_CARRERA_ID, -1)

        if (torneoId == -1L || carreraId == -1) {
            Toast.makeText(this, R.string.error_missing_race_information, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupActionDropdown()
        setupPenaltyControls()
        binding.btnSaveIncident.setOnClickListener { guardarIncidencia() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarAddIncident)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddIncident.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupActionDropdown() {
        val actions = resources.getStringArray(R.array.incident_actions)
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, actions)
        adapter.setDropDownViewResource(R.layout.item_dropdown)
        binding.etIncidentAction.setAdapter(adapter)
        binding.etIncidentAction.setDropDownBackgroundResource(R.color.black)
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
        val dorsalText = binding.etCarNumber.text?.toString()?.trim().orEmpty()
        val actionText = binding.etIncidentAction.text?.toString()?.trim().orEmpty()

        var isValid = true
        if (dorsalText.isEmpty()) {
            setError(binding.tilCarNumber, getString(R.string.error_car_required))
            isValid = false
        } else {
            binding.tilCarNumber.error = null
        }

        if (actionText.isEmpty()) {
            setError(binding.tilIncidentAction, getString(R.string.error_action_required))
            isValid = false
        } else {
            binding.tilIncidentAction.error = null
        }

        val dorsal = dorsalText.toIntOrNull()
        if (isValid && dorsal == null) {
            setError(binding.tilCarNumber, getString(R.string.error_car_required))
            isValid = false
        }

        if (!isValid || dorsal == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            val coche = cocheDao.obtenerCochePorDorsal(torneoId, dorsal)
            if (coche == null) {
                withContext(Dispatchers.Main) {
                    setError(binding.tilCarNumber, getString(R.string.error_car_not_found))
                }
                return@launch
            }

            val calendario = Calendar.getInstance()
            val incidencia = IncidenciaEntity(
                torneoId = torneoId.toInt(),
                carreraId = carreraId,
                cocheId = coche.idCoche,
                tipoIncidencia = actionText,
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

    private fun setError(layout: TextInputLayout, message: String) {
        layout.error = message
        layout.editText?.requestFocus()
    }

    companion object {
        const val EXTRA_TORNEO_ID = "EXTRA_TORNEO_ID"
        const val EXTRA_CARRERA_ID = "EXTRA_CARRERA_ID"
    }
}