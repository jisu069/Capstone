package com.example.navigationwalkers

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class Frag3 : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
