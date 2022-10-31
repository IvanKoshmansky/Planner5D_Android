package com.example.android.planner5d.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.android.planner5d.LocalRepository
import com.example.android.planner5d.R
import com.example.android.planner5d.databinding.FragmentFloorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FloorFragment : Fragment() {

    private val mainViewModel: MainViewModel by hiltNavGraphViewModels(R.id.navigation)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: FragmentFloorBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_floor,
            container, false)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.floorViewState.collect { uiState ->
                    when (uiState) {
                        is LocalRepository.RoomPlanOrError.RoomPlanOk -> showRoomPlanOk()
                        is LocalRepository.RoomPlanOrError.RoomPlanError -> showRoomPlanError()
                    }
                }
            }
        }

        return binding.root
    }

    private fun showRoomPlanOk() {

    }

    private fun showRoomPlanError() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            val args = FloorFragmentArgs.fromBundle(requireArguments())
            mainViewModel.setupFloorState(args.projectKey)
        }
    }
}
