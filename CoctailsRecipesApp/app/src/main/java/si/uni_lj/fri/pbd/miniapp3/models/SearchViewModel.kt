package si.uni_lj.fri.pbd.miniapp3.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import si.uni_lj.fri.pbd.miniapp3.RecipeRepository
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIngredientDTO


class SearchViewModel(application1: Application): ViewModel() {
    var recipeRepository: RecipeRepository? = null
    var postModelListLiveData: LiveData<IngredientsDTO?>?= null
    var filteredRecipesData: LiveData<RecipesByIngredientDTO?>? = null
    var application: Application? = null


    init {
        application = application1
        // Log.d("SearchFragment", "SearchViewModel created!")
        // initialize RecipeRepository
        recipeRepository = RecipeRepository(application!!)
        // initialize live data
        postModelListLiveData = MutableLiveData()
    }

    // get all ingredients from remote database through RecipeRepository
    fun fetchAllIngredients(): LiveData<IngredientsDTO?>? {
        postModelListLiveData = recipeRepository?.fetchAllIngredients()
        return postModelListLiveData
    }

    // get all cocktails that include selected ingredient from remote database through RecipeRepository
    fun fetchFilteredRecipes(ingredient: String): LiveData<RecipesByIngredientDTO?>? {
        filteredRecipesData = recipeRepository?.fetchFilteredRecipes(ingredient)
        return filteredRecipesData
    }
}

// initialize SearchViewModel with arguments with SearchViewModelFactory
class SearchViewModelFactory( private var application: Application): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(application) as T
    }

}