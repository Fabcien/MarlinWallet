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
        
        // Check if we have a wallet address
        val address = getSharedPreferences("wallet", MODE_PRIVATE)
            .getString("address", null)
        
        if (address != null) {
            // Show QR code with address
            qrCodeImageView.visibility = View.VISIBLE
            logoImageView.visibility = View.VISIBLE
            openOnPhoneContainer.visibility = View.GONE
            
            val qrBitmap = generateQRCode(address, 320, 320)
            qrCodeImageView.setImageBitmap(qrBitmap)
        } else {
            // Show "open on phone" UI
            qrCodeImageView.visibility = View.GONE
            logoImageView.visibility = View.GONE
            openOnPhoneContainer.visibility = View.VISIBLE
            
            // Handle tap to open phone app
            openOnPhoneContainer.setOnClickListener {
                openPhoneApp()
            }
        }
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

