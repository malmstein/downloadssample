package com.duckduckgo.downloads

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.duckduckgo.downloads.toyvpn.ToyVpnClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.cta_externalVPN).setOnClickListener {
            startActivity(Intent(this, ToyVpnClient::class.java))
        }

    }

}