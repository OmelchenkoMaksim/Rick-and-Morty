package com.android.andersenrickandmorty.fragments.details

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.adapters.EpisodesAdapterPaging
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.fragments.main.EXTRA_CHARACTER
import com.android.andersenrickandmorty.fragments.main.NavigationFragment
import com.android.andersenrickandmorty.models.CharacterModel
import com.android.andersenrickandmorty.models.EpisodeModel
import com.android.andersenrickandmorty.retrofit.RetrofitInstance
import com.android.andersenrickandmorty.room.RickApplication
import com.android.andersenrickandmorty.viewmodels.PagingViewModel
import com.android.andersenrickandmorty.viewmodels.PagingViewModelFactory
import com.bumptech.glide.Glide
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val CHARACTERS_DETAILS_STRING = "CharactersDetailsFragment"

class CharactersDetailsFragment : Fragment() {

    private lateinit var characterRecyclerViewDetails: RecyclerView
    private lateinit var episodesAdapterPaging: EpisodesAdapterPaging
    private lateinit var onClickUpdateContainer: NavigationFragment.OnClickUpdateContainer
    private lateinit var viewModel: PagingViewModel
    private var characterId: Int = 0
    private var episodesNumbersList = linkedSetOf<String>()
    private var retroLaunchCount = 0

    private lateinit var imageCharacterDetails: ImageView
    private lateinit var characterIdDetails: TextView
    private lateinit var characterNameDetails: TextView
    private lateinit var characterStatusDetails: TextView
    private lateinit var characterSpeciesDetails: TextView
    private lateinit var characterTypeDetails: TextView
    private lateinit var characterGenderDetails: TextView
    private lateinit var characterOriginDetails: TextView
    private lateinit var characterLocationDetails: TextView
    private lateinit var characterCreatedDetails: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterId = requireArguments().getSerializable(EXTRA_CHARACTER) as Int
        onClickUpdateContainer = context as NavigationFragment.OnClickUpdateContainer
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details_character, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        episodesNumbersList.clear()
        findAllViews(view)
        initViewModel()
        setAdapter()
        fillDetails()
        setHasOptionsMenu(true)
    }

    private fun findAllViews(view: View) {
        imageCharacterDetails = view.findViewById(R.id.image_character_details)
        characterIdDetails = view.findViewById(R.id.character_id_details)
        characterNameDetails = view.findViewById(R.id.character_name_details)
        characterStatusDetails = view.findViewById(R.id.character_status_details)
        characterSpeciesDetails = view.findViewById(R.id.character_species_details)
        characterTypeDetails = view.findViewById(R.id.character_type_details)
        characterGenderDetails = view.findViewById(R.id.character_gender_details)
        characterOriginDetails = view.findViewById(R.id.character_origin_details)
        characterLocationDetails = view.findViewById(R.id.character_location_details)
        characterCreatedDetails = view.findViewById(R.id.character_created_details)
        characterRecyclerViewDetails = view.findViewById(R.id.recyclerViewCharactersDetails)
        progressBar = view.findViewById(R.id.progress_bar_character_details)
    }

    private fun initViewModel() {
        DataBase.useCharacterFilters = false
        DataBase.useEpisodeFilters = false
        DataBase.useEpisodeForDetails = true
        viewModel = ViewModelProvider(
            this, PagingViewModelFactory(
                (requireActivity().application as RickApplication).repository,
                DataBase
            )
        )[PagingViewModel::class.java]
    }

    private fun setAdapter() {
        episodesAdapterPaging = EpisodesAdapterPaging(requireContext())
        characterRecyclerViewDetails.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        characterRecyclerViewDetails.adapter = episodesAdapterPaging
    }

    private fun fillDetails() {
        DataBase.findCharacterById(characterId).apply {
            Glide.with(imageCharacterDetails)
                .load(image)
                .placeholder(R.drawable.splash_scream)
                .error(R.drawable.splash_scream)
                .into(imageCharacterDetails)

            characterIdDetails.text = id.toString()
            characterNameDetails.text = name
            characterStatusDetails.text = status
            characterSpeciesDetails.text = species
            characterTypeDetails.text = type
            characterGenderDetails.text = gender
            characterOriginDetails.text = origin.name
            characterLocationDetails.text = location.name
            characterCreatedDetails.text = created
            episodesNumbersList.addAll(episode)

            addOnClickListenersToFields(this)
            launchAdapter(this)
        }
    }

    private fun addOnClickListenersToFields(characterModel: CharacterModel) {
        val originId: Int = try {
            (characterModel.origin.url.filter { it.isDigit() }.toInt())
        } catch (e: Exception) {
            1
        }

        characterOriginDetails.setOnClickListener {
            onClickUpdateContainer.updateContainer(
                LocationsDetailsFragment.newInstance(
                    originId
                )
            )
        }

        val locationId: Int = try {
            (characterModel.location.url.filter { it.isDigit() }).toInt()
        } catch (e: Exception) {
            1
        }

        characterLocationDetails.setOnClickListener {
            onClickUpdateContainer.updateContainer(
                LocationsDetailsFragment.newInstance(
                    locationId
                )
            )
        }
    }

    private fun launchAdapter(characterModel: CharacterModel) {
        progressBar.visibility = View.VISIBLE
        DataBase.detailsDataEpisode.clear()
        DataBase.useEpisodeForDetails = true

        if (online) {
            val prefix = "https://rickandmortyapi.com/api/episode/"
            val removeList: MutableList<String> = mutableListOf()
            episodesNumbersList.forEach { url ->
                DataBase.episodesList.firstOrNull {
                    it.url == url
                }?.let {
                    removeList.add(url)
                }
            }
            // delete existing elements, to download only the missing
            episodesNumbersList.removeAll(removeList)
            retroLaunchCount = episodesNumbersList.size
            episodesNumbersList.forEach {
                launchRetrofitAndGet(
                    RetrofitInstance.retrofitGetInstance().getEpisodeForDetails(
                        it.removePrefix(prefix).toInt()
                    )
                )
            }
        }
        lifecycleScope.launch {
            while (true) {
                delay(50)
                if (retroLaunchCount == 0) {
                    characterModel.episode.forEach { url ->
                        DataBase.episodesList.firstOrNull {
                            it.url == url
                        }?.let { DataBase.detailsDataEpisode.add(it) }
                    }
                    progressBar.visibility = View.GONE
                    viewModel.getEpisodesListData().collectLatest {
                        episodesAdapterPaging.submitData(it)
                    }
                    break
                }
            }
            this.cancel()
        }
    }

    private fun launchRetrofitAndGet(call: Call<EpisodeModel>) {
        call.enqueue(object : Callback<EpisodeModel> {
            override fun onResponse(call: Call<EpisodeModel>, response: Response<EpisodeModel>) {
                if (!response.isSuccessful) {
                    retroLaunchCount = 0
                    return
                }
                DataBase.detailsDataEpisode.add(response.body()!!)
                retroLaunchCount--
                call.cancel()
            }

            override fun onFailure(call: Call<EpisodeModel>, t: Throwable) {
                retroLaunchCount = 0
            }
        })
    }

    companion object {
        var characterLatestID = 0
        fun newInstance(characterId: Int) = CharactersDetailsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_CHARACTER, characterId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_layout, menu)
        menu.findItem(R.id.menu_item_search).isVisible = false
        menu.findItem(R.id.menu_item_filter).isVisible = false
    }

    override fun toString(): String = CHARACTERS_DETAILS_STRING
}