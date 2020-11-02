package com.example.diwatch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

data class TimeRecordsModel(val starttime: Long, val endtime: Long, val elapsed: Long, val activity: String?)

class TimeRecordsListAdapter(var ctx: Context, var resource: Int, var items: List<TimeRecordsModel> )
    : ArrayAdapter<TimeRecordsModel>(ctx, resource, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(ctx)
        val view= layoutInflater.inflate(resource, null)
        val timerecord: TimeRecordsModel = items[position]

        val timeFormatter = SimpleDateFormat("HH:mm:ss")
        view.findViewById<TextView>(R.id.starttime_textview).text =
            timeFormatter.format(Date(timerecord.starttime))

        view.findViewById<TextView>(R.id.endtime_textview).text =
            timeFormatter.format(Date(timerecord.endtime))

        view.findViewById<TextView>(R.id.elapsed_textview).text = timerecord.elapsed.toString() + " sec"


        view.findViewById<TextView>(R.id.activity_textview).text = timerecord.activity

        return view
    }

}
