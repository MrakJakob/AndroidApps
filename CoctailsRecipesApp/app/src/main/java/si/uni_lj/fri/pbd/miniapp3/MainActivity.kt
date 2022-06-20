package si.uni_lj.fri.pbd.miniapp3

import android.app.PendingIntent.getActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import si.uni_lj.fri.pbd.miniapp3.databinding.ActivityMainBinding
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientsDTO
import si.uni_lj.fri.pbd.miniapp3.rest.RestAPI
import si.uni_lj.fri.pbd.miniapp3.rest.ServiceGenerator
import timber.log.Timber
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {

    companion object {
        private const val NUM_OF_TABS = 2
    }

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        // val toolbar = binding.toolbar
        // setSupportActionBar(toolbar)

        configureTabLayout()
    }


    private fun configureTabLayout(){
        val tabLayout = binding.tabLayout
        val viewPager2 = binding.viewpager

        val tabAdapter = SectionsPagerAdapter(this, NUM_OF_TABS)

        viewPager2.adapter = tabAdapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when(position){
                0 -> tab.text = getString(R.string.SearchBy)
                1 -> tab.text = getString(R.string.Favourites)
            }
        }.attach()

    }


}