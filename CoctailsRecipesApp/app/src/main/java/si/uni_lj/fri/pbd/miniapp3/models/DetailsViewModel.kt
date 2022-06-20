package si.uni_lj.fri.pbd.miniapp3.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import si.uni_lj.fri.pbd.miniapp3.CoreApplication
import si.uni_lj.fri.pbd.miniapp3.RecipeRepository
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipeDetailsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIdDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIngredientDTO

class DetailsViewModel(application: Application): ViewModel() {
    var recipeRepository: RecipeRepository? = null
    private var recipeDetails: LiveData<RecipesByIdDTO?>? = null
    var application: Application? = application

    init {
        // Log.d("SearchFragment", "SearchViewModel created!")
        // initialize recipeRepository
        recipeRepository = RecipeRepository(application)
    }

    // REMOTE DATABASE
    fun fetchRecipeDetails(id: String): LiveData<RecipesByIdDTO?> {
        // get recipe details with idDrink = id from remote database through recipeRepository
        recipeDetails = recipeRepository?.fetchRecipeDetails(id)
        return recipeDetails!!
    }

    // LOCAL DATABASE
    fun addFavouriteRecipe(recipeDetails: RecipeDetailsDTO) {
        // insert recipe into local database through recipeRepository
        recipeRepository?.insertRecipeFavourite(recipeDetails)
    }

    fun getSelectedRecipe(id: String): MutableLiveData<RecipeDetails>? {
        // get recipe details with idDrink = id from local database through recipeRepository
        return recipeRepository?.findRecipe(id)
    }

    fun deleteSelectedRecipe(id: String) {
        // delete recipe with idDrink = id from local database through recipeRepository
        recipeRepository?.deleteFavouriteRecipe(id)
    }


}

// initialize DetailsViewModel with arguments with DetailsViewModelFactory
class DetailsViewModelFactory(private var application: Application): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailsViewModel(application) as T
    }
}