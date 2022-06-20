package si.uni_lj.fri.pbd.miniapp3

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails
import si.uni_lj.fri.pbd.miniapp3.models.*
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipeSummaryDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIngredientDTO


class FavouritesFragment : Fragment() {

    private val viewModel: FavouritesViewModel by viewModels() {
        FavouritesViewModelFactory(activity?.application!!)
    }

    private var favouriteRecipes: LiveData<List<RecipeDetails>>? = null
    private var recipesByIngredientDTO: LiveData<RecipesByIngredientDTO?>? = null

    // adapter
    private var adapter: RecyclerViewAdapter? = null

    // mapper
    val mapper = Mapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("fragments", "pride v favourites")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        // Set recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFavourites)
        val layoutManager = LinearLayoutManager(view.context)
        recyclerView?.layoutManager = layoutManager
        // Thread { viewModel.deleteAllDataBase() }.start()  DELETE ALL DATA


        favouriteRecipes = viewModel.fetchAllFavourites()
        favouriteRecipes!!.observe(viewLifecycleOwner) {
            if (favouriteRecipes?.value!!.isNotEmpty()){
                Log.d("favourites", favouriteRecipes?.value!!.size.toString())

                var listOfRecipeSummary: MutableList<RecipeSummaryDTO> = mutableListOf<RecipeSummaryDTO>()

                var i = 0
                for (recipe in favouriteRecipes?.value!!) {
                    listOfRecipeSummary.add(i, mapper.mapRecipeDetailsToRecipeSummaryDTO(recipe))
                    i++
                }
                recipesByIngredientDTO = transformData(listOfRecipeSummary)
                // Log.d("favourites", recipesByIngredientDTO.value?.recipes!![5].strDrinkThumb)



                adapter = RecyclerViewAdapter(requireActivity(), recipesByIngredientDTO!!, "FAVOURITES", null)
                recyclerView.adapter = adapter
            }
            else {
                Log.d("OnResume", "pride na null")
                recipesByIngredientDTO = null
                adapter?.clear()
            }
        }

        return view

    }

    fun transformData(listOfRecipeSummary: MutableList<RecipeSummaryDTO>): LiveData<RecipesByIngredientDTO?>{
        val data = MutableLiveData<RecipesByIngredientDTO?>()
        data.value = RecipesByIngredientDTO()
        data.value?.recipes = listOfRecipeSummary
        return data
    }
    // we refresh the view when we return from DetailsActivity
    override fun onResume() {
        super.onResume()
        if (adapter != null) {
            adapter?.clear()
            if(recipesByIngredientDTO != null){
                adapter?.addAll(recipesByIngredientDTO!!)
            } else {
               //  Log.d("OnResume", "recipe.value je null")
            }
        } else {
            // Log.d("OnResume", "adapter je null")
        }
    }
}