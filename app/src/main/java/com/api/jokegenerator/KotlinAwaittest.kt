package com.api.jokegenerator

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import co.metalab.asyncawait.async
import com.google.android.gms.tasks.Tasks.await
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Boolean
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class KotlinAwaittest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_awaittest)
        val task =   asyncThing(this@KotlinAwaittest)
        task.execute()
    }
    private class asyncThing internal constructor(activity: KotlinAwaittest) : AsyncTask<Int?, Int?, String>() {
        var type = ""
        var jokeSaved = false
        override fun onPreExecute() {
            super.onPreExecute()
        }

        protected fun doInBackground(vararg integers: Int): String {
           Thread.sleep(10000)
            return "done"
        }

        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
        }

        init {
        }

        override fun doInBackground(vararg p0: Int?): String {
            TODO("Not yet implemented")
        }
    }
}