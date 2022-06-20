package si.uni_lj.fri.pbd.miniapp2

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File
import java.io.IOException
import kotlin.properties.Delegates


class MediaPlayerService : Service() {
    companion object {
        private val TAG: String? = MediaPlayerService::class.simpleName
        const val ACTION_STOP = "stop_service"
        const val ACTION_START = "start_service"
        const val ACTION_START_SONG = "start_song"
        const val ACTION_PAUSE = "pause_song"
        const val ACTION_RESUME = "resume_song"
        const val ACTION_STOP_SONG = "stop_song"
        private const val UPDATE_RATE_MS = 1000L
        private const val channelID = "background_music_player"
        private const val NOTIFICATION_ID = 69
    }
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var currTime: Long = 0

    private lateinit var serviceBinder: Binder

    var mediaPlayer: MediaPlayer? = null
    var songs : MutableList<String>? = mutableListOf()
    private var song: AssetFileDescriptor? = null
    var isSongPlaying = false

    private lateinit var totalTime: String

    // on start flag
    private var start = 0

    // song index
    private var index by Delegates.notNull<Int>()

    // stop boolean
    private var stop = false

    // Notification manager
    private lateinit var notificationManager: NotificationManagerCompat
    private var builder:NotificationCompat.Builder? = null  // notification builder

    // acceleration service
    private lateinit var accelerationService: AccelerationService
    private var gesturesOn = false

    // main activity context
    private val context:Context = this

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Binding service")
        return serviceBinder
    }

    private val mConnectionAccelerationService: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.d(MainActivity.TAG, "Acceration service bound")
            val binderAccelerationService = iBinder as AccelerationService.RunServiceBinder
            accelerationService = binderAccelerationService.service
            checkAccelerometerSensorHandler.sendEmptyMessage(1)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d("gestures", "Service disconnect")
        }
    }


    override fun onCreate() {
        Log.d(TAG, "Creating service")

        startTime = 0
        endTime = 0
        currTime = 0

        serviceBinder = RunServiceBinder()

        // init notificationManager
        notificationManager = NotificationManagerCompat.from(this)


        // Get songs from assets folder
        val assetManager: AssetManager = assets
        try {
            val files: Array<String> = assetManager.list("") as Array<String>

            files.forEach { file ->

                val fileExt = File(file).extension
                if(fileExt == "mp3"){
                    Log.d("files1", file)
                    songs?.add(file)
                }
            }
        } catch (e: IOException){
            e.printStackTrace()
        }
//        songName = songNamePlh
//        totalTime = totalTimePlh
        index = (0 until songs!!.size).random()


        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer()
        }
        else {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        }
        setSong()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting service")

       // Check if the intent's action is to stop the service
        if (intent.action == ACTION_STOP){
            // If so, stop the service
            exit()
        } else if (intent.action == ACTION_PAUSE){
            // if the intent was pause, we check whether the song is playing or not
            if(isSongPlaying){
                // if it is playing we pause it
                pause()
                stop = false
                Log.d("notification", "pride1")
                // rebuild the notification to display "Pause"
                notificationManager.cancel(NOTIFICATION_ID)
                foreground()
                updateForeground(currTime.toInt())
            } else {
                // else we resume it
                resume()
                stop = false
                Log.d("notification", "pride2")
                // rebuild the notification to display "Resume"
                notificationManager.cancel(NOTIFICATION_ID)
                foreground()
                updateForeground(currTime.toInt())
            }

        }
        else if(intent.action == ACTION_STOP_SONG) {
            stop()
            stop = true
            notificationManager.cancel(NOTIFICATION_ID)
            foreground()
            updateForeground(currTime.toInt())
        } else if (intent.action == ACTION_START_SONG){
            playRandom()
            stop = false
            notificationManager.cancel(NOTIFICATION_ID)
            foreground()
            updateForeground(currTime.toInt())
        }

        return START_STICKY
    }

    // when we turn the gestures on we bind and start the accelerationservice
    fun gesturesOn(){
        // Start AccelerationService
        val i = Intent(this, AccelerationService::class.java)
        i.action = ACTION_START
        bindService(i, mConnectionAccelerationService, 0);
        startService(i)
        gesturesOn = true

    }

    // when we turn gestures off, we stop and unbind the Acceleration service
    fun gesturesOff(){
        // Log.d("gestures", "prije")
        // accelerationService.stopSelf()
        stopService(Intent(this, AccelerationService::class.java))
        unbindService(mConnectionAccelerationService)
        gesturesOn = false
    }

    // get song and its info from assets, start playing if the app wasn't just started
    fun setSong(){
        // Log.d("setSong", "$songs")
        song = assets.openFd(songs!![index])
        // Log.d("setSong", "$song")
        mediaPlayer!!.setDataSource(song!!.fileDescriptor, song!!.startOffset, song!!.length)
        mediaPlayer!!.prepare()
        endTime = (mediaPlayer?.duration!!).toLong()

        val time = (endTime / 1000).toInt()
        val minutes: Int = time / 60
        val extraSeconds: Int = time - minutes * 60
        totalTime = if(extraSeconds < 10){
            "$minutes:0$extraSeconds"
        } else {
            "$minutes:$extraSeconds"
        }

        if(start != 0){
            startSongTimer()
            mediaPlayer!!.start()
            isSongPlaying = true
        }
        start = 1
    }

    // get song index
    fun getSong(): Int{
        return index
    }

    // pause song
    fun pause() {
        mediaPlayer!!.pause()
        isSongPlaying = false
    }

    // resume song
    fun resume() {
        startSongTimer()
        mediaPlayer!!.start()
        isSongPlaying = true
    }

    // stop song
    fun stop(){
        mediaPlayer!!.stop()
        mediaPlayer!!.prepare()
        currTime = 0
        startTime = 0
        isSongPlaying = false
    }

    // next song
    fun next(){
        mediaPlayer!!.stop()
        mediaPlayer!!.reset()
        isSongPlaying = false
        currTime = 0
        startTime = System.currentTimeMillis()
        val len = songs?.size
        index = (index+1)% len!!
        // Log.d("next", "$index, len=$len")
        setSong()
    }

    // previous song
    fun back() {
        mediaPlayer!!.stop()
        mediaPlayer!!.reset()
        isSongPlaying = false
        currTime = 0
        startTime = System.currentTimeMillis()
        if (index == 0){
            index = songs?.size?.minus(1)!!
        } else { index-- }
        // Log.d("next", "$index")
        setSong()
    }

    // random song
    fun playRandom(){
        mediaPlayer!!.stop()
        mediaPlayer!!.reset()
        currTime = 0
        startTime = System.currentTimeMillis()
        index = (0 until songs!!.size).random()
        setSong()
    }

    fun exit(){
        // on exit we stop and release media player, stop foreground and send an intent to show phone's home screen
        isSongPlaying = false
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        stopForeground(true)

        // adapted from https://stackoverflow.com/questions/36665670/android-sending-a-broadcast-to-mainactivity-and-then-showing-a-textview
        val i = Intent(MainActivity.ACTION_EXIT_APP)
        i.putExtra("success", true)
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(i)
        // stopSelf()

        // Log.d("broadcast", "pride media exit with intent = $i")

    }

    fun startSongTimer(){
        if(currTime <= 0){
            startTime = System.currentTimeMillis()
            isSongPlaying = true
        } else {
            Log.d("currTime", "$currTime")
            startTime = System.currentTimeMillis() - currTime * 1000
        }
    }

    // function to count and store time
    fun elapsedTime(): Long {
        if(endTime > startTime){
            // Log.d("elapsed", "first: endTime = $endTime, startTime = $startTime")
            currTime = (endTime - startTime) / 1000
            return (endTime - startTime) / 1000

        } else {
            // Log.d("elapsed", "second: endTime = $endTime, startTime = $startTime, currTime=$currTime")
            currTime = (System.currentTimeMillis() - startTime) / 1000
            if(System.currentTimeMillis() - startTime >= endTime){
                return -1
            }

            if(builder != null){
                // function to update notification time
                updateForeground( ((System.currentTimeMillis() - startTime) / 1000).toInt())
            }

            return (System.currentTimeMillis() - startTime) / 1000
        }
    }

    inner class RunServiceBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    // put the service in the foreground
    fun foreground(){
        // start updating time in notification
        updateNotificationTimeHandler.sendEmptyMessage(1)
        // start foreground
        startForeground(NOTIFICATION_ID, createNotification())
    }

    fun updateNotification(time:Int): Notification {
        // formatting time..
        val minutes: Int = time / 60
        val extraSeconds: Int = time - minutes * 60
        val total = if(extraSeconds < 10){
            "$minutes:0$extraSeconds"
        } else {
            "$minutes:$extraSeconds"
        }
        // update notification text and title
        builder?.setContentText("$total/$totalTime")
        builder?.setContentTitle(songs!![index])
        return builder?.build()!!
    }

    fun updateForeground(time:Int) {
        startForeground(NOTIFICATION_ID, updateNotification(time))
    }

    // put the service in the background
    fun background(){
        updateNotificationTimeHandler.removeMessages(1)
        stopForeground(true)
    }

    // Handler to update the UI every second when song is playing
    private val updateNotificationTimeHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(message: Message) {
            if (isSongPlaying!!) {
                // if the song is playing we update time in the notification
                // Log.d("handler", "updating notification time")
                elapsedTime()
                sendEmptyMessageDelayed(1, UPDATE_RATE_MS)
            }

            // Log.d("handler", "pride")
        }
    }

    // Handler to check whether accelerometer sensor has changed
    private val checkAccelerometerSensorHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // if gestures are turned on
            if (gesturesOn){
                // check whether sensor has changed
                if (accelerationService.sensorChanged){
                    // if sensor changed than we check if the song is playing or not
                    if (isSongPlaying){
                        // if it is we pause it
                        pause()
                        Log.d("gestures", "pause")
                    } else {
                        // else we resume it
                        Log.d("gestures", "resume")
                        // resume()
                        // we send a broadcast to MainActivity so that we can start updating UI again and resume the song from there
                        val i = Intent(MainActivity.ACTION_RESUME_SONG)
                        i.putExtra("success", true)
                        LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(i)

                    }
                }
                // We send delayed messages every 175ms not 500ms because the emulator is less sensitive to movement than an actual phone,
                // so I increased the interval of checking whether the sensor has changed
                sendEmptyMessageDelayed(1, 175)
            }
        }
    }

    fun setSongTime(time: String){
        totalTime = time
    }

    /**
     * Creates a notification for placing the service into the foreground
     *
     * @return a notification for interacting with the service when in the foreground
     */
    // TODO: Uncomment for creating a notification for the foreground service
    private fun createNotification(): Notification {

        // EXIT
        val actionIntentExit = Intent(this, MediaPlayerService::class.java)
        actionIntentExit.action = ACTION_STOP
        // PAUSE
        val actionIntentPause = Intent(this, MediaPlayerService::class.java)
        actionIntentPause.action = ACTION_PAUSE
        // RESUME
        val actionIntentResume = Intent(this, MediaPlayerService::class.java)
        actionIntentResume.action = ACTION_RESUME
        // STOP
        val actionIntentStop = Intent(this, MediaPlayerService::class.java)
        actionIntentStop.action = ACTION_STOP_SONG

        // START
        val actionIntentStart = Intent(this, MediaPlayerService::class.java)
        actionIntentStart.action = ACTION_START_SONG

        val actionPendingIntentExit = PendingIntent.getService(this, 0, actionIntentExit, PendingIntent.FLAG_IMMUTABLE)
        val actionPendingIntentPause = PendingIntent.getService(this, 0, actionIntentPause, PendingIntent.FLAG_IMMUTABLE)
        val actionPendingIntentStop = PendingIntent.getService(this, 0, actionIntentStop, PendingIntent.FLAG_IMMUTABLE)
        val actionPendingIntentStart = PendingIntent.getService(this, 0, actionIntentStart, PendingIntent.FLAG_IMMUTABLE)


        builder = NotificationCompat.Builder(this, channelID)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setSmallIcon(R.drawable.music_player)
            .setChannelId(channelID)
            .addAction(android.R.drawable.ic_media_pause, "Exit",actionPendingIntentExit)
            .addAction(R.drawable.icons_stop, "Stop", actionPendingIntentStop)
        if(stop){  // if the user clicked stop in notification
            builder?.addAction(R.drawable.playiconwhite, "Start", actionPendingIntentStart)
        }
        else if(isSongPlaying){  // else if the song is playing we display "Pause" in notification
            // Log.d("notification", "pride3")
            builder?.addAction(R.drawable.pauseiconwhite, "Pause", actionPendingIntentPause)
        } else if (!isSongPlaying) {  // if it's not playing we display "Resume"
            // Log.d("notification", "pride4")
            builder?.addAction(R.drawable.playiconwhite, "Resume", actionPendingIntentPause)
        }


        val resultIntent = Intent(this, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
            PendingIntent.FLAG_IMMUTABLE)
        builder?.setContentIntent(resultPendingIntent)
        return builder!!.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        } else {
            val channel = NotificationChannel(channelID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW)
            channel.description = getString(R.string.channel_desc)
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            val managerCompat = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            managerCompat.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        if (gesturesOn) {
            gesturesOn = false
            stopService(Intent(this, AccelerationService::class.java))
            unbindService(mConnectionAccelerationService)
        }
    }
}