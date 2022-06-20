package si.uni_lj.fri.pbd.miniapp3.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import si.uni_lj.fri.pbd.miniapp3.RecipeRepository
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails

class FavouritesViewModel(application1: Application): ViewModel() {

    // application
    var application: Application? = null

    // recipe repository
    var recipeRepository: RecipeRepository? = null

    // Favourite recipes
    var recipesFavourite: LiveData<List<RecipeDetails>>? = null

    init {
        application = application1
        // Log.d("SearchFragment", "SearchViewModel created!")
        // initialize RecipeRepository
        recipeRepository = RecipeRepository(application!!)
    }

    // get all cocktails recipes that we marked as favourite from local database through RecipeRepository
    fun fetchAllFavourites(): LiveData<List<RecipeDetails>>{
        recipesFavourite = recipeRepository?.allFavourites()
        return recipesFavourite!!
    }

    // Delete all cocktail recipes that were saved locally
    fun deleteAllDataBase() {
        recipeRepository?.deleteDatabase()
    }
}

// initialize FavouritesViewModel with arguments with FavouritesViewModelFactory
class FavouritesViewModelFactory( private var application: Application): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavouritesViewModel(application) as T
    }
}