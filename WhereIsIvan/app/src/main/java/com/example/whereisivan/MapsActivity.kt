package com.example.whereisivan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.File
import java.io.IOException
import kotlin.collections.HashMap


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    private val locationRequestCode = 10
    private val zoomLevel = 5f
    private lateinit var locationManager: LocationManager
    private lateinit var geocoder: Geocoder

    private lateinit var mSoundPool: SoundPool
    private var mLoaded: Boolean = false
    private var mSoundMap: HashMap<Int, Int> = HashMap()

    private val REQUEST_PERMISSION = 100
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath: String


    private val locationListener = object : LocationListener {
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        override fun onLocationChanged(location: Location) {
            updateLocationDisplay(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    }

    private fun updateLocationDisplay(location: Location?) {
        val latitude = location?.latitude ?: 0
        val longitude = location?.longitude ?: 0

        val state = geocoder.getFromLocation(location!!.latitude, location.longitude, 1)

        tv_trenutna_sirina.text = "$longitude"
        tv_trenutna_duzina.text = "$latitude"
        tv_trenutna_drzava.text = state[0].countryName
        tv_trenutno_mjesto.text = state[0].locality
        tv_trenutna_adresa.text = state[0].thoroughfare

        updateMap(location)

    }


    private fun updateMap(location: Location) {

        val currentCity = LatLng(location.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(currentCity).title("Ovdje sam!").snippet("Konačno znam gdje se nalazim!"))
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCity, zoomLevel))

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(this)
        loadSounds()
        trackLocation()

        btn_take_photo.setOnClickListener {
            takePhoto()
        }


    }


    private fun loadSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mSoundPool = SoundPool.Builder().setMaxStreams(10).build()
        } else {
            this.mSoundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }
        this.mSoundPool.setOnLoadCompleteListener { _, _, _ -> mLoaded = true }
        this.mSoundMap[R.raw.drop] = this.mSoundPool.load(this, R.raw.drop, 1)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray) {
        when (requestCode) {
            locationRequestCode -> {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    trackLocation()
                else
                    Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show()
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun trackLocation() {
        if (hasPermissionCompat(locationPermission)) {
            startTrackingLocation()
        } else {
            requestPermisionCompat(arrayOf(locationPermission), locationRequestCode)
        }
    }

    private fun startTrackingLocation() {

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        val provider = locationManager.getBestProvider(criteria, true)
        val minTime = 1000L
        val minDistance = 10.0F
        try {
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener)
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(locationListener)
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_PERMISSION)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sydney = LatLng(-33.865143, 151.209900)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel))
        setMapLongClick(mMap)

    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.addMarker(
                    MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))

            )

            if (this.mLoaded) {
                val soundID = this.mSoundMap[R.raw.drop] ?: 0
                this.mSoundPool.play(soundID, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createCapturedPhoto()
                } catch (ex: IOException) {
                    // If there is error while creating the File, it will be null
                    null
                }
                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                            this,
                            "${BuildConfig.APPLICATION_ID}.fileprovider",
                            it
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createCapturedPhoto(): File {

        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("${tv_trenutno_mjesto.text}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }


}