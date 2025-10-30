package com.example.appcarreras.ui.races

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appcarreras.R
import com.example.appcarreras.data.database.DatabaseProvider
import com.example.appcarreras.data.entity.CarreraEntity
import com.example.appcarreras.databinding.FragmentRacesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class RacesFragment : Fragment() {

    private var _binding: FragmentRacesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RaceAdapter
    private val racesList = mutableListOf<Race>()

    private var torneoId: Long = -1L
    private val db by lazy { DatabaseProvider.getDatabase(requireContext()) }
    private val carreraDao by lazy { db.carreraDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        torneoId = arguments?.getLong(ARG_TORNEO_ID) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRacesBinding.inflate(inflater, container, false)

        adapter = RaceAdapter(racesList)
        binding.recyclerRaces.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRaces.adapter = adapter

        binding.fabCreateRace.setOnClickListener { mostrarDialogoNuevaCarrera() }

        cargarCarreras()

        return binding.root
    }

    private fun cargarCarreras() {
        if (torneoId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val carreras = carreraDao.obtenerCarrerasPorTorneo(torneoId.toInt())
            val races = carreras.map { carrera ->
                Race(
                    id = carrera.idCarrera,
                    name = carrera.nombreCarrera,
                    date = carrera.fechaCarrera,
                )
            }

            withContext(Dispatchers.Main) {
                racesList.clear()
                racesList.addAll(races)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun mostrarDialogoNuevaCarrera() {
        if (torneoId == -1L) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_carrera, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreCarrera)
        val etFecha = dialogView.findViewById<EditText>(R.id.etFechaCarrera)

        val mostrarSelectorFecha = {
            val calendario = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val fechaFormateada =
                        String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    etFecha.setText(fechaFormateada)
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH),
            ).show()
        }

        etFecha.setOnClickListener { mostrarSelectorFecha() }
        etFecha.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) mostrarSelectorFecha() }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(dialogView)
            .setPositiveButton("Agregar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val botonPositivo = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonPositivo.setTextColor(resources.getColor(R.color.orange, null))
            botonPositivo.setOnClickListener {
                val nombre = etNombre.text.toString().trim()
                val fecha = etFecha.text.toString().trim()

                var esValido = true
                if (nombre.isEmpty()) {
                    etNombre.error = getString(R.string.error_nombre_vacio)
                    esValido = false
                }
                if (fecha.isEmpty()) {
                    etFecha.error = getString(R.string.error_fecha_vacia)
                    esValido = false
                }

                if (!esValido) return@setOnClickListener

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    carreraDao.insertarCarrera(
                        CarreraEntity(
                            torneoId = torneoId.toInt(),
                            nombreCarrera = nombre,
                            fechaCarrera = fecha,
                        ),
                    )

                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        cargarCarreras()
                    }
                }
            }

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.white, null))
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        cargarCarreras()
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
