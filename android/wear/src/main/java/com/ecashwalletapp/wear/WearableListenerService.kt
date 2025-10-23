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
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == WALLET_DATA_PATH) {
                    // Extract address from data
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val address = dataMap.getString(KEY_ADDRESS)
                    
                    if (address != null) {
                        // Save to SharedPreferences
                        getSharedPreferences("wallet", MODE_PRIVATE)
                            .edit()
                            .putString("address", address)
                            .apply()
                        
                        // Restart MainActivity to show QR code
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

