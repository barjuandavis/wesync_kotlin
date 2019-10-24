package com.wesync


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.wesync.ui.main.MetronomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentNavHost, MetronomeFragment.newInstance()).commitNow()
        }

    }


}
