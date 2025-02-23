package com.myme.qrapp

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import com.myme.qrapp.ui.notifications.today
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//class QRCodeActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_qr_code)
//
//        val receiptPlanCode = intent.getStringExtra("receiptPlanCode") ?: ""
//        val planId = intent.getStringExtra("receiptPlanId") ?: ""
//        val itemCount = intent.getIntExtra("itemCount", 0)
//
//        val qrCodeLayout = findViewById<LinearLayout>(R.id.qrCodeLayout)
//
//        // itemCount 만큼 QR 코드 생성
//        for (i in 1..itemCount) {
//            val qrData = "$receiptPlanCode-$planId-$i"
//            Log.d("chk0","$qrData")
//            val bitMatrix = generateQRCode(qrData)
//            val bitmap = bitmapFromMatrix(bitMatrix)
//
//            // QR 코드 이미지를 ImageView에 설정
//            val imageView = ImageView(this)
//            imageView.setImageBitmap(bitmap)
//            qrCodeLayout.addView(imageView)
//        }
//    }
//
//    private fun generateQRCode(data: String): BitMatrix {
//        val writer = QRCodeWriter()
//        return writer.encode(data, BarcodeFormat.QR_CODE, 216, 216)
//    }
//
//    private fun bitmapFromMatrix(matrix: BitMatrix): Bitmap {
//        val width = matrix.width
//        val height = matrix.height
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
//
//        for (x in 0 until width) {
//            for (y in 0 until height) {
//                bitmap.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
//            }
//        }
//        return bitmap
//    }
//}


class QRCodeActivity  :AppCompatActivity(){


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val issuePlanId = intent.getStringExtra("planId") ?: ""
        Log.d("chk", "$issuePlanId")
        setContentView(R.layout.activity_qr_code)
//        val planId = intent.getStringExtra("receiptPlanId") ?: ""
//        val receiptPlanCode = intent.getStringExtra("receiptPlanCode") ?: ""
//        val itemCount = intent.getIntExtra("itemCount", 0)
//        val date = intent.getStringExtra("date")?: "$today"
//        val pn = intent.getStringExtra("pn")?: ""

        val qrCodeLayout = findViewById<LinearLayout>(R.id.qrCodeLayout)

        if(issuePlanId !== ""){
            val pn = intent.getStringExtra("pn")?: ""
            SearchWithPn(pn, issuePlanId)
        } else{
            val planId = intent.getStringExtra("receiptPlanId") ?: ""
            val receiptPlanCode = intent.getStringExtra("receiptPlanCode") ?: ""
            val itemCount = intent.getIntExtra("itemCount", 0)
            val date = intent.getStringExtra("date")?: "$today"
            val pn = intent.getStringExtra("pn")?: ""
            // itemCount 만큼 QR 코드 생성
            for (i in 1..itemCount) {
                val qrData = "$planId-$i"
                Log.d("chk0","$qrData")
                InboundProducts(qrData, date)
            }
        }




    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun InboundProducts(outboundProductId: String, date: String) {

        val url = "https://api.mywareho.me/v1/storages/receipts/${outboundProductId}/items"

        val client = OkHttpClient.Builder()
            .cookieJar(MyCookieJar.INSTANCE)
            .build()
        val json = """{"selectedDate":"$date "}"""
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody) // POST 요청으로 변경
            .addHeader("Content-Type", "application/json") // JSON 형식 설정
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Error", "Request failed: ${e.message} $outboundProductId")
                runOnUiThread {
                    Toast.makeText(applicationContext, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("chk", "Response: ${response.body?.string()} $outboundProductId")

                    } else {
                        val jsonObject = response.body?.string()?.let { JSONObject(it) }
                        val message = jsonObject?.getString("message")
                        if (message != null) {
                            Log.d("Response", "$jsonObject $outboundProductId")
                        }
                    }
                }
            }
        })
    }

    private fun SearchWithPn(pn: String, issuePlanId : String) {
        val url = "https://api.mywareho.me/v1/storages/inventories/${pn}/details"
        Log.d("chk1","$pn $issuePlanId")
        val client = OkHttpClient.Builder()
            .cookieJar(MyCookieJar.INSTANCE)
            .build()

        val request = Request.Builder()
            .url(url)
            .get() // POST 요청으로 변경
            .addHeader("Content-Type", "application/json") // JSON 형식 설정
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Error", "Request failed: ${e.message} $pn")
                runOnUiThread {
                    Toast.makeText(applicationContext, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onResponse(call: okhttp3.Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        response.body?.string()?.let { responseBody ->
                            try {
                                val jsonObject = JSONObject(responseBody)
                                val contentArray = jsonObject.getJSONArray("content")
                                val itemIds = mutableListOf<Int>()

                                for (i in 0 until contentArray.length()) {
                                    val item = contentArray.getJSONObject(i)
                                    val itemId = item.getInt("itemId")
                                    itemIds.add(itemId)
                                }

                                Log.d("chk", "Extracted itemIds: $itemIds $issuePlanId")

                                // 각 itemId에 대해 새로운 API 요청 보내기
                                for (itemId in itemIds) {
                                    sendIssueRequest(itemId, "$issuePlanId") // planId를 1234로 예시 설정
                                }

                            } catch (e: JSONException) {
                                Log.e("JSONError", "Parsing error: ${e.message}")
                            }
                        }
                    } else {
                        response.body?.string()?.let {
                            Log.d("Response", "Failed response: $it $issuePlanId")
                        }
                    }
                }
            }
        })
    }
    private fun sendIssueRequest(itemId: Int, planId: String) {
        val url = "https://api.mywareho.me/v1/storages/issues/$itemId/items"

        val client = OkHttpClient.Builder()
            .cookieJar(MyCookieJar.INSTANCE)
            .build()

        val json = JSONObject().apply {
            put("issuePlanId", planId)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Error", "Issue request failed: ${e.message} (itemId: $itemId)")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("IssueResponse", "Success for itemId: $itemId")
                } else {
                    Log.d("IssueResponse", "Failed for itemId: $itemId, Response: ${response.body?.string()}")
                }
            }
        })
    }
}
