package com.ecashwalletapp

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import com.facebook.react.modules.core.DeviceEventManagerModule

class MainActivity : ReactActivity() {

  private val TAG = "MainActivity"
  private var pendingPaymentUri: String? = null
  
  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "eCashWalletApp"

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleNfcIntent(intent)
  }
  
  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    
    // Ignore NFC intents when app is already running (foreground)
    // Only process NFC when launching from background
    if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
      return
    }
  }
  
  /**
   * Handle NFC NDEF intent when app is launched from background
   */
  private fun handleNfcIntent(intent: Intent?) {
    if (intent == null) {
      return
    }
    
    if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
      
      // Parse the NDEF message
      val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
      if (rawMessages != null && rawMessages.isNotEmpty()) {
        val ndefMessage = rawMessages[0] as NdefMessage
        val records = ndefMessage.records
        
        if (records.isNotEmpty()) {
          val record = records[0]
          
          // Check if it's a URI record (TNF = 0x01, Type = "U")
          if (record.tnf.toInt() == 0x01) {
            val payload = record.payload
            if (payload.isNotEmpty()) {
              // First byte is the URI identifier code (0x00 for no prefix)
              val uriBytes = payload.copyOfRange(1, payload.size)
              val uri = String(uriBytes, Charsets.UTF_8)
              
              // Store for sending when React Native is ready
              sendPaymentRequest(uri)
            }
          }
        }
      }
    }
  }
  
  /**
   * Store a payment request URI and schedule sending to React Native
   */
  private fun sendPaymentRequest(uri: String) {
    pendingPaymentUri = uri
    tryToSendPendingPaymentRequest()
  }
  
  override fun onResume() {
    super.onResume()
    
    // Check if we have a pending payment request to send
    if (pendingPaymentUri != null) {
      tryToSendPendingPaymentRequest()
    }
  }
  
  /**
   * Try to send pending payment request to React Native
   * Waits for React context and event listeners to be ready
   */
  private fun tryToSendPendingPaymentRequest(attempt: Int = 0) {
    if (pendingPaymentUri == null) {
      return
    }
    
    val uri = pendingPaymentUri
    val reactContext = reactInstanceManager.currentReactContext
    
    if (reactContext != null && attempt >= 5) {
      // Wait 1 second for event listeners to be registered
      try {
        reactContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("PAYMENT_REQUEST", uri)
        pendingPaymentUri = null
      } catch (e: Exception) {
        Log.e(TAG, "Error sending payment request to React Native", e)
      }
    } else if (attempt < 30) {
      // Retry every 200ms (max 6 seconds)
      Handler(Looper.getMainLooper()).postDelayed({
        tryToSendPendingPaymentRequest(attempt + 1)
      }, 200)
    }
  }
}
