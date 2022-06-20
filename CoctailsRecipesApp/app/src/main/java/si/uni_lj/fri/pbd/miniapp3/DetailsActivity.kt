package si.uni_lj.fri.pbd.miniapp3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat.postDelayed
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import si.uni_lj.fri.pbd.miniapp3.databinding.ActivityDetailsBinding
import si.uni_lj.fri.pbd.miniapp3.models.DetailsViewModel
import si.uni_lj.fri.pbd.miniapp3.models.DetailsViewModelFactory
import si.uni_lj.fri.pbd.miniapp3.models.Mapper
import si.uni_lj.fri.pbd.miniapp3.models.RecipeDetailsIM
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipeDetailsDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIdDTO
import java.io.ByteArrayOutputStream
import java.net.URL


class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private var mapper = Mapper


    // bitmap image
    var bitmap: String? = null

    // Details view model
    private val viewModel: DetailsViewModel by viewModels() {
        DetailsViewModelFactory(application)
    }

    // Coroutine
    private val processingScope = CoroutineScope(Dispatchers.IO)


    // UI data
    private lateinit var cocktailDetails: LiveData<RecipesByIdDTO?>


    // UI elements
    private lateinit var cocktailImage: ImageView
    private lateinit var cocktailName: TextView
    private lateinit var cocktailIngredients: TextView
    private lateinit var cocktailMeasuresList: TextView
    private lateinit var cocktailInstructions: TextView
    private lateinit var buttonFavourites: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        Log.d("details", "pride v onCreate")

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // init UI elements
        cocktailImage = binding.imageView3
        cocktailName = binding.detailsCoctailName
        cocktailIngredients = binding.detailsCocktailIngredients
        cocktailMeasuresList = binding.detailsMeasuresList
        cocktailInstructions = binding.detailsInstructions
        buttonFavourites = binding.button

        // Log.d("details", "pride v onCreate 2")

        var cocktailID: String? = intent.getStringExtra("Content")

        var startedFrom: String? = intent.getStringExtra("StartedFrom")

        if (startedFrom == "SEARCH") {
            // we access remote server for data
            // Log.d("details", "pride v onCreate 3")
            cocktailDetails = viewModel.fetchRecipeDetails(cocktailID!!)
            cocktailDetails.observe(this) {
                val contentList: RecipeDetailsDTO = cocktailDetails.value?.recipes!![0]

                // Display ingredients
                val displayedIngredients = StringBuilder()
                val ingredients = listOf(contentList.strIngredient1, contentList.strIngredient2, contentList.strIngredient3, contentList.strIngredient5, contentList.strIngredient4, contentList.strIngredient6, contentList.strIngredient7, contentList.strIngredient8, contentList.strIngredient9, contentList.strIngredient10, contentList.strIngredient11,  contentList.strIngredient12,  contentList.strIngredient13, contentList.strIngredient14, contentList.strIngredient15)
                var first = true
                // Display all ingredients
                for(ingredient in ingredients){
                    if (ingredient != null){
                        if (first){
                            displayedIngredients.append(ingredient)
                            first = false
                        } else {
                            displayedIngredients.append(", $ingredient")
                        }

                    }
                }
                cocktailIngredients.text = displayedIngredients.toString()

                // Display measures
                val measures = listOf(contentList.strMeasure1, contentList.strMeasure2, contentList.strMeasure3, contentList.strMeasure5, contentList.strMeasure4, contentList.strMeasure6, contentList.strMeasure7, contentList.strMeasure8, contentList.strMeasure9, contentList.strMeasure10, contentList.strMeasure11, contentList.strMeasure12, contentList.strMeasure13, contentList.strMeasure14, contentList.strMeasure15)
                val displayedMeasures = StringBuilder()
                first = true
                for(measure in measures){
                    if(measure != null){
                        if (first){
                            displayedMeasures.append(measure)
                            first = false
                        } else {
                            displayedMeasures.append(", $measure")
                        }
                    }
                }
                // set the UI elements
                cocktailMeasuresList.text = displayedMeasures.toString()
                // Log.d("ingredients", contentList.idDrink)
                cocktailName.text = contentList.strDrink
                cocktailInstructions.text = contentList.strInstructions
                buttonFavourites.isEnabled = false
                processingScope.launch {

                    doProcessing(contentList.strDrinkThumb, "SEARCH")
                }

                // set onclick listener for button "Favourites"
                buttonFavourites.setOnClickListener()  {
                    val recipe = viewModel.getSelectedRecipe(cocktailID)
                    // get selected recipe from database and await for data
                    recipe?.observe(this) {
                        if (recipe.value == null){
                            Log.d("favourite", "null")
                            if (bitmap != null) {
                                cocktailDetails.value!!.recipes!![0].strImageSource = bitmap!!
                            }
                            // add recipe to local database
                            viewModel.addFavouriteRecipe(cocktailDetails.value!!.recipes!![0])
                            Toast.makeText(
                                this, "Recipe added to favourites",
                                Toast.LENGTH_LONG
                            ).show()
                            buttonFavourites.isEnabled = false

                        } else {
                            Toast.makeText(
                                this, "Recipe already in favourites",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

        } else {
            // we fetch data from local database
            val cocktaildetails = viewModel.getSelectedRecipe(cocktailID!!)
            cocktaildetails!!.observe(this) {
                // transform data from RecipeDetails to RecipeDetailsIM
                val cocktailDetails: RecipeDetailsIM = mapper.mapRecipeDetailsToRecipeDetailsIm(true,
                    cocktaildetails.value!!
                )
                // val ingredients: String = concatIngredients(cocktailDetails)
                // val measures: String = concatMeasures(cocktailDetails)
                buttonFavourites.text = "Remove"
                cocktailName.text = cocktailDetails.strDrink
                cocktailInstructions.text = cocktailDetails.strInstructions
                val ingredients = listOf(cocktailDetails.strIngredient1, cocktailDetails.strIngredient2, cocktailDetails.strIngredient3, cocktailDetails.strIngredient5, cocktailDetails.strIngredient4, cocktailDetails.strIngredient6, cocktailDetails.strIngredient7, cocktailDetails.strIngredient8, cocktailDetails.strIngredient9, cocktailDetails.strIngredient10, cocktailDetails.strIngredient11,  cocktailDetails.strIngredient12,  cocktailDetails.strIngredient13, cocktailDetails.strIngredient14, cocktailDetails.strIngredient15)
                val displayedIngredients = StringBuilder()
                var first = true
                for(ingredient in ingredients){
                     if (ingredient != null){
                         if (first){
                             displayedIngredients.append(ingredient)
                             first = false
                         } else {
                             displayedIngredients.append(", $ingredient")
                         }

                     }
                }
                cocktailIngredients.text = displayedIngredients.toString()

                val measures = listOf(cocktailDetails.strMeasure1, cocktailDetails.strMeasure2, cocktailDetails.strMeasure3, cocktailDetails.strMeasure5, cocktailDetails.strMeasure4, cocktailDetails.strMeasure6, cocktailDetails.strMeasure7, cocktailDetails.strMeasure8, cocktailDetails.strMeasure9, cocktailDetails.strMeasure10, cocktailDetails.strMeasure11, cocktailDetails.strMeasure12, cocktailDetails.strMeasure13, cocktailDetails.strMeasure14, cocktailDetails.strMeasure15)
                val displayedMeasures = StringBuilder()
                first = true
                for(measure in measures){
                    if(measure != null){
                        if (first){
                            displayedMeasures.append(measure)
                            first = false
                        } else {
                            displayedMeasures.append(", $measure")
                        }
                    }
                }
                cocktailMeasuresList.text = displayedMeasures.toString()
                processingScope.launch {
                    doProcessing(cocktailDetails.strImageSource!!, "FAVOURITES")
                }
                buttonFavourites.setOnClickListener() {
                    viewModel.deleteSelectedRecipe(cocktailID)
                    // if user clicks "Remove" button we delete the recipe from local database and switch view to FavouritesFragment
                    val mainHandler = Handler(Looper.getMainLooper()).postDelayed ({
                        runOnUiThread(Runnable {
                            Toast.makeText(
                                this, "Recipe removed from favourites",
                                Toast.LENGTH_LONG
                            ).show()
                            onBackPressed()
                        }) }, 1000)
                }
            }
        }
    }
    // do processing on a separate thread
    fun doProcessing(url: String, origin: String){
        if (origin == "SEARCH") {
            val url = URL(url)
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            this.runOnUiThread(Runnable {
                // Stuff that updates the UI
                binding.imageView3.setImageBitmap(bmp)
            })
            bitmap = BitMapToString(bmp)!!
        } else {
            var img: Bitmap? = StringToBitMap(url)
            this.runOnUiThread(Runnable {
                // Stuff that updates the UI
                binding.imageView3.setImageBitmap(img)
            })
        }
        runOnUiThread(Runnable {
            // Stuff that updates the UI
            buttonFavourites.isEnabled = true
        })

    }

    fun BitMapToString(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun StringToBitMap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }
    }
}



