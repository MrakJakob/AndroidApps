package si.uni_lj.fri.pbd.miniapp1
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.ClassCastException



class NewFragment : Fragment() {
    private lateinit var buttonCamera: Button
    private lateinit var image: ImageView
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var listener: OnSaveClicked? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new, container, false)

        // CAMERA
        if(ContextCompat.checkSelfPermission(view.context, Manifest.permission.CAMERA)  // Check for camera permission, if there is none, ask user for it
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.CAMERA), 1)
        }
        image = view?.findViewById(R.id.memo_image)!!
        buttonCamera = view.findViewById(R.id.button_take_photo)!!
        val buttonSave = view.findViewById<Button>(R.id.button_save_memo)

        // Open camera
        buttonCamera.setOnClickListener {
            val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            resultLauncher.launch(camIntent)
        }

        // Handle captured image
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                handleCameraImage(data)
            }
        }

        // SAVE MEMO
        buttonSave?.setOnClickListener{
            onSave(view)
        }

        return view
    }

    // Set memo image to captured image
    private fun handleCameraImage(intent: Intent?){
        val bitmap = intent?.extras?.get("data") as Bitmap
        image.setImageBitmap(bitmap)
    }

    // Handle on save
    private fun onSave(view : View){
        val memoDataHandler = MemoDataHandler(view.context)
        val status = memoDataHandler.saveData(view)
        if (status != 1) {
            return
        }
        listener?.onSaveButtonClicked()
    }

    // Handle interface
    override fun onAttach(context: Context){
        super.onAttach(context)
        // make it obligatory for main activity to implement the interface
        try {
            listener = context as OnSaveClicked
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "must implement OnButtonClickedListener()")
        }
    }


    interface OnSaveClicked {
        fun onSaveButtonClicked()
    }
}