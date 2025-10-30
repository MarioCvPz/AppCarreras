package com.example.appcarreras

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.data.entity.CocheEntity
import com.example.appcarreras.databinding.FragmentCarsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarsFragment : Fragment() {

    private var _binding: FragmentCarsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CarAdapter
    private val carsList = mutableListOf<Car>()

    private var torneoId: Long = -1L
    private val db by lazy { DatabaseProvider.getDatabase(requireContext()) }
    private val cocheDao by lazy { db.cocheDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        torneoId = arguments?.getLong(ARG_TORNEO_ID) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarsBinding.inflate(inflater, container, false)

        adapter = CarAdapter(requireContext(),carsList)
        binding.recyclerCars.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCars.adapter = adapter

        // 🔥 Cargar coches del torneo
        cargarCoches()


        // FAB -> abrir pantalla de agregar coche
        binding.fabAddCar.setOnClickListener {
            val intent = Intent(requireContext(), AddCarActivity::class.java)
            intent.putExtra("TORNEO_ID", torneoId)
            startActivity(intent)
        }

        // Buscador
        binding.etBuscarCar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun cargarCoches() {
        if (torneoId == -1L) return
        lifecycleScope.launch(Dispatchers.IO) {
            val cochesTorneo: List<CocheEntity> = cocheDao.obtenerCochesPorTorneo(torneoId.toInt())

            val lista = cochesTorneo.map {
                Car(
                    id = it.idCoche,
                    name = "#${it.dorsal} ${it.marca} ${it.modelo}",
                    team = it.color,
                    status = CarStatus.valueOf(it.status)
                )
            }

            withContext(Dispatchers.Main) {
                carsList.clear()
                carsList.addAll(lista)
                adapter.filter("") // Mostrar todos
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarCoches() // 🔁 se recarga al volver del AddCarActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TORNEO_ID = "torneo_id"

        fun newInstance(torneoId: Long): CarsFragment {
            val fragment = CarsFragment()
            val args = Bundle()
            args.putLong(ARG_TORNEO_ID, torneoId)
            fragment.arguments = args
            return fragment
        }
    }
}
