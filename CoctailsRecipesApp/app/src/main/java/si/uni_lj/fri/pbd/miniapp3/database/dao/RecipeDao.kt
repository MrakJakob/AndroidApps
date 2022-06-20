package si.uni_lj.fri.pbd.miniapp3.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import si.uni_lj.fri.pbd.miniapp3.database.entity.RecipeDetails

@Dao
interface RecipeDao {
    @Query("SELECT * FROM RecipeDetails WHERE idDrink = :idDrink")
    fun getRecipeById(idDrink: String?): RecipeDetails?

    // the missing methods
    // insert given recipe to local database
    @Insert
    fun insertRecipe(recipe: RecipeDetails)

    // get all recipes saved to local database
    @get:Query("SELECT * FROM RecipeDetails")
    val allRecipes: LiveData<List<RecipeDetails>>

    // delete all saved recipes from local database
    @Query("DELETE FROM RecipeDetails")
    fun deleteAllData()

    // delete recipe with idDrink = idDrink from local database
    @Query("DELETE FROM RecipeDetails WHERE idDrink = :idDrink")
    fun deleteRecipe(idDrink: String?)
}