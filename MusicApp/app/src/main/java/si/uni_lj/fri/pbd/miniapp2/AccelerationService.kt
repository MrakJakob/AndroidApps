package si.uni_lj.fri.pbd.miniapp2

import android.app.Service

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.lang.Math.abs

class AccelerationService : Service() {
    companion object {
        private val TAG: String? = AccelerationService::class.simpleName
    }

    private lateinit var serviceBinder: Binder
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    var sensorChanged = false
    // Media service
    // private var mediaService: MediaPlayerService? = null

    override fun onCreate() {
        Log.d(TAG, "Creating service")
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        serviceBinder = RunServiceBinder()
    }

    // acceleration sensor listener
    private val sensorListener:SensorEventListener = object : SensorEventListener {
        private val Noise_threshold = 1
        private var Xt_prev:Float = 0F
        private var Yt_prev:Float = 0F
        private var Zt_prev:Float = 0F

        private var command = "IDLE"

        // when sensor changes
        override fun onSensorChanged(p0: SensorEvent?) {
            val Xt = p0?.values!![0]
            val Yt = p0?.values!![1]
            val Zt = p0?.values!![2]

            var dX = abs(Xt_prev - Xt).toInt()  // mogoce ni najbl natancno
            var dY = abs(Yt_prev - Yt).toInt()
            var dZ = abs(Zt_prev - Zt).toInt()

            Xt_prev = Xt
            Yt_prev = Yt
            Zt_prev = Zt

            if(dX <= Noise_threshold) {
                dX = 0
                command = "IDLE"
            }
            if (dY <= Noise_threshold){
                dY = 0
                command = "IDLE"
            }
            if (dZ <= Noise_threshold){
                dZ = 0
                command = "IDLE"
            }

            if (dX > dZ){
                command = "HORIZONTAL"
            }
            if (dZ > dX){
                command = "VERTICAL"
            }
            sensorChanged = command != "IDLE"
            Log.d("command", "$command")
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            Log.d("accuracy", "$p0, $p1")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return serviceBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
    }

    inner class RunServiceBinder : Binder() {
        val service: AccelerationService
            get() = this@AccelerationService
    }
}