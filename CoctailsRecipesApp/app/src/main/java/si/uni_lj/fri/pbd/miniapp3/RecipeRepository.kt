package si.uni_lj.fri.pbd.miniapp3

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import si.uni_lj.fri.pbd.miniapp3.database.Database
import si.uni_lj.fri.pbd.miniapp3.database.dao.RecipeDao
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails
import si.uni_lj.fri.pbd.miniapp3.models.Mapper
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipeDetailsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIdDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIngredientDTO
import si.uni_lj.fri.pbd.miniapp3.rest.RestAPI
import si.uni_lj.fri.pbd.miniapp3.rest.ServiceGenerator
import timber.log.Timber
import java.lang.StringBuilder

class RecipeRepository(application: Application) {
    // val ingredients: LiveData<IngredientsDTO?>?
    private val recipeDao: RecipeDao?
    private var recipesFavourite: LiveData<List<RecipeDetails>>? = null
    val selectedRecipeResults = MutableLiveData<RecipeDetails>()
    val mapper = Mapper

    init {
        // Log.d("database", application.toString())
        // initialize local database
        val db: Database = application?.let {
            Database.getDatabase(it.applicationContext)!!

        }
        recipeDao = db.recipeDao()
    }


    // LOCAL DATABASE
    // get all recipes saved to local database
    fun allFavourites(): LiveData<List<RecipeDetails>> {
        recipesFavourite = recipeDao?.allRecipes
        return recipesFavourite!!
    }

    // delete all data from local database
    fun deleteDatabase(){
        recipeDao!!.deleteAllData()
    }

    // insert recipe = newRecipeDTO to local database
    fun insertRecipeFavourite(newRecipeDTO: RecipeDetailsDTO) {
        var newRecipe: RecipeDetails = mapper.mapRecipeDetailsDtoToRecipeDetails(true, newRecipeDTO)
        Database.databaseWriteExecutor.execute(Runnable {
            recipeDao?.insertRecipe(newRecipe)
        })
    }

    // delete recipe with idDrink = id from local database
    fun deleteFavouriteRecipe(id: String) {
       Database.databaseWriteExecutor.execute(Runnable {
          recipeDao?.deleteRecipe(id)
       })
    }

    // get recipe with idDrink = id from local database
    fun findRecipe(id: String): MutableLiveData<RecipeDetails> {
        Database.databaseWriteExecutor.execute(Runnable {
            selectedRecipeResults.postValue(recipeDao?.getRecipeById(id))
        })
        return selectedRecipeResults
    }


    // REMOTE DATABASE
    // get all ingredients from remote database
    fun fetchAllIngredients(): LiveData<IngredientsDTO?> {
        val serviceGenerator = ServiceGenerator
        // create restAPI service
        val restAPI = serviceGenerator.createService(RestAPI::class.java)
        // call restAPI for data
        val mcall: Call<IngredientsDTO?>? = restAPI.allIngredients
        // initialize live data
        val data = MutableLiveData<IngredientsDTO?>()

        // await for data from remote database
        mcall?.enqueue(object: Callback<IngredientsDTO?> {
            override fun onResponse(
                call: Call<IngredientsDTO?>,
                response: Response<IngredientsDTO?>
            ) {
                val res = response.body()
                if(response.code() == 200 && res != null) {
                    // if everything went ok
                    data.value = res
                } else {
                    data.value = null
                }
            }
            override fun onFailure(call: Call<IngredientsDTO?>, t: Throwable) {
                Timber.e(t.message.toString())
            }
        })
        return data
    }


    // get filtered recipes that include ingredient = ingredient from remote database
    fun fetchFilteredRecipes(ingredient: String): LiveData<RecipesByIngredientDTO?> {
        val serviceGenerator = ServiceGenerator
        // create restAPI service
        val restAPI = serviceGenerator.createService(RestAPI::class.java)
        // call restAPI for data
        val mcall: Call<RecipesByIngredientDTO> = restAPI.getFiltered(ingredient)!!
        val data = MutableLiveData<RecipesByIngredientDTO?>()

        // await for data from remote database
        mcall.enqueue(object: Callback<RecipesByIngredientDTO?> {
            override fun onResponse(
                call: Call<RecipesByIngredientDTO?>,
                response: Response<RecipesByIngredientDTO?>
            ) {
                val res = response.body()
                if(response.code() == 200 && res != null) {
                    // if everything went ok
                    data.value = res
                    // Log.d("data1", "data repository: ${data.value}")
                } else {
                    data.value = null
                    // Log.d("data1", "data repository null: ${data.value}")
                }
                // Log.d("api","$data")
            }

            override fun onFailure(call: Call<RecipesByIngredientDTO?>, t: Throwable) {
                Timber.e(t.message.toString())
            }
        })
        // Log.d("dataRepo", "${data.value}")
        return data
    }

    // get recipe details for recipe with idDrink = id from remote database
    fun fetchRecipeDetails(id: String): LiveData<RecipesByIdDTO?> {
        val serviceGenerator = ServiceGenerator
        // create restAPI service
        val restAPI = serviceGenerator.createService(RestAPI::class.java)
        // Log.d("id", id)
        // call restAPI for data
        val mcall: Call<RecipesByIdDTO>? = restAPI.getDetails(id)
        val data = MutableLiveData<RecipesByIdDTO?>()

        // await for data from remote database
        mcall!!.enqueue(object: Callback<RecipesByIdDTO> {
            override fun onResponse(
                call: Call<RecipesByIdDTO>,
                response: Response<RecipesByIdDTO>
            ) {
                val res = response.body()

                if(response.code() == 200 && res != null) {
                    // OK
                    data.value = res
                    // Log.d("data1", "data recipe: ${data.value}")
                } else {

                    data.value = null
                    // Log.d("data1", "data repository null: ${data.value}")
                }
                // Log.d("details","${data.value?.recipes!![0].idDrink}")
            }

            override fun onFailure(call: Call<RecipesByIdDTO>, t: Throwable) {
                Timber.e(t.message.toString())
            }
        })
        // Log.d("dataRepo", "${data.value}")
        return data
    }
}