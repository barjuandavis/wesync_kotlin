package com.wesync


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders

class MainActivity : AppCompatActivity() {

    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)

    }

    override fun onStop() {
        super.onStop()
        Log.d("onStop","Activity is not visible")
    }

    override fun onDestroy() {
        Log.d("onDestroy","Activity is destroyed")
        super.onDestroy()
    }

}
