package com.duckduckgo.downloads

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.system.Os.close
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DownloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        findViewById<Button>(R.id.cta_download).setOnClickListener {
            if (hasWriteStoragePermission()) {
                downloadFile()
            } else {
                requestWriteStoragePermission()
            }
        }

    }

    override fun onBackPressed() {
        // go back to previous screen or get out if first page
        onSupportNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadFile()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun downloadFile() {
        val fileURI = "http://ipv4.download.thinkbroadband.com/20MB.zip"
        val mimeType = "application/zip"
        val fileName = "20MB.zip"


        val request = DownloadManager.Request(fileURI.toUri()).apply {
            allowScanningByMediaScanner()
            setMimeType(mimeType)
            setAllowedOverMetered(true)
            setTitle(fileName)
            setDescription(fileName)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setDestinationInExternalPublicDir("Download", "20MB.zip")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val requestId = manager?.enqueue(request)
        monitorDownloadState(requestId)
    }

    private fun monitorDownloadState(id: Long?) = runBlocking {
        launch(Dispatchers.IO) {
            if (id != null) {
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val myDownloadQuery = DownloadManager.Query().setFilterById(id)

                while (true) {
                    val cursor: Cursor = downloadManager.query(myDownloadQuery)
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val status = cursor.getInt(columnIndex)
                        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = cursor.getInt(columnReason)
                        val columnTotal = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val total = cursor.getInt(columnTotal)
                        val columnCurrent = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val current = cursor.getInt(columnCurrent)

                        Log.d("DownloadsSample", "DownloadManager job $id status $status reason $reason total $total current $current")
                        delay(1000)
                    }
                }
            }
        }
    }

    private fun hasWriteStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWriteStoragePermission() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)
    }

    companion object {
        private const val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 200
    }
}