package com.iclaude.scheduledrecorder.ui.fragments.remote

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RemoteViewModel : ViewModel() {

    val firebaseUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    val userConnected: ObservableBoolean = ObservableBoolean(firebaseUser != null)
}