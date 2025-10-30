package com.example.appcarreras.ui.racedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.databinding.FragmentRaceIncidentsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RaceIncidentsFragment : Fragment() {

    private var _binding: FragmentRaceIncidentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RaceIncidentAdapter
    private val incidents = mutableListOf<RaceIncident>()

    private var torneoId: Long = -1L
    private var carreraId: Int = -1

    private val db by lazy { DatabaseProvider.getDatabase(requireContext()) }
    private val incidenciaDao by lazy { db.incidenciaDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            torneoId = it.getLong(ARG_TORNEO_ID, -1L)
            carreraId = it.getInt(ARG_CARRERA_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRaceIncidentsBinding.inflate(inflater, container, false)

        adapter = RaceIncidentAdapter(incidents)
        binding.recyclerRaceIncidents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRaceIncidents.adapter = adapter

        cargarIncidencias()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        cargarIncidencias()
    }

    private fun cargarIncidencias() {
        if (torneoId == -1L || carreraId == -1) return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val incidenciasConCoche = incidenciaDao.obtenerIncidenciasConCoche(torneoId.toInt(), carreraId)
            val mapped = incidenciasConCoche.map {
                val time = String.format("%02d:%02d", it.incidencia.hora, it.incidencia.minuto)
                val carName = "${it.marca} ${it.modelo}".trim()
                RaceIncident(
                    id = it.incidencia.idIncidencia,
                    carNumber = it.dorsal,
                    carName = carName,
                    incidentType = it.incidencia.tipoIncidencia,
                    time = time,
                    penaltyLaps = it.incidencia.vueltasPenalizacion,
                )
            }
            withContext(Dispatchers.Main) {
                adapter.updateData(mapped)
                val isEmpty = mapped.isEmpty()
                binding.emptyIncidents.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.recyclerRaceIncidents.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TORNEO_ID = "ARG_TORNEO_ID"
        private const val ARG_CARRERA_ID = "ARG_CARRERA_ID"

        fun newInstance(torneoId: Long, carreraId: Int): RaceIncidentsFragment {
            val fragment = RaceIncidentsFragment()
            fragment.arguments = Bundle().apply {
                putLong(ARG_TORNEO_ID, torneoId)
                putInt(ARG_CARRERA_ID, carreraId)
            }
            return fragment
        }
    }
}