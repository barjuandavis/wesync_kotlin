package com.wesync.util

import androidx.databinding.InverseMethod

object Converters {

    fun toBPM(s: CharSequence) : Long {
        return s.toString().toLong()
    }

    @InverseMethod("toBPM")
    fun fromBPM(bpm: Long) : CharSequence {
        return "$bpm"
    }
}