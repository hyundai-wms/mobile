package com.myme.qrapp.ui.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.myme.qrapp.MyCookieJar
import com.myme.qrapp.QRCodeActivity
import com.myme.qrapp.R
import com.myme.qrapp.SharedViewModel
import com.myme.qrapp.databinding.FragmentDashboardBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: SharedViewModel
    private var receiptPlanCode : String = ""
    private var planId : String = ""
    private var planCount : String = ""
    private lateinit var root : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        StrictMode.enableDefaults()
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        root = binding.root

        var nowDataId = ""


        // LiveData 관찰
        sharedViewModel.qrCodeLiveData.observe(viewLifecycleOwner) { qrCodeData ->
            Log.d("chk","$qrCodeData")
            if(qrCodeData==""){
                binding.noInfoTextView.visibility = View.VISIBLE
                binding.infoLayout.visibility = View.GONE
                displayReceiptPlan(qrCodeData)
                binding.btnReturn.isEnabled = false
                binding.btnInbound.isEnabled = false
                binding.btnReturn.setTextColor(Color.parseColor("#888888")) // 회색 텍스트
                binding.btnReturn.setBackgroundColor(Color.parseColor("#E0E0E0")) // 회색 배경
                binding.btnReturn.alpha = 0.5f // 투명도 낮추기
                binding.btnInbound.alpha = 0.5f // 투명도 낮추기
            }else{
                binding.noInfoTextView.visibility = View.GONE
                binding.infoLayout.visibility = View.VISIBLE
                // UI 업데이트
                nowDataId = qrCodeData
                if(sharedViewModel.isInbound.value == false){
                    Log.d("chk", "QR Code Data Out: $qrCodeData")
                    sendRequestWithItemId(qrCodeData)
                    planId=qrCodeData
                } else{
                    val splitedQRCode = qrCodeData.split("-")
                    receiptPlanCode = splitedQRCode[0]
                    planId = splitedQRCode[1]
                    planCount = splitedQRCode[2]
                    Log.d("chk", "QR Code Data In: $nowDataId")
                    sendRequestWithPlanId(receiptPlanCode)
                }
            }
        }

        sharedViewModel.isInbound.observe(viewLifecycleOwner){ isInbound ->
            if(isInbound){
                binding.btnReturn.setOnClickListener {
                    returnProduct("$planId-$planCount")
                }
                binding.btnInbound.setOnClickListener {
                    InboundProduct("$planId-$planCount")
                }
            } else {
                binding.btnInbound.text = "출고"
                binding.btnReturn.text = "취소"
                binding.btnReturn.setOnClickListener {
                    //TODO: 취소 버튼
                    //returnProduct("$planId-$planCount")
                    sharedViewModel.qrCodeLiveData.value = ""
                }
                binding.btnInbound.setOnClickListener {

                    OutboundProduct("$planId")
                }
            }
        }

        return root
    }

    private fun sendRequestWithPlanId(outBoundProductId: String) {
        // 쿼리 파라미터로 selectedDate를 URL에 추가
        val url = "https://api.mywareho.me/v1/storages/receipts/plans?receiptPlanCode=$receiptPlanCode"

        // OkHttpClient에 CookieJar를 설정하여 쿠키를 관리
        val client = OkHttpClient.Builder()
            .cookieJar(MyCookieJar.INSTANCE)
            .build()

        val request = Request.Builder()
            .url(url) // 이미 쿼리 파라미터가 추가된 URL
            .get() // GET 방식으로 요청 (post를 사용하지 않고 쿼리 파라미터를 사용하면 GET 요청이 일반적)
            .addHeader("Content-Type", "application/json") // JSON 형식의 Content-Type 설정 (선택사항)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // 요청 실패 처리
                Log.e("Error", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        // 응답 처리
//                        Log.d("chk", "Response: ${response.body?.string()}")
                        response.body?.string()?.let { displayReceiptPlan(it) }
                    } else {
                        // 오류 처리
//                        Log.d("chk", "Error response: ${response.body?.string()}")
                        Toast.makeText(requireContext(), "Request failed: ${response.body?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun sendRequestWithItemId(itemId: String) {
        // 쿼리 파라미터로 selectedDate를 URL에 추가
        val url = "https://api.mywareho.me/v1/storages/inventories/items/${itemId}"

        // OkHttpClient에 CookieJar를 설정하여 쿠키를 관리
        val client = OkHttpClient.Builder()
            .cookieJar(MyCookieJar.INSTANCE)
            .build()

        val request = Request.Builder()
            .url(url) // 이미 쿼리 파라미터가 추가된 URL
            .get() // GET 방식으로 요청 (post를 사용하지 않고 쿼리 파라미터를 사용하면 GET 요청이 일반적)
            .addHeader("Content-Type", "application/json") // JSON 형식의 Content-Type 설정 (선택사항)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // 요청 실패 처리
                Log.e("Error", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        // 응답 처리
//                        Log.d("chk", "Response: ${response.body?.string()}")
                        response.body?.string()?.let { displayItem(it) }
                    } else {
                        // 오류 처리
                        Log.d("chk", "Error response: ${response.body?.string()}")
//                        Toast.makeText(requireContext(), "Request failed: ${response.body?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun InboundProduct(outBoundProductId: String) {
        val url = "https://api.mywareho.me/v1/storages/receipts/${outBoundProductId}/items"

        // 현재 날짜를 yyyy-MM-dd 형식으로 변환
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = dateFormat.format(Date())

        val json = """{"selectedDate":"$selectedDate"}"""
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val client = OkHttpClient.Builder()
            .cookieJar(MyCookieJar.INSTANCE)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody) // POST 요청으로 변경
            .addHeader("Content-Type", "application/json") // JSON 형식 설정
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Error", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
//                        val responseBody = response.body?.string()
                        val responseBody = response.body?.string()
                        Log.d("chk", "Response: $responseBody")
                        // 응답에서 itemId 추출
                        val jsonObject = JSONObject(responseBody)
                        val itemId = jsonObject.getString("itemId")
                        val bayNumber = jsonObject.getString("bayNumber")
                        // QR 코드 생성
                        generateQRCode(itemId, bayNumber)
                        Toast.makeText(requireContext(), "입고 완료했습니다.", Toast.LENGTH_SHORT).show()
                        sharedViewModel.setQrCodeValue("")
                    } else {
//                        Log.d("chk", "Error response: ${response.body?.string()}")
                        try {
                            val jsonObject = response.body?.string()?.let { JSONObject(it) }
                            val message = jsonObject?.getString("message")
                            if (message != null) {
                                Log.d("Response", message)
                            }  // message 값을 로그로 출력
                            Toast.makeText(requireContext(), "${message}", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("Error", "JSON 파싱 오류: ${e.message}")
                        }

                    }
                }
            }
        })
    }
    private fun returnProduct(outBoundProductId: String) {
        val url = "https://api.mywareho.me/v1/storages/receipts/${outBoundProductId}/returns"

        // 현재 날짜를 yyyy-MM-dd 형식으로 변환
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = dateFormat.format(Date())

        // JSON 바디 생성
        val json = """{"selectedDate":"$selectedDate"}"""
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val client = OkHttpClient.Builder()
            .cookieJar(MyCookieJar.INSTANCE)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody) // POST 요청으로 변경
            .addHeader("Content-Type", "application/json") // JSON 형식 설정
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Error", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "이미 처리된 상품입니다", Toast.LENGTH_SHORT).show()
                    sharedViewModel.setQrCodeValue("")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("chk", "Response: ${response.body?.string()}")
                        Toast.makeText(requireContext(), "반품되었습니다", Toast.LENGTH_SHORT).show()
                        sharedViewModel.setQrCodeValue("")
                    } else {
//                        Log.d("chk", "Error response: ${response.body?.string()}")
                        val jsonObject = response.body?.string()?.let { JSONObject(it) }
                        val message = jsonObject?.getString("message")
                        if (message != null) {
                            Log.d("Response", message)
                        }  // message 값을 로그로 출력
                        Toast.makeText(requireContext(), "${message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun OutboundProduct(itemId: String) {
        var issuePlanId = sharedViewModel.planId.value

        val url = "https://api.mywareho.me/v1/storages/issues/${itemId}/items"

        val client = OkHttpClient.Builder()
            .cookieJar(MyCookieJar.INSTANCE)
            .build()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = dateFormat.format(Date())
        val json = """{"issuePlanId":"$issuePlanId "}"""
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody) // POST 요청으로 변경
            .addHeader("Content-Type", "application/json") // JSON 형식 설정
            .build()
        Log.d("chk123", "$itemId $issuePlanId")
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Error", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("chk", "Response: ${response.body?.string()}")
                        Toast.makeText(requireContext(), "츨고되었습니다.", Toast.LENGTH_SHORT).show()
                        sharedViewModel.qrCodeLiveData.value = ""
                    } else {
                        val jsonObject = response.body?.string()?.let { JSONObject(it) }
                        val message = jsonObject?.getString("message")
                        if (message != null) {
                            Log.d("Response", message)
                            Toast.makeText(requireContext(), "${message}", Toast.LENGTH_SHORT).show()
                            sharedViewModel.qrCodeLiveData.value = ""
                        }
                    }
                }
            }
        })
    }

    private fun displayReceiptPlan(response: String) {
        Log.d("test","$response")
        if(response == ""){
            binding.OutboundPlanDate.text ="QR을 입력해주세요"

        } else{
            val jsonObject = JSONObject(response)
            val content = jsonObject.getJSONArray("content")

            val item = content.getJSONObject(0)
            val productNumber = item.getString("productNumber")
            val productName = item.getString("productName")
            val companyName = item.getString("companyName")
            val companyCode = item.getString("companyCode")
            val planDate = item.getString("receiptPlanDate")

            // 행 생성
            binding.OutboundPlanIdText.text = "$receiptPlanCode-$planCount"
            binding.OutboundPlanDate.text = planDate
            binding.OutboundPnText.text = productNumber
            binding.OutboundPnameText.text = productName
            binding.OutboundCompNameText.text = companyName
            binding.OutboundCompCodeText.text = companyCode
            binding.btnReturn.isEnabled = true
            binding.btnInbound.isEnabled = true
        }
    }

    private fun displayItem(response: String) {
        Log.d("test","$response")
        if(response == ""){
            binding.OutboundPlanDate.text ="QR을 입력해주세요"
        } else{
            val jsonObject = JSONObject(response)

            val productNumber = jsonObject.getString("productNumber")
            val productName = jsonObject.getString("productName")
            val companyName = jsonObject.getString("companyName")
            val companyCode = jsonObject.getString("companyCode")
            val bayNumber = jsonObject.getString("bayNumber")
            val issuePlanId = jsonObject.getString("issuePlanId")

            // 행 생성
            binding.OutboundPlanIdText.text = "$receiptPlanCode-$planCount"
            binding.OutboundPlanDate.text = issuePlanId
            binding.planCodeText.text = "출고 예정 코드"
            binding.planDateText.text = "출고 예정 일자"
            binding.OutboundPnText.text = productNumber
            binding.OutboundPnameText.text = productName
            binding.OutboundCompNameText.text = companyName
            binding.OutboundCompCodeText.text = companyCode
            binding.bayLayout.visibility = View.VISIBLE
            binding.bayNumber.text = bayNumber
            binding.btnReturn.isEnabled = true
            binding.btnInbound.isEnabled = true
        }


    }

    private fun generateQRCode(itemId: String, bayNumber : String) {
        try {
            // QR 코드 생성
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(itemId, BarcodeFormat.QR_CODE, 216, 216)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }

            // QR 코드 다이얼로그 표시
            val imageView = ImageView(requireContext())
            imageView.setImageBitmap(bmp)

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("납품위치-$bayNumber")
                .setView(imageView)
                .setPositiveButton("닫기") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialog.setCancelable(false)

            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))


        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "QR 생성 실패", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}