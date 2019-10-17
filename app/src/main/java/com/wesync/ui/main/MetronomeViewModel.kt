package com.wesync.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel


class MetronomeViewModel : ViewModel() {
    private var _bpmString = MutableLiveData<String>()
    var bpmString : LiveData<String> = _bpmString
    var bpm = Transformations.map(_bpmString) {
        it.toLong()
    }



}
