package si.uni_lj.fri.pbd.miniapp3

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SectionsPagerAdapter(fa: FragmentActivity?, private val tabCounter: Int): FragmentStateAdapter(fa!!) {
    override fun getItemCount(): Int {
        return tabCounter
    }

    override fun createFragment(position: Int): Fragment {
        Log.d("SectionsPagerAdapter", "pride $position")

        when(position){
            0 -> return SearchFragment()
            1 -> return FavouritesFragment()
            else -> {
                return SearchFragment()
            }
        }
    }
}