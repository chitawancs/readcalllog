package com.example.myreadcalllog

import android.Manifest.permission.READ_CALL_LOG
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val permission: String = READ_CALL_LOG
    private val requestCode: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            val callLogList = readCallLog(this,"01/02/2023","28/02/2023")
            callLogList.forEach {
                Log.d("XXX", it)
            }

        }
    }

    private fun readCallLog(context: Context,startDate: String,endDate: String): List<String> {
        val callLogUri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DATE, CallLog.Calls.DURATION)
        val sortOrder = "${CallLog.Calls.DATE} DESC"
        val selection = "${CallLog.Calls.DATE} >= ? AND ${CallLog.Calls.DATE} < ?"
        val selectionArgs = arrayOf(getStartOfDayInMillis(startDate).toString(), getEndOfDayInMillis(endDate).toString())
        val cursor = context.contentResolver.query(callLogUri, projection, selection, selectionArgs, sortOrder)
        val callLogList = mutableListOf<String>()

        cursor?.use {
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)
                val type = cursor.getString(typeIndex)
                val date = formatDate(cursor.getLong(dateIndex))
                val duration = cursor.getString(durationIndex)
                val callLog = "Number: $number\nType: $type\nDate: $date \nDuration: $duration"
                callLogList.add(callLog)
            }
        }
        return callLogList
    }

    private fun formatDate(date: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(date))
    }

    private fun getStartOfDayInMillis(date: String): Long {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startOfDay = Calendar.getInstance().apply {
            time = dateFormat.parse(date) ?: Date()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return startOfDay.timeInMillis
    }

    private fun getEndOfDayInMillis(date: String): Long {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val endOfDay = Calendar.getInstance().apply {
            time = dateFormat.parse(date) ?: Date()
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return endOfDay.timeInMillis
    }
}