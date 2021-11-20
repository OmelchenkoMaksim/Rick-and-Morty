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
import com.android.andersenrickandmorty.fragments.details.CharactersDetailsFragment
import com.android.andersenrickandmorty.fragments.main.NavigationFragment
import com.android.andersenrickandmorty.models.CharacterModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class CharactersAdapterPaging(
    private val context: Context
) :
    PagingDataAdapter<CharacterModel, CharactersAdapterPaging.ViewHolder>(DiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.recycler_character_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardViewCharacter: CardView = view.findViewById(R.id.card_view_character)
        private val name: TextView = view.findViewById(R.id.text_view_name_character)
        private val species: TextView = view.findViewById(R.id.text_view_species_character)
        private val status: TextView = view.findViewById(R.id.text_view_status_character)
        private val gender: TextView = view.findViewById(R.id.text_view_gender_character)
        private val image: ImageView = view.findViewById(R.id.image_character)

        private val onClickUpdateContainer: NavigationFragment.OnClickUpdateContainer =
            view.context as NavigationFragment.OnClickUpdateContainer

        fun bind(item: CharacterModel) {
            cardViewCharacter.setOnClickListener {
                CharactersDetailsFragment.characterLatestID = item.id
                onClickUpdateContainer.updateContainer(
                    CharactersDetailsFragment.newInstance(item.id)
                )
            }
            name.text = item.name
            species.text = item.species
            status.text = item.status
            gender.text = item.gender

            Glide.with(image)
                .load(item.image)
                .placeholder(R.drawable.splash_main)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(image)
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<CharacterModel>() {
        override fun areItemsTheSame(oldItem: CharacterModel, newItem: CharacterModel): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: CharacterModel, newItem: CharacterModel): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.species == newItem.species
                    && oldItem.status == newItem.status
                    && oldItem.gender == newItem.gender
        }
    }
}