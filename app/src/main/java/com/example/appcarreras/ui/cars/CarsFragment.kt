package com.example.appcarreras.ui.cars

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
import com.example.appcarreras.R
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

        adapter = CarAdapter(
            requireContext(),
            carsList,
            onEditClick = { car -> editarCoche(car) },
            onDeleteClick = { car -> eliminarCoche(car) }
        )
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
            // Obtener todos los coches del torneo: los del torneo + los de todas las carreras
            val todosLosCoches: List<CocheEntity> = cocheDao.obtenerTodosLosCochesDelTorneo(torneoId.toInt())

            val lista = todosLosCoches
                .distinctBy { it.idCoche } // Eliminar duplicados por si acaso
                .sortedBy { it.dorsal } // Ordenar por dorsal
                .map {
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

    private fun editarCoche(car: Car) {
        lifecycleScope.launch(Dispatchers.IO) {
            val coche = cocheDao.obtenerCochePorId(car.id)
            withContext(Dispatchers.Main) {
                coche?.let {
                    val intent = Intent(requireContext(), AddCarActivity::class.java)
                    intent.putExtra("TORNEO_ID", torneoId)
                    intent.putExtra(AddCarActivity.EXTRA_COCHE_ID, it.idCoche)
                    intent.putExtra(AddCarActivity.EXTRA_IS_EDIT_MODE, true)
                    if (it.carreraId != null) {
                        intent.putExtra(AddCarActivity.EXTRA_CARRERA_ID, it.carreraId)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun eliminarCoche(car: Car) {
        lifecycleScope.launch(Dispatchers.IO) {
            val coche = cocheDao.obtenerCochePorId(car.id)
            withContext(Dispatchers.Main) {
                coche?.let {
                    val nombreCompleto = "${it.marca} ${it.modelo}"
                    androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
                        .setTitle(R.string.dialog_delete_car_title)
                        .setMessage(getString(R.string.dialog_delete_car_message, it.dorsal, nombreCompleto))
                        .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                cocheDao.eliminarCoche(it)
                                cargarCoches()
                            }
                        }
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                }
            }
        }
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
