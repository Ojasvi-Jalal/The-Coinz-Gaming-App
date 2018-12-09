package com.example.ojasvi.coinz

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log.d
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNUSED_EXPRESSION")
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
    private var shilRate = ""
    private  var quidRate = ""
    private var dolrRate = ""
    private var penyRate = ""
    private var bankButton: ImageView? = null

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
    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        storeWallet?.firestoreSettings = settings

        Mapbox.getInstance(this, getString(R.string.ACCESS_TOKEN))
        storeWallet = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        bankButton = findViewById(R.id.piggyBank)

        mapView = findViewById(R.id.mapboxMapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        doAsync {
            coins = DownloadFileTask("http://homepages.inf.ed.ac.uk/stg/coinz/$formattedDate/coinzmap.geojson").run()
            d(tag, coins)
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap?){
        if(mapboxMap == null){
            d(tag,"[onMapReady] mapboxMap is null")
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

            val prefSettings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

            shilRate = prefSettings.getString("shil rate", "")
            quidRate = prefSettings.getString("quid rate", "")
            dolrRate = prefSettings.getString("dolr rate", "")
            penyRate = prefSettings.getString("peny rate", "")

            val rates  = JSONObject(coins).getJSONObject("rates")
            quidRate = rates.get("QUID").toString()
            d(tag,quidRate)
            shilRate = rates.get("SHIL").toString()
            d(tag,shilRate)
            dolrRate = rates.get("DOLR").toString()
            d(tag,dolrRate)
            penyRate = rates.get("PENY").toString()
            d(tag,penyRate)

            //val source = GeoJsonSource("my.data.source",featureCollection)
            val features : List<Feature>? = featureCollection.features()
            val ref = storeWallet?.collection(COLLECTION_KEY)?.document(mAuth?.currentUser?.email!!)
            ref
                    ?.collection("wallet")
                    ?.get()
                    ?.addOnCompleteListener { task ->
                if (task.result != null) {
                    for (document in task.result!!)
                        coinPresent.add(document.toObject(Coin::class.java))
                    if (features != null) {
                        for (feature: Feature in features) {
                            val geo = feature.geometry() as com.mapbox.geojson.Point

                            val currency = feature.getStringProperty("currency")

                            var coinIcon = R.drawable.test
                            if (currency == "DOLR") {
                                coinIcon = R.drawable.dolr
                            }
                            else if (currency == "QUID")
                                coinIcon = R.drawable.quid
                            else if (currency == "PENY")
                                coinIcon = R.drawable.peny
                            else if (currency == "SHIL")
                                coinIcon = R.drawable.shil

                            val icons = IconFactory.getInstance(this)

                            if (downloadDate == formattedDate) {
                                if (coinPresent.map { it -> it.id }.contains(feature.getStringProperty("id")))
                                    d(tag, "Won't add this marker")
                                else {
                                    map?.addMarker(MarkerOptions()
                                            .position(LatLng(geo.latitude(), geo.longitude()))
                                            .title(currency)
                                            .icon(icons.fromResource(coinIcon)))
                                }
                            } else {
                                map?.addMarker(MarkerOptions()
                                        .position(LatLng(geo.latitude(), geo.longitude()))
                                        .title(currency)
                                        .icon(icons.fromResource(coinIcon)))
                                downloadDate = formattedDate
                            }
                            bankButton?.setOnClickListener{
                                d(MainMenuActivity.TAG,"Opening the bank")
                                val intent = Intent(this, BankActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
                true
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
                            val currency =  feature.getStringProperty("currency")
                            val value = feature.getStringProperty("value")
                            val id = feature.getStringProperty("id")
                            var coin: Coin = Coin(currency,value,id)
                            d(tag,coin.toString())
                            val geo = feature.geometry() as com.mapbox.geojson.Point
                            if(markerLoc.latitude == geo.latitude() && markerLoc.longitude == geo.longitude()){
                                wallet.add(coin)
                                ref?.collection("wallet")?.add(coin)
                            }
                        }
                    }
                    map?.removeMarker(marker)
                    toast("You collected a coin!")
                    d(tag,wallet.toString())
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
           d(tag, "Permissions are granted")
            initialiseLocationEngine()
            initialiseLocationLayer()
        }
        else{
            d(tag,"Permissions are not granted")
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
            d(tag,"[onLocationChanged] location is null")
        }else{
            originLocation = location
            setCameraPosition(originLocation)
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        d(tag,"[onConnected] requesting location updates")
        locationEngine.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        d(tag, "Permissions: $permissionsToExplain")
        //present popup message or dialog
    }

    override fun onPermissionResult(granted: Boolean) {
        d(tag,"[onPermissionResult] granted == $granted")
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
        downloadDate= settings.getString("lastDownloadDate", "")
        shilRate    = settings.getString("shilRate","")
        quidRate    = settings.getString("quidRate","")
        penyRate    = settings.getString("quidRate","")
        dolrRate    = settings.getString("quidRate","")
        // Write a message to ”logcat” (for debugging purposes)
        d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")
    }
    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationLayer(){
        if(mapView == null){
            d(tag,"mapView is null") }
        else{
            if(map == null){
                d(tag,"map is null")
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
        d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        editor.putString("shilRate",shilRate)
        editor.putString("quidRate",quidRate)
        editor.putString("dolrRate",dolrRate)
        editor.putString("penyRate",penyRate)

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
