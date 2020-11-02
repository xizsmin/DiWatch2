package com.example.diwatch

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.postDelayed
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.sql.Time

var count:Long? = 0

var init:Boolean = true
var start_time: Long = 0
var end_time: Long = 0

class MainActivity : AppCompatActivity() {

    var timerecord_list: MutableList<TimeRecordsModel> = mutableListOf<TimeRecordsModel>()

    lateinit var timeRecordDB: TimeRecordDB

    fun loadTimeRecordFromDB(): MutableList<TimeRecordsModel> {
        return timeRecordDB.totalTimeRecords
    }

    var timecount:Long = 0
    val mHandler: Handler = Handler()
    lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "OnCreate?", Toast.LENGTH_SHORT).show()

        startService(Intent(this, TimerService::class.java))

        runnable = Runnable {
            timecount += 1
            val hour = timecount / 3600
            val min = (timecount - hour *3600)/60
            val sec = timecount - hour*3600 - min*60

            clock.text = String.format("%02d:%02d:%02d", hour, min, sec)
            mHandler.postDelayed(runnable, 1000)
        }


        timeRecordDB = TimeRecordDB(this)
        timerecord_list = loadTimeRecordFromDB()

        val timerecord_listview_adapter = TimeRecordsListAdapter(this, R.layout.list_adapter, timerecord_list)
        timerecords_listview.adapter = timerecord_listview_adapter

        registerReceiver(messageReceiver, intentFilter)

        timer_button.setOnCheckedChangeListener { buttonView, isChecked ->
            Toast.makeText(this, "button checked: " + isChecked.toString(), Toast.LENGTH_SHORT).show()

            if (init) {
                init = false
                start_time = System.currentTimeMillis()
                mHandler.post(runnable)
            }

            else {
                init = true
                mHandler.removeCallbacks(runnable)

                end_time = System.currentTimeMillis()
                val elapsed = (end_time - start_time)/1000
                val timeRecord = TimeRecordsModel(start_time, end_time, elapsed, "dummy")
                timerecord_list.add(timeRecord)
                timeRecordDB.addTimeRecord(timeRecord)

                timecount = 0
                clock.text = String.format("%02d:%02d:%02d", 0, 0, 0)
                sendBroadcast(Intent(TimerService.BROADCAST_ACTION_RESET_COUNT))
            }
        }

        reset_button.setOnClickListener {
            Toast.makeText(this, "DROP TABLE!", Toast.LENGTH_SHORT).show()
            timerecord_listview_adapter.notifyDataSetChanged()
            timeRecordDB.resetTimeRecord()

            timerecord_list.clear()



        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPause() {

        super.onPause()
        if (mHandler.hasCallbacks(runnable))
            mHandler.removeCallbacks(runnable)


        sendBroadcast(Intent().also {
            it.action = TimerService.BROADCAST_ACTION_SYNC_COUNT
            it.putExtra("timecount", timecount)
            sendBroadcast(it)
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onDestroy() {
        super.onDestroy()
        if (mHandler.hasCallbacks(runnable))
            mHandler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(this, "init: " + init.toString(), Toast.LENGTH_SHORT).show()
        if (!init) {
            sendBroadcast(Intent(TimerService.BROADCAST_ACTIVITY_RESUMED))
        }
    }

    inner class MessageReceiver: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val count = intent?.getLongExtra("timecount", 0)
            timecount = count!!
            mHandler.post(runnable)
         }
    }
    val messageReceiver = MessageReceiver()
    val intentFilter = IntentFilter(TimerService.BROADCAST_ACTION_UPDATE_COUNT)

}