package com.wesync.ui.main


import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.wesync.metronome.MetronomeService
import com.wesync.R
import com.wesync.SharedViewModel
import com.wesync.connection.ConnectionManagerService
import com.wesync.databinding.MetronomeFragmentBinding
import com.wesync.util.ConnectionCodes
import com.wesync.util.ServiceSubscriber
import com.wesync.util.UserTypes

class MetronomeFragment : Fragment() {

    private lateinit var viewModel          : MetronomeViewModel
    private lateinit var sharedViewModel    : SharedViewModel
    private lateinit var binding            : MetronomeFragmentBinding
    private lateinit var subscriber         : ServiceSubscriber
    private var mService                    : MetronomeService? = null
    private var mCService                   : ConnectionManagerService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = DataBindingUtil.inflate(inflater,R.layout.metronome_fragment,container,false)
        viewModel = ViewModelProviders.of(this)
            .get(MetronomeViewModel::class.java)
       binding.setLifecycleOwner { this.lifecycle }
       binding.viewmodel = viewModel
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("onViewCreated","MetronomeFragment Created!")
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        doBindService()
    }
    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }

    private fun doBindService() {
        try {
            subscriber = ServiceSubscriber(activity!!.applicationContext, activity)
            subscriber.connServiceConnected.observe(this, Observer {
                if (it) mCService = subscriber.connectionService!!
            })
            subscriber.metronomeConnected.observe(this, Observer {
                if (it) mService = subscriber.metronomeService!!
            })
            subscriber.subscribe()
        } catch (e: Exception) {}
        subscribeToViewModel()
    }
    private fun subscribeToViewModel() {
        viewModel.bpm.observe(this, Observer {
            try {
                mService?.onBPMChanged(it)
                sharedViewModel.config.value = it
            }
            catch (e: Exception) {}
        })
        viewModel.isPlaying.observe( this, Observer { try {
            Log.d("playObserver","playObserved:$it")
            if (it) {
                binding.playButton.setText(R.string.stop_button)
                binding.playButton.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorStop))
            } else {
                binding.playButton.setText(R.string.play_button)
                binding.playButton.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorPlay))
            }
            mService?.onPlay()
        } catch (e: Exception) {Log.d("playObserver", "Preparing...")}})
        sharedViewModel.userTypes.observe(this, Observer {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (it) {
                UserTypes.SOLO -> {
                    binding.newSession.text = resources.getString(R.string.new_session)
                    binding.newSession.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorPrimary))
                    binding.joinSession.text = resources.getString(R.string.join_session)
                    binding.joinSession.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorPrimaryDark))
                    binding.notification.text =
                        resources.getString(R.string.not_connected)
                    if (sharedViewModel.isAdvertising.value == true) sharedViewModel.toggleAdvertise()
                }
                UserTypes.SESSION_HOST -> {
                    binding.notification.text =
                        resources.getString(R.string.current_session, sharedViewModel.getSessionName())
                    binding.newSession.text = resources.getString(R.string.dismiss_session)
                    binding.newSession.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorStop))
                    if (sharedViewModel.isAdvertising.value == false) sharedViewModel.toggleAdvertise()
                }
                UserTypes.SLAVE -> TODO("lupa mau ngapain")
            }
        })
        sharedViewModel.isAdvertising.observe(this, Observer {
            if (sharedViewModel.userTypes.value == UserTypes.SESSION_HOST) {
                if (it) {
                    binding.joinSession.setText(R.string.advertising)
                    binding.joinSession.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorStop))
                }
                else {
                    binding.joinSession.setText(R.string.not_advertising)
                    binding.joinSession.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorPlay))
                }
            }
        })
        binding.newSession.setOnClickListener(OnConnectionFragmentClickListener())
        binding.joinSession.setOnClickListener(OnConnectionFragmentClickListener())
    }

    inner class OnConnectionFragmentClickListener: View.OnClickListener {
        override fun onClick(v: View) {
            val args: Int
            when (v.id) {
                R.id.new_session -> {
                     onNewSessionButtonClicked()
                }
                R.id.join_session -> {
                    onJoinSessionButtonClicked(v)
                }
            }
        }
    }

    private fun doUnbindService() {
       subscriber.unsubscribe()
    }

    private fun onNewSessionButtonClicked() {
        if (sharedViewModel.userTypes.value == UserTypes.SOLO) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("What is your Name?")
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            builder.setPositiveButton("OK") { _, _ ->
                sharedViewModel.onNewSession(input.text?.toString()) //TODO: PERHATIKANNNNN INIII
                if (viewModel.isPlaying.value!!) viewModel.onPlayClicked()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
        else if (sharedViewModel.userTypes.value == UserTypes.SESSION_HOST) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Are you sure want to end this Session?")
            builder.setPositiveButton("OK") { _, _ ->
                sharedViewModel.endSession() //TODO: PERHATIKANNNNN INIII
                if (viewModel.isPlaying.value!!) viewModel.onPlayClicked()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
    }
    private fun onJoinSessionButtonClicked(v: View?) {
        if (sharedViewModel.userTypes.value == UserTypes.SOLO) {
            val args: Int = ConnectionCodes.JOIN_SESSION.v
            val action = MetronomeFragmentDirections.
                actionMetronomeFragmentToConnectionFragment()
            action.connectionType = args
            v!!.findNavController().navigate(action)
        }
        else if (sharedViewModel.userTypes.value == UserTypes.SESSION_HOST) {
            sharedViewModel.toggleAdvertise()
        }
    }
}
