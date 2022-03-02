package com.sunanalyzer.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SizeF
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.sunanalyzer.app.clickListener.DialogCallBacks
import com.sunanalyzer.app.clickListener.RecyclerRowClick
import com.sunanalyzer.app.databinding.ActivityGetAreaMapBinding
import com.sunanalyzer.app.model.WorkAreaConfigurationModel
import com.sunanalyzer.app.ui.bottomsheet.BottomSheetDrawingType
import com.sunanalyzer.app.ui.bottomsheet.BottomSheetWorkAreaSetting
import com.sunanalyzer.app.utility.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*


class GetAreaMapActivity : BaseActivity(), OnMapReadyCallback,
    RecyclerRowClick {

    private var CurrentPoly: Polygon? = null
    private var polyline: Polyline? = null
    lateinit var mBinder: ActivityGetAreaMapBinding

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var supportMapFragment: SupportMapFragment? = null
    private var mapView: View? = null
    private val REQUEST_CHECK_SETTINGS = 2
    private var selectedMeasurementUnit = AppConstant.AreaType.M2
    private var labelMeasurement = ""
    private val outerMarker: ArrayList<Marker> = ArrayList()
    private val centerMarker: ArrayList<Marker> = ArrayList()
    private var canDrawPoint = true
    private var mapCameraZoom = 19f
    private var isFirstBuilding = true
    private var isShowCurrentLocation = true
    private var isMarkerDragStart = false

    /*SunAnalyzer variable Declaration*/
    private var allWorkArea: ArrayList<Polygon> = ArrayList()
    private var innerPolygonArray: ArrayList<Polygon> = ArrayList()
    private var innerPolygonBoxes: ArrayList<Polygon> = ArrayList()
    private var subboxes: ArrayList<Polygon> = ArrayList()
    private var workAreaId = 0
    private var workAreaConfigurationSettings = WorkAreaConfigurationModel()

    companion object {
        private var address = ""
        private var postalCode = ""
        private var strState = ""
        private var strStateCode = ""
        private var city = ""
        private var strCityCode = ""
        private var strCountry = ""
        private var strCountryCode = ""
        private var strZip = ""
        private var latitudeFinal = 0.0
        private var longitudeFinal = 0.0
        private var totalArea = 0.0
        private var mMap: GoogleMap? = null

        /*Id Prefix*/
        var parentWorkArea_ = "Parent_WorkArea_"
        var innerPoly_ = "innerPoly_"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViews()
    }

    private fun initializeViews() {
        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_get_area_map)
        setUpToolbarWithBackText(
            mBinder.toolbarView.toolbar,
            "Sun Analyzer",
            true
        )
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapView = supportMapFragment!!.view
        supportMapFragment!!.getMapAsync(this)
        getMeasurementUnit()
        mBinder.toolbarView.llClear.setOnClickListener(clickListener)
        mBinder.toolbarView.llUndo.setOnClickListener(clickListener)
        mBinder.toolbarView.llEdit.setOnClickListener(clickListener)
        enableDraw()

    }


    private fun getMeasurementUnit() {
        labelMeasurement = if (selectedMeasurementUnit == AppConstant.AreaType.M2) "m" else "ft"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        askPermission()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private fun setupGoogleMap() {
        mMap!!.uiSettings.isZoomControlsEnabled = false
        mMap!!.uiSettings.isMyLocationButtonEnabled = true
        mMap!!.uiSettings.isCompassEnabled = true
        mMap!!.uiSettings.setAllGesturesEnabled(true)
        mMap!!.isMyLocationEnabled = true

        if (mapView != null) {
            val locationButton =
                (mapView!!.findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
            layoutParams.setMargins(0, 20, 40, 0)

            val parent = mapView!!.findViewById<View>("1".toInt()).parent as ViewGroup
            val compassButton = parent.getChildAt(4)
            val rlp = compassButton.layoutParams as RelativeLayout.LayoutParams
            rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_START)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
            rlp.setMargins(0, 20, 0, 0)
            compassButton.layoutParams = rlp
        }
        mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mFusedLocationClient!!.lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null && isFirstBuilding) {
                val mLastLocation = task.result
                var latLng: LatLng? = null
                if (mLastLocation != null && mLastLocation.latitude != null && mLastLocation.longitude != null) {
                    latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)

                }
                latitudeFinal = latLng!!.latitude
                longitudeFinal = latLng!!.longitude
                if (latLng != null) {
                    val cameraPosition =
                        CameraPosition.Builder().target(latLng).zoom(mapCameraZoom).build()
                    mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    CoordinateToAddress("${latitudeFinal},${longitudeFinal}").execute()
                }
            }
        }
        mMap!!.setOnMyLocationButtonClickListener {
            createLocationRequest()
            false
        }
        mMap!!.setOnCameraChangeListener {
            if (isFirstBuilding) {
                latitudeFinal = it.target.latitude
                longitudeFinal = it.target.longitude
                CoordinateToAddress("${latitudeFinal},${longitudeFinal}").execute()
            }

        }
        mMap!!.setOnMapClickListener { lntlng ->

            if (canDrawPoint) {
                var marker = createMarker(lntlng, outerMarker.size, R.drawable.ic_marker_big)
                outerMarker.add(marker)
                setOuterMarkerPath()
                drawPolyLine()
            }
        }
        mMap!!.setOnMarkerClickListener { marker ->

            val firstMarker = outerMarker.first()
            if (firstMarker != null && firstMarker == marker) {
                polyline?.let { it.remove() }
                drawPolygon(true)
                mBinder.toolbarView.llUndo.gone()

            } else if (canDrawPoint
            ) {
                centerMarker.forEach {
                    it.setAnchor(0.5f, 0.5f)
                    it.isDraggable = false
                    it.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_small))
                }
                setOuterMarkerPath()

                marker.isDraggable = true
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_dragable))
                marker.setAnchor(0.5f, 1f)
            }
            false
        }
        mMap!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(arg0: Marker) {
                isMarkerDragStart = true
                if (polyline != null) {
                    polyline!!.color = Color.DKGRAY
                }
            }

            override fun onMarkerDrag(marker: Marker) {
                var isCenterMarker = centerMarker.any { marker == it }
                if (isCenterMarker) {
                    var pos: Int = marker!!.tag as Int
                    centerMarker.remove(marker)
                    outerMarker.add(pos + 1, marker)
                }

                drawPolyLine()
            }

            override fun onMarkerDragEnd(marker: Marker) {
                var isCenterMarker = centerMarker.any { marker == it }

                if (isCenterMarker) {
                    var pos: Int = marker!!.tag as Int
                    centerMarker.remove(marker)
                    outerMarker.add(pos + 1, marker)
                }
                setOuterMarkerPath()
                isMarkerDragStart = false
                drawPolyLine()

            }


        })
    }

    private fun setOuterMarkerPath() {
        outerMarker.forEach {
            it.setAnchor(0.5f, 0.5f)
            it.isDraggable = false
            it.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_big))
        }

        if (outerMarker.isNotEmpty()) {
            outerMarker.last()
                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_big_last))
        }
    }

    private fun getCenterPoint(points: List<LatLng>): LatLng {
        val centroid = doubleArrayOf(0.0, 0.0)
        for (i in points.indices) {
            centroid[0] += points[i].latitude
            centroid[1] += points[i].longitude
        }
        val totalPoints = points.size
        centroid[0] = centroid[0] / totalPoints
        centroid[1] = centroid[1] / totalPoints
        return LatLng(centroid[0], centroid[1])
    }

    private fun createMarker(position: LatLng?, size: Int, icMarkerBig: Int): Marker {
        var marker = mMap!!.addMarker(MarkerOptions().position(position!!).draggable(false))
        marker!!.tag = size
        marker!!.setAnchor(0.5f, 0.5f)
        marker!!.rotation = 180f
        marker!!.setIcon(BitmapDescriptorFactory.fromResource(icMarkerBig))
        return marker
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun drawPolygon(isFillColor: Boolean) {
        if (CurrentPoly != null) {
            CurrentPoly!!.remove()
        }
        CurrentPoly = mMap!!.addPolygon(
            PolygonOptions()
                .clickable(true)
                .addAll(outerMarker.map { it.position })
        )
        CurrentPoly!!.strokeWidth = 4f
        CurrentPoly!!.strokeColor = Color.DKGRAY
        if (isFillColor)
            CurrentPoly!!.fillColor =
                if (isMarkerDragStart) Color.argb(80, 0, 0, 0) else Color.argb(50, 249, 177, 32)
        addCenterPoint()
        openDrawingTypeOptionDialog()
    }

    private fun drawPoly(
        id: String,
        fillColor: String,
        strokeColor: String,
        path: ArrayList<LatLng>
    ): Polygon {
        var polygon = mMap!!.addPolygon(
            PolygonOptions()
                .clickable(true)
                .addAll(path)
        )
        polygon.strokeWidth = 1.5f
        polygon.tag = id
        polygon.strokeColor = Color.parseColor(strokeColor)
        polygon.fillColor = Color.parseColor(fillColor)
        return polygon

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun drawPolyLine() {
        if (polyline != null) {
            polyline!!.remove()
        }
        polyline = mMap!!.addPolyline(
            PolylineOptions()
                .clickable(true)
                .width(2.0F)
                .color(if (isMarkerDragStart) Color.DKGRAY else this.getColor(R.color.colorPrimary))
                .addAll(outerMarker.map { it.position })
        )

    }

    private fun addCenterPoint() {
        centerMarker.forEach {
            it.remove()
        }
        centerMarker.clear()
        val totalPoint: ArrayList<LatLng> = ArrayList()
        totalArea = 0.0
        if (outerMarker.size > 1) {
            for (i in outerMarker.indices) {
                var secondIndex = if (i == outerMarker.size - 1) 0 else (i + 1)
                var coordinate0 = outerMarker[i].position
                var coordinate1 = outerMarker[secondIndex].position
                var centerLntLong = LatLngBounds.builder().include(coordinate0)
                    .include(coordinate1)
                    .build().center
                totalPoint.add(outerMarker[i].position)
                if (secondIndex != 0) {
                    centerMarker.add(
                        createMarker(
                            centerLntLong,
                            centerMarker.size,
                            R.drawable.ic_marker_small
                        )
                    )

                } else if (secondIndex == 0) {
                    centerMarker.add(
                        createMarker(
                            centerLntLong,
                            centerMarker.size,
                            R.drawable.ic_marker_small
                        )
                    )
                }
            }


        }
        totalArea = meterToFt(
            SphericalUtil.computeLength(totalPoint),
            selectedMeasurementUnit == AppConstant.AreaType.M2
        ).toDouble()
        workAreaConfigurationSettings.totalWorkArea = totalArea
        mBinder.toolbarView.llClear.visibility =
            if (outerMarker.size > 0) View.VISIBLE else View.GONE
        mBinder.toolbarView.llUndo.visibility =
            if (outerMarker.size > 0) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private val clickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.llClear -> {
                resetDrawArea()

            }
            R.id.llUndo -> {
                undoDrawPoint()
            }

            R.id.llEdit -> {
                showWorkAreaSetting()
            }
        }
    }

    private fun showWorkAreaSetting() {
        BottomSheetWorkAreaSetting().newInstance(
            this,
            workAreaConfigurationSettings,
            object : DialogCallBacks {
                override fun onDismiss(bundle: Bundle) {
                    bundle.let { it ->
                        workAreaConfigurationSettings =
                            it.getParcelable("workAreaConfigurationModel")!!
                        setOffset(CurrentPoly!!.tag as String)
                        newApplyPanelBasedOnOffSet(innerPolygonArray.last())
                    }
                }

                override fun dialogClick(position: Int, id: Int, drawingType: String) {

                }
            }).show(supportFragmentManager, "bottomSheet")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun undoDrawPoint() {
        if (outerMarker.isNotEmpty() && outerMarker.size > 1) {
            val indexToRemove = outerMarker.size - 1
            if (outerMarker.size > indexToRemove) {
                outerMarker[indexToRemove].remove()
                outerMarker.removeAt(indexToRemove)
                drawPolyLine()
                setOuterMarkerPath()
            }
        } else {
            resetDrawArea()
        }
    }

    private fun resetDrawArea() {
        centerMarker.forEach { it.remove() }
        outerMarker.forEach { it.remove() }
        centerMarker.clear()
        outerMarker.clear()
        if (CurrentPoly != null) {
            CurrentPoly!!.remove()
        }
        if (polyline != null) {
            polyline!!.remove()
        }
        enableDraw()
        mBinder.toolbarView.llClear.gone()
        mBinder.toolbarView.llUndo.gone()
    }

    private fun enableDraw() {
        canDrawPoint = true
    }


    inner class CoordinateToAddress(private val mapCoordinatesTxt: String) :
        AsyncTask<String, String, String>() {

        var jsonResults: StringBuilder? = null

        override fun onPreExecute() {
            super.onPreExecute()
            jsonResults = StringBuilder()
        }

        override fun doInBackground(vararg params: String): String {
            var url: URL? = null
            try {
                url =
                    URL("${AppConstant.GOOGLE_MAP_URL}?key=${getString(R.string.key_google_map)}&latlng=${mapCoordinatesTxt}")
                val conn = url.openConnection() as HttpURLConnection
                val in1 = InputStreamReader(conn.inputStream)

                var read: Int
                val buff = CharArray(1024)

                while (in1.read(buff).also { read = it } != -1) {
                    jsonResults!!.append(buff, 0, read)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
            return jsonResults.toString()
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            try {
                Log.e("google", result)
                val mJsonObject = JSONObject(result)
                val mJsonArray = JSONArray(mJsonObject.getString("results"))
                val mJsonObject2 = mJsonArray.getJSONObject(0)
                address = mJsonObject2.getString("formatted_address")

                val addressComponents = mJsonObject2.getJSONArray("address_components")
                for (i in 0 until addressComponents.length()) {
                    val zero2 = addressComponents.getJSONObject(i)
                    val longName = zero2.getString("long_name")
                    val shortName = zero2.getString("short_name")
                    val mtypes = zero2.getJSONArray("types")
                    var Type: String? = ""
                    if (mtypes != null && mtypes.length() > 0) {
                        Type = mtypes.getString(0)
                    }

                    if (!TextUtils.isEmpty(Type) && !longName.isNullOrEmpty()) {
                        if (Type.equals("locality", ignoreCase = true)) {
                            city = longName
                            strCityCode = shortName
                        }
                        if (Type.equals("administrative_area_level_1", ignoreCase = true)) {
                            strState = longName
                        }
                        if (Type.equals("administrative_area_level_1", ignoreCase = true)) {
                            strStateCode = shortName
                        }
                        if (Type.equals("country", ignoreCase = true)) {
                            strCountry = longName
                            strCountryCode = shortName
                        }
                        if (Type.equals("postal_code", ignoreCase = true)) {
                            postalCode = longName
                            strZip = postalCode
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Error", e.toString() + "")
                Log.e("Error", e.localizedMessage + "")
            }
        }
    }


    private fun askPermission() {
        if (
            !RuntimeAppPermission.checkGrantedPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            || !RuntimeAppPermission.checkGrantedPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )


        ) {
            var permissionsList: ArrayList<String> = ArrayList()
            permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            RuntimeAppPermission.getPermissions(this, permissionsList)
        } else {
            createLocationRequest()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (grantResults.isNotEmpty()) {
            var rejectedPemissionCount = 0
            var success = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    success = false
                    rejectedPemissionCount++
                }
            }
            if (success) {
                createLocationRequest()
            } else {
                var permissionsList: ArrayList<String> = ArrayList()
                permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION)

                RuntimeAppPermission.getRuntimePermissionSnackBar(
                    mBinder.root,
                    rejectedPemissionCount,
                    permissionsList,
                    this
                )
            }
        }
    }

    private fun createLocationRequest() {
        var locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            setupGoogleMap()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this@GetAreaMapActivity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                RESULT_OK -> {
                    setupGoogleMap()
                }
                RESULT_CANCELED -> {
                    Log.e("location Request", "Cancel By User")
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Please enable GPS")
                    builder.setPositiveButton("Enable") { _, _ ->
                        createLocationRequest()
                    }

                    builder.setNegativeButton("Cancel") { _, _ ->
                        finish()
                    }
                    builder.show()
                }
            }
        }
    }

    override fun rowClick(pos: Int, flag: Int) {
    }

    override fun loadMore(pos: Int) {

    }

    private fun openDrawingTypeOptionDialog() {

        BottomSheetDrawingType().newInstance(this, object : DialogCallBacks {
            override fun onDismiss(bundle: Bundle) {

            }

            override fun dialogClick(position: Int, id: Int, drawingType: String) {
                setShapeType(drawingType)
            }
        }).show(supportFragmentManager, "bottomSheet")
    }

    private fun setShapeType(type: String) {
        when (type) {
            AppConstant.DrawingType.WORK_AREA -> {
                CurrentPoly?.let {
                    it.tag = parentWorkArea_ + workAreaId++
                    it.zIndex = (-5).toFloat()

                    allWorkArea.add(it)
                    mBinder.toolbarView.llEdit.visible()
                    drawEdgesPoly(it)
                }
            }
            AppConstant.DrawingType.SHADOW -> {}
            AppConstant.DrawingType.FREE_AREA -> {}

        }
    }

    private fun drawEdgesPoly(pw: Polygon) {
        var vertices = pw.points
        var newPath: ArrayList<LatLng> = ArrayList()
        for (i in vertices.indices) {
            newPath.add(
                SphericalUtil.computeOffset(
                    vertices[i],
                    workAreaConfigurationSettings.offset,
                    SphericalUtil.computeHeading(vertices[i], getCenterPoint(vertices))
                )
            )
        }
        var id = innerPoly_ + pw.tag;
        val exist = innerPolygonArray.find { it.tag == id }
        if (exist != null) {
            innerPolygonArray.remove(exist)
        }

        innerPolygonArray.add(drawPoly(id, "#28F9B120", "#FF0000", newPath))

    }

    private fun setOffset(workAreaID: String) {
        val innerPolyID = innerPoly_ + workAreaID
        val innerPolygon = innerPolygonArray.find { it.tag == innerPolyID }
        if (innerPolygon != null) {
            innerPolygonArray.remove(innerPolygon)
            innerPolygon.remove()
        }
        val workAreaPolygon = allWorkArea.find { it.tag == workAreaID }
        if (workAreaPolygon != null) {
            drawEdgesPoly(workAreaPolygon)
        }

    }

    private fun newApplyPanelBasedOnOffSet(polygon: Polygon) {
        if (polygon == null) return
        innerPolygonBoxes.forEach { it.remove() }
        innerPolygonBoxes.clear()
        var bounds: LatLngBounds.Builder = LatLngBounds.builder()
        var path = polygon.points
        path.map { bounds.include(it) }
        var projection = mMap!!.projection
        val zoom = 21
        val powBase = 2.0.pow(zoom.toDouble())
        val pnlSize = workAreaConfigurationSettings.layoutType.takeLast(4).dropLast(1).split("x")
        var polyOrigin = projection.toScreenLocation(polygon.points[0])
        var rotatingAngle = workAreaConfigurationSettings.azimuthAngle
        var pnlw = 0.0
        var pnlh = 0.0

        if (workAreaConfigurationSettings.panelType) {
            pnlw = workAreaConfigurationSettings.panelW * pnlSize[0].toMyDouble()
            pnlh = workAreaConfigurationSettings.panelH * pnlSize[1].toMyDouble()
        } else {
            pnlw = workAreaConfigurationSettings.panelH * pnlSize[0].toMyDouble()
            pnlh = workAreaConfigurationSettings.panelW * pnlSize[1].toMyDouble()
        }

        pnlh *= cos(degreesToRadians(workAreaConfigurationSettings.panelTilt))
        var pnlpxw = pnlw / (156543.03392 * cos(path[0].latitude * Math.PI / 180) / 2.0.pow(zoom))
        var pnlpxh = pnlh / (156543.03392 * cos(path[0].latitude * Math.PI / 180) / 2.0.pow(zoom))
        var spaceBetween = 0.0
        if (pnlh != 0.0) {
            spaceBetween = workAreaConfigurationSettings.pitchDistance / pnlh
        }
        val sw = bounds.build().southwest
        val ne = bounds.build().northeast
        var swPoint = projection.toScreenLocation(sw)
        var nePoint = projection.toScreenLocation(ne)
        var boxSize = SizeF(pnlpxw.toFloat(), pnlpxh.toFloat())
        var maxX = floor(abs((swPoint.x - nePoint.x)) / boxSize.width)
        var maxY = floor(abs((swPoint.y - nePoint.y)) / boxSize.height)
        var counter = 0
        var panelCount = 0
        var y = -10
        while (y < (maxY * 2).toInt()) {
            /*counter++*/
            for (x in -16..(maxX * 2).toInt()) {
                val posArray: ArrayList<Point> = ArrayList()
                val point1 =
                    Point(
                        ((swPoint.x + boxSize.width * x).toInt()),
                        ((swPoint.y - boxSize.height * y).toInt())
                    )
                val point2 =
                    Point(
                        ((swPoint.x + boxSize.width * (x + 1)).toInt()),
                        ((swPoint.y - boxSize.height * y).toInt())
                    )
                val point3 =
                    Point(
                        ((swPoint.x + boxSize.width * (x + 1)).toInt()),
                        ((swPoint.y - boxSize.height * (y + 1)).toInt())
                    )
                val point4 = Point(
                    ((swPoint.x + boxSize.width * x).toInt()),
                    ((swPoint.y - boxSize.height * (y + 1)).toInt())
                )

                posArray.add(rotatePoint(point1, polyOrigin, rotatingAngle))
                posArray.add(rotatePoint(point2, polyOrigin, rotatingAngle))
                posArray.add(rotatePoint(point3, polyOrigin, rotatingAngle))
                posArray.add(rotatePoint(point4, polyOrigin, rotatingAngle))

                var flag = true
                var coords: ArrayList<LatLng> = ArrayList()
                posArray.forEach { pos ->
                    val posLatLng = projection.fromScreenLocation(pos)
                    coords.add(posLatLng)
                    if (flag) {
                        flag = PolyUtil.containsLocation(posLatLng, polygon.points, true)
                        bounds.include(posLatLng)
                    }
                }
                /*remove panel which intersect shadow*/
                if (flag) {

                }
                /*Draw grid code*/
                if (flag) {
                    panelCount++
                    val grid = drawPoly(
                        "" + polygon.tag + "_panel_$panelCount",
                        "#00000000",
                        "#FFFFFF",
                        coords
                    )
                    innerPolygonBoxes.add(grid)
                }
            }
            y += spaceBetween.toInt()
            y++
        }

        newApplyPanelBasedOnPanelCount(innerPolygonBoxes[0], 0)
    }

    private fun newApplyPanelBasedOnPanelCount(polygon: Polygon, inputPanelCount: Int) {
        if (polygon == null) return
        subboxes.forEach { it.remove() }
        subboxes.clear()
        var bounds: LatLngBounds.Builder = LatLngBounds.builder()
        var path = polygon.points
        path.map { bounds.include(it) }
        var projection = mMap!!.projection
        val zoom = 21
        val powBase = 2.0.pow(zoom.toDouble())
        var polyOrigin = projection.toScreenLocation(polygon.points[0])
        var rotatingAngle = workAreaConfigurationSettings.azimuthAngle
        val pnlSize = workAreaConfigurationSettings.layoutType.takeLast(4).dropLast(1).split("x")
        var pnlw = 0.0
        var pnlh = 0.0

        if (workAreaConfigurationSettings.panelType) {
            pnlw = workAreaConfigurationSettings.panelW
            pnlh = workAreaConfigurationSettings.panelH
        } else {
            pnlw = workAreaConfigurationSettings.panelH
            pnlh = workAreaConfigurationSettings.panelW
        }

        pnlh *= cos(degreesToRadians(workAreaConfigurationSettings.panelTilt))
        var pnlpxw = pnlw / (156543.03392 * cos(path[0].latitude * Math.PI / 180) / 2.0.pow(zoom))
        var pnlpxh = pnlh / (156543.03392 * cos(path[0].latitude * Math.PI / 180) / 2.0.pow(zoom))

        var boxSize = SizeF(pnlpxw.toFloat(), pnlpxh.toFloat())
        var counter = 0
        var panelCount = 0
        var y = -10
        while (y < pnlSize[1].toInt()) {
            /*counter++*/
            for (x in -16..pnlSize[0].toInt()) {
                val posArray: ArrayList<Point> = ArrayList()
                val point1 =
                    Point(
                        ((polyOrigin.x + boxSize.width * x).toInt()),
                        ((polyOrigin.y - boxSize.height * y).toInt())
                    )
                val point2 =
                    Point(
                        ((polyOrigin.x + boxSize.width * (x + 1)).toInt()),
                        ((polyOrigin.y - boxSize.height * y).toInt())
                    )
                val point3 =
                    Point(
                        ((polyOrigin.x + boxSize.width * (x + 1)).toInt()),
                        ((polyOrigin.y - boxSize.height * (y + 1)).toInt())
                    )
                val point4 = Point(
                    ((polyOrigin.x + boxSize.width * x).toInt()),
                    ((polyOrigin.y - boxSize.height * (y + 1)).toInt())
                )

                posArray.add(rotatePoint(point1, polyOrigin, rotatingAngle))
                posArray.add(rotatePoint(point2, polyOrigin, rotatingAngle))
                posArray.add(rotatePoint(point3, polyOrigin, rotatingAngle))
                posArray.add(rotatePoint(point4, polyOrigin, rotatingAngle))

                var flag = true
                var coords: ArrayList<LatLng> = ArrayList()
                posArray.forEach { pos ->
                    val posLatLng = projection.fromScreenLocation(pos)
                    coords.add(posLatLng)
                    if (flag) {
                        flag = PolyUtil.containsLocation(posLatLng, polygon.points, true)
                        bounds.include(posLatLng)
                    }
                }
                /*remove panel which intersect shadow*/
                if (flag) {

                }
                /*Draw Panel code*/
                if (flag) {
                    panelCount++
                    val panel = mMap!!.addPolygon(
                        PolygonOptions()
                            .clickable(false)
                            .addAll(coords)
                    )
                    panel.strokeWidth = 1.5f
                    panel.tag = "" + polygon.tag + "_panel_$panelCount"
                    panel.strokeColor = Color.parseColor("#FFFFFF")
                    panel.fillColor = Color.parseColor("#2B2C3C")
                    panel.zIndex = 17F
                    subboxes.add(panel)
                }
            }
            y++
        }
Log.e("panelCount",""+subboxes.size)
    }

    private fun degreesToRadians(degrees: Double): Double {
        return degrees * (Math.PI / 180)
    }

    private fun rotatePoint(
        point: Point,
        origin: Point,
        angle: Double
    ): Point {
        var angleRad = angle * Math.PI / 180.0;
        return Point(
            (((cos(angleRad) * (point.x - origin.x) - sin(angleRad) * (point.y - origin.y) + origin.x).toInt())),
            (((sin(angleRad) * (point.x - origin.x) + cos(angleRad) * (point.y - origin.y) + origin.y).toInt()))
        )
    }
}


