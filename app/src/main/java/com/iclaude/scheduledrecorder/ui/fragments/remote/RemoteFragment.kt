package com.iclaude.scheduledrecorder.ui.fragments.remote

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iclaude.scheduledrecorder.R


private const val ARG_POSITION = "position"


class RemoteFragment : Fragment() {

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remote, container, false)
    }
}
