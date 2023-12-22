package ddwu.com.mobile.photomemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ddwu.com.mobile.photomemo.data.MemoDao
import ddwu.com.mobile.photomemo.data.MemoDatabase
import ddwu.com.mobile.photomemo.data.MemoDto
import ddwu.com.mobile.photomemo.databinding.ActivityShowMemoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class ShowMemoActivity : AppCompatActivity() {

    val TAG = "ShowMemoActivityTag"

    val showMemoBinding by lazy {
        ActivityShowMemoBinding.inflate(layoutInflater)
    }

    lateinit var memoDto : MemoDto

    val memoDB : MemoDatabase by lazy {
        MemoDatabase.getDatabase(this)
    }

    val memoDao : MemoDao by lazy {
        memoDB.memoDao()
    }

    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var googleMap : GoogleMap
    private lateinit var geocoder : Geocoder
    private lateinit var currentLoc : Location

    var centerMarker : Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(showMemoBinding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        /*전달받은 intent 에서 memoDto 를 읽어온 후 각 view 에 지정*/
        val i = intent.getSerializableExtra("memoDto")
        var list = arrayListOf<MemoDto>()
        list.add(i as MemoDto)

        if (list.isNotEmpty()) {
            memoDto = list[0]

            // Assuming you have TextViews in your layout with IDs tvTitle, tvContent, etc.
            showMemoBinding.tvMemo.setText(memoDto.memo)
            showMemoBinding.tvStartDate.setText(memoDto.startDate)
            showMemoBinding.tvEndDate.setText(memoDto.endDate)
            showMemoBinding.tvTitle.setText(memoDto.title)
            showMemoBinding.tvSubTitle.setText(memoDto.subTitle)
        }

        showMemoBinding.btnModify.setOnClickListener {
            memoDto.title = showMemoBinding.tvTitle.text.toString()
            memoDto.memo = showMemoBinding.tvMemo.text.toString()
            memoDto.startDate = showMemoBinding.tvStartDate.text.toString()
            memoDto.endDate = showMemoBinding.tvEndDate.text.toString()
            memoDto.subTitle = showMemoBinding.tvSubTitle.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                memoDao.updateMemo(memoDto)
            }
            Toast.makeText(this, "Implement modifying data", Toast.LENGTH_SHORT).show()
        }

        showMemoBinding.btnClose.setOnClickListener {
            finish()
        }

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync (mapReadyCallback)

        fusedLocationClient.removeLocationUpdates(locCallback)

    }
    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "GoogleMap is ready")

            geocoder.getFromLocationName(memoDto.location, 5) { addresses ->
                CoroutineScope(Dispatchers.Main).launch {
                    val targetLoc: LatLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))
                    Log.d(TAG, addresses[0].latitude.toString())
                    addMarker(targetLoc)
                }
            }
            // 마커 클릭 이벤트 처리
            googleMap.setOnMarkerClickListener { marker ->
                Toast.makeText(this@ShowMemoActivity, marker.tag.toString(), Toast.LENGTH_SHORT).show()
                false // true일 경우 이벤트처리 종료이므로 info window 미출력
            }
            // 마커 InfoWindow 클릭 이벤트 처리
            googleMap.setOnInfoWindowClickListener { marker ->
                Toast.makeText(this@ShowMemoActivity, marker.title, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val locCallback : LocationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onLocationResult(locResult: LocationResult) {
            currentLoc = locResult.locations.get(0)
        }
    }

    fun addMarker(targetLoc: LatLng) {  // LatLng(37.606320, 127.041808)
        val markerOptions: MarkerOptions = MarkerOptions() // 마커를 표현하는 Option 생성
        markerOptions.position(targetLoc) // 필수
            .title(memoDto.location)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
// .icon(BitmapDescriptorFactory.fromResource(R.mipmap.android))
        centerMarker = googleMap.addMarker(markerOptions) // 지도에 마커 추가, 추가마커 반환
        centerMarker?.showInfoWindow() // 마커 터치 시 InfoWindow 표시
        centerMarker?.tag = "database_id"
    }

    val locRequest = LocationRequest.Builder(5000)
        .setMinUpdateIntervalMillis(3000)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .build()

    /*위치 정보 수신 시작*/
    private fun startLocUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locRequest,     // LocationRequest 객체
            locCallback,    // LocationCallback 객체
            Looper.getMainLooper()  // System 메시지 수신 Looper
        )
    }
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locCallback)
    }

}