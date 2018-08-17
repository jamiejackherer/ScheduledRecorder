package com.iclaude.scheduledrecorder.ui.fragments.remote

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.iclaude.scheduledrecorder.SingleLiveEvent

class RemoteViewModel : ViewModel() {

    val firebaseUserObs: ObservableField<FirebaseUser?> = ObservableField(FirebaseAuth.getInstance().getCurrentUser())

    val userConnectedObs: ObservableBoolean = ObservableBoolean(firebaseUserObs.get() != null)

    val logInCommand = SingleLiveEvent<Unit>()
    fun doLogin(): Unit = logInCommand.call()



}