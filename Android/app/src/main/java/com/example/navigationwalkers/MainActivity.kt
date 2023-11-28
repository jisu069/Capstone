package com.example.navigationwalkers

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.Manifest
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.UiSettings
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import org.json.JSONException
import org.json.JSONObject
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fm: FragmentManager
    private lateinit var ft: FragmentTransaction
    private lateinit var frag1: Frag1
    private lateinit var frag2: Frag2
    private lateinit var frag3: Frag3
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            naverMap = it

            naverMap.setContentPadding(0, 50, 0, 0)
            naverMap.locationSource = locationSource
            naverMap.locationTrackingMode = LocationTrackingMode.Follow

            val uiSettings: UiSettings = naverMap.uiSettings
            uiSettings.isLocationButtonEnabled = true

            val cameraPosition = CameraPosition(
                LatLng(currentLatitude, currentLongitude), 17.0
            )
            val cameraUpdate = CameraUpdate.toCameraPosition(cameraPosition).animate(CameraAnimation.None)
            naverMap.moveCamera(cameraUpdate)
        }









        frag1 = Frag1() // frag1 초기화
        frag3 = Frag3() // frag3 초기화
        setFrag(0)
    }



    private fun setFrag(n: Int) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()

        when (n) {
            0 -> {
                ft.replace(R.id.main_frame, frag1)
            }
            1 -> {
                ft.replace(R.id.main_frame, frag2)
            }
            2 -> {
                ft.replace(R.id.main_frame, frag3)
            }
        }

        ft.commit()
    }
    private fun showMapView() {
        mapView.visibility = View.VISIBLE
    }

    private fun hideMapView() {
        mapView.visibility = View.GONE
    }


    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 정보 요청
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude

                        // 여기에서 현재 위치 정보를 사용할 수 있습니다.
                        // 예를 들어, 현재 위치를 지도의 특정 위치로 이동하는 등의 작업을 수행할 수 있습니다.
                        // 또한 목적지 위치 정보도 이곳에서 처리할 수 있습니다.
                    }
                }
        } else {
            // 위치 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }



override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
