package com.mvc.simple.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.mvc.simple.model.LabelListResponse


class SharedPreference(mContext: Context) {

    private val prefMode: Int = 0
    private var PRIVATE_MODE = 0
    private var sharedPref: SharedPreferences = mContext.getSharedPreferences(Constants.prefName, prefMode)
    val editor: SharedPreferences.Editor = sharedPref.edit()

    private var langPref: SharedPreferences = mContext.getSharedPreferences(Constants.labelPrefName, PRIVATE_MODE)

    fun clearSharedPreference(): Boolean {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        //sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        val token = fcm_token
        editor.clear()
        editor.putString("fcm_token", token)
        return editor.commit()
    }

    fun removeValue(KEY_NAME: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove(KEY_NAME)
        editor.apply()
    }

    var isLogin: Boolean?
        get() = sharedPref.getBoolean("isLogin", false)
        set(value) {
            editor.putBoolean("isLogin", value!!).commit()
        }

    var isOwnerLogin: Boolean
        get() = sharedPref.getBoolean("isOwnerLogin", false)
        set(value) {
            editor.putBoolean("isOwnerLogin", value!!).commit()
        }


    var isGuest: Boolean?
        get() = sharedPref.getBoolean("isGuest", false)
        set(value) {
            editor.putBoolean("isGuest", value!!).commit()
        }

    var fcm_token: String? = ""
        get() {
            return langPref.getString(Constants.Preference.access_token, "")
        }
        set(value) {
            field = value
            val editor: SharedPreferences.Editor = langPref.edit()
            editor.putString(Constants.Preference.access_token, value)
            editor.apply()

        }

    fun setLanguageCode(code: String) {
        langPref.edit().putString(Constants.Preference.LANGUAGE_CODE, code).apply()
    }

    fun getLanguageCode(): String {
        return langPref.getString(Constants.Preference.LANGUAGE_CODE, "").toString()
    }


    fun getLabel(key: String): String {
        return langPref.getString(key, "").toString()
    }

    @SuppressLint("CommitPrefEdits")
    fun initLabel(labelList: List<LabelListResponse.Payload>) {

        val edit = langPref.edit()
        for (label in labelList) {
            Log.e(
                "languagelabel",
                "languagelabel = public static final String " + label.code + " = \"" + label.code + "\"" + ";"
            )
            edit.putString(label.code, label.value)
        }
        edit.apply()
    }


}