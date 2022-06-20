package si.uni_lj.fri.pbd.miniapp3

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import si.uni_lj.fri.pbd.miniapp3.models.SearchViewModel
import si.uni_lj.fri.pbd.miniapp3.models.SearchViewModelFactory
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIngredientDTO
import java.lang.System.load


class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(activity?.application!!)
    }
        private var spinnerAdapter: BaseAdapter? = null
        private lateinit var swipeRefresh: SwipeRefreshLayout
        private var positionItem: Int = -1
        private var adapter: RecyclerViewAdapter? = null
        private lateinit var data: LiveData<RecipesByIngredientDTO?>
        private lateinit var progressBar: MaterialProgressBar


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            viewModel.fetchAllIngredients()
            Log.d("fragment","pride v search")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // Inflate the layout for this fragment
            val view = inflater.inflate(R.layout.fragment_search, container, false)
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSearch)
            val layoutManager = LinearLayoutManager(view.context)
            recyclerView?.layoutManager = layoutManager

            progressBar = view.findViewById(R.id.indeterminate_horizontal_progress)
            // progressBar.clearAnimation()
            // progressBar.visibility = View.VISIBLE

            // Log.d("data1", "${viewModel?.fetchAllIngredients()?.value}")
            val spinnerView = view.findViewById<Spinner>(R.id.spinnerview)
            swipeRefresh = view.findViewById(R.id.swipe_refresh)

            // Check if the user has internet connection
            if (isOnline(requireContext())){
                // await for data from remote database
                viewModel.postModelListLiveData?.observe(viewLifecycleOwner) {
                    // UPDATE UI
                    // initialize SpinnerAdapter and populate it with received data
                    spinnerAdapter = SpinnerAdapter(view.context, viewModel.postModelListLiveData!!)
                    spinnerView.adapter = spinnerAdapter
                }
            } else {
                // if the user doesn't have internet connection display a toast
                Toast.makeText(
                    activity, "No internet connection",
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
            }

            // set listener on spinner
            spinnerView?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                // when user selects a certain item in spinner
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    positionItem = position
                    // Check if the user has internet connection
                    if (isOnline(context!!)){
                        // display loading progress bar
                        progressBar.visibility = View.VISIBLE
                        // request data from remote database
                        viewModel.fetchFilteredRecipes(spinnerAdapter?.getItem(positionItem).toString())
                        // await for data from remote database
                        viewModel.filteredRecipesData?.observe(viewLifecycleOwner) {
                            // UPDATE UI
                            Log.d("spinner", "pride2")
                            data = viewModel.filteredRecipesData!!
                            // Log.d("spinner", "data = ${viewModel?.filteredRecipesData?.value?.recipes!![0].strDrink}")
                            // initialize RecyclerViewAdapter and populate it with received data
                            adapter = RecyclerViewAdapter(activity!!, viewModel.filteredRecipesData!!, "SEARCH", progressBar)
                            recyclerView?.adapter = adapter

                        }
                    } else {
                        Toast.makeText(
                            activity, "No internet connection",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            // set on swipe refresh listener
            swipeRefresh.setOnRefreshListener {
                progressBar.visibility = View.VISIBLE
                adapter?.clear()
                if (positionItem != -1 && isOnline(requireContext())) {
                    // request data from remote database and await them
                    viewModel.fetchFilteredRecipes(spinnerAdapter?.getItem(positionItem).toString())
                    viewModel.filteredRecipesData?.observe(viewLifecycleOwner) {
                        adapter?.addAll(viewModel.filteredRecipesData!!)

                    }
                } else {
                    Toast.makeText(
                        activity, "No internet connection",
                        Toast.LENGTH_LONG
                    ).show()
                }
                swipeRefresh.isRefreshing = false
            }
            return view
        }
    // Function for checking internet connectivity
    // Adapted from: https://stackoverflow.com/questions/51141970/check-internet-connectivity-android-in-kotlin
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}


