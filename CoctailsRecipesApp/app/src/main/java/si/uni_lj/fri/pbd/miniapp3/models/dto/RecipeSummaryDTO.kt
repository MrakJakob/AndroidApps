package si.uni_lj.fri.pbd.miniapp3.models.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RecipeSummaryDTO(@field:Expose @field:SerializedName("strDrink") var strDrink: String,
                       @field:Expose @field:SerializedName("strDrinkThumb") val strDrinkThumb: String,
                       @field:Expose @field:SerializedName("idDrink") val idDrink: String)
{
    @SerializedName("strDrink1")
    @Expose
    val strDrink1: String? = null

    @SerializedName("strDrinkThumb1")
    @Expose
    val strDrinkThumb1: String? = null

    @SerializedName("idDrink1")
    @Expose
    val idDrink1: String? = null
}