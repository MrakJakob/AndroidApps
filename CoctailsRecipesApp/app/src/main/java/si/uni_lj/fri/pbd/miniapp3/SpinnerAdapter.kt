package si.uni_lj.fri.pbd.miniapp3

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.miniapp3.models.dto.IngredientsDTO

class SpinnerAdapter(context: Context, ingredients: LiveData<IngredientsDTO?>): BaseAdapter() {
    private var context: Context = context
    private var ingredients = ingredients.value?.ingredients
    private var view: View? = null


    override fun getCount(): Int {
        return ingredients!!.size
    }

    override fun getItem(p0: Int): Any? {
        return ingredients!![p0].strIngredient1
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, converterView: View?, parent: ViewGroup?): View {
        val text: TextView
        if (converterView == null){
            view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
            text = view!!.findViewById(R.id.text_view_spinner_item)
            view!!.tag = text
        } else {
            view = converterView
            text = view!!.tag as TextView
        }
        // insert ingredient into spinner item text
        text.text = ingredients!![position].strIngredient1
        return view!!
    }
}