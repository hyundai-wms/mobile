package com.myme.qrapp.ui.notifications

import com.myme.qrapp.QRCodeActivity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.myme.qrapp.MyCookieJar
import com.myme.qrapp.R
//import com.myme.qrapp.ui.com.myme.qrapp.QRCodeActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*
import android.widget.TableRow
import androidx.lifecycle.ViewModelProvider
import com.myme.qrapp.SharedViewModel


class NotificationsFragment : Fragment() {

    private lateinit var selectedDate: String
    private lateinit var selectDateButton: Button
    private lateinit var selectedDateTextView: TextView
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var root : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_notifications, container, false)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        selectDateButton = root.findViewById(R.id.selectDateButton)
        selectedDateTextView = root.findViewById(R.id.selectedDateTextView)
        StrictMode.enableDefaults()
        selectDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        return root
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                selectedDate = formattedDate
                selectedDateTextView.text = "Selected Date: $formattedDate"
                if(sharedViewModel.isInbound.value == false){
                    val url = "https://api.mywareho.me/v1/storages/issues/plans?issuePlanStartDate=$selectedDate&issuePlanEndDate=$selectedDate"
                    sendRequestWithSelectedDate(url)
                }else{
                    val url = "https://api.mywareho.me/v1/storages/receipts/plans?receiptPlanStartDate=$selectedDate&receiptPlanEndDate=$selectedDate"
                    sendRequestWithSelectedDate(url)
                }

            },
            year, month, dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun sendRequestWithSelectedDate(url : String) {

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

            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        // 응답 처리
//                        Log.d("chk", "Response: ${response.body?.string()}")
                        if(sharedViewModel.isInbound.value == true){
                            response.body?.string()?.let { displayReceiptPlans(it) }
                        } else{
                            response.body?.string()?.let { displayReceiptPlans(it) }
                        }


                    } else {
                        // 오류 처리
                        Log.d("chk", "Error response: ${response.body?.string()}")
                    }
                }
            }
        })
    }

    private fun displayReceiptPlans(response: String) {
        val jsonObject = JSONObject(response)
        val content = jsonObject.getJSONArray("content")

        // TableLayout에 데이터를 동적으로 추가
        val tableLayout = root.findViewById<TableLayout>(R.id.tableLayout)
        tableLayout.removeAllViews()  // 기존 뷰 제거

        for (i in 0 until content.length()) {
            val item = content.getJSONObject(i)
            if(sharedViewModel.isInbound.value==true){
                val receiptPlanCode = item.getString("receiptPlanCode")
                val receiptPlanId = item.getString("receiptPlanId")
                val productNumber = item.getString("productNumber")
                val productName = item.getString("productName")
                val itemCount = item.getInt("itemCount")

                // 행 생성
                val tableRow = TableRow(requireContext())

                // 각 셀에 텍스트 추가
                val productNumberTextView = TextView(requireContext()).apply { text = productNumber }
                val productNameTextView = TextView(requireContext()).apply { text = productName }
                val itemCountTextView = TextView(requireContext()).apply { text = itemCount.toString() }

                // 클릭 이벤트 추가: 셀 클릭 시 QR 코드 생성 화면으로 이동
//            if(sharedViewModel.isInbound.value == true) {
//
//            }
                tableRow.setOnClickListener {
                    val intent = Intent(requireContext(), QRCodeActivity::class.java)
                    intent.putExtra("receiptPlanCode", receiptPlanCode)
                    intent.putExtra("itemCount", itemCount)
                    intent.putExtra("receiptPlanId",receiptPlanId)
                    startActivity(intent)
                }

                tableRow.addView(productNumberTextView)
                tableRow.addView(productNameTextView)
                tableRow.addView(itemCountTextView)
                tableLayout.addView(tableRow)
            } else{
                val issuePlanCode = item.getString("issuePlanCode")
                val issuePlanId = item.getString("issuePlanId")
                val productNumber = item.getString("productNumber")
                val productName = item.getString("productName")
                val itemCount = item.getInt("itemCount")

                // 행 생성
                val tableRow = TableRow(requireContext())

                // 각 셀에 텍스트 추가
                val productNumberTextView = TextView(requireContext()).apply { text = productNumber }
                val productNameTextView = TextView(requireContext()).apply { text = productName }
                val itemCountTextView = TextView(requireContext()).apply { text = itemCount.toString() }


                tableRow.addView(productNumberTextView)
                tableRow.addView(productNameTextView)
                tableRow.addView(itemCountTextView)
                tableLayout.addView(tableRow)
            }

        }
    }
}
