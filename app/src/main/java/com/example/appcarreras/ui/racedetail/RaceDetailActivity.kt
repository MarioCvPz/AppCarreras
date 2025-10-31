package com.example.appcarreras.ui.racedetail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import androidx.core.view.isVisible
import com.example.appcarreras.R
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.databinding.ActivityRaceDetailBinding
import com.example.appcarreras.ui.cars.AddCarActivity
import com.example.appcarreras.ui.incidents.AddIncidentActivity
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class RaceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRaceDetailBinding

    private var torneoId: Long = -1L
    private var carreraId: Int = -1
    private var raceName: String = ""
    private var raceDate: String = ""
    private var isFabMenuOpen = false

    private val db by lazy { DatabaseProvider.getDatabase(this) }
    private val incidenciaDao by lazy { db.incidenciaDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        torneoId = intent.getLongExtra(EXTRA_TORNEO_ID, -1L)
        carreraId = intent.getIntExtra(EXTRA_RACE_ID, -1)
        raceName = intent.getStringExtra(EXTRA_RACE_NAME) ?: ""
        raceDate = intent.getStringExtra(EXTRA_RACE_DATE) ?: ""

        setupToolbar()
        setupViewPager()
        setupFabMenu()

        onBackPressedDispatcher.addCallback(this) {
            if (isFabMenuOpen) {
                toggleFabMenu()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarRace)
        supportActionBar?.title = if (raceName.isNotEmpty()) raceName else getString(R.string.title_race_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarRace.subtitle = raceDate.takeIf { it.isNotEmpty() }
        binding.toolbarRace.setSubtitleTextColor(Color.WHITE)
        binding.toolbarRace.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViewPager() {
        val adapter = RaceDetailPagerAdapter(this, torneoId, carreraId)
        binding.viewPagerRace.adapter = adapter
        binding.viewPagerRace.offscreenPageLimit = 2

        TabLayoutMediator(binding.tabLayoutRace, binding.viewPagerRace) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_title_race_cars)
                else -> getString(R.string.tab_title_race_incidents)
            }
        }.attach()
    }

    private fun setupFabMenu() {
        binding.fabMain.setOnClickListener { toggleFabMenu() }
        binding.fabAddCar.setOnClickListener {
            toggleFabMenu()
            if (torneoId == -1L) return@setOnClickListener
            val intent = Intent(this, AddCarActivity::class.java).apply {
                putExtra("TORNEO_ID", torneoId)
                if (carreraId != -1) {
                    putExtra(AddCarActivity.EXTRA_CARRERA_ID, carreraId)
                }
            }
            startActivity(intent)
        }
        binding.fabAddIncident.setOnClickListener {
            toggleFabMenu()
            if (torneoId == -1L || carreraId == -1) return@setOnClickListener
            val intent = Intent(this, AddIncidentActivity::class.java).apply {
                putExtra(AddIncidentActivity.EXTRA_TORNEO_ID, torneoId)
                putExtra(AddIncidentActivity.EXTRA_CARRERA_ID, carreraId)
            }
            startActivity(intent)
        }
        binding.fabExportExcel.setOnClickListener {
            toggleFabMenu()
            if (torneoId == -1L || carreraId == -1) return@setOnClickListener
            mostrarDialogoExportar()
        }
    }

    private fun toggleFabMenu() {
        isFabMenuOpen = !isFabMenuOpen
        binding.groupFabOptions.isVisible = isFabMenuOpen
        binding.fabMain.setImageResource(
            if (isFabMenuOpen) R.drawable.ic_close else R.drawable.ic_add
        )
    }

    private fun mostrarDialogoExportar() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_export_title)
            .setMessage(R.string.dialog_export_message)
            .setPositiveButton(R.string.dialog_export_positive) { _, _ ->
                exportarIncidenciasAExcel()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun exportarIncidenciasAExcel() {
        lifecycleScope.launch {
            val incidencias = withContext(Dispatchers.IO) {
                incidenciaDao.obtenerIncidenciasConCoche(torneoId.toInt(), carreraId)
            }

            if (incidencias.isEmpty()) {
                Toast.makeText(this@RaceDetailActivity, R.string.message_no_incidents_to_export, Toast.LENGTH_SHORT).show()
                return@launch
            }

            val resultado = withContext(Dispatchers.IO) {
                runCatching {
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet(getString(R.string.excel_sheet_incidents))

                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue(getString(R.string.excel_header_car_number))
                    headerRow.createCell(1).setCellValue(getString(R.string.excel_header_car_name))
                    headerRow.createCell(2).setCellValue(getString(R.string.excel_header_incident_type))
                    headerRow.createCell(3).setCellValue(getString(R.string.excel_header_time))
                    headerRow.createCell(4).setCellValue(getString(R.string.excel_header_penalty))

                    incidencias.forEachIndexed { index, item ->
                        val row = sheet.createRow(index + 1)
                        row.createCell(0).setCellValue(item.dorsal.toString())
                        val carName = "${item.marca} ${item.modelo}".trim()
                        row.createCell(1).setCellValue(carName)
                        row.createCell(2).setCellValue(item.incidencia.tipoIncidencia)
                        val time = String.format(Locale.getDefault(), "%02d:%02d", item.incidencia.hora, item.incidencia.minuto)
                        row.createCell(3).setCellValue(time)
                        row.createCell(4).setCellValue(item.incidencia.vueltasPenalizacion.toString())
                    }

                    for (i in 0..4) {
                        sheet.autoSizeColumn(i)
                    }

                    val exportDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                    if (!exportDir.exists()) {
                        exportDir.mkdirs()
                    }

                    val sanitizedName = if (raceName.isNotEmpty()) {
                        raceName.replace("[^A-Za-z0-9_]".toRegex(), "_")
                    } else {
                        getString(R.string.excel_default_race_name)
                    }
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "incidencias_${sanitizedName}_$timestamp.xlsx"
                    val file = File(exportDir, fileName)
                    FileOutputStream(file).use { output ->
                        workbook.write(output)
                    }
                    workbook.close()
                    file
                }.getOrNull()
            }

            if (resultado != null) {
                Toast.makeText(
                    this@RaceDetailActivity,
                    getString(R.string.message_export_success, resultado.absolutePath),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this@RaceDetailActivity, R.string.message_export_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_TORNEO_ID = "EXTRA_TORNEO_ID"
        const val EXTRA_RACE_ID = "EXTRA_RACE_ID"
        const val EXTRA_RACE_NAME = "EXTRA_RACE_NAME"
        const val EXTRA_RACE_DATE = "EXTRA_RACE_DATE"
    }
}