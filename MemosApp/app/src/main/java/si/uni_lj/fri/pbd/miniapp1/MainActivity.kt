package si.uni_lj.fri.pbd.miniapp1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN



class MainActivity : AppCompatActivity(), ListFragment.OnButtonClickedListener, NewFragment.OnSaveClicked, RecyclerAdapter.OnCardClicked, DetailsFragment.OnDelete {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting default screen
        setContentView(R.layout.activity_main)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = ListFragment()
        fragmentTransaction.add(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }


    // Handling transition from listfragment to newfragment
    override fun onButtonClicked() {
        Log.d("createFragment", "pride hehe")
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = NewFragment()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(TRANSIT_FRAGMENT_OPEN.toString())
        fragmentTransaction.commit()
    }

    // Handling transition when saving memo
    override fun onSaveButtonClicked() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.commit()
        supportFragmentManager.popBackStack()
    }

    // Handling transition from listfragment to detailsfragment when clicking on a certain card/memo
    override fun onCardClick(v: View, position: Int) {
        val bundle = Bundle()
        bundle.putString("position", position.toString())
        val fragment = DetailsFragment()
        fragment.arguments = bundle   // set fragmentclass arguments
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(TRANSIT_FRAGMENT_OPEN.toString())
        fragmentTransaction.commit()
    }

    // Handling on memo deletion event
    override fun onDelete() {
       supportFragmentManager.popBackStack()
    }
}