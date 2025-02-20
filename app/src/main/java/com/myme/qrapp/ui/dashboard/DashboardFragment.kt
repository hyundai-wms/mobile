package com.myme.qrapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        root = binding.root

        var nowDataId = ""

        binding.btnReturn.setOnClickListener {
            returnProduct("$planId-$planCount")
        }
        binding.btnInbound.setOnClickListener {
            InboundProduct("$planId-$planCount")
        }

        // LiveData 관찰
        sharedViewModel.qrCodeLiveData.observe(viewLifecycleOwner) { qrCodeData ->
            // UI 업데이트
            nowDataId = qrCodeData
            val splitedQRCode = qrCodeData.split("-")
            receiptPlanCode = splitedQRCode[0]
            planId = splitedQRCode[1]
            planCount = splitedQRCode[2]
            Log.d("chk", "QR Code Data: $nowDataId")
            sendRequestWithPlanId(receiptPlanCode)
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
                        //Log.d("chk", "Response: ${response.body?.string()}")
                        response.body?.string()?.let { displayReceiptPlan(it) }
                        Toast.makeText(requireContext(), "Request successful", Toast.LENGTH_SHORT).show()
                    } else {
                        // 오류 처리
                        Log.d("chk", "Error response: ${response.body?.string()}")
                        Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
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
                        Log.d("chk", "Response: ${response.body?.string()}")
                        Toast.makeText(requireContext(), "Request successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("chk", "Error response: ${response.body?.string()}")
                        Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("chk", "Response: ${response.body?.string()}")
                        Toast.makeText(requireContext(), "Request successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("chk", "Error response: ${response.body?.string()}")
                        Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun displayReceiptPlan(response: String) {
        val jsonObject = JSONObject(response)
        val content = jsonObject.getJSONArray("content")


        val item = content.getJSONObject(0)
//        val receiptPlanId = item.getString("receiptPlanId")
        val productNumber = item.getString("productNumber")
        val productName = item.getString("productName")
        val companyName = item.getString("companyName")
        val companyCode = item.getString("companyCode")
        val planDate = item.getString("receiptPlanDate")

        // 행 생성
        val tableRow = TableRow(requireContext())
        binding.OutboundPlanIdText.text = "$receiptPlanCode-$planCount"
        binding.OutboundPlanDate.text = planDate
        binding.OutboundPnText.text = productNumber
        binding.OutboundPnameText.text = productName
        binding.OutboundCompNameText.text = companyName
        binding.OutboundCompCodeText.text = companyCode

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}