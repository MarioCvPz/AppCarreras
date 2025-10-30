package com.example.appcarreras

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CampeonatoAdapter
    private val listaCampeonatos = mutableListOf<Campeonato>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar RecyclerView
        adapter = CampeonatoAdapter(this, listaCampeonatos)
        binding.recyclerCampeonatos.layoutManager = LinearLayoutManager(this)
        binding.recyclerCampeonatos.adapter = adapter

        // FAB para añadir campeonato
        binding.fabAdd.setOnClickListener { mostrarDialogoNuevoCampeonato() }

        // Cargar algunos ejemplos
        cargarEjemploInicial()
    }

    private fun cargarEjemploInicial() {
        listaCampeonatos.addAll(
            listOf(
            )
        )
        adapter.notifyDataSetChanged()
    }

    private fun mostrarDialogoNuevoCampeonato() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_campeonato, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreCampeonato)

        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setPositiveButton("Agregar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val botonPositivo = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonPositivo.setTextColor(resources.getColor(R.color.orange, null))
            botonPositivo.setOnClickListener {
                val nombre = etNombre.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    val nuevo = Campeonato(nombre, 0)
                    adapter.agregarCampeonato(nuevo)
                    dialog.dismiss()
                } else {
                    etNombre.error = "El nombre no puede estar vacío"
                }
            }

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.white, null))
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_campeonatos, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Buscar torneo..."
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText.orEmpty())
                return true
            }
        })

        return true
    }
}
