package com.example.android.planner5d.main

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.android.planner5d.R
import com.example.android.planner5d.databinding.FragmentGalleryBinding
import com.example.android.planner5d.main.viewpaging.GalleryClickListener
import com.example.android.planner5d.main.viewpaging.GalleryPagingDataAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GalleryFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding: FragmentGalleryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery,
            container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = mainViewModel

        val items = mainViewModel.galleryItems
        val galleryAdapter = GalleryPagingDataAdapter(GalleryClickListener { projectKey ->
            mainViewModel.onItemClicked(projectKey)
        })
        binding.galleryList.adapter = galleryAdapter

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.main_menu, menu)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        mainViewModel.clearOverviewCache()
//        return super.onOptionsItemSelected(item)
//    }
}
