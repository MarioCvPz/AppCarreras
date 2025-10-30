package com.example.appcarreras.ui.racedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.data.entity.CocheEntity
import com.example.appcarreras.databinding.FragmentRaceCarsBinding
import com.example.appcarreras.ui.cars.Car
import com.example.appcarreras.ui.cars.CarAdapter
import com.example.appcarreras.ui.cars.CarStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RaceCarsFragment : Fragment() {

    private var _binding: FragmentRaceCarsBinding? = null
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRaceCarsBinding.inflate(inflater, container, false)

        adapter = CarAdapter(requireContext(), carsList)
        binding.recyclerRaceCars.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRaceCars.adapter = adapter

        cargarCoches()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        cargarCoches()
    }

    private fun cargarCoches() {
        if (torneoId == -1L) return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val coches: List<CocheEntity> = cocheDao.obtenerCochesPorTorneo(torneoId.toInt())
            val mapped = coches.map {
                Car(
                    id = it.idCoche,
                    name = "#${it.dorsal} ${it.marca} ${it.modelo}",
                    team = it.color,
                    status = CarStatus.valueOf(it.status)
                )
            }
            withContext(Dispatchers.Main) {
                carsList.clear()
                carsList.addAll(mapped)
                adapter.filter("")
                val isEmpty = carsList.isEmpty()
                binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.recyclerRaceCars.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TORNEO_ID = "ARG_TORNEO_ID"

        fun newInstance(torneoId: Long): RaceCarsFragment {
            val fragment = RaceCarsFragment()
            fragment.arguments = Bundle().apply {
                putLong(ARG_TORNEO_ID, torneoId)
            }
            return fragment
        }
    }
}