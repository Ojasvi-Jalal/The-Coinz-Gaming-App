package com.example.ojasvi.coinz

import android.content.Context
import android.content.pm.FeatureInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.location.Location
import android.os.Bundle
import android.location.LocationManager
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdate
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.jetbrains.anko.*

import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// OnMapReadyCallback,
class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener {

    private val tag = "MainActivity"
    private var mapView: MapView? = null
    private var map: MapboxMap? = null

    private var downloadDate = "" // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile" // for storing preferences

    private lateinit var originLocation: Location
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationLayerPlugin : LocationLayerPlugin
    private val date = LocalDate.now()
    private val formatDate = DateTimeFormatter.ofPattern("uuuu/MM/dd")
    private val formattedDate = date.format(formatDate)
    private var coins = ""

    //Saving collected coins
    //initialise firestore
    private var storeWallet: FirebaseFirestore? = null

    //initialise dataset
   // private var wallet: MutableList<Coin> = MutableList()
    private val wallet = mutableListOf<Coin>()
    private var mAuth: FirebaseAuth? = null
    private val coinPresent = mutableListOf<Coin>()

    companion object {
        private const val COLLECTION_KEY = "wallets"
        private const val DOCUMENT_KEY = "User"
        private const val NAME_FIELD = "Currency"
        private const val TEXT_FIELD = "Worth in Gold"
    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //o.j@setSupportActionBar(toolbar)
        Mapbox.getInstance(this, getString(R.string.ACCESS_TOKEN))
        storeWallet = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        storeWallet?.firestoreSettings = settings

        mapView = findViewById(R.id.mapboxMapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        doAsync {
            coins = DownloadFileTask("http://homepages.inf.ed.ac.uk/stg/coinz/$formattedDate/coinzmap.geojson").run()
            Log.d(tag, coins)
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap?){
        if(mapboxMap == null){
            Log.d(tag,"[onMapReady] mapboxMap is null")
        }
        else{
            map = mapboxMap
            //Set user interface options
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true

            //Make location information available
            enableLocation()
            //add the coins
            val featureCollection : FeatureCollection = FeatureCollection.fromJson(coins)
            //val source = GeoJsonSource("my.data.source",featureCollection)
            val features : List<Feature>? = featureCollection?.features()
            storeWallet?.collection(COLLECTION_KEY)?.document(mAuth?.uid!!)?.collection("wallet")?.get()?.addOnCompleteListener { task ->
                if(task.result !=null) {
                    for (document in task.result!!)
                        coinPresent.add(document.toObject(Coin::class.java))
                    if(features != null) {
                        for (feature: Feature in features) {
                            var geo = feature.geometry() as com.mapbox.geojson.Point

                            var currency = feature.getStringProperty("currency")

                            var coinIcon = R.drawable.test
                            if(currency == "DOLR")
                                coinIcon = R.drawable.dolr
                            else if (currency== "QUID")
                                coinIcon = R.drawable.quid
                            else if (currency == "PENY")
                                coinIcon = R.drawable.peny
                            else if (currency == "SHIL")
                                coinIcon = R.drawable.shil

                            val icons = IconFactory.getInstance(this)

                            if(coinPresent.map { it -> it.id }.contains(feature.getStringProperty("id")) )
                                Log.d(tag,"Won't add this marker")
                            else {
                                map?.addMarker(MarkerOptions()
                                        .position(LatLng(geo.latitude(), geo.longitude()))
                                        .title(currency)
                                        .icon(icons.fromResource(coinIcon)))
                            }
                        }
                    }
                }
            }

            //collect the coins
            map?.setOnMarkerClickListener { marker ->
                val markerLocation = Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = marker.position.latitude
                    longitude = marker.position.longitude
                }
                if(originLocation.distanceTo(markerLocation)<2005) {
                    val markerLoc =  marker.position
                    if(features != null) {
                        for (feature: Feature in features){
                            var currency =  feature.getStringProperty("currency")
                            var value = feature.getStringProperty("value")
                            var id = feature.getStringProperty("id")
                            var coin: Coin = Coin(currency,value,id)
                            Log.d(tag,coin.toString())
                            var geo = feature.geometry() as com.mapbox.geojson.Point
                            if(markerLoc.latitude == geo.latitude() && markerLoc.longitude == geo.longitude()){
                                wallet.add(coin)
                                storeWallet?.collection(COLLECTION_KEY)?.document(mAuth?.uid!!)?.collection("wallet")?.add(coin)
                            }
                        }
                    }
                    map?.removeMarker(marker)
                    toast("You collected a coin!")
                    Log.d(tag,wallet.toString())
                }
                else
                {
                    toast("Coin not within 25 meters, sorry! :(")
                }
                true
            }
        }
    }

    private fun enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
           Log.d(tag, "Permissions are granted")
            initialiseLocationEngine()
            initialiseLocationLayer()
        }
        else{
            Log.d(tag,"Permissions are not granted")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationEngine(){
        locationEngine = LocationEngineProvider(this)
                .obtainBestLocationEngineAvailable()
        locationEngine.apply {
            interval = 5000 // preferably every 5 seconds
            fastestInterval = 1000 // at most every second
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine.lastLocation
        if(lastLocation!=null){
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        }else{
            locationEngine.addLocationEngineListener(this)
        }
    }

    private fun setCameraPosition(location: Location){
        val latlng = LatLng(location.latitude,location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
    }

    override fun onLocationChanged(location: Location?) {
        if(location == null){
            Log.d(tag,"[onLocationChanged] location is null")
        }else{
            originLocation = location
            setCameraPosition(originLocation)
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        Log.d(tag,"[onConnected] requesting location updates")
        locationEngine.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d(tag, "Permissions: $permissionsToExplain")
        //present popup message or dialog
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag,"[onPermissionResult] granted == $granted")
        if(granted){
            enableLocation()
        }
        else{
            //Open a dialogue with the user
        }
    }

    public override fun onStart(){
        super.onStart()
        mapView?.onStart()
        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "")
        // Write a message to ”logcat” (for debugging purposes)
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")
    }
    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationLayer(){
        if(mapView == null){
            Log.d(tag,"mapView is null") }
        else{
            if(map == null){
                Log.d(tag,"map is null")
            }
            else{
                locationLayerPlugin = LocationLayerPlugin(mapView!!,map!!,locationEngine)
                locationLayerPlugin.apply{
                    setLocationLayerEnabled(true)
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView?.onStop()
        Log.d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}
