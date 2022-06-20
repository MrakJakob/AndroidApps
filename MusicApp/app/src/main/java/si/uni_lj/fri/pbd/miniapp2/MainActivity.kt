package si.uni_lj.fri.pbd.miniapp2

import android.content.*
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import si.uni_lj.fri.pbd.miniapp2.MediaPlayerService.Companion.ACTION_START
import si.uni_lj.fri.pbd.miniapp2.databinding.ActivityMainBinding
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity::class.simpleName

        // Message type for the handler
        private const val MSG_UPDATE_TIME = 1
        private const val UPDATE_RATE_MS = 1000L

        // Broadcast receiver variable
        val ACTION_EXIT_APP: String = "exit"
        val ACTION_RESUME_SONG: String = "pause"
    }

    // Buttons
    private lateinit var binding: ActivityMainBinding
    private lateinit var buttonPlay: Button
    private lateinit var buttonPause: Button
    private lateinit var buttonForward: Button
    private lateinit var buttonBackwards: Button
    private lateinit var buttonGesture: Button
    private lateinit var buttonStop: Button
    private lateinit var buttonExit: Button


    // Text view
    private  lateinit var songName: TextView
    private lateinit var currTime: TextView
    private lateinit var totalTime: TextView

    // progress bar
    private lateinit var progressBar: SeekBar

    // song index
    private var index by Delegates.notNull<Int>()

    private var gesture = false

    // Service
    private var serviceBound = false
    private var mediaService: MediaPlayerService? = null
    // private var accelerationService: AccelerationService? = null

    // Coroutine
    private val processingScope = CoroutineScope(Dispatchers.IO)

    // TotalSongDuration
    private var totalSongDuration: Int = 0

    // Context
    private val context:Context = this

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.d(TAG, "Service bound")
            val binderMedia = iBinder as MediaPlayerService.RunServiceBinder
            mediaService = binderMedia.service
            processingScope.launch { doProcessing() }
            serviceBound = true
            mediaService?.background()

            setSong(mediaService?.getSong()!!)
            // Update the UI if the service is already running the timer
            // Log.d("onRestart", "pride")
            if (mediaService?.isSongPlaying == true) {
                Log.d("onRestart", "pride2")
                updateUIStartRun()
                // buttonPlay.setBackgroundResource(R.drawable.iconpausewhite)  // song is playing so we have to change the button to pause
            }
            // Log.d("broadcast", "register receiver")
            var filter = IntentFilter()
            filter.addAction(ACTION_RESUME_SONG)
            filter.addAction(ACTION_EXIT_APP)
            LocalBroadcastManager.getInstance(context)
                .registerReceiver(mReceiver!!, filter)

            // Log.d("broadcast", "registered receiver = $mReceiver")
            // Log.d("onResume", "pride v onResume update")
            val currentPosition = mediaService?.mediaPlayer!!.currentPosition
            progressBar.progress = currentPosition
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(TAG, "Service disconnect")
            serviceBound = false
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // init buttons
        buttonPlay = binding.play
        buttonPause = binding.pause
        buttonForward = binding.forward
        buttonBackwards = binding.backwards
        buttonGesture = binding.gesture
        buttonStop = binding.stop
        buttonExit = binding.exit

        // init text views
        songName = binding.textView
        currTime = binding.currentState
        totalTime = binding.songLength

        // progress bar
        progressBar = binding.progressbar

        // get random song index
        if(mediaService?.songs != null){
            index = (0 until mediaService?.songs!!.size).random()
            setSong(index)
        }else {
            Log.d("songName", "${mediaService?.songs}")
        }

        // LISTENERS
        buttonPlay.setOnClickListener(){
            if (serviceBound && mediaService?.isSongPlaying!!){
                // play random song if the song is playing
                playRandom()
                // updateUIStopRun()
            }
            else {
                // else resume song
                resume()
                // and start updating UI
                updateUIStartRun()
            }
        }

        buttonPause.setOnClickListener(){
            if (serviceBound && mediaService?.isSongPlaying!!){
                pause()
            }
        }

        buttonStop.setOnClickListener() {
            // stop the song and reset it to 00:00
            mediaService?.stop()
            updateUIStopRun()
            currTime.text = "0:00"
            progressBar.progress = 0
            // buttonPlay.setBackgroundResource(R.drawable.iconplaywhite)
        }


        buttonForward.setOnClickListener() {
            next()
        }

        buttonBackwards.setOnClickListener() {
            mediaService?.back()
            currTime.text = "0:00"
            progressBar.progress = 0
            setSong(mediaService?.getSong()!!)
            updateUIStartRun()
            processingScope.launch { doProcessing() }
        }

        // handling gestures
        buttonGesture.setOnClickListener() {
            if(!gesture){
                buttonGesture.setBackgroundResource(R.drawable.icons_shake_phone_50)
                Toast.makeText(view.context,
                    "Gestures On",
                    Toast.LENGTH_SHORT).show()
                mediaService?.gesturesOn()
                gesture = true
            } else {
                buttonGesture.setBackgroundResource(R.drawable.icons_shake_phone)
                Toast.makeText(view.context,
                    "Gestures Off" +
                            "",
                    Toast.LENGTH_SHORT).show()
                mediaService?.gesturesOff()
                gesture = false
            }

        }

        buttonExit.setOnClickListener(){
            mediaService?.exit()
            exit()
        }
    }

    // set song, UI and service
    private fun setSong(index: Int){
        // formatting time to desired format...
        val seconds: Int = mediaService?.mediaPlayer!!.duration / 1000
        totalSongDuration = seconds
        val minutes: Int = seconds / 60
        val extraSeconds: Int = seconds - minutes * 60
        val total = if(extraSeconds < 10){
            "$minutes:0$extraSeconds"
        } else {
            "$minutes:$extraSeconds"
        }

        totalTime.text = total
        mediaService?.setSongTime(total)
        songName.text = mediaService?.songs!![index]
        // Log.d("songName", "${mediaService?.songs!![index]}")
        // buttonPlay.setBackgroundResource(R.drawable.iconplaywhite)
        // Set the maximum value of progress bar
        progressBar.max = mediaService?.mediaPlayer?.duration!!
    }

    // play random song
    private fun playRandom(){
        mediaService?.playRandom()
        currTime.text = "0:00"
        progressBar.progress = 0
        processingScope.launch { doProcessing() }
        setSong(mediaService?.getSong()!!)
    }

    // play next song
    private fun next(){
        mediaService?.next()
        updateUIStopRun()
        currTime.text = "0:00"
        processingScope.launch { doProcessing() }
        progressBar.progress = 0
        setSong(mediaService?.getSong()!!)
        updateUIStartRun()
    }

    // pause song
    private fun pause() {
        mediaService?.pause()
        // buttonPlay.setBackgroundResource(R.drawable.iconplaywhite)
    }

    // resume song
    private fun resume() {
        mediaService?.resume()
        processingScope.launch { doProcessing() }
        // buttonPlay.setBackgroundResource(R.drawable.iconpausewhite)
    }

    // exit app
    private fun exit(){
        // Adapted from https://stackoverflow.com/questions/3226495/how-to-exit-from-the-application-and-show-the-home-screen
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        finish()
    }

    // Handler to update the UI every second when song is playing
    private val updateTimeHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(message: Message) {
            if (MSG_UPDATE_TIME == message.what && mediaService?.isSongPlaying!!) {
                Log.d(TAG, "updating time")
                updateUITimer()
                sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_RATE_MS)
            }
            Log.d("handler", "pride")
        }
    }

    override fun onStart(){
        super.onStart()
        Log.d(TAG, "Starting and binding media service");
        // Binding MediaPlayerService
        val i = Intent(this, MediaPlayerService::class.java)
        i.action = ACTION_START
        Log.d("broadcast", "binding service")
        bindService(i, mConnection, 0);
        startService(i)
    }

    override fun onResume() {
        super.onResume()
        updateUITimer()
        //Log.d("start", "prride")
        //Log.d("onResume", "pride v onResume")
    }



    override fun onStop() {
        super.onStop()
        updateUIStopRun()

        if (serviceBound) {
            // if the song is playing then we put the MediaPlayerService in the foreground
            if(mediaService?.isSongPlaying == true){
                mediaService?.foreground()
            } else {  // else we stop the service
                stopService(Intent(this, MediaPlayerService::class.java))
            }
            unbindService(mConnection)
            serviceBound = false
        }
    }

    private fun doProcessing() {
        // While the song is playing, update the progress bar
        while(mediaService?.isSongPlaying!!){
            Log.d("coroutine", "pride")
            try {
                Thread.sleep(UPDATE_RATE_MS)  // update every UPDATE_RATE_MS
            } catch (e: InterruptedException){
                e.printStackTrace()
            }
            // Get current song progress
            if(mediaService?.isSongPlaying!!) {
                val currentPosition = mediaService?.mediaPlayer!!.currentPosition
                progressBar.progress = currentPosition
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver!!)
    }

    /**
     * Updates the UI when a run starts
     */
    private fun updateUIStartRun() {
        updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)

    }

    /**
     * Updates the UI when a run stops
     */
    private fun updateUIStopRun() {
        updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
    }

    private fun updateUITimer() {
        if(serviceBound){
            val seconds = mediaService?.elapsedTime()?.toInt()
            if (seconds == -1){
                next()
                return
            }
            val minutes: Int = seconds!! / 60
            val extraSeconds: Int = seconds - minutes * 60
            val total:String = if (extraSeconds < 10){
                "$minutes:0$extraSeconds"
            }else {
                "$minutes:$extraSeconds"
            }
            currTime.text = total

            // Log.d("currentPos", "$currentPosition")

        }
        Log.d("handler", "pride2")
    }

    // Broadcast receiver adapted from https://stackoverflow.com/questions/36665670/android-sending-a-broadcast-to-mainactivity-and-then-showing-a-textview
    var mReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("broadcast", "pride")
            if (intent?.action == ACTION_EXIT_APP) {
                exit()
            } else if (intent?.action == ACTION_RESUME_SONG){
                resume()
                updateUIStartRun()
            }
        }
    }
}