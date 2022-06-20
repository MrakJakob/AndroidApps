package si.uni_lj.fri.pbd.miniapp3.rest

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import si.uni_lj.fri.pbd.miniapp3.models.RecipeDetailsIM
import si.uni_lj.fri.pbd.miniapp3.models.dto.*

interface RestAPI {
    // get all ingredients
    @get:GET("list.php?i=list")
    val allIngredients: Call<IngredientsDTO?>?

    // get filtered cocktail summaries with ingredient = arg from remote database
    @GET("filter.php")
    fun getFiltered(@Query("i") arg: String): Call<RecipesByIngredientDTO>?

    // get cocktail recipe details for a recipe with idDrink = id from remote database
    @GET("lookup.php")
    fun getDetails(@Query("i") id: String): Call<RecipesByIdDTO>?
}