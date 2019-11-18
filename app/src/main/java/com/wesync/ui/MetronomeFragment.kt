package com.wesync.ui


import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.InputType

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.wesync.metronome.MetronomeService
import com.wesync.R
import com.wesync.MainViewModel
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.MetronomeFragmentBinding
import com.wesync.util.service.ServiceSubscriber
import com.wesync.util.UserTypes

class MetronomeFragment : Fragment(){

    private lateinit var mainViewModel    : MainViewModel
    private lateinit var binding            : MetronomeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = DataBindingUtil.inflate(inflater,R.layout.metronome_fragment,container,false)
       return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let{
            mainViewModel = ViewModelProvider.AndroidViewModelFactory.
                getInstance(it.application).create(MainViewModel::class.java)
            binding.viewmodel = mainViewModel
            binding.lifecycleOwner = it
            subscribeToViewModel()
        }
    }

    private fun subscribeToViewModel() {
        binding.newSession.setOnClickListener(OnConnectionFragmentClickListener())
        binding.joinSession.setOnClickListener(OnConnectionFragmentClickListener())
    }

    inner class OnConnectionFragmentClickListener: View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.new_session -> onNewSessionButtonClicked()
                R.id.join_session -> onJoinSessionButtonClicked()
            }
        }
    }


    private fun onNewSessionButtonClicked() {
        val builder = AlertDialog.Builder(context)
        if (mainViewModel.userType.value == UserTypes.SOLO) {
            builder.setTitle("What is your Name?")
            val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                mainViewModel.onNewSession(input.text?.toString())
                if (mainViewModel.isPlaying.value!!) mainViewModel.flipIsPlaying()
            }

            builder.setNegativeButton("Cancel") {
                    dialog, _ -> dialog.cancel()
            }

            builder.show()
        }
        else  {
            var t = "Are you sure want to end this Session?"
            if (mainViewModel.userType.value == UserTypes.SLAVE)
                t = "Are you sure want to quit this Session?"
            builder.setTitle(t)

            builder.setPositiveButton("OK") { _, _ ->
                mainViewModel.endSession()
                if (mainViewModel.isAdvertising.value!!) mainViewModel.toggleAdvertise()
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }


    private fun onJoinSessionButtonClicked() {
        if (mainViewModel.userType.value == UserTypes.SOLO) {
            findNavController().navigate(
                MetronomeFragmentDirections
                    .actionMetronomeFragmentToConnectionFragment())
        }
        else if (mainViewModel.userType.value == UserTypes.SESSION_HOST) {
            mainViewModel.toggleAdvertise()
        }
    }
}
