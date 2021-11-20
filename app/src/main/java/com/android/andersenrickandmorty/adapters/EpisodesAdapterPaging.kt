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
import com.android.andersenrickandmorty.fragments.details.EpisodesDetailsFragment
import com.android.andersenrickandmorty.fragments.main.NavigationFragment
import com.android.andersenrickandmorty.models.EpisodeModel
import com.bumptech.glide.Glide

class EpisodesAdapterPaging(
    private val context: Context
) :
    PagingDataAdapter<EpisodeModel, EpisodesAdapterPaging.ViewHolder>(DiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.recycler_episode_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val cardView: CardView = view.findViewById(R.id.card_view_episode)
        private val name: TextView = view.findViewById(R.id.text_view_name_episode)
        private val number: TextView = view.findViewById(R.id.text_view_number_episode)
        private val date: TextView = view.findViewById(R.id.text_view_air_date_episode)
        private val image: ImageView = view.findViewById(R.id.image_episode)

        private val onClickUpdateContainer: NavigationFragment.OnClickUpdateContainer =
            view.context as NavigationFragment.OnClickUpdateContainer


        fun bind(item: EpisodeModel) {
            cardView.setOnClickListener {
                EpisodesDetailsFragment.episodeLatestID = item.id
                onClickUpdateContainer.updateContainer(
                    EpisodesDetailsFragment.newInstance(item.id)
                )
            }

            name.text = item.name
            number.text = item.episode
            date.text = item.air_date

            Glide.with(image)
                .load(R.drawable.rick_episode)
                .placeholder(R.drawable.rick_episode)
                .into(image)
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<EpisodeModel>() {
        override fun areItemsTheSame(oldItem: EpisodeModel, newItem: EpisodeModel): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: EpisodeModel, newItem: EpisodeModel): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.episode == newItem.episode
                    && oldItem.air_date == newItem.air_date
        }
    }
}