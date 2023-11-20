package com.example.artis

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.artis.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

class QRProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrprofile)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val currentUserId = currentUser.uid
            val userReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId)
            userReference.child("qrCodeLink").addListenerForSingleValueEvent(object:
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val imageView = findViewById<ImageView>(R.id.qrCode)
                    val qrCodeLink = dataSnapshot.getValue(String::class.java)
                    if (qrCodeLink != null) {
                        // Generate QR
                        val qrCodeBitmap = generateQRCode(qrCodeLink, 500, 500)
                        // Tampilkan atau bagikan QR Code
                        imageView?.setImageBitmap(qrCodeBitmap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        } else {
            // log out handler
        }
    }

    fun generateQRCode(link: String, width: Int, height: Int): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(link, BarcodeFormat.QR_CODE, width, height)

        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}