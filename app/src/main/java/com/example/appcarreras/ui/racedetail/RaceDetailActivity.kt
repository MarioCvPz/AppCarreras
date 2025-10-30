package com.example.appcarreras.ui.racedetail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import androidx.core.view.isVisible
import com.example.appcarreras.R
import com.example.appcarreras.databinding.ActivityRaceDetailBinding
import com.example.appcarreras.ui.cars.AddCarActivity
import com.example.appcarreras.ui.incidents.AddIncidentActivity
import com.google.android.material.tabs.TabLayoutMediator

class RaceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRaceDetailBinding

    private var torneoId: Long = -1L
    private var carreraId: Int = -1
    private var raceName: String = ""
    private var raceDate: String = ""
    private var isFabMenuOpen = false

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
            Toast.makeText(this, R.string.message_export_placeholder, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFabMenu() {
        isFabMenuOpen = !isFabMenuOpen
        binding.groupFabOptions.isVisible = isFabMenuOpen
        binding.fabMain.setImageResource(
            if (isFabMenuOpen) R.drawable.ic_close else R.drawable.ic_add
        )
    }

    companion object {
        const val EXTRA_TORNEO_ID = "EXTRA_TORNEO_ID"
        const val EXTRA_RACE_ID = "EXTRA_RACE_ID"
        const val EXTRA_RACE_NAME = "EXTRA_RACE_NAME"
        const val EXTRA_RACE_DATE = "EXTRA_RACE_DATE"
    }
}