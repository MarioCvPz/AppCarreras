package com.example.appcarreras.ui.races

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.databinding.FragmentRacesBinding

class RacesFragment : Fragment() {

    private var _binding: FragmentRacesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RaceAdapter
    private val racesList = mutableListOf<Race>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRacesBinding.inflate(inflater, container, false)

        racesList.addAll(
            listOf(
            )
        )

        binding.fabCreateRace.setOnClickListener {
            // Aquí abriremos un diálogo para crear carrera
            Log.d("RacesFragment", "FAB Create Race presionado")
        }


        adapter = RaceAdapter(racesList)
        binding.recyclerRaces.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRaces.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private const val ARG_TORNEO_ID = "torneo_id"

        fun newInstance(torneoId: Long): RacesFragment {
            val fragment = RacesFragment()
            val args = Bundle()
            args.putLong(ARG_TORNEO_ID, torneoId)
            fragment.arguments = args
            return fragment
        }
    }
}
