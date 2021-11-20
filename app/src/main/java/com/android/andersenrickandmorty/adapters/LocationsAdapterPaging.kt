package com.android.andersenrickandmorty.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.fragments.details.LocationsDetailsFragment
import com.android.andersenrickandmorty.fragments.main.NavigationFragment
import com.android.andersenrickandmorty.models.LocationModel
import com.bumptech.glide.Glide

class LocationsAdapterPaging(
    private val context: Context
) :
    PagingDataAdapter<LocationModel, LocationsAdapterPaging.ViewHolder>(DiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.recycler_location_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView: CardView = view.findViewById(R.id.card_view_location)
        private val name: TextView = view.findViewById(R.id.text_view_name_location)
        private val type: TextView = view.findViewById(R.id.text_view_type_location)
        private val dimension: TextView = view.findViewById(R.id.text_view_dimension_location)
        private val image: ImageView = view.findViewById(R.id.image_location)

        private val onClickUpdateContainer: NavigationFragment.OnClickUpdateContainer =
            view.context as NavigationFragment.OnClickUpdateContainer


        fun bind(item: LocationModel) {
            cardView.setOnClickListener {
                LocationsDetailsFragment.locationLatestID = item.id
                onClickUpdateContainer.updateContainer(
                    LocationsDetailsFragment.newInstance(item.id)
                )
            }

            name.text = item.name
            type.text = item.type
            dimension.text = item.dimension

            Glide.with(image)
                .load(R.drawable.rick_and_morty_location)
                .placeholder(R.drawable.rick_and_morty_location)
                .into(image)
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<LocationModel>() {
        override fun areItemsTheSame(oldItem: LocationModel, newItem: LocationModel): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: LocationModel, newItem: LocationModel): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.type == newItem.type
                    && oldItem.dimension == newItem.dimension
        }
    }
}