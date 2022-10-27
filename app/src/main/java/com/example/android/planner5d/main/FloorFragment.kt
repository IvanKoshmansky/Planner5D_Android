package com.example.android.planner5d.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.android.planner5d.R
import com.example.android.planner5d.databinding.FragmentFloorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FloorFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: FragmentFloorBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_floor,
            container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
