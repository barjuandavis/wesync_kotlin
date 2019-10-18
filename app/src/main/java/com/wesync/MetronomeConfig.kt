package com.wesync

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

object MetronomeConfig {
    var bpm: Long = 1
    var observer = Observer<Long> {
        Log.d("observer","observed!")
        bpm = it
    }

}

