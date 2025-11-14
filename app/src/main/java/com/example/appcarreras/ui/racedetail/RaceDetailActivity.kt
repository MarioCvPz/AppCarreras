package com.example.appcarreras.ui.racedetail

import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.example.appcarreras.R
import com.example.appcarreras.data.dao.IncidenciaDao
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.databinding.ActivityRaceDetailBinding
import com.example.appcarreras.ui.cars.AddCarActivity
import com.example.appcarreras.ui.incidents.AddIncidentActivity
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class RaceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRaceDetailBinding

    private var torneoId: Long = -1L
    private var carreraId: Int = -1
    private var raceName: String = ""
    private var raceDate: String = ""
    private var isFabMenuOpen = false

    private val db by lazy { DatabaseProvider.getDatabase(this) }
    private val incidenciaDao by lazy { db.incidenciaDao() }

    private val sharedPrefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    private val openDirectoryLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let { handleDirectorySelected(it) }
    }


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
                0 -> getString(R.string.tab_title_race_incidents)
                else -> getString(R.string.tab_title_race_cars)
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
        // El botón de agregar incidencia ahora está en el fragment
        binding.fabAddIncident.visibility = View.GONE
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
        val currentFolder = getExportDirectoryLabel()
        val exportDirUri = getSelectedExportDirectoryUri()
        
        if (exportDirUri != null && currentFolder != null) {
            // Si ya hay una carpeta seleccionada, exportar directamente
            exportarIncidenciasAExcel()
        } else {
            // Si no hay carpeta, abrir selector de directorio
            abrirSelectorDirectorio()
        }
    }

    private fun exportarIncidenciasAExcel() {
        val exportDirUri = getSelectedExportDirectoryUri()
        if (exportDirUri == null) {
            Toast.makeText(this, R.string.message_export_no_directory, Toast.LENGTH_SHORT).show()
            return
        }
        val exportDirectoryLabel = getExportDirectoryLabel(exportDirUri)
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
                    val csvContent = buildCsvContent(incidencias)
                    val sanitizedName = if (raceName.isNotEmpty()) {
                        raceName.replace("[^A-Za-z0-9_]".toRegex(), "_")
                    } else {
                        getString(R.string.excel_default_race_name)
                    }
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "incidencias_${sanitizedName}_$timestamp.csv"
                    val directory = DocumentFile.fromTreeUri(this@RaceDetailActivity, exportDirUri)
                        ?: throw IllegalStateException("Invalid directory")

                    directory.findFile(fileName)?.delete()

                    val file = directory.createFile("text/csv", fileName)
                        ?: throw IllegalStateException("Unable to create file")

                    contentResolver.openOutputStream(file.uri)?.bufferedWriter()?.use { writer ->
                        writer.write(csvContent)
                    } ?: throw IllegalStateException("Unable to open output stream")

                    val previewPath = createPreviewCsv(csvContent, fileName)

                    ExportResult(file.name ?: fileName, previewPath)
                }.getOrNull()
            }

            if (resultado != null) {
                val displayPath = if (exportDirectoryLabel.isNotEmpty()) {
                    "$exportDirectoryLabel/${resultado.fileDisplayName}"
                } else {
                    resultado.fileDisplayName
                }
                Toast.makeText(
                    this@RaceDetailActivity,
                    getString(R.string.message_export_success, displayPath),
                    Toast.LENGTH_LONG
                ).show()
                resultado.previewPath?.let { preview ->
                    Log.d(TAG, "Preview CSV saved at $preview")
                    Toast.makeText(
                        this@RaceDetailActivity,
                        getString(R.string.message_export_preview_location, preview),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this@RaceDetailActivity, R.string.message_export_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirSelectorDirectorio() {
        openDirectoryLauncher.launch(getSelectedExportDirectoryUri())
    }

    private fun handleDirectorySelected(uri: Uri) {
        try {
            val previousUriString = sharedPrefs.getString(KEY_EXPORT_TREE_URI, null)
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            val previousUri = previousUriString?.let(Uri::parse)
            if (previousUri != null && previousUri != uri) {
                releasePersistedPermission(previousUri)
            }
            sharedPrefs.edit().putString(KEY_EXPORT_TREE_URI, uri.toString()).apply()
            
            // Exportar automáticamente después de seleccionar la carpeta
            exportarIncidenciasAExcel()
        } catch (securityException: SecurityException) {
            Toast.makeText(this, R.string.message_export_directory_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun releasePersistedPermission(uri: Uri) {
        contentResolver.persistedUriPermissions.firstOrNull { it.uri == uri }?.let { permission ->
            var flags = 0
            if (permission.isReadPermission) flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
            if (permission.isWritePermission) flags = flags or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.releasePersistableUriPermission(permission.uri, flags)
        }
    }

    private fun getSelectedExportDirectoryUri(): Uri? {
        val uriString = sharedPrefs.getString(KEY_EXPORT_TREE_URI, null) ?: return null
        val uri = Uri.parse(uriString)
        val hasPermission = contentResolver.persistedUriPermissions.any { persisted ->
            persisted.uri == uri && persisted.isWritePermission
        }
        return if (hasPermission) {
            uri
        } else {
            sharedPrefs.edit().remove(KEY_EXPORT_TREE_URI).apply()
            null
        }
    }

    private fun getExportDirectoryLabel(): String? {
        val uri = getSelectedExportDirectoryUri() ?: return null
        return getExportDirectoryLabel(uri)
    }

    private fun getExportDirectoryLabel(uri: Uri): String {
        val document = DocumentFile.fromTreeUri(this, uri)
        val documentName = document?.name
        if (!documentName.isNullOrEmpty()) {
            return documentName
        }
        val lastSegment = uri.lastPathSegment
        return if (!lastSegment.isNullOrEmpty()) {
            lastSegment
        } else {
            uri.toString()
        }
    }

    private fun buildCsvContent(incidencias: List<IncidenciaDao.IncidenciaConCoche>): String {
        val headers = listOf(
            getString(R.string.excel_header_car_number),
            getString(R.string.excel_header_car_name),
            getString(R.string.excel_header_incident_type),
            getString(R.string.excel_header_time),
            getString(R.string.excel_header_penalty),
        )
        return buildString {
            appendLine(headers.joinToString(separator = ",") { escapeCsv(it) })
            incidencias.forEach { item ->
                val carName = "${item.marca} ${item.modelo}".trim()
                val time = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    item.incidencia.hora,
                    item.incidencia.minuto,
                )
                val values = listOf(
                    item.dorsal.toString(),
                    carName,
                    item.incidencia.tipoIncidencia,
                    time,
                    item.incidencia.vueltasPenalizacion.toString(),
                )
                appendLine(values.joinToString(separator = ",") { escapeCsv(it) })
            }
        }
    }

    private fun createPreviewCsv(csvContent: String, fileName: String): String? {
        return runCatching {
            val prefix = fileName.substringBefore('.')
                .takeIf { it.length >= 3 }
                ?: "csv_preview"
            val tempFile = File.createTempFile(prefix, ".csv", cacheDir)
            tempFile.writeText(csvContent, Charsets.UTF_8)
            tempFile.absolutePath
        }.getOrNull()
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private data class ExportResult(val fileDisplayName: String, val previewPath: String?)

    companion object {
        const val EXTRA_TORNEO_ID = "EXTRA_TORNEO_ID"
        const val EXTRA_RACE_ID = "EXTRA_RACE_ID"
        const val EXTRA_RACE_NAME = "EXTRA_RACE_NAME"
        const val EXTRA_RACE_DATE = "EXTRA_RACE_DATE"
        private const val PREFS_NAME = "race_detail_prefs"
        private const val KEY_EXPORT_TREE_URI = "key_export_tree_uri"
        private const val TAG = "RaceDetailActivity"
    }
}

