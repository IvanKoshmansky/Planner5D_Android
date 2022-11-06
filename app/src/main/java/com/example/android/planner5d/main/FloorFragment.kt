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
import timber.log.Timber

@AndroidEntryPoint
class FloorFragment : Fragment() {

    private val mainViewModel: MainViewModel by hiltNavGraphViewModels(R.id.navigation)
    private lateinit var binding: FragmentFloorBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_floor,
            container, false)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // запускать новую корутину всегда когда наступает состояние STARTED
                // и останавливать ее когда наступает состояние STOPPED
                mainViewModel.floorViewState.collect { uiState ->
                    // Flow сохраняет контекст корутины из которой был запущен (context preservation)
                    // при запуске во lifecycleScope и viewModelScope диспетчер Main
                    // поэтому отсюда обновлять UI можно
                    // первый элемент, который сюда поступает - значение по умолчанию
                    // далее StateFlow всегда хранит последний сохраненный элемент и передает его здесь
                    Timber.d("debug_regex: обновить UI $uiState")
                    Timber.d("debug_regex: элемент принят в $coroutineContext")
                    when (uiState) {
                        is LocalRepository.RoomPlanFromRepo.RoomPlanLoading -> showRoomPlanLoading()
                        is LocalRepository.RoomPlanFromRepo.RoomPlanOk -> showRoomPlanOk(uiState)
                        is LocalRepository.RoomPlanFromRepo.RoomPlanError -> showRoomPlanError()
                    }
                }
            }
        }

        return binding.root
    }

    private fun showRoomPlanLoading() {
        binding.textView.text = "загрузка"
    }

    private fun showRoomPlanOk(plan: LocalRepository.RoomPlanFromRepo.RoomPlanOk) {
        binding.textView.text = plan.roomPlan.projectName
        binding.floorView.setFloorPlan(plan.roomPlan)
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
