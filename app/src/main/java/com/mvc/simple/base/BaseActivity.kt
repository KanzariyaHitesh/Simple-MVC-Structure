package com.mvc.simple.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.mvc.simple.repository.RepoModel
import com.mvc.simple.util.Constants
import com.mvc.simple.util.GPSTracker
import dagger.android.AndroidInjection
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*
import com.mvc.simple.R
import com.mvc.simple.util.GlobalMethods
import com.mvc.simple.util.MessageDialog

/**
 * Created by Hitesh Kanzariya
 */

abstract class BaseActivity : AppCompatActivity() {

    lateinit var gpsTracker: GPSTracker
    val REQUEST_CHECK_SETTINGS = 1221

    protected abstract fun initializeViewModel()
    abstract fun observeViewModel()
    protected abstract fun initViewBinding()

    private lateinit var progressDialog: Dialog
    val msgDialog = MessageDialog.getInstance()!!
    val repo: RepoModel by inject()

    internal var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        initViewBinding()
        initializeViewModel()
        observeViewModel()
        gpsTracker = GPSTracker(this@BaseActivity)


        progressDialog = Dialog(this, R.style.MaterialDialogSheet)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.progress_loader)
        //val progressImage = progressDialog.findViewById<ImageView>(R.id.ivProgress)
        //Glide.with(this).load(R.drawable.loading).into(progressImage)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        //setLocale(this)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun showProgressDialog() {
        if (!progressDialog.isShowing && (!this.isFinishing)) {
            progressDialog.show()
        }
    }

    fun dismissProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getConvertUTC(format:String?="yyyy-MM-dd HH:mm:ss",dateInString: String): String? {
        val outputFmt = SimpleDateFormat(""+format)
        val date: Date = outputFmt.parse(dateInString)
        outputFmt.timeZone = TimeZone.getTimeZone("UTC")
        return outputFmt.format(date)
    }

    fun showMessageDialog(msg: String, isConfirm: Boolean = false,msgOkBtn:String?="Yes",msgNoBtn:String?="No") {
        val bundle = Bundle()
        bundle.putString("tvMsgText", msg)
        if (isConfirm) {
            bundle.putString("okTxt", ""+msgOkBtn)
            bundle.putString("cancelTxt", ""+msgNoBtn)
        } else {
            bundle.putString("okTxt", ""+msgOkBtn)
        }

        msgDialog.arguments = bundle
        if (msgDialog != null && msgDialog.isAdded)
            msgDialog.dismiss()

        if (!this.isFinishing) {
            msgDialog.show(this.supportFragmentManager, "")
        }
    }

    fun replaceFragment(
            @NonNull fragment: Fragment,
            backStackName: Boolean = false,
            popStack: Boolean = false,
            @IdRes containerViewId: Int = R.id.mainContainer
    ) {
        val transition = supportFragmentManager.beginTransaction()
        /* transition.setCustomAnimations(
                 android.R.anim.fade_in,
                 android.R.anim.fade_out,
                 android.R.anim.fade_in,
                 android.R.anim.fade_out
         )*/
        transition.setCustomAnimations(
                R.anim.slide_in_from_right,
                R.anim.slide_out_from_left,
                R.anim.slide_in_from_left,
                R.anim.slide_out_from_right
        )

        if (popStack)
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        if (backStackName)
            transition.addToBackStack("")

        transition.replace(containerViewId, fragment).commitAllowingStateLoss()
    }

    fun addFragment(
            @NonNull fragment: Fragment,
            backStackName: Boolean = false,
            aTAG: String = "",
            @IdRes containerViewId: Int
    ) {
        /*supportFragmentManager
            .beginTransaction()
            .add(containerViewId, fragment)
            .commit()*/

        val transition = supportFragmentManager.beginTransaction()
        if (backStackName)
            transition.addToBackStack(aTAG)

        transition.addToBackStack(null)

        transition.add(containerViewId, fragment).commitAllowingStateLoss()
    }


    lateinit var tempMap: GoogleMap
    var tempMarker: Marker? = null
    lateinit var tempLatLong: LatLng
    var tempMarkerImage: Int = 0
    var tempMarkerHeight: Int = 0
    var tempMarkerWidth: Int = 0
    var tempCameraZoomLavel = Constants.cameraZoomLavel15_0_f
    var tempIsUpdateLocation = false
    var tempIsForcePremission = false

    fun setMarker() {
        tempLatLong = LatLng(gpsTracker.latitude, gpsTracker.longitude)
        tempMarker = placeMarkerOnMapWithSize(
                tempMap!!,
                tempMarker,
                tempLatLong,
                tempMarkerImage,
                tempMarkerHeight,
                tempMarkerWidth,
                tempIsUpdateLocation,
                tempCameraZoomLavel
        )
        tempIsUpdateLocation = false
    }

    fun setMarker(
            latLng: LatLng,
            marker: Marker?,
            mMap: GoogleMap?,
            cameraZoomLavel: Float = Constants.cameraZoomLavel15_0_f,
            isAnimateCamera: Boolean = true
    ): Marker? {
        Log.e(
                TAG,
                "setMarker = latLng=${latLng.latitude},${latLng.longitude}"
        )
        if (gpsTracker.latitude == 0.0 && gpsTracker.longitude == 0.0) {
            gpsTracker = GPSTracker(this@BaseActivity)
            setMarker()
        }
        val cameraPosition = CameraPosition.fromLatLngZoom(latLng, cameraZoomLavel)
        val cu = CameraUpdateFactory.newCameraPosition(cameraPosition)

        if (isAnimateCamera) {
            mMap?.animateCamera(cu)
        } else {
            mMap?.moveCamera(cu)
        }
        marker!!.position = latLng

        return marker
    }

    fun checkPermissionsWithMarkerSet(
            map: GoogleMap,
            marker: Marker?,
            markerImage: Int = 0,
            markerHeight: Int = 30,
            markerWidth: Int = 30,
            cameraZoomLavel: Float = Constants.cameraZoomLavel15_0_f,
            isUpdateCamera: Boolean = false,
            isForcePremission: Boolean = false
    ): Marker? {

        tempMap = map
        tempMarker = marker
        tempMarkerImage = markerImage
        tempMarkerHeight = markerHeight
        tempMarkerWidth = markerWidth
        tempCameraZoomLavel = cameraZoomLavel
        tempIsUpdateLocation = isUpdateCamera
        tempIsForcePremission = isForcePremission

        var isPermission = permissionChecking()

        tempMarker = placeMarkerOnMapWithSize(
                tempMap!!,
                tempMarker,
                LatLng(gpsTracker.latitude, gpsTracker.longitude),
                tempMarkerImage,
                tempMarkerHeight,
                tempMarkerWidth,
                tempIsUpdateLocation,
                tempCameraZoomLavel
        )

        return tempMarker
    }

    fun permissionChecking(): Boolean {
        var isPermission = false
        if (ActivityCompat.checkSelfPermission(
                        this@BaseActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this@BaseActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (gpsTracker.canGetLocation()) {
                if (gpsTracker.latitude == 0.0 || gpsTracker.longitude == 0.0) {
                    displayLocationSettingsRequest(this@BaseActivity)
                } else {
                    setMarker()
                }
            } else {
                displayLocationSettingsRequest(this@BaseActivity)
            }
            // displayLocationSettingsRequest(this@BaseActivity)
            isPermission = true
        } else {
            requestLocationPermissions()
            isPermission = false
        }
        return isPermission
    }

    private val LOCATION_PERMS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    )

    val LOCATION_REQUEST: Int = 1003
    fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
                this@BaseActivity,
                LOCATION_PERMS,
                LOCATION_REQUEST
        )
    }

    public fun placeMarkerOnMapWithSize(
            mMap: GoogleMap,
            mMarker: Marker?,
            latLng: LatLng,
            markerImg: Int,
            markerHeight: Int,
            markerWeight: Int,
            isUpdateCamera: Boolean,
            cameraZoomLavel: Float = Constants.cameraZoomLavel15_0_f

    ): Marker? {

        val bitmapdraw = ContextCompat.getDrawable(this, markerImg) as BitmapDrawable
        val b = bitmapdraw.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, markerWeight, markerHeight, false)

        val markerOptions =
                MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))

        //val marker = mMap!!.addMarker(markerOptions)
        var marker = mMarker
        //tempMarker = mMap!!.addMarker(markerOptions)

        try {
            marker.let {
                marker!!.position = latLng
            }
        } catch (e: Exception) {
            marker = mMap!!.addMarker(markerOptions)
        }


        //val titleStr = getAddress(location)  // add these two lines
//        markerOptions.title("Current location")
        marker!!.title = ""
//        mMap!!.addMarker(markerOptions)

        if (isUpdateCamera) {
            val cameraPosition = CameraPosition.fromLatLngZoom(latLng, cameraZoomLavel)
            val cu = CameraUpdateFactory.newCameraPosition(cameraPosition)
            mMap?.animateCamera(cu)
        }
        return marker
    }

    var TAG = " BaseActivity"

    private fun displayLocationSettingsRequest(context: Context?) {
        val googleApiClient = GoogleApiClient.Builder(context!!)
                .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000 / 2.toLong()
        val builder =
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val pendingResult: PendingResult<LocationSettingsResult> =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        pendingResult.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.e(TAG, "All location settings are satisfied.")
                    //iniMap()
                    if (tempIsUpdateLocation) {
                        setCurrentLocation()
                    }
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.e(
                            TAG,
                            "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try { // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(
                                context as Activity?,
                                REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    Log.e(
                            TAG,
                            "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                    )
                }
            }
        }
    }

    fun setCurrentLocation() {
        Log.e(TAG, " gpsTracker->${gpsTracker.location}")
        Log.e(
                TAG,
                " latitude,longitude->${gpsTracker.latitude},${gpsTracker.longitude}"
        )
        if (gpsTracker.canGetLocation()) {

            if (gpsTracker.latitude == 0.0 || gpsTracker.longitude == 0.0) {
                displayLocationSettingsRequest(this@BaseActivity)
                return
            } else {
                setMarker()
                //setAddressFromLocation(gpsTracker.getLatitude(), gpsTracker.getLongitude())
            }
        }
    }

    val cameraZoomLavel11_0_f = 11.0f
    val cameraZoomLavel12_0_f = 12.0f
    val cameraZoomLavel13_0_f = 13.0f
    val cameraZoomLavel14_0_f = 14.0f
    val cameraZoomLavel15_0_f = 15.0f
    val cameraZoomLavel16_0_f = 16.0f
    val cameraZoomLavel17_0_f = 17.0f
    val cameraZoomLavel18_0_f = 18.0f
    val cameraZoomLavel19_0_f = 19.0f
    val cameraZoomLavel20_0_f = 20.0f

    @SuppressLint("SetTextI18n")
    open fun setCustomizationMarker(
            map: GoogleMap,
            mMarker: Marker?,
            latLng: LatLng,
            msgForMarker: String,
            selected: Boolean
    ): Marker {
// todo set specific image
// LatLng latLng;
        var marker = mMarker
        // todo set customise marker icon
        val viewMarker: View =
                (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                        .inflate(R.layout.layout_simple_marker, null)
        val tvName: TextView = viewMarker.findViewById(R.id.tvName)
        val llSelectMain: LinearLayout = viewMarker.findViewById(R.id.llSelectMain)
        if (selected) {
            llSelectMain.background = resources.getDrawable(R.drawable.bg_black_rounded_small)
            tvName.setTextColor(ContextCompat.getColor(baseContext, R.color.colorWhite))
        } else {
            tvName.setTextColor(ContextCompat.getColor(baseContext, R.color.colorBlack))
            llSelectMain.background = resources.getDrawable(R.drawable.bg_white_rounded_small)
        }
        tvName.text = "" + msgForMarker
        val bmp: Bitmap = createDrawableFromView(this, viewMarker)!!
        val markerOptions = MarkerOptions()
        markerOptions.position(LatLng(latLng.latitude, latLng.longitude))
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp))
        markerOptions.flat(true)
        run {
            marker = map.addMarker(markerOptions)
        }
        return marker!!
    }

    open fun createDrawableFromView(
            context: Context,
            view: View
    ): Bitmap? {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay
                .getMetrics(displayMetrics)
        view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(
                0, 0, displayMetrics.widthPixels,
                displayMetrics.heightPixels
        )
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(260, 165, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    var isConnected: Boolean = false
    fun isInternetConnected(): Boolean {
        isConnected = GlobalMethods.isInternetAvailable(baseContext)
        if (!isConnected) {
            showInternetDialog()
        }
        return isConnected

    }

     var timer: CountDownTimer? = null
    fun showInternetDialog() {
        val msgDialog = MessageDialog.getInstance()
        val bundle = Bundle()
        bundle.putString(
                "tvMsgText",
                "No internet connection"
        )
        bundle.putString("okTxt", "OK")
        bundle.putString("msgType", "" + Constants.Param.ERROR)
        msgDialog.setArguments(bundle)
        msgDialog.show(supportFragmentManager, "")
        timer = object : CountDownTimer(3500, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
//                timer!!.cancel()
                if (msgDialog.isVisible)
                    msgDialog!!.dismiss()
            }
        }.start()
        msgDialog.setListener(object : MessageDialog.OnClicks {
            override fun set(ok: Boolean) {
                msgDialog.dismiss()
                if (msgDialog.isVisible)
                    timer!!.cancel()
            }


        })

    }

    @Synchronized
    fun showLoading() {
        if (dialog == null) {
            dialog = Dialog(this)
        }
        dialog!!.setContentView(R.layout.progress_loader)
        // ((TextView) dialog.findViewById(R.id.tvMsg)).setText(getString(R.string.text_please_wait));
        dialog!!.window!!.setBackgroundDrawableResource(R.color.transparent)
        dialog!!.show()
    }

    fun hideLoading() {
        if ((dialog != null) and dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    fun msgDialog(msg: String, dialogType: String? = Constants.Param.ERROR) {
        var dialogMsg = MessageDialog.getInstance()
        val bundle = Bundle()
        bundle.putString("tvMsgText", msg)
        bundle.putString("okTxt", "OK")
        bundle.putString("msgType", "" + dialogType)
        dialogMsg.arguments = bundle
//        if (dialogType.equals(Constants.Param.ERROR)) {
//            setStatusBar(ContextCompat.getColor(baseContext!!, R.color.colorPrimary))
//        } else {
//            setStatusBar(ContextCompat.getColor(baseContext!!, R.color.colorPrimary))
//        }
        if (dialogMsg.isAdded) {
            return
        }
        dialogMsg.show(supportFragmentManager, "")
       /* timer = object : CountDownTimer(3500, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
//                setStatusBar(ContextCompat.getColor(baseContext!!, R.color.transparent))
                timer!!.cancel()
                if (dialogMsg.isVisible)
                    dialogMsg!!.dismiss()
            }
        }.start()*/
        dialogMsg.setListener(object : MessageDialog.OnClicks {
            override fun set(ok: Boolean) {
                dialogMsg.dismiss()
                if (dialogMsg.isVisible)
                    timer!!.cancel()

            }
        })
    }

    fun setTexts(
            view: View,
            wedgetType: String,
            defaultValue_IfTextNull: String,
            value_For_set_In_View: String
    ) {

        var textValue =
                if (!value_For_set_In_View.isNullOrEmpty()) value_For_set_In_View else defaultValue_IfTextNull

        if (wedgetType.equals(Constants.Param.TYPE_EDITTEXT_SETHINT)) {
            view as EditText
            view.hint = "$textValue"
        }

        if (wedgetType.equals(Constants.Param.TYPE_EDITTEXT_SETTEXT)) {
            view as EditText
            view.setText("$textValue")
        }

        if (wedgetType.equals(Constants.Param.TYPE_TEXTVIEW)) {
            view as TextView
            view.text = "$textValue"
        }

        if (wedgetType.equals(Constants.Param.TYPE_BUTTON)) {
            view as Button
            view.text = "$textValue"
        }
    }
}