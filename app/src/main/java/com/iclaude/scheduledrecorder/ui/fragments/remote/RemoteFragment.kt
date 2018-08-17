package com.iclaude.scheduledrecorder.ui.fragments.remote

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.iclaude.scheduledrecorder.databinding.FragmentRemoteBinding
import java.util.*


private const val ARG_POSITION = "position"


class RemoteFragment : Fragment() {

    private val RC_SIGN_IN = 123

    var viewModel: RemoteViewModel? = null

    companion object {
        @JvmStatic
        fun newInstance(position: Int) =
                RemoteFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_POSITION, position)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(RemoteViewModel::class.java)

        // Login command.
        viewModel?.logInCommand?.observe(this, Observer { logIn() })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentRemoteBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    fun logIn() {
        // Choose authentication providers.
        val providers = Arrays.asList(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.TwitterBuilder().build())

        // Create and launch sign-in intent.
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN)
    }

    // Return from login Activity. Have we successfully logged-in?
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != RC_SIGN_IN) return

        if (resultCode === RESULT_OK) {
            viewModel?.firebaseUserObs?.set(FirebaseAuth.getInstance().getCurrentUser())
        } else {
            // Sign in failed.
        }
    }
}
