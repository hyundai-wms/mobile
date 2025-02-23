package com.myme.qrapp.ui.notifications

import android.app.AlertDialog
import com.myme.qrapp.QRCodeActivity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myme.qrapp.SharedViewModel
import com.myme.qrapp.databinding.FragmentNotificationsBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class NotificationsFragment : Fragment() {

    private lateinit var selectedDate: String
    private lateinit var selectDateButton: TextView
    private lateinit var selectedDateTextView: TextView
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<ProductItem>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root = binding.root

        selectDateButton = binding.selectDateButton
        selectedDateTextView = binding.selectedDateTextView

        productAdapter = ProductAdapter(productList, sharedViewModel) { planId ->
            showConfirmationDialog(planId)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }

        selectDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        if (!sharedViewModel.isInbound.value!!) {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//            val url = "https://api.mywareho.me/v1/storages/issues/plans?issuePlanStartDate=$today&issuePlanEndDate=$today"
            val url ="https://api.mywareho.me/v1/storages/issues/today"
            Log.d("chk","$today, $url")
            selectedDateTextView.text = "선택된 날짜: $today"
            sendRequestWithSelectedDate(url)
        }

        return root
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                selectedDateTextView.text = "선택된 날짜: $selectedDate"
                val url = if (sharedViewModel.isInbound.value == true)
                    "https://api.mywareho.me/v1/storages/receipts/plans?receiptPlanStartDate=$selectedDate&receiptPlanEndDate=$selectedDate"
                else
                    "https://api.mywareho.me/v1/storages/issues/plans?issuePlanStartDate=$selectedDate&issuePlanEndDate=$selectedDate"
//                val url = if (sharedViewModel.isInbound.value == true) "https://api.mywareho.me/v1/storages/receipts/today" else "https://api.mywareho.me/v1/storages/issues/today"
                sendRequestWithSelectedDate(url)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun sendRequestWithSelectedDate(url: String) {
        val client = OkHttpClient.Builder().cookieJar(MyCookieJar.INSTANCE).build()
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Error", "Request failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                requireActivity().runOnUiThread {
                    response.body?.string()?.let { if(url.endsWith("today")){parseResponse2(it)} else{
                        parseResponse(it)
                    } }
                }
            }
        })
    }

    private fun parseResponse(response: String) {
        val jsonObject = JSONObject(response)
        val content = jsonObject.getJSONArray("content")
        productList.clear()
        for (i in 0 until content.length()) {
            val item = content.getJSONObject(i)

            val planId = if (sharedViewModel.isInbound.value == true) item.getString("receiptPlanId") else item.getString("issuePlanId")
            Log.d("chk","$item $planId")

                productList.add(
                    ProductItem(
                        receiptPlanCode = if (sharedViewModel.isInbound.value == true) item.getString("receiptPlanCode") else item.getString("issuePlanCode"),
                        productNumber = item.getString("productNumber"),
                        productName = item.getString("productName"),
                        itemCount = item.getInt("itemCount"),
                        planId = planId
                    )
                )
        }
        if(productList.size!=0){
            binding.recyclerView.visibility = View.VISIBLE
            binding.nonePlanText.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.nonePlanText.visibility = View.VISIBLE
        }
        binding.topLayout.visibility = View.VISIBLE
        productAdapter.notifyDataSetChanged()
    }
    private fun parseResponse2(response: String) {
        val jsonObject = JSONObject(response)
        val content = jsonObject.getJSONArray("content")
        productList.clear()
        for (i in 0 until content.length()) {
            val item = content.getJSONObject(i)

            val planId = if (sharedViewModel.isInbound.value == true) item.getString("receiptPlanId") else item.getString("issuePlanId")
            Log.d("chk","$item $planId")
            if(item.getString("issueStatus") != "DONE"){
                productList.add(
                    ProductItem(
                        receiptPlanCode = if (sharedViewModel.isInbound.value == true) item.getString("receiptPlanCode") else item.getString("issuePlanCode"),
                        productNumber = item.getString("productNumber"),
                        productName = item.getString("productName"),
                        itemCount = item.getInt("totalItemCount"),
                        planId = planId
                    )
                )
            }


        }
        if(productList.size!=0){
            binding.recyclerView.visibility = View.VISIBLE
            binding.nonePlanText.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.nonePlanText.visibility = View.VISIBLE
        }
        binding.topLayout.visibility = View.VISIBLE
        productAdapter.notifyDataSetChanged()
    }

    private fun showConfirmationDialog(planId: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("출고 진행")
            .setMessage("출고를 진행하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                sharedViewModel.setPlanId(planId)
                findNavController().navigate(R.id.action_navigation_notifications_to_navigation_home)
            }
            .setNegativeButton("취소", null)
            .create()



        dialog.show()
        // Positive button 색상 설정
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))

        // Negative button 색상 설정
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
    }
}

class ProductAdapter(
    private val products: List<ProductItem>,
    private val viewModel: SharedViewModel,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productNumber: TextView = view.findViewById(R.id.tvProductNumber)
        val productName: TextView = view.findViewById(R.id.tvProductName)
        val itemCount: TextView = view.findViewById(R.id.tvItemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productNumber.text = product.productNumber
        holder.productName.text = product.productName
        holder.itemCount.text = product.itemCount.toString()

        holder.itemView.setOnClickListener {
            // sharedViewModel의 isInbound 값 확인
            if (viewModel.isInbound.value == true) {
                val intent = Intent(holder.itemView.context, QRCodeActivity::class.java)
                intent.putExtra("receiptPlanCode", product.receiptPlanCode) // receiptPlanCode는 예시로, 실제 필요한 값을 넣어주세요
                intent.putExtra("itemCount", product.itemCount)
                intent.putExtra("receiptPlanId", product.planId)
                holder.itemView.context.startActivity(intent)
            } else {
                // isInbound가 false일 경우 다른 처리 필요하면 여기에 추가
                onItemClick(product.planId)
            }
        }
    }

    override fun getItemCount(): Int = products.size
}

data class ProductItem(
    val productNumber: String,
    val productName: String,
    val itemCount: Int,
    val planId: String,
    val receiptPlanCode: String
)

