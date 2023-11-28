package com.example.navigationwalkers

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.TextView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class Frag1 : Fragment() {
    private var view: View? = null
    private val TAG = this.javaClass.simpleName

    private val clientId = "6l0j2YZC4VXejH4eq4Oo"
    private val clientSecret = "J5CNbC7dEe"

    private lateinit var searchView: SearchView
    private lateinit var resultListView: ListView
    private lateinit var resultAdapter: ArrayAdapter<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mapxFormatted: String? = null
    private var mapyFormatted: String? = null
    private lateinit var coordinatesMap: MutableMap<String, Pair<String, String>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.frag1, container, false)
        searchView = view?.findViewById(R.id.searchView) as SearchView
        val frameLayout2 = view?.findViewById<FrameLayout>(R.id.frameLayout2)
        resultListView = view?.findViewById(R.id.listView)!!
        searchView.setOnTouchListener { v, event ->
            searchView.onActionViewExpanded()
            frameLayout2?.visibility = View.VISIBLE
            false
        }
        searchView.isSubmitButtonEnabled = true

        resultAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList())
        resultListView.adapter = resultAdapter
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        resultListView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = resultAdapter.getItem(position) // 클릭한 아이템 텍스트 가져오기
            frameLayout2?.visibility = View.GONE
            val mapxFormatted = coordinatesMap[selectedItem]?.first ?: "0.0"
            val mapyFormatted = coordinatesMap[selectedItem]?.second ?: "0.0"


            val fragment = Return1()
            val bundle = Bundle()
            bundle.putString("selectedItem", selectedItem)
            bundle.putString("mapxFormatted", mapxFormatted)
            bundle.putString("mapyFormatted", mapyFormatted)

            fragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frame, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }





        // SearchView의 검색 이벤트 처리

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    getResultSearch(query)

                    resultListView.setBackgroundColor(resources.getColor(android.R.color.white))


                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        // 위치 서비스 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // 위치 정보 요청 및 표시
        requestLocation()

        return view
    }

    private fun requestLocation() {
        // 위치 권한 확인
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 위치 정보 요청
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latitude = location.latitude
                        val longitude = location.longitude
                    }
                }
        } else {
            // 위치 권한 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    // 위치 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 권한이 부여되면 위치 정보 요청
            requestLocation()
        }
    }
    private fun convertCoord(coord: String): String {
        // 넘어온 좌표값을 정수형으로 변환하고, 1E6 나 1E7로 나누어 실수형 좌표로 변환합니다.
        return (coord.toDouble() / 1E7).toString()
    }

    private fun getResultSearch(query: String) {
        val apiInterface = ApiClient.getInstance().create(ApiInterface::class.java)
        val call = apiInterface.getSearchResult(clientId, clientSecret, "local.json", query, 5)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful && response.body() != null) {
                    val resultJson = response.body()!!

                    try {
                        val jsonObject = JSONObject(resultJson)
                        val items = jsonObject.getJSONArray("items")

                        val resultList = ArrayList<String>()
                        coordinatesMap = mutableMapOf() // Initialize the map to store the coordinates

                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            val title = item.getString("title").replace(Regex("<[/]?b>"), "")
                            val mapx = convertCoord(item.getString("mapx"))
                            val mapy = convertCoord(item.getString("mapy"))


                            // Store the coordinates in the map with the title as the key
                            coordinatesMap[title] = Pair(mapx, mapy)

                            resultList.add(title)
                        }

                        resultAdapter.clear()
                        resultAdapter.addAll(resultList)
                    } catch (e: Exception) {
                        // 에러 처리
                    }
                } else {
                    // API 호출 실패 처리
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                // API 호출 에러 처리
            }
        })
    }





}
