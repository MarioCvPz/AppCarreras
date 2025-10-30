package com.example.appcarreras.ui.torneo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appcarreras.databinding.ActivityTorneoDetailBinding
import com.google.android.material.tabs.TabLayoutMediator

class TorneoDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTorneoDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTorneoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar nombre del torneo desde el intent
        val nombreTorneo = intent.getStringExtra("nombreCampeonato") ?: "Tournament"
        val torneoId = intent.getLongExtra("TORNEO_ID", -1L)

        // Configurar Toolbar
        setSupportActionBar(binding.toolbarTorneo)
        supportActionBar?.title = nombreTorneo
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarTorneo.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar ViewPager con Tabs
        val adapter = ViewPagerAdapter(this, torneoId)
        binding.viewPager.adapter = adapter

        val tabTitles = listOf("Cars", "Races")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

    }
}
