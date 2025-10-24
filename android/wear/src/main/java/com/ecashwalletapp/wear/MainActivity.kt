package com.ecashwalletapp.wear

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.ecashwalletapp.NfcHceService
import com.ecashwalletapp.R
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.tasks.Task
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val qrCodeImageView = findViewById<ImageView>(R.id.qrCode)
        val logoImageView = findViewById<ImageView>(R.id.logo)
        val openOnPhoneContainer = findViewById<LinearLayout>(R.id.openOnPhoneContainer)
        
        // Check if we have both wallet address and BIP21 prefix
        val prefs = getSharedPreferences("wallet", MODE_PRIVATE)
        val address = prefs.getString("address", null)
        val bip21Prefix = prefs.getString("bip21_prefix", null)
        
        if (address != null && bip21Prefix != null) {
            // Show QR code with address
            qrCodeImageView.visibility = View.VISIBLE
            logoImageView.visibility = View.VISIBLE
            openOnPhoneContainer.visibility = View.GONE
            
            val qrBitmap = generateQRCode(address, 320, 320)
            qrCodeImageView.setImageBitmap(qrBitmap)
            
            // Create BIP21 URI and set for NFC HCE
            val bip21Uri = createBip21Uri(address, bip21Prefix)
            NfcHceService.setBip21Uri(bip21Uri)
        } else {
            // Show "open on phone" UI if either address or BIP21 prefix is missing
            qrCodeImageView.visibility = View.GONE
            logoImageView.visibility = View.GONE
            openOnPhoneContainer.visibility = View.VISIBLE
            
            // Clear NFC URI since we don't have complete wallet data
            NfcHceService.clearBip21Uri()
            
            // Handle tap to open phone app
            openOnPhoneContainer.setOnClickListener {
                openPhoneApp()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Update NFC URI when activity resumes (in case data was updated while paused)
        updateNfcUri()
    }
    
    /**
     * Update the NFC URI based on current wallet data
     */
    private fun updateNfcUri() {
        val prefs = getSharedPreferences("wallet", MODE_PRIVATE)
        val address = prefs.getString("address", null)
        val bip21Prefix = prefs.getString("bip21_prefix", null)
        
        if (address != null && bip21Prefix != null) {
            // Create BIP21 URI and set for NFC HCE
            val bip21Uri = createBip21Uri(address, bip21Prefix)
            NfcHceService.setBip21Uri(bip21Uri)
        } else {
            // Clear NFC URI if we don't have complete wallet data
            NfcHceService.clearBip21Uri()
        }
    }
    
    /**
     * Create a BIP21 URI from an eCash address
     * Strips the address prefix (ectest: or ecash:) and adds the BIP21 prefix
     * The BIP21 prefix is received from the phone app (config.bip21Prefix from config.ts)
     */
    private fun createBip21Uri(address: String, bip21Prefix: String): String {
        // Strip any existing prefix (ectest: or ecash:)
        val cleanAddress = if (address.contains(":")) {
            address.substring(address.indexOf(":") + 1)
        } else {
            address
        }
        
        // Return BIP21 URI with the prefix from config
        return "$bip21Prefix$cleanAddress"
    }
    
    private fun generateQRCode(content: String, width: Int, height: Int): Bitmap {
        val writer = QRCodeWriter()
        
        // Disable quiet zone (border) to maximize QR code size
        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.MARGIN to 0
        )
        
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        
        // Use IntArray for much faster bitmap creation (10-20x faster than setPixel)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) Color.WHITE else Color.TRANSPARENT
            }
        }
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        
        return bitmap
    }
    
    private fun openPhoneApp() {
        // Send a message to the phone to open the app
        val messageClient = Wearable.getMessageClient(this)
        
        // Get connected nodes (phones)
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                // Send message to each connected phone
                messageClient.sendMessage(
                    node.id,
                    "/open_app",
                    null
                )
            }
        }
    }
}

