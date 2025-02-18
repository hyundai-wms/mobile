package com.myme.qrapp

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix

class QRCodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        val receiptPlanCode = intent.getStringExtra("receiptPlanCode") ?: ""
        val planId = intent.getStringExtra("receiptPlanId") ?: ""
        val itemCount = intent.getIntExtra("itemCount", 0)

        val qrCodeLayout = findViewById<LinearLayout>(R.id.qrCodeLayout)

        // itemCount 만큼 QR 코드 생성
        for (i in 1..itemCount) {
            val qrData = "$receiptPlanCode-$planId-$i"
            val bitMatrix = generateQRCode(qrData)
            val bitmap = bitmapFromMatrix(bitMatrix)

            // QR 코드 이미지를 ImageView에 설정
            val imageView = ImageView(this)
            imageView.setImageBitmap(bitmap)
            qrCodeLayout.addView(imageView)
        }
    }

    private fun generateQRCode(data: String): BitMatrix {
        val writer = QRCodeWriter()
        return writer.encode(data, BarcodeFormat.QR_CODE, 215, 216)
    }

    private fun bitmapFromMatrix(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}