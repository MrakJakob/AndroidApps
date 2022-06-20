package si.uni_lj.fri.pbd.miniapp1

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*



class MemoDataHandler(private val context: Context) {

    private val PREFS_FILE = "localpref"
    private var timeStampID: Int = 0
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)!!

    /* ------------- SAVING ---------------*/
    fun saveData(view: View) : Int {
        val dateformat = SimpleDateFormat("HH:mm:ss MM/dd/yyyy",
            Locale.US)
        val temp = Date()


        val timeStamp = dateformat.format(temp)

        val memo_title = view.findViewById<EditText>(R.id.edit_memo_title)
        val memo_description = view.findViewById<EditText>(R.id.edit_memo_description)
        val memo_img = view.findViewById<ImageView>(R.id.memo_image)
        if(memo_title.text.isEmpty() || memo_description.text.isEmpty()){
            Toast.makeText(view.context,
                "Empty field not allowed!",
                Toast.LENGTH_SHORT).show()
            return 0
        }

        // TODO : Treba preverit a je uporabnik sploh naredu sliko
        val bitmap: Bitmap = memo_img.drawable.toBitmap()

        val memo = MemoModel(memo_title.text.toString(), memo_description.text.toString(), timeStamp.toString(), bitmap, timeStampID)
        val memoImg = getStringFromBitmap(memo.img!!)
        val memo_json = JSONObject()
        try {
            memo_json.put("id", memo.id)
            memo_json.put("title", memo.title)
            memo_json.put("description", memo.description)
            memo_json.put("timestamp", memo.timestamp)
            memo_json.put("image", memoImg)
        } catch (e:JSONException){
            e.printStackTrace()
        }
        Log.d("onSave", "Pride + $memo")
        storeJsonToPreferences(memo_json)
        return 1
    }
    private fun getStringFromBitmap(bitmap: Bitmap) : String{
        val compressQuality = 100
        val encodedImage : String
        val byteArrayBitmapStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, compressQuality, byteArrayBitmapStream)
        val arr : ByteArray = byteArrayBitmapStream.toByteArray()
        encodedImage = Base64.encodeToString(arr, Base64.DEFAULT)
        return encodedImage
    }
    // store JSON object to shared preferences
    private fun storeJsonToPreferences(obj: JSONObject){
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("localpref", Context.MODE_PRIVATE)!!  // MYBE NAROBE context
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        try {
            // Log.d("response", sharedPreferences.all.size.toString())
            editor.putString(timeStampID.toString(), obj.toString()).apply()
        } catch (e: JSONException){
            e.printStackTrace()
        }
    }
    /* ------------------------------------------------*/

    /* -------------GETTING DATA ---------------*/
    // get all data from shared preferences
    fun getData() : MutableList<MemoModel> {
        // sharedPreferences.edit().clear().commit()  //  DELETE ALL
        val allEntries = sharedPreferences.all
        val memos = List(allEntries.size) { MemoModel("","","", null, 0) }.toMutableList()
        // Log.d("memos", "memos created")
        var index = memos.size

        for ((key, value1) in allEntries) {
            val value = value1!!
            // Log.d("map values", "$key: $value")
            val strJSON = value.toString()
            try {
                val response = JSONObject(strJSON)
                val title = response.get("title")
                val description = response.get("description")
                val timestamp = response.get("timestamp")
                val imgBitmap = getBitmapFromString(response.get("image") as String)
                val id = response.get("id") as Int
                memos[index - 1].title = title as String
                memos[index - 1].description = description as String
                memos[index - 1].timestamp = timestamp as String
                memos[index - 1].img = imgBitmap
                memos[index - 1].id = id
                index -= 1

            } catch (e: JSONException) {
            }
        }
        return memos
    }
    fun deleteMemo(position: Int){
        sharedPreferences.edit().remove(position.toString()).apply()
    }

    /*
    * This Function converts the String back to Bitmap
    */
    private fun getBitmapFromString(stringImg : String) : Bitmap {
        val decodedString : ByteArray = Base64.decode(stringImg, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

}