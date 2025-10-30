package com.example.appcarreras.ui.torneo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appcarreras.ui.cars.CarsFragment
import com.example.appcarreras.ui.races.RacesFragment

class ViewPagerAdapter(activity: FragmentActivity, private val torneoId: Long) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        CarsFragment(),
        RacesFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CarsFragment.newInstance(torneoId)
            1 -> RacesFragment.newInstance(torneoId)
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}
