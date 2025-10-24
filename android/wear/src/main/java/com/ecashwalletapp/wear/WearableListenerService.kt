package com.ecashwalletapp.wear

import android.content.Intent
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearableListenerService : WearableListenerService() {
    
    companion object {
        private const val WALLET_DATA_PATH = "/wallet_data"
        private const val KEY_ADDRESS = "address"
        private const val KEY_BIP21_PREFIX = "bip21_prefix"
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == WALLET_DATA_PATH) {
                    // Extract address and BIP21 prefix from data
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val address = dataMap.getString(KEY_ADDRESS)
                    val bip21Prefix = dataMap.getString(KEY_BIP21_PREFIX)
                    
                    if (address != null) {
                        // Check if this is the first time receiving address
                        val prefs = getSharedPreferences("wallet", MODE_PRIVATE)
                        val previousAddress = prefs.getString("address", null)
                        val wasInitialized = previousAddress != null
                        
                        // Save both address and BIP21 prefix to SharedPreferences
                        val editor = prefs.edit()
                        editor.putString("address", address)
                        if (bip21Prefix != null) {
                            editor.putString("bip21_prefix", bip21Prefix)
                        }
                        editor.apply()
                        
                        // Start or refresh MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        if (wasInitialized) {
                            // App was already initialized - use SINGLE_TOP to preserve state
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        } else {
                            // First initialization - use CLEAR_TOP to restart fresh
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

