package si.uni_lj.fri.pbd.miniapp3

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipeSummaryDTO
import si.uni_lj.fri.pbd.miniapp3.models.dto.RecipesByIngredientDTO
import java.net.URL


class RecyclerViewAdapter(
    private val activity: FragmentActivity, recipes: LiveData<RecipesByIngredientDTO?>,
    private val origin: String, private val progressBar: MaterialProgressBar?
): RecyclerView.Adapter<RecyclerViewAdapter.CardViewHolder?>() {

    private var recipes: List<RecipeSummaryDTO>? = recipes.value?.recipes

    // Coroutine
    private val processingScope = CoroutineScope(Dispatchers.IO)

    private lateinit var context: Context

    inner class CardViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
        var itemImage: ImageView? = null
        var itemContent: TextView? = null

        init {
            itemImage = itemView?.findViewById(R.id.image_view)
            itemContent = itemView?.findViewById(R.id.text_view_content)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_grid_item, parent, false)
        context = v.context
        return CardViewHolder(v)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        // we need async loading of cards content, because we need to load the image from the URL
        processingScope.launch {
            doProcessing(holder, position)
        }

        // set onclick listener on each recipe's image
        holder.itemImage?.setOnClickListener() {
            onCardClicked(position)
        }


    }
    fun doProcessing(holder: CardViewHolder, position: Int){
            // different actions depending on origin (origin is either SearchFragment or FavouritesFragment)
            if (origin == "SEARCH") {
                val url = URL(recipes!![position].strDrinkThumb)
                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                // UI must be updated on UiThread
                activity.runOnUiThread(Runnable {
                    // set cocktail recipe's image and name
                    holder.itemImage?.setImageBitmap(bmp)
                    holder.itemContent?.text = recipes!![position].strDrink
                    // stop displaying the progres bar
                    if(progressBar != null) {
                        progressBar.visibility = View.GONE
                    }
                })
            } else {
                if (!recipes!!.isNullOrEmpty()){
                    var img: Bitmap? = StringToBitMap(recipes!![position].strDrinkThumb)
                    activity.runOnUiThread(Runnable {
                        // Stuff that updates the UI
                        if (recipes!!.isNotEmpty()) {
                            holder.itemImage?.setImageBitmap(img)
                            holder.itemContent?.text = recipes!![position].strDrink
                        }
                        if(progressBar != null) {
                            progressBar.visibility = View.GONE
                        }
                    })
                }
            }



    }
    // transform string to bitmap
    fun StringToBitMap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }
    }

    override fun getItemCount(): Int {
        return recipes!!.size
    }

    // when card's image is clicked, launch DetailsActivity
    fun onCardClicked(position: Int) {
        val i = Intent(context, DetailsActivity::class.java)
        // Add recipe's id and origin to intent (origin is either SearchFragment or FavouritesFragment)
        i.putExtra("Content", recipes!![position].idDrink)
        i.putExtra("StartedFrom", origin)
        // Log.d("details", "pride on card clicked")
        context.startActivity(i)
    }

    // Clean all elements of the recycler
    fun clear() {
        recipes = emptyList()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(list: LiveData<RecipesByIngredientDTO?>) {
        recipes = list?.value?.recipes
        notifyDataSetChanged()
    }


}