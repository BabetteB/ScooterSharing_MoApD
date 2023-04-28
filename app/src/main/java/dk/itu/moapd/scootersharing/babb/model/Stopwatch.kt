package dk.itu.moapd.scootersharing.babb.model

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.*

class StopWatch : Service()
{
    override fun onBind(p0: Intent?): IBinder? = null

    private val timer = Timer()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        val time = intent.getDoubleExtra(TIME_EXTRA, 0.0)
        timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        return START_NOT_STICKY
    }

    override fun onDestroy()
    {
        timer.cancel()
        super.onDestroy()
    }

    private inner class TimeTask(private var time: Double) : TimerTask()
    {
        override fun run()
        {
            val intent = Intent(TIMER_UPDATED)
            time++
            intent.putExtra(TIME_EXTRA, time)
            sendBroadcast(intent)
        }
    }

    companion object
    {
        const val TIMER_UPDATED = "timerUpdated"
        const val TIME_EXTRA = "timeExtra"
    }

    fun startStopTimer(timerStarted : Boolean, serviceIntent: Intent, time: Double)
    {
        if(timerStarted)
            stopTimer(serviceIntent)
        else
            startTimer(serviceIntent, time)
    }

    fun startTimer(serviceIntent : Intent, time : Double)
    {
        serviceIntent.putExtra(TIME_EXTRA, time)
        startService(serviceIntent)
    }

    fun stopTimer(serviceIntent : Intent)
    {
        stopService(serviceIntent)
    }






}