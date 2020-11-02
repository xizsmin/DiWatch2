package com.example.diwatch

import android.annotation.SuppressLint
import android.app.Service
import android.content.*
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

var timecount: Long = 0

class TimerService: Service() {

    companion object {
        public val BROADCAST_ACTION_UPDATE_COUNT = "android.intent.action.BROADCAST_ACTION_UPDATE_COUNT"
        public val BROADCAST_ACTIVITY_RESUMED = "android.intent.action.BROADCAST_ACTIVITY_RESUMED"
        public val BROADCAST_ACTION_RESET_COUNT = "android.intent.action.BROADCAST_ACTION_RESET_COUNT"
        public val BROADCAST_ACTION_SYNC_COUNT= "android.intent.action.BROADCAST_ACTION_SYNC_COUNT"

    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService() : TimerService = this@TimerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("TimerService", "onStartCommand *********************************")
        return START_STICKY
    }

    lateinit var runnable: Runnable
    override fun onCreate() {
        timecount = 0

        registerReceiver(resumeEventReceiver, intentFilter)
        registerReceiver(resumeEventReceiver, intentResumedFilter)
        registerReceiver(resumeEventReceiver, intentPausedFilter)

        val mHandler: Handler = Handler()
        runnable= Runnable {
            timecount += 1
            mHandler.postDelayed(runnable, 1000)
        }
        mHandler.post(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        Log.i("TimerService", "onDestroy *********************************")
    }


    val intentFilter = IntentFilter(TimerService.BROADCAST_ACTION_RESET_COUNT)
    val intentResumedFilter = IntentFilter(TimerService.BROADCAST_ACTIVITY_RESUMED)
    val intentPausedFilter = IntentFilter(TimerService.BROADCAST_ACTION_SYNC_COUNT)


    class ResumeEventReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TimerService.BROADCAST_ACTION_RESET_COUNT) {
                timecount = 0
            } else if (intent?.action == TimerService.BROADCAST_ACTIVITY_RESUMED) {

                Intent().also {
                    it.action = TimerService.BROADCAST_ACTION_UPDATE_COUNT
                    it.putExtra("timecount", timecount)
                    context?.sendBroadcast(it)
                }
            } else if (intent?.action == TimerService.BROADCAST_ACTION_SYNC_COUNT) {

                timecount = intent?.getLongExtra("timecount", 0)
            }
        }
    }

    val resumeEventReceiver = ResumeEventReceiver()


}

