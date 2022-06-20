package si.uni_lj.fri.pbd.miniapp1

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream



class DetailsFragment : Fragment() {
    private var listener: OnDelete? = null


    private lateinit var memoTitle: TextView
    private lateinit var memoDescription: TextView
    private lateinit var memoTimestamp: TextView
    private lateinit var memoImage: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_details, container, false)

        val position = arguments?.getString("position")?.toInt() // Position of current card/memo
        val memoDataHandler = MemoDataHandler(view.context)
        val memos = memoDataHandler.getData()

        // Acquiring all content containers
        memoTitle = view.findViewById(R.id.details_memo_title)
        memoDescription = view.findViewById(R.id.details_memo_description)
        memoTimestamp = view.findViewById(R.id.details_memo_timestamp)
        memoImage = view.findViewById(R.id.details_memo_image)

        // Setting content of content containers
        memoTitle.text = memos[position!!].title
        memoDescription.text = memos[position].description
        memoTimestamp.text = memos[position].timestamp
        memoImage.setImageBitmap(memos[position].img)

        // Handle delete event
        val deleteButton = view.findViewById<Button>(R.id.details_memo_delete)
        deleteButton.setOnClickListener{
            memoDataHandler.deleteMemo(memos[position].id)
            listener?.onDelete()
        }

        // Handle share event
        val shareButton: Button = view.findViewById(R.id.details_memo_share)
        shareButton.setOnClickListener{
          share(memos, position, view)
        }

        return view
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        // make it obligatory for main activity to implement the interface
        try {
            listener = context as OnDelete
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement OnButtonClickedListener()")
        }
    }


    private fun share(memos: MutableList<MemoModel>, position: Int, view: View){
        val bit: Bitmap? = memos[position].img
        val mail = Intent(Intent.ACTION_SEND)
        mail.type = "image/jpeg"

        val bytes = ByteArrayOutputStream()  // opening byte stream
        bit?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(view.context.contentResolver, bit, "Title", null)
        val imageUri = Uri.parse(path)

        // Set mail content
        mail.putExtra(Intent.EXTRA_STREAM, imageUri)
        mail.putExtra(Intent.EXTRA_SUBJECT, memoTitle.text)
        mail.putExtra(Intent.EXTRA_TEXT, "${memoDescription.text}\n\n${memoTimestamp.text}")

        startActivity(Intent.createChooser(mail, "Select"))

        bytes.close()  // closing byte stream
    }

    interface OnDelete {
        fun onDelete()
    }
}