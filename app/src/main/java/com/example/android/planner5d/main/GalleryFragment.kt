package com.example.android.planner5d.main

import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.planner5d.R
import com.example.android.planner5d.databinding.FragmentGalleryBinding
import com.example.android.planner5d.main.viewpaging.GalleryClickListener
import com.example.android.planner5d.main.viewpaging.GalleryPagingDataAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GalleryFragment : Fragment() {

    private val mainViewModel: MainViewModel by hiltNavGraphViewModels(R.id.navigation)
    private lateinit var binding: FragmentGalleryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery,
            container, false)

        binding.lifecycleOwner = viewLifecycleOwner  // не забывать
        binding.viewModel = mainViewModel            // не забывать

        val items = mainViewModel.galleryItems
        val galleryAdapter = GalleryPagingDataAdapter(GalleryClickListener { projectKey ->
            mainViewModel.onItemClicked(projectKey)
        })
        binding.galleryList.adapter = galleryAdapter

        // TODO: сделать через callback в котором можно измерить размеры layout
        var widthPixels = 0
        var spanCount = 2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            widthPixels = activity?.windowManager?.currentWindowMetrics?.bounds?.width() ?: 0
        } else {
            val outMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            activity?.windowManager?.defaultDisplay?.getRealMetrics(outMetrics)
            widthPixels = outMetrics.widthPixels
        }
        if (widthPixels != 0) {
            spanCount = widthPixels / 540
        }

        binding.galleryList.layoutManager = GridLayoutManager(activity, spanCount)

        //разница между viewLifecycleOwner и lifecycleOwner
        //viewLifecycleOwner is tied to when the fragment has (and loses) its UI (onCreateView(), onDestroyView())
        //this is tied to the fragment's overall lifecycle (onCreate(), onDestroy()), which may be substantially longer

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                items.collectLatest {
                    galleryAdapter.submitData(it)
                }
            }
        }

        mainViewModel.navigateToFloorFragment.observe(viewLifecycleOwner) { projectKey ->
            projectKey?.let {
                findNavController().navigate(
                    GalleryFragmentDirections.actionGalleryFragmentToGroundFloorFragment(projectKey)
                )
                mainViewModel.navigateToFloorFragmentDone()
            }
        }

        mainViewModel.needToRefreshAdapter.observe(viewLifecycleOwner) { trigger ->
            if (trigger) {
                mainViewModel.needToRefreshAdapterReset()
                galleryAdapter.refresh()
            }
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mainViewModel.clearLocalGallery()
        return super.onOptionsItemSelected(item)
    }
}
