package com.example.navigationwalkers

import kotlinx.coroutines.*
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.UiSettings
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

class Return1 : Fragment() {
    private var selectedItem: String? = null
    private var mapxFormatted: String? = null
    private var mapyFormatted: String? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private var selectedIndex: Int = 0
    private val pathOverlays = mutableListOf<PathOverlay>()
    private lateinit var shortestButton: Button
    private lateinit var mainStreetButton: Button
    private lateinit var safeRouteButton: Button
    private lateinit var optimalButton: Button
    private lateinit var guidanceButton: Button
    private lateinit var directionButton: Button
    private lateinit var locationSource: FusedLocationSource
    private lateinit var currentLocationMarker: Marker
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val markers = mutableListOf<Marker>()
    private lateinit var tts: TextToSpeech
    private val leftTurnCoords = mutableSetOf<LatLng>()
    private val rightTurnCoords = mutableSetOf<LatLng>()
    private val straightCoords = mutableSetOf<LatLng>()
    private lateinit var locationCallback: LocationCallback
    private val guidedCoords = HashSet<LatLng>()
    private val guidedCoords0m = HashSet<LatLng>()
    private val guidedCoords10m = HashSet<LatLng>()
    private val guidedCoords30m = HashSet<LatLng>()
    private val passedCoords = HashSet<LatLng>()
    private lateinit var turnTextView: TextView
    private val routeColors = listOf(
        Color.argb(0, 0, 0, 0),    // 0번 경로 색상 (빨간색)
        Color.argb(0, 0, 0, 0),   // 1번 경로 색상 (파란색)
        Color.YELLOW,
        Color.argb(0, 0, 0, 0),
        Color.argb(0, 0, 0, 0)// 4번 경로 색상 (초록색)
    )
    internal var isGuidanceRunning = false
    private var isGuidingLeft30m = false
    private var isGuidingRight30m = false
    private var isGuidingLeft100m = false
    private var isGuidingRight100m = false
    private var isGuiding0m = false
    private val toleranceDistance = 150.0
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var isOffRoute = false












    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedItem = it.getString("selectedItem")
            mapxFormatted = it.getString("mapxFormatted")
            mapyFormatted = it.getString("mapyFormatted")
            currentLatitude = it.getDouble("currentLatitude")
            currentLongitude = it.getDouble("currentLongitude")
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            currentLocationMarker = Marker()
            locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)


        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (isGuidanceRunning) {
                    val location = locationResult.lastLocation

                    location?.let {
                        // 위치 업데이트 처리

                        val isOffRoute = isOffRoute(it)
                        if (isOffRoute) {
                            Log.d("isOffRoute", isOffRoute.toString())
                            recomputeRoute(it.latitude, it.longitude)
                        }
                        speakOnApproachingLeftTurn10(it)
                        speakOnApproachingRightTurn10(it)
                        speakOnApproachingStraight(it)
                        speakOnApproachingStraight2(it)
                        onLocationChanged(it)
                    }
                }
            }
        }
        startLocationUpdates()
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.KOREA)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                }
            } else {
            }
        }
    }
    






    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.return1, container, false)

        shortestButton = view.findViewById(R.id.shortest)
        mainStreetButton = view.findViewById<Button>(R.id.mainstreet)
        safeRouteButton = view.findViewById<Button>(R.id.saferoute)
        optimalButton = view.findViewById<Button>(R.id.optimal)
        directionButton = view.findViewById<Button>(R.id.direction)
        turnTextView = view.findViewById(R.id.turn)
        turnTextView.visibility = View.GONE
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)




        guidanceButton = view.findViewById<Button>(R.id.guidance)
        guidanceButton.visibility = View.GONE
        guidanceButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 위치 정보 요청
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val currentLatitude = location.latitude
                            val currentLongitude = location.longitude

                            startGuidance(currentLatitude, currentLongitude)
                        }
                    }
            } else {
                // 위치 권한 요청
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }



        // 지도 초기화
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)

// 지도를 비동기적으로 로드하고 naverMap을 설정
        mapView.getMapAsync { nMap ->
            naverMap = nMap
            naverMap.setContentPadding(0, 50, 0, 120)

            naverMap.locationSource = locationSource
            val uiSettings: UiSettings = naverMap.uiSettings

            // 현재 위치 버튼 활성화
            uiSettings.isLocationButtonEnabled = true

            // 현재 위치 정보 가져오기
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 위치 정보 요청
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            currentLatitude = location.latitude
                            currentLongitude = location.longitude

                            val latLongArray = parseLatLong(mapyFormatted,mapxFormatted)
                            if (latLongArray != null) {
                                val latitude = latLongArray[0]
                                val longitude = latLongArray[1]
                                Log.d("Latitude", "Latitude: $latitude")
                                Log.d("Longitude", "Longitude: $longitude")


                                // 현재 위치를 Naver 지도에 표시
                                val destinationLatitude = longitude
                                val destinationLongitude = latitude
                                showDestinationOnlyOnMap(
                                    naverMap,
                                    destinationLatitude,
                                    destinationLongitude
                                )
                                showDestinationOnMap(
                                    naverMap,
                                    destinationLatitude,
                                    destinationLongitude
                                )

                                // 서버 요청 전송

                            }
                        }
                    }
            } else {
                // 위치 권한 요청
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }

        shortestButton.setOnClickListener {
            selectedIndex = 0
            clearAndRedrawRoute(0)

            // shortestButton의 배경색을 회색으로 변경
            shortestButton.setBackgroundColor(Color.LTGRAY)

            // 다른 버튼의 배경색을 원래 색상으로 재설정
            mainStreetButton.setBackgroundColor(Color.WHITE)
            safeRouteButton.setBackgroundColor(Color.WHITE)
            optimalButton.setBackgroundColor(Color.WHITE)
        }

        mainStreetButton.setOnClickListener {
            selectedIndex = 1
            clearAndRedrawRoute(1)

            // mainStreetButton의 배경색을 회색으로 변경
            mainStreetButton.setBackgroundColor(Color.LTGRAY)

            // 다른 버튼의 배경색을 원래 색상으로 재설정
            shortestButton.setBackgroundColor(Color.WHITE)
            safeRouteButton.setBackgroundColor(Color.WHITE)
            optimalButton.setBackgroundColor(Color.WHITE)
        }

        safeRouteButton.setOnClickListener {
            selectedIndex = 2
            clearAndRedrawRoute(2)

            // safeRouteButton의 배경색을 회색으로 변경
            safeRouteButton.setBackgroundColor(Color.LTGRAY)

            // 다른 버튼의 배경색을 원래 색상으로 재설정
            shortestButton.setBackgroundColor(Color.WHITE)
            mainStreetButton.setBackgroundColor(Color.WHITE)
            optimalButton.setBackgroundColor(Color.WHITE)
        }

        optimalButton.setOnClickListener {
            selectedIndex = 3
            clearAndRedrawRoute(4)

            // optimalButton의 배경색을 회색으로 변경
            optimalButton.setBackgroundColor(Color.LTGRAY)

            // 다른 버튼의 배경색을 원래 색상으로 재설정
            shortestButton.setBackgroundColor(Color.WHITE)
            mainStreetButton.setBackgroundColor(Color.WHITE)
            safeRouteButton.setBackgroundColor(Color.WHITE)
        }
        directionButton.setOnClickListener {
            // 서버와 통신하여 방향 정보 가져오기
            shortestButton.setBackgroundColor(Color.LTGRAY)

            sendRequestToServer()
            guidanceButton.visibility = View.VISIBLE
            directionButton.visibility = View.GONE
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 위치 정보 요청
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            currentLatitude = location.latitude
                            currentLongitude = location.longitude

                            val latLongArray = parseLatLong(mapyFormatted,mapxFormatted)
                            if (latLongArray != null) {
                                val latitude = latLongArray[0]
                                val longitude = latLongArray[1]
                                Log.d("Latitude", "Latitude: $latitude")
                                Log.d("Longitude", "Longitude: $longitude")

                                // 현재 위치를 Naver 지도에 표시
                                val destinationLatitude = longitude
                                val destinationLongitude = latitude
                                showCurrentLocationOnMap(
                                    naverMap,
                                    currentLatitude,
                                    currentLongitude,
                                    destinationLatitude,
                                    destinationLongitude
                                )
                                showDestinationOnMap(
                                    naverMap,
                                    destinationLatitude,
                                    destinationLongitude
                                )
                            }
                        }
                    }
            } else {
                // 위치 권한 요청
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }

        return view
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 업데이트 간격 (10초)
        }

        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(requireContext(), permission)

        if (granted) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            // 위치 권한을 요청하는 로직 추가
        }
    }
    private fun showDestinationOnlyOnMap(
        naverMap: NaverMap,
        destinationLatitude: Double,
        destinationLongitude: Double
    ) {
        val destinationLocation = LatLng(destinationLatitude, destinationLongitude)

        // 이전에 생성된 마커를 지우기
        if (::currentLocationMarker.isInitialized) {
            currentLocationMarker.map = null
        }

        // 목적지를 표시할 마커 생성
        val destinationMarker = Marker()
        destinationMarker.position = destinationLocation
        destinationMarker.map = naverMap
        destinationMarker.iconTintColor = Color.RED

        // 지도를 목적지 위치로 이동
        val cameraUpdate = CameraUpdate.scrollTo(destinationLocation)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun showCurrentLocationOnMap(naverMap: NaverMap,
                                         currentLatitude: Double,
                                         currentLongitude: Double,
                                         destinationLatitude: Double,
                                         destinationLongitude: Double) {
        val currentLocation = LatLng(currentLatitude, currentLongitude)
        val destinationLocation = LatLng(destinationLatitude, destinationLongitude)

        // 이전에 생성된 마커를 지우기
        if (::currentLocationMarker.isInitialized) {
            currentLocationMarker.map = null
        }

        // 현재 위치를 표시할 마커 생성
        currentLocationMarker = Marker()
        currentLocationMarker.position = currentLocation
        currentLocationMarker.map = naverMap

        // 목적지를 표시할 마커 생성
        val destinationMarker = Marker()
        destinationMarker.position = destinationLocation
        destinationMarker.map = naverMap
        destinationMarker.iconTintColor = Color.RED

        // 현재 위치와 목적지 중간 지점 계산
        val middleLatitude = (currentLatitude + destinationLatitude) / 2
        val middleLongitude = (currentLongitude + destinationLongitude) / 2
        val middleLocation = LatLng(middleLatitude, middleLongitude)

        // 지도를 현재 위치와 목적지 중간 지점으로 이동
        val cameraUpdate = CameraUpdate.scrollTo(middleLocation)
        naverMap.moveCamera(cameraUpdate)

    }
    private fun showDestinationOnMap(naverMap: NaverMap, latitude: Double, longitude: Double) {
        val destinationLocation = LatLng(latitude, longitude)

        // 목적지를 표시할 마커 생성
        val destinationMarker = Marker()
        destinationMarker.position = destinationLocation
        destinationMarker.map = naverMap
        destinationMarker.iconTintColor = Color.RED
    }



    private fun parseLatLong(mapxFormatted: String?, mapyFormatted: String?): DoubleArray? {
        return try {
            val latitude = mapyFormatted?.toDouble() ?: 0.0
            val longitude = mapxFormatted?.toDouble() ?: 0.0
            Log.d("parseLatLong", "Latitude: $latitude, Longitude: $longitude")
            doubleArrayOf(latitude, longitude)
        } catch (e: NumberFormatException) {
            null
        }
    }
    private var routeData: MutableList<List<List<Double>>>? = null



    private fun sendRequestToServer() {
        val latLongPair = parseLatLong(mapyFormatted,mapxFormatted)

        if (latLongPair != null) {
            val (latitude, longitude) = latLongPair

            val requestData = RequestData(
                LocationData(currentLongitude, currentLatitude),
                LocationData(latitude, longitude)
            )

            val apiService = RetrofitInstance.getRetrofitInstance().create(ApiService::class.java)
            val call = apiService.getRoute(requestData)

            call.enqueue(object : Callback<List<ServerResponse>> {
                override fun onResponse(call: Call<List<ServerResponse>>, response: Response<List<ServerResponse>>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()

                        if (responseData != null && responseData.isNotEmpty()) {
                            routeData = mutableListOf()

                            for (serverResponse in responseData) {
                                val coordinatesList = mutableListOf<List<Double>>()

                                for (feature in serverResponse.features) {
                                    val geometry = feature.geometry
                                    if (geometry.type == "LineString") {
                                        val coordinates = geometry.coordinates as List<List<Double>>
                                        coordinatesList.addAll(coordinates)
                                    }
                                }

                                routeData?.add(coordinatesList)
                            }

                            // 경로 데이터를 저장한 후, 다른 함수에서 그릴 수 있습니다.
                            clearAndRedrawRoute(0)
                            val tempRouteData = routeData
                            val distances = if (tempRouteData != null) {
                                calculatePathDistanceForRoutes(tempRouteData)
                            } else {
                                listOf(0.0, 0.0, 0.0)
                            }

                            val averageWalkingSpeed = 5.0 // km/h, 평균 이동 속도

                            val estimatedTimeForShortest = calculateEstimatedTime(distances[0], averageWalkingSpeed)
                            shortestButton.text = "큰길우선\n\n${String.format("%.2f km", distances[0])}\n\n $estimatedTimeForShortest"

                            val estimatedTimeForMainStreet = calculateEstimatedTime(distances[1], averageWalkingSpeed)
                            mainStreetButton.text = "최단거리\n\n${String.format("%.2f km", distances[1])} \n\n $estimatedTimeForMainStreet"

                            val estimatedTimeForSafeRoute = calculateEstimatedTime(distances[2], averageWalkingSpeed)
                            safeRouteButton.text = "안심거리\n\n${String.format("%.2f km", distances[2])} \n\n $estimatedTimeForSafeRoute"

                            val estimatedTimeForShortest2 = calculateEstimatedTime(distances[4], averageWalkingSpeed)
                            optimalButton.text = "계산최단거리\n\n${String.format("%.2f km", distances[4])} \n\n $estimatedTimeForShortest2"
                        }
                    } else {
                        // 서버에서 오류 응답을 받은 경우 처리
                        Log.e("ResponseError", "Server error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<ServerResponse>>?, t: Throwable?) {
                    // 서버 통신 실패 처리
                    Log.e("RequestFailure", "Request failed: ${t?.message}")
                }
            })
        }
    }

    private fun startGuidance(currentLatitude: Double, currentLongitude: Double) {
        val startGuidanceMessage = "경로 안내를 시작합니다."

        clearAndRedrawRoute2(currentLatitude, currentLongitude)
        shortestButton.visibility = View.GONE
        mainStreetButton.visibility = View.GONE
        safeRouteButton.visibility = View.GONE
        optimalButton.visibility = View.GONE
        guidanceButton.visibility = View.GONE
        turnTextView.visibility = View.VISIBLE

        if (::currentLocationMarker.isInitialized) {
            currentLocationMarker.map = null
        }

        val myLocation = LatLng(currentLatitude, currentLongitude)
        naverMap.moveCamera(CameraUpdate.toCameraPosition(CameraPosition(myLocation, 18.0)))

        val cameraUpdate = CameraUpdate.scrollTo(myLocation).animate(CameraAnimation.Linear)
        naverMap.moveCamera(cameraUpdate)

        speak(startGuidanceMessage) {
            // 이 블록은 "경로 안내를 시작합니다." 메시지가 완료된 후에 실행됩니다.

            isGuidanceRunning = true


        }
    }

    private fun speak(text: String, onComplete: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "GuidanceMessage")
            tts.setOnUtteranceCompletedListener { utteranceId ->
                if (utteranceId == "GuidanceMessage") {
                    onComplete()
                }
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "GuidanceMessage")
        } else {
            val map = HashMap<String, String>()
            map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "GuidanceMessage"
            tts.setOnUtteranceCompletedListener { utteranceId ->
                if (utteranceId == "GuidanceMessage") {
                    onComplete()
                }
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, map)
        }
    }


    private fun clearAndRedrawRoute(routeIndex: Int) {
        val routeData = routeData // 경로 데이터를 임시 변수에 복사합니다.

        if (routeData != null && routeIndex < routeData.size) {
            // 이전에 추가된 모든 경로와 마커 제거
            for (overlay in pathOverlays) {
                overlay.map = null
            }

            // 이전에 추가된 모든 마커 제거
            for (marker in markers) {
                marker.map = null
            }
            markers.clear()

            // 모든 경로 그리기
            for (i in 0 until min(routeData.size, routeColors.size)) {
                val coordinatesList = routeData[i]
                val pathCoords = coordinatesList.map { LatLng(it[1], it[0]) }

                val path = PathOverlay()
                path.coords = pathCoords

                // 경로 색상 설정
                path.color = routeColors[i] ?: Color.BLACK
                //선택한 경로 제외 회색으로
                /*if (i == 3) {
                    path.color = Color.argb(0, 0, 0, 0)
                } else {
                    path.color = Color.GRAY
                }*/

                path.width = 20

                // 지도에 해당 경로 추가
                path.map = naverMap
                pathOverlays.add(path)
            }

            // 선택한 경로만 한 번 더 그리기
            if (routeIndex < routeData.size) {
                val selectedCoordinatesList = routeData[routeIndex]
                val selectedPathCoords = selectedCoordinatesList.map { LatLng(it[1], it[0]) }

                val selectedPath = PathOverlay()
                selectedPath.coords = selectedPathCoords
                selectedPath.color = routeColors.getOrNull(routeIndex) ?: Color.BLACK // 선택한 경로의 색상
                selectedPath.width = 20

                // 지도에 선택한 경로 추가
                selectedPath.map = naverMap
                pathOverlays.add(selectedPath)

                // 4번째 경로에 마커 추가
                if (routeIndex == 2) {
                    if (routeData.size > 3) {
                        for (coords in routeData[3]) {
                            val marker = Marker()
                            marker.position = LatLng(coords[1], coords[0])
                            marker.width = 70
                            marker.height = 70
                            marker.map = naverMap
                            marker.icon = MarkerIcons.BLACK
                            marker.iconTintColor = Color.CYAN
                            markers.add(marker)
                        }
                    }
                }
            }
        } else {
            // 경로 데이터가 null이거나 경로 인덱스가 범위를 벗어날 경우 처리
        }
    }
    private fun clearAndRedrawRoute2(currentLatitude: Double, currentLongitude: Double) {
        val routeData = routeData // 경로 데이터를 임시 변수에 복사합니다.

        if (routeData != null && selectedIndex < routeData.size) {
            for (path in pathOverlays) {
                path.map = null
            }

            // 선택한 경로만 그리기
            val selectedCoordinatesList = routeData[selectedIndex]
            val selectedPathCoords = selectedCoordinatesList.map { LatLng(it[1], it[0]) }.toMutableList()

            // 현재 위치 좌표 추가
            selectedPathCoords.add(0, LatLng(currentLatitude, currentLongitude))

            val selectedPath = PathOverlay()
            selectedPath.coords = selectedPathCoords
            selectedPath.color = Color.GREEN
            selectedPath.width = 20

            // 지도에 선택한 경로 추가
            selectedPath.map = naverMap // 이 부분이 올바르게 설정되어야 합니다.
            pathOverlays.add(selectedPath)
            detectTurnDirection(selectedPathCoords)
        } else {
            // 경로 데이터가 null이거나 경로 인덱스가 범위를 벗어날 경우 처리
        }
    }

    private fun speak(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
        } else {
            val map = HashMap<String, String>()
            map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MessageId"
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, map)
        }
    }

    private fun detectTurnDirection(selectedPathCoords: List<LatLng>) {
        if (selectedPathCoords.size < 3) {
            // 경로 좌표가 충분하지 않을 때 처리
            return
        }


        for (i in 1 until selectedPathCoords.size - 1) {
            val previousCoord = selectedPathCoords[i - 1]
            val currentCoord = selectedPathCoords[i]
            val nextCoord = selectedPathCoords[i + 1]

            val previousLocation = Location("Previous")
            previousLocation.latitude = previousCoord.latitude
            previousLocation.longitude = previousCoord.longitude

            val currentLocation = Location("Current")
            currentLocation.latitude = currentCoord.latitude
            currentLocation.longitude = currentCoord.longitude

            val nextLocation = Location("Next")
            nextLocation.latitude = nextCoord.latitude
            nextLocation.longitude = nextCoord.longitude

            val bearing1 = previousLocation.bearingTo(currentLocation)
            val bearing2 = currentLocation.bearingTo(nextLocation)


            var angle = bearing2 - bearing1
            if (angle < -180) {
                angle += 360
            } else if (angle > 180) {
                angle -= 360
            }
            val distanceToNextTurn = calculateDistance(currentLocation, nextCoord)

            if (angle > 30) {
                rightTurnCoords.add(currentCoord)
                println("우회전: $currentCoord,다음 회전까지 거리: $distanceToNextTurn")
            } else if (angle < -30) {
                leftTurnCoords.add(currentCoord)
                println("좌회전: $currentCoord, 다음 회전까지 거리: $distanceToNextTurn")
            } else {
                straightCoords.add(currentCoord)
                println("직진: $currentCoord, 다음 회전까지 거리: $distanceToNextTurn")
            }
        }
    }



    // 현재 위치와 회전 좌표 사이의 거리 계산 함수
    fun calculateDistance(currentLocation: Location, turnCoord: LatLng): Float {
        val turnLocation = Location("Turn")
        turnLocation.latitude = turnCoord.latitude
        turnLocation.longitude = turnCoord.longitude
        return currentLocation.distanceTo(turnLocation)
    }


    val turnThreshold0 = 5.0
    val turnThreshold20 = 20.0
    val guidedTurnCoords = mutableSetOf<LatLng>()
    private val turnThreshold = 5.0f

    // 회전 지점 안내 텍스트를 업데이트하는 함수
    fun updateTurnGuidanceText(currentLocation: Location) {
        // 가장 가까운 회전 지점을 찾기 전에 지나친 회전 지점을 제외
        val upcomingTurns = (leftTurnCoords + rightTurnCoords).filter { turnCoord ->
            !guidedTurnCoords.contains(turnCoord)
        }

        var minDistance = Float.MAX_VALUE
        var closestTurn: LatLng? = null
        for (turnCoord in upcomingTurns) {
            val distance = calculateDistance(currentLocation, turnCoord)
            if (distance < minDistance) {
                minDistance = distance
                closestTurn = turnCoord
            }
        }

        // 가장 가까운 회전 지점에 대한 안내 메시지를 생성
        closestTurn?.let {
            val directionResource = when {
                leftTurnCoords.contains(it) && minDistance < 30 -> R.drawable.left // 30m 이하 남았을 때 좌회전 이미지
                rightTurnCoords.contains(it) && minDistance < 30 -> R.drawable.right // 30m 이하 남았을 때 우회전 이미지
                else -> R.drawable.straight // 그 외의 경우는 직진 이미지
            }

            // 거리를 10m 단위로 반올림합니다.
            val roundedDistance = (minDistance / 10).roundToInt() * 10
            val text = "$roundedDistance 미터"

            // 텍스트와 함께 이미지를 TextView에 설정합니다.
            turnTextView.text = text
            turnTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, directionResource, 0)
            turnTextView.compoundDrawablePadding = 10 // 이미지와 텍스트 사이의 패딩을 설정합니다.

            // 새로운 지점을 안내된 지점 목록에 추가합니다.
            if (minDistance <= turnThreshold) {
                guidedTurnCoords.add(it)
            }
        }
    }

    // 사용자 위치가 업데이트 될 때마다 호출되는 함수
    fun onLocationChanged(currentLocation: Location) {
        // 지나쳤던 지점들을 확인하고 목록에서 제거합니다.
        val iterator = guidedTurnCoords.iterator()
        while (iterator.hasNext()) {
            val turnCoord = iterator.next()
            val distance = calculateDistance(currentLocation, turnCoord)
            if (distance < turnThreshold) {
                // 지나친 지점은 제거합니다.
                iterator.remove()
            }
        }

        // 회전 지점 안내 텍스트를 업데이트합니다.
        updateTurnGuidanceText(currentLocation)
    }








    fun speakOnApproachingStraight(currentLocation: Location) {
        for (rightTurnCoord in rightTurnCoords) {
            val distance = calculateDistance(currentLocation, rightTurnCoord)

            if (distance <= turnThreshold0 && !guidedCoords0m.contains(rightTurnCoord)) {
                guidedCoords10m.add(rightTurnCoord)
                isGuiding0m = true
            }
        }
    }
    fun speakOnApproachingStraight2(currentLocation: Location) {
        for (leftTurnCoord in leftTurnCoords) {
            val distance = calculateDistance(currentLocation, leftTurnCoord)

            if (distance <= turnThreshold0 && !guidedCoords0m.contains(leftTurnCoord)) {
                guidedCoords10m.add(leftTurnCoord)
                isGuiding0m = true
            }
        }
    }

    fun speakOnApproachingLeftTurn10(currentLocation: Location) {
        if(isGuiding0m) {
            for (leftTurnCoord in leftTurnCoords) {
                val distance = calculateDistance(currentLocation, leftTurnCoord)
                if (distance <= turnThreshold20 && !guidedCoords10m.contains(leftTurnCoord)) {
                    val text = "20m 앞에서 좌회전하세요."
                    speak(text)
                    turnTextView.text = text
                    guidedCoords10m.add(leftTurnCoord)
                    isGuiding0m = false
                }
            }
        }
    }

    fun speakOnApproachingRightTurn10(currentLocation: Location) {
        if(isGuiding0m) {
            for (rightTurnCoord in rightTurnCoords) {
                val distance = calculateDistance(currentLocation, rightTurnCoord)
                if (distance <= turnThreshold20 && !guidedCoords10m.contains(rightTurnCoord)) {
                    val text = "20m 앞에서 우회전하세요."
                    speak(text)
                    turnTextView.text = text
                    guidedCoords10m.add(rightTurnCoord)
                    isGuiding0m = false
                }
            }
        }
    }

    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // 지구의 반지름 (킬로미터)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }


    fun calculatePathDistance(pathCoords: List<LatLng>): Double {
        var totalDistance = 0.0
        for (i in 0 until pathCoords.size - 1) {
            val start = pathCoords[i]
            val end = pathCoords[i + 1]
            totalDistance += haversineDistance(start.latitude, start.longitude, end.latitude, end.longitude)
        }
        return totalDistance
    }

    fun calculatePathDistanceForRoutes(routeData: MutableList<List<List<Double>>>): List<Double> {
        val distances = mutableListOf<Double>()

        for (pathCoordsList in routeData) {
            val pathCoords = pathCoordsList.map { LatLng(it[1], it[0]) }
            val distance = calculatePathDistance(pathCoords)
            distances.add(distance)
        }

        return distances
    }

    fun calculateEstimatedTime(distanceInKm: Double, averageSpeedKmH: Double): String {
        val timeInHours = distanceInKm / averageSpeedKmH

        val hours = timeInHours.toInt()
        val minutes = ((timeInHours - hours) * 60).toInt()

        return if (hours > 0) {
            "$hours 시간 $minutes 분"
        } else {
            "$minutes 분"
        }
    }
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isGuidanceRunning) {

                isGuidanceRunning = false
                clearAndRedrawRoute(selectedIndex)
                shortestButton.visibility = View.VISIBLE
                mainStreetButton.visibility = View.VISIBLE
                safeRouteButton.visibility = View.VISIBLE
                optimalButton.visibility = View.VISIBLE
                directionButton.visibility = View.VISIBLE
                guidanceButton.visibility = View.GONE
                turnTextView.visibility = View.GONE
                mainStreetButton.setBackgroundColor(Color.WHITE)
                shortestButton.setBackgroundColor(Color.WHITE)
                safeRouteButton.setBackgroundColor(Color.WHITE)
                optimalButton.setBackgroundColor(Color.WHITE)
                val desiredZoomLevel = 14.0
                for (overlay in pathOverlays) {
                    overlay.map = null
                }

                for (marker in markers) {
                    marker.map = null
                }
                markers.clear()


                val cameraUpdate = CameraUpdate.zoomTo(desiredZoomLevel)

                naverMap.moveCamera(cameraUpdate)

                val latLongArray = parseLatLong(mapyFormatted,mapxFormatted)
                if (latLongArray != null) {
                    val latitude = latLongArray[0]
                    val longitude = latLongArray[1]



                    // 현재 위치를 Naver 지도에 표시
                    val destinationLatitude = longitude
                    val destinationLongitude = latitude
                    showDestinationOnlyOnMap(
                        naverMap,
                        destinationLatitude,
                        destinationLongitude
                    )
                    showDestinationOnMap(
                        naverMap,
                        destinationLatitude,
                        destinationLongitude
                    )
                }

            } else {
                // 경로 안내 중이 아니라면 기본적인 뒤로가기 동작 수행
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

    private fun isOffRoute(currentLocation: Location): Boolean {
        val routeData = routeData // 경로 데이터를 임시 변수에 복사합니다.
        if (routeData != null && selectedIndex < routeData.size) {
            val selectedCoordinatesList = routeData[selectedIndex]
            val closestCoordinate = findClosestCoordinate(currentLocation, selectedCoordinatesList)
            val distance = calculateDistance(currentLocation, LatLng(closestCoordinate[1], closestCoordinate[0]))
            Log.d("RouteStatus", "closestCoordinate: $closestCoordinate")
            Log.d("RouteStatus", "distance: $distance")
            if (distance > toleranceDistance) {
                Log.d("RouteStatus", "경로를 벗어났습니다. 거리: $distance")
                return true // 경로를 벗어남
            }
        }
        Log.d("RouteStatus", "경로를 벗어나지 않았습니다.")
        return false // 경로를 벗어나지 않음
    }

    private fun findClosestCoordinate(currentLocation: Location, coordinatesList: List<List<Double>>): List<Double> {
        var closestCoordinate = coordinatesList[0]
        var minDistance = calculateDistance(currentLocation, LatLng(closestCoordinate[1], closestCoordinate[0]))

        for (coordinate in coordinatesList) {
            val distance = calculateDistance(currentLocation, LatLng(coordinate[1], coordinate[0]))
            if (distance < minDistance) {
                minDistance = distance
                closestCoordinate = coordinate
            }
        }

        return closestCoordinate
    }
    private fun recomputeRoute(currentLatitude: Double, currentLongitude: Double) {
        // 경로 재탐색을 위한 서버 요청 또는 다른 로직 추가
        // 경로를 재탐색하고 그에 따른 UI 업데이트 등을 수행할 수 있습니다.
        startGuidance2(currentLatitude, currentLongitude)
        sendRequestToServer2(currentLatitude, currentLongitude)
        val text = "경로를 재탐색합니다."
        speak(text)
        turnTextView.text = text

    }

    private fun sendRequestToServer2(currentLatitude: Double, currentLongitude: Double) {
        val latLongPair = parseLatLong(mapyFormatted,mapxFormatted)

        if (latLongPair != null) {
            val (latitude, longitude) = latLongPair

            val requestData = RequestData(
                LocationData(currentLongitude, currentLatitude),
                LocationData(latitude, longitude)
            )

            val apiService = RetrofitInstance.getRetrofitInstance().create(ApiService::class.java)
            val call = apiService.getRoute(requestData)


            call.enqueue(object : Callback<List<ServerResponse>> {
                override fun onResponse(call: Call<List<ServerResponse>>, response: Response<List<ServerResponse>>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()

                        if (responseData != null && responseData.isNotEmpty()) {
                            routeData = mutableListOf()

                            for (serverResponse in responseData) {
                                val coordinatesList = mutableListOf<List<Double>>()

                                for (feature in serverResponse.features) {
                                    val geometry = feature.geometry
                                    if (geometry.type == "LineString") {
                                        val coordinates = geometry.coordinates as List<List<Double>>
                                        coordinatesList.addAll(coordinates)
                                    }
                                }

                                routeData?.add(coordinatesList)
                            }

                            // 경로 데이터를 저장한 후, 다른 함수에서 그릴 수 있습니다.
                            clearAndRedrawRoute2(currentLatitude, currentLongitude)
                            val tempRouteData = routeData
                            val distances = if (tempRouteData != null) {
                                calculatePathDistanceForRoutes(tempRouteData)
                            } else {
                                listOf(0.0, 0.0, 0.0)
                            }

                            val averageWalkingSpeed = 5.0 // km/h, 평균 이동 속도

                            val estimatedTimeForShortest = calculateEstimatedTime(distances[0], averageWalkingSpeed)
                            shortestButton.text = "큰길우선\n\n${String.format("%.2f km", distances[0])}\n\n $estimatedTimeForShortest"

                            val estimatedTimeForMainStreet = calculateEstimatedTime(distances[1], averageWalkingSpeed)
                            mainStreetButton.text = "최단거리\n\n${String.format("%.2f km", distances[1])} \n\n $estimatedTimeForMainStreet"

                            val estimatedTimeForSafeRoute = calculateEstimatedTime(distances[2], averageWalkingSpeed)
                            safeRouteButton.text = "안심거리\n\n${String.format("%.2f km", distances[2])} \n\n $estimatedTimeForSafeRoute"

                            val estimatedTimeForShortest2 = calculateEstimatedTime(distances[4], averageWalkingSpeed)
                            optimalButton.text = "계산최단거리\n\n${String.format("%.2f km", distances[4])} \n\n $estimatedTimeForShortest2"
                        }
                    } else {
                        // 서버에서 오류 응답을 받은 경우 처리
                        Log.e("ResponseError", "Server error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<ServerResponse>>?, t: Throwable?) {
                    // 서버 통신 실패 처리
                    Log.e("RequestFailure", "Request failed: ${t?.message}")
                }
            })
        }
    }
    private fun startGuidance2(currentLatitude: Double, currentLongitude: Double) {
        val startGuidanceMessage = "경로 안내를 시작합니다."
        shortestButton.visibility = View.GONE
        mainStreetButton.visibility = View.GONE
        safeRouteButton.visibility = View.GONE
        optimalButton.visibility = View.GONE
        guidanceButton.visibility = View.GONE
        turnTextView.visibility = View.VISIBLE

        if (::currentLocationMarker.isInitialized) {
            currentLocationMarker.map = null
        }

        val myLocation = LatLng(currentLatitude, currentLongitude)
        naverMap.moveCamera(CameraUpdate.toCameraPosition(CameraPosition(myLocation, 18.0)))

        val cameraUpdate = CameraUpdate.scrollTo(myLocation).animate(CameraAnimation.Linear)
        naverMap.moveCamera(cameraUpdate)

        speak(startGuidanceMessage) {

            isGuidanceRunning = true
        }
    }

}

