package com.example.diwatch

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class TimeRecordDB(val context: Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERION) {
    companion object {
        private val DATABASE_VERION = 1
        private val DATABASE_NAME = "TIMERECORD.db"
        private val TABLE_NAME = "timerecord"
        private val COL_STARTTIME = "starttime"
        private val COL_ENDTIME = "endtime"
        private val COL_ELAPSED = "elapsed"
        private val COL_ACTIVITY = "activity"
        val CREATE_TABLE_QUERY= ("CREATE TABLE IF NOT EXISTS $TABLE_NAME " +
                "($COL_STARTTIME INTEGER PRIMARY KEY, " + "$COL_ENDTIME INTEGER, " +
                "$COL_ELAPSED INTEGER, $COL_ACTIVITY TEXT)")

        val DROP_TABLE_QUERY = ("DROP TABLE IF EXISTS $TABLE_NAME")
        val SELECT_ALL_QUERY = ("SELECT * FROM $TABLE_NAME")

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(CREATE_TABLE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL(DROP_TABLE_QUERY)
        onCreate(db)
    }

    val totalTimeRecords: MutableList<TimeRecordsModel>
        get() {

            val ret = ArrayList<TimeRecordsModel>()
            val db = this.writableDatabase
            onCreate(db)
            val cursor = db.rawQuery(SELECT_ALL_QUERY, null)
            if (cursor.moveToFirst()) {
                do {
                    val timeRecord = TimeRecordsModel(
                        cursor.getLong(cursor.getColumnIndex(COL_STARTTIME)),
                        cursor.getLong(cursor.getColumnIndex(COL_ENDTIME)),
                        cursor.getLong(cursor.getColumnIndex(COL_ELAPSED)),
                        cursor.getString(cursor.getColumnIndex(COL_ACTIVITY))
                    )
                    ret.add(timeRecord)

                } while(cursor.moveToNext())
            }
            db.close()
            return ret
        }

    fun addTimeRecord(timeRecord:TimeRecordsModel) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_STARTTIME, timeRecord.starttime)
        values.put(COL_ENDTIME, timeRecord.endtime)
        values.put(COL_ELAPSED, timeRecord.elapsed)
        values.put(COL_ACTIVITY, timeRecord.activity)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun updateTimeRecord(timeRecord: TimeRecordsModel): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_STARTTIME, timeRecord.starttime)
        values.put(COL_ENDTIME, timeRecord.endtime)
        values.put(COL_ELAPSED, timeRecord.elapsed)
        values.put(COL_ACTIVITY, timeRecord.activity)

        return db.update(TABLE_NAME, values, "$COL_STARTTIME=?", arrayOf(timeRecord.starttime.toString()))
    }

    fun deleteTimeRecord(timeRecord: TimeRecordsModel) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_STARTTIME=?", arrayOf(timeRecord.starttime.toString()))
        db.close()
    }

    fun resetTimeRecord() {
        val db = this.writableDatabase
        db.execSQL(DROP_TABLE_QUERY)
        db.close()
    }

}
