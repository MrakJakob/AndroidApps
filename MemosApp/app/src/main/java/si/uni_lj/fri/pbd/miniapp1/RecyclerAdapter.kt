package si.uni_lj.fri.pbd.miniapp1

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.lang.ClassCastException



class RecyclerAdapter(memos: MutableList<MemoModel>) : RecyclerView.Adapter<RecyclerAdapter.CardViewHolder?>() {
    private var memos: MutableList<MemoModel>? = memos
    private var listener: OnCardClicked? = null
    // private OnClick

    inner class CardViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var itemImage: ImageView? = null
        var itemTitle: TextView? = null
        var itemTimestamp: TextView? = null

        init {
            // TODO: set the above fields, show Snackbar when a user clicks on an item
            itemImage = itemView?.findViewById(R.id.item_image)
            itemTitle = itemView?.findViewById(R.id.item_title)
            itemTimestamp = itemView?.findViewById(R.id.item_timestamp)

            // On memo/card click listener
            itemView?.setOnClickListener {
                listener?.onCardClick(itemView, this.layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CardViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_item_memo_model, viewGroup, false)
        Log.d("logshared", "pride oncreateviewholder")
        try {
            listener = v.context as OnCardClicked
        } catch (e: ClassCastException) {
            throw ClassCastException(v.context.toString() + "must implement OnCardClickListener()")
        }
        return CardViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: CardViewHolder, i: Int) {
        viewHolder.itemTitle?.text = memos?.get(i)?.title
        viewHolder.itemImage?.setImageBitmap(memos?.get(i)?.img)
        viewHolder.itemTimestamp?.text = memos?.get(i)?.timestamp

    }

    override fun getItemCount(): Int {
        if (memos != null) {
            return memos!!.size
        }
        return 0
    }


    interface OnCardClicked {
        fun onCardClick(v: View, position: Int)
    }
}