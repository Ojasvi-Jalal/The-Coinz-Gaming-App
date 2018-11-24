package com.example.ojasvi.coinz

import android.os.AsyncTask
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

//interface DownloadCompleteListener{
//   fun downloadComplete(result: String)
//}
//
//object DownloadCompleteRunner : DownloadCompleteListener{
//    var result : String? = null
//    override fun downloadComplete(result: String) {
//        this.result = result
//    }
//}
//
//class DownloadFileTask(private val caller: DownloadCompleteListener):
//        AsyncTask<String, Void, String>() {
////    override fun doInBackground(var)
//}

class DownloadFileTask(private val url: String){
    var result : String? = null

    fun run():String = try {
        loadFileFromNetwork(url)
    } catch ( e: IOException){
        "Unable to load content. Check your network connection"
    }

    private fun loadFileFromNetwork(urlString: String): String{
        val stream: InputStream = downloadUrl(urlString)
        //Read input from stream, build result as a string

        return stream.bufferedReader().use { it.readText() }
    }

    // Given a string representation of a URL, sets up a connection and gets an input stream.
    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        // Also available: HttpsURLConnection
        conn.readTimeout = 10000 // milliseconds
        conn.connectTimeout = 15000 // milliseconds
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect() // Starts the query
        return conn.inputStream
    }
}