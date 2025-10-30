package com.example.appcarreras

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

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
