package ddwu.com.mobile.photomemo

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ddwu.com.mobile.photomemo.data.MemoDao
import ddwu.com.mobile.photomemo.data.MemoDatabase
import ddwu.com.mobile.photomemo.data.MemoDto
import ddwu.com.mobile.photomemo.databinding.ActivityAddMemoBinding
import ddwu.com.mobile.photomemo.databinding.ActivityRecommendBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
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
import ddwu.com.mobile.photomemo.data.Tour
import ddwu.com.mobile.photomemo.ui.TourAdapter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.util.Locale


class RecommendActivity : AppCompatActivity() {

    val mobile_os = "AND"
    val mobile_app = "AppTest"
    val content_type_id = 12

    var map_x = "127.37842067865687"
    var map_y = "36.3441880732059"
    //lateinit var map_x : String
    //lateinit var map_y : String
    val radius = 2000
    val type = "json"

    lateinit var rcTourList : RecyclerView
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var googleMap : GoogleMap
    private lateinit var geocoder : Geocoder
    private lateinit var currentLoc : Location
    val serviceUrl = "http://apis.data.go.kr/B551011/KorService1/locationBasedList1"
    val serviceKey = "7KiKaWizfOtKu5Ir4XdTLNMltuwP1TU6I8S9/g9kOUgcxH/jkwdbe826XOAEnTaM5/mPnkvESbW40uriWdQJWQ=="

    var centerMarker : Marker? = null

    val recommendBinding by lazy {
        ActivityRecommendBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(recommendBinding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        recommendBinding.button.setOnClickListener {
            finish()
        }

        recommendBinding.btnSearch.setOnClickListener {
            geocoder.getFromLocationName(recommendBinding.tvSearch.text.toString(), 5) { addresses ->
                CoroutineScope(Dispatchers.Main).launch {
                    val targetLoc: LatLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))
                    Log.d(TAG, addresses[0].latitude.toString())
                    addMarker(targetLoc)

                    map_x = addresses[0].longitude.toString()
                    map_y = addresses[0].latitude.toString()
                    Log.d(TAG, "${map_y}")
                    val requstUrl = serviceUrl +
                            "?serviceKey=" + serviceKey +
                            //"&pageNo=" + page_no +
                            //"&numOfRows=" + num_of_rows +
                            "&MobileApp=" + mobile_app +
                            "&MobileOS=" + mobile_os +
                            "&mapX=" + map_x +
                            "&mapY=" + map_y +
                            "&radius=" + radius +
                            "&contentTypeId=" + content_type_id
                    //"&areaCode=" + area_code +
                    //"&sigunguCode=" + sigungu_code +
                    //"&listYN=" + list_yn

                    fetchXML(requstUrl)

                }
            }
        }

        val mapFragment: SupportMapFragment
                = supportFragmentManager.findFragmentById(R.id.mapRec)
                as SupportMapFragment
        mapFragment.getMapAsync (mapReadyCallback)

        fusedLocationClient.removeLocationUpdates(locCallback)

        rcTourList = findViewById(R.id.rcTourList)

        // 리아시클러뷰 설정
        rcTourList.layoutManager = LinearLayoutManager(this)

        // 이 url 주소 가지고 xml에서 데이터 파싱하기

    }

    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "GoogleMap is ready")
        }
    }

    val locCallback : LocationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onLocationResult(locResult: LocationResult) {
            currentLoc = locResult.locations.get(0)
            geocoder.getFromLocation(currentLoc.latitude, currentLoc.longitude, 5) { addresses ->
                CoroutineScope(Dispatchers.Main).launch {
                    Log.d(TAG, "${currentLoc.latitude}")
                    //showData("위도: ${currentLoc.latitude}, 경도: ${currentLoc.longitude}")
                    //showData(addresses?.get(0)?.getAddressLine(0).toString())
                }
            }
            val targetLoc = LatLng(currentLoc.longitude,currentLoc.latitude)
            Log.d(TAG, "${currentLoc.latitude}")
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))
        }
    }

    fun addMarker(targetLoc: LatLng) {  // LatLng(37.606320, 127.041808)
        val markerOptions: MarkerOptions = MarkerOptions() // 마커를 표현하는 Option 생성
        markerOptions.position(targetLoc) // 필수
            .title("위치")
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

    // xml 파싱하기
    fun fetchXML(url : String) {
        lateinit var page : String  // url 주소 통해 전달받은 내용 저장할 변수

        // xml 데이터 가져와서 파싱하기
        // 외부에서 데이터 가져올 때 화면 계속 동작하도록 AsyncTask 이용
        class getDangerGrade : AsyncTask<Void, Void, Void>() {
            // url 이용해서 xml 읽어오기
            override fun doInBackground(vararg p0: Void?): Void? {
                // 데이터 스트림 형태로 가져오기
                val stream = URL(url).openStream()
                val bufReader = BufferedReader(InputStreamReader(stream, "UTF-8"))

                // 한줄씩 읽어서 스트링 형태로 바꾼 후 page에 저장
                page = ""
                var line = bufReader.readLine()
                while (line != null) {
                    page += line
                    line = bufReader.readLine()
                }

                return null
            }

            // 읽어온 xml 파싱하기
            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                var itemList : ArrayList<Tour> = arrayListOf()  // 저장될 데이터 배열

                var tagImage = false   // 이미지 태그
                var tagTitle = false   // 제목 태그
                var tagAddr1 = false   // 주소 태그
                var tagAddr2 = false   // 상세주소 태그

                var firstimage = ""    // 이미지
                var title = ""         // 제목
                var addr1 = ""         // 주소
                var addr2 = ""         // 상세 주소

                var factory = XmlPullParserFactory.newInstance()    // 파서 생성
                factory.setNamespaceAware(true)                     // 파서 설정
                var xpp = factory.newPullParser()                   // XML 파서

                // 파싱하기
                xpp.setInput(StringReader(page))

                // 파싱 진행
                var eventType = xpp.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {}
                    else if (eventType == XmlPullParser.START_TAG) {
                        var tagName = xpp.name
                        if (tagName.equals("firstimage")) tagImage = true
                        else if (tagName.equals("title")) tagTitle = true
                        else if (tagName.equals("addr1")) tagAddr1 = true
                        else if (tagName.equals("addr2")) tagAddr2 = true
                    }
                    if (eventType == XmlPullParser.TEXT) {
                        if (tagImage) {         // 이미지
                            firstimage = xpp.text
                            tagImage = false
                        }
                        else if (tagTitle) {    // 제목
                            title = xpp.text
                            tagTitle = false

                            // 기관명까지 다 읽으면 하나의 데이터 다 읽은 것임
                            var item = Tour(firstimage, title, addr1, addr2)
                            itemList.add(item)
                        }
                        else if (tagAddr1) {    // 주소
                            addr1 = xpp.text
                            tagAddr1 = false
                        }
                        else if (tagAddr2) {    // 상세주소
                            addr2 = xpp.text
                            tagAddr2 = false
                        }
                    }
                    if (eventType == XmlPullParser.END_TAG) {}

                    eventType = xpp.next()
                }
                // 리아시클러 뷰에 데이터 연결
                rcTourList.adapter = TourAdapter(itemList)
            }
        }

        getDangerGrade().execute()
    }


}