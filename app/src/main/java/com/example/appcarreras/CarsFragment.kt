package com.example.appcarreras

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.databinding.FragmentCarsBinding

class CarsFragment : Fragment() {

    private var _binding: FragmentCarsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CarAdapter
    private val carsList = mutableListOf<Car>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarsBinding.inflate(inflater, container, false)

        // Datos de ejemplo
        carsList.addAll(
            listOf(
            )
        )

        adapter = CarAdapter(carsList)
        binding.recyclerCars.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCars.adapter = adapter

        binding.fabAddCar.setOnClickListener {
            // Aquí abriremos un diálogo para agregar coche
            // (por ahora solo un log)
            Log.d("CarsFragment", "FAB Add Car presionado")
        }


        // Filtro de búsqueda
        binding.etBuscarCar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
