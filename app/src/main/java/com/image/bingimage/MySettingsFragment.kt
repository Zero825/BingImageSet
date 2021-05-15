package com.image.bingimage

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference


class MySettingsFragment : PreferenceFragmentCompat(){

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val p1= findPreference<SwitchPreference>("auto_set_wallpaper")
        val p2=findPreference<CheckBoxPreference>("set_lock_wallpaper")
        val p3= findPreference<CheckBoxPreference>("set_wallpaper")
        if (p1!!.isChecked){
            p2!!.isVisible = true
            p3!!.isVisible = true
        }
        p1.setOnPreferenceChangeListener { preference, newValue ->
            if (newValue as Boolean){
                p2!!.isVisible = true
                p3!!.isVisible = true
            }else{
                p2!!.isVisible = false
                p3!!.isVisible = false
            }
            true
        }
    }


}


