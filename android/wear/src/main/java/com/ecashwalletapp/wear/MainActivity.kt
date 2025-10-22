package com.ecashwalletapp.wear

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.ecashwalletapp.R

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val textView = findViewById<TextView>(R.id.text)
        textView.text = "eCash Wallet"
    }
}

