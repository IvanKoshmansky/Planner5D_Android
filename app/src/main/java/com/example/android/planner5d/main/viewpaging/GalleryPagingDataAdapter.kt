package com.example.android.planner5d.main.viewpaging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.android.planner5d.databinding.GalleryItemBinding
import com.example.android.planner5d.models.PlannerProject
import timber.log.Timber

class GalleryPagingDataAdapter (private val clickListener: GalleryClickListener) :
    PagingDataAdapter<PlannerProject, GalleryPagingDataAdapter.GalleryViewHolder>(DiffCallback()) {

    // onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder.from(parent)
    }

    // onBindViewHolder
    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class GalleryViewHolder (val binding: GalleryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlannerProject?, clickListener: GalleryClickListener) {
            // Note that item may be null. ViewHolder must support binding a
            // null item as a placeholder.
            // TODO: добавить обработку null для item
            Timber.d(if (item == null) { "debug_regex: !binding item null!" } else {""})
            binding.item = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): GalleryViewHolder {
                val layourInflater = LayoutInflater.from(parent.context)
                val binding = GalleryItemBinding.inflate(layourInflater, parent, false)
                return GalleryViewHolder(binding)
            }
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<PlannerProject>() {
        override fun areItemsTheSame(oldItem: PlannerProject, newItem: PlannerProject): Boolean {
            return oldItem.key == newItem.key
        }
        override fun areContentsTheSame(oldItem: PlannerProject, newItem: PlannerProject): Boolean {
            return oldItem == newItem
        }
    }
}

class GalleryClickListener(val clickLamdba: (key: String) -> Unit) {
    fun onClick(item: PlannerProject) = clickLamdba(item.key)
}
