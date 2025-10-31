package com.example.appcarreras.ui.racedetail

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class RaceDetailPagerAdapter(
    activity: FragmentActivity,
    private val torneoId: Long,
    private val carreraId: Int,
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RaceCarsFragment.newInstance(torneoId, carreraId)
            else -> RaceIncidentsFragment.newInstance(torneoId, carreraId)
        }
    }
}