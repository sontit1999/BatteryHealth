package com.procharger.fastprocharrging.quickcharge.ui.chargehistory

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ChartAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager) {
    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

    override fun getCount(): Int {
        return numItems
    }

    override fun getItem(i: Int): Fragment {
        if (i == 0) {
            return fragment1.newInstance(0, "Page # 1")
        }
        if (i == 1) {
            return fragment2.newInstance(0, "Page # 1")
        }
        return fragment1.newInstance(0, "Page # 1")
    }

    companion object {
        private const val numItems = 3
    }

    init {
        fragmentManager.popBackStack(null as String?, 1)
    }
}