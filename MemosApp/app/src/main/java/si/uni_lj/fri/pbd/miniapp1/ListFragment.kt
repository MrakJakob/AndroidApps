package si.uni_lj.fri.pbd.miniapp1

import android.content.Context

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.ClassCastException



class ListFragment : Fragment() {

    var listener: OnButtonClickedListener? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<*>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        val memoDataHandler = MemoDataHandler(container?.context!!)
        val memos = memoDataHandler.getData()

        recyclerView = view.findViewById(R.id.recycler_view)
        layoutManager = LinearLayoutManager(view.context)
        recyclerView?.layoutManager = layoutManager

        // pass memos to recycler adapter
        adapter = RecyclerAdapter(memos)
        recyclerView?.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle button for adding memos
        val button : FloatingActionButton = view.findViewById(R.id.fab)
        button.setOnClickListener() {
            listener?.onButtonClicked()
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        // make it obligatory for main activity to implement the interface
        try {
            listener = context as OnButtonClickedListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement OnButtonClickedListener()")
        }
    }

    interface OnButtonClickedListener {
        fun onButtonClicked()
    }
}