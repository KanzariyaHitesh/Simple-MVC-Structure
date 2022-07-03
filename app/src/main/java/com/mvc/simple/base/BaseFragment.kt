package com.mvc.simple.base


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mvc.simple.R
import com.mvc.simple.base.BaseActivity
import com.mvc.simple.repository.RepoModel
import com.mvc.simple.util.Constants
import dagger.android.support.AndroidSupportInjection
import org.koin.android.ext.android.inject
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class BaseFragment : Fragment() {

    protected abstract fun initializeViewModel()
    abstract fun observeViewModel()

    var baseActivity: BaseActivity? = null
    val repo: RepoModel by inject()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivity = activity as BaseActivity
        initializeViewModel()
        observeViewModel()
    }

    fun handleShowMessage(message: String) {
        baseActivity!!.showMessageDialog(message)
    }

    fun replaceFragment(
            @NonNull fragment: Fragment,
            backStackName: Boolean = false,
            popStack: Boolean = false,
            @IdRes containerViewId: Int = R.id.mainContainer
    ) {
        baseActivity!!.replaceFragment(fragment, backStackName, popStack, containerViewId)
    }

    fun addFragment(
            @NonNull fragment: Fragment,
            backStackName: Boolean = false,
            aTAG: String = "",
            @IdRes containerViewId: Int = R.id.mainContainer

    ) {
        baseActivity!!.addFragment(fragment, backStackName, aTAG, containerViewId)
    }


    public fun placeMarkerOnMapWithSize(
            mMap: GoogleMap,
            location: LatLng,
            markerImg: Int,
            markerHeight: Int,
            markerWidth: Int
    ): Marker {

        val bitmapdraw = ContextCompat.getDrawable(baseActivity!!, markerImg) as BitmapDrawable
        val b = bitmapdraw.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, markerWidth, markerHeight, false)

        val markerOptions =
                MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
        val marker = mMap.addMarker(markerOptions)

        //val titleStr = getAddress(location)  // add these two lines
        markerOptions.title("")

        mMap.addMarker(markerOptions)

        return marker
    }

    fun clearBackStack() {
        val manager = baseActivity!!.supportFragmentManager
        Log.e("TAG", "Fragment Count-> " + manager.backStackEntryCount)
        if (manager.backStackEntryCount > 0) {
            val first = manager.getBackStackEntryAt(0)
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun shareKit(context: Context, shareBodyText: String, subject: String, SharingOption: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)
        context.startActivity(Intent.createChooser(sharingIntent, SharingOption))
    }

    fun checkPermissionsWithMarkerSet(
            map: GoogleMap,
            marker: Marker?,
            markerImage: Int,
            markerHeight: Int,
            markerWidth: Int,
            cameraZoomLavel: Float,
            isUpdateCamera: Boolean,
            isForcePremission: Boolean
    ): Marker? {
        return baseActivity!!.checkPermissionsWithMarkerSet(
                map,
                marker,
                markerImage,
                markerHeight,
                markerWidth,
                cameraZoomLavel,
                isUpdateCamera,
                isForcePremission
        )
    }

    public fun placeMarkerOnMapWithSize(
            mMap: GoogleMap,
            marker: Marker?,
            latLng: LatLng,
            markerImg: Int,
            markerHeight: Int,
            markerWidth: Int,
            isUpdateCamera: Boolean,
            cameraZoomLavel: Float = 15.0f
    ) {
        baseActivity?.placeMarkerOnMapWithSize(
                mMap,
                marker,
                latLng,
                markerImg,
                markerHeight,
                markerWidth,
                isUpdateCamera,
                cameraZoomLavel
        )
    }

    public fun locationEnabled() {
        val lm = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (!gps_enabled && !network_enabled) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(baseActivity!!)
//        builder.setTitle(getString(R.string.dialog_permission_title))
//        builder.setMessage(getString(R.string.dialog_permission_message))
            //builder.setTitle("Permission")
            builder.setMessage("Enable GPS Service")

            builder.setPositiveButton("Settings") { dialog, which ->
                dialog.cancel()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }

            builder.setNegativeButton(
                    getString(android.R.string.cancel)
            ) { dialog, which ->
                dialog.cancel()
                locationEnabled()
            }
            builder.setCancelable(false)
            builder.show()

        } else {
            // todo lovation enabled
        }
    }

    open fun hideKeyboard() {
        val imm: InputMethodManager = baseActivity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = baseActivity?.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(baseActivity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setMagin(view: View, left: Int, right: Int, top: Int, bottom: Int) {
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(left, top, right, bottom)
        view.layoutParams = params
    }

    fun isInternetConnected(): Boolean {
        return baseActivity!!.isInternetConnected()
    }
    fun showLoading() {
        baseActivity?.showLoading();
    }

    fun hideLoading() {
        baseActivity?.hideLoading();
    }

    fun msgDialog(msg: String, dialogTye: String? = Constants.Param.ERROR) {
        if (activity != null) {
            (activity as BaseActivity).msgDialog(msg, dialogTye)
        }
    }

    open fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^*?&+.=!])(?=\\S+$).{8,}$"
//        val PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]])(?=.*[$@$!%*?&#.])[A-Za-z0-9$@$!%*?&#.]{8,}"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

}