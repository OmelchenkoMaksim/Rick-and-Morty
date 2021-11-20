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
import com.android.andersenrickandmorty.adapters.CharactersAdapterPaging
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.DataBase.useCharacterForDetails
import com.android.andersenrickandmorty.fragments.main.EXTRA_EPISODE
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

const val EPISODES_DETAILS_STRING = "EpisodesDetailsFragment"

class EpisodesDetailsFragment : Fragment() {

    private lateinit var episodeRecyclerViewDetails: RecyclerView
    private lateinit var charactersAdapterPaging: CharactersAdapterPaging
    private lateinit var onClickUpdateContainer: NavigationFragment.OnClickUpdateContainer
    private lateinit var viewModel: PagingViewModel
    private var episodeId: Int = 0
    private var characterNumbersList = linkedSetOf<String>()
    private var retroLaunchCount = 0

    private lateinit var imageEpisodeDetails: ImageView
    private lateinit var episodeIdDetails: TextView
    private lateinit var episodeNameDetails: TextView
    private lateinit var episodeAirDateDetails: TextView
    private lateinit var episodeEpisodeDetails: TextView
    private lateinit var episodeCreatedDetails: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        episodeId = requireArguments().getSerializable(EXTRA_EPISODE) as Int
        onClickUpdateContainer = context as NavigationFragment.OnClickUpdateContainer
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_episodes_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        characterNumbersList.clear()
        findAllViews(view)
        initViewModel()
        setAdapter()
        fillDetails()
        setHasOptionsMenu(true)
    }

    private fun findAllViews(view: View) {
        imageEpisodeDetails = view.findViewById(R.id.image_episode_details)
        episodeIdDetails = view.findViewById(R.id.episode_id_details)
        episodeNameDetails = view.findViewById(R.id.episode_name_details)
        episodeAirDateDetails = view.findViewById(R.id.episode_air_date_details)
        episodeEpisodeDetails = view.findViewById(R.id.episode_episode_details)
        episodeCreatedDetails = view.findViewById(R.id.episode_created_details)
        episodeRecyclerViewDetails = view.findViewById(R.id.recyclerViewEpisodeDetails)
        progressBar = view.findViewById(R.id.progress_bar_episode_details)
    }

    private fun initViewModel() {
        DataBase.useCharacterFilters = false
        DataBase.useEpisodeFilters = false
        DataBase.useCharacterForDetails = true
        viewModel = ViewModelProvider(
            this, PagingViewModelFactory(
                (requireActivity().application as RickApplication).repository,
                DataBase
            )
        )[PagingViewModel::class.java]
    }


    private fun setAdapter() {
        charactersAdapterPaging = CharactersAdapterPaging(requireContext())
        episodeRecyclerViewDetails.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        episodeRecyclerViewDetails.adapter = charactersAdapterPaging
    }

    private fun fillDetails() {
        DataBase.findEpisodeById(episodeId).apply {
            Glide.with(imageEpisodeDetails)
                .load(R.drawable.rick_episode)
                .placeholder(R.drawable.rick_episode)
                .error(R.drawable.rick_episode)
                .into(imageEpisodeDetails)
            episodeIdDetails.text = id.toString()
            episodeNameDetails.text = name
            episodeAirDateDetails.text = air_date
            episodeEpisodeDetails.text = episode
            episodeCreatedDetails.text = created
            characterNumbersList.addAll(characters)

            launchAdapter(this)
        }
    }

    private fun launchAdapter(episodeModel: EpisodeModel) {
        progressBar.visibility = View.VISIBLE
        DataBase.detailsDataCharacter.clear()
        useCharacterForDetails = true

        if (online) {
            val prefix = "https://rickandmortyapi.com/api/character/"
            val removeList: MutableList<String> = mutableListOf()
            characterNumbersList.forEach { url ->
                DataBase.charactersList.firstOrNull {
                    it.url == url
                }?.let {
                    removeList.add(url)
                }
            }
            characterNumbersList.removeAll(removeList)
            retroLaunchCount = characterNumbersList.size
            characterNumbersList.forEach {
                launchRetrofitAndGet(
                    RetrofitInstance.retrofitGetInstance().getCharacterForDetails(
                        it.removePrefix(prefix).toInt()
                    )
                )
            }
        }
        lifecycleScope.launch {
            while (true) {
                delay(50)
                if (retroLaunchCount == 0) {
                    episodeModel.characters.forEach { url ->
                        DataBase.charactersList.firstOrNull {
                            it.url == url
                        }?.let { DataBase.detailsDataCharacter.add(it) }
                    }
                    progressBar.visibility = View.GONE
                    viewModel.getCharactersListData().collectLatest {
                        charactersAdapterPaging.submitData(it)
                    }
                    break
                }
            }
            this.cancel()
        }
    }

    private fun launchRetrofitAndGet(call: Call<CharacterModel>) {
        call.enqueue(object : Callback<CharacterModel> {
            override fun onResponse(
                call: Call<CharacterModel>,
                response: Response<CharacterModel>
            ) {
                if (!response.isSuccessful) {
                    retroLaunchCount = 0
                    return
                }
                DataBase.detailsDataCharacter.add(response.body()!!)
                retroLaunchCount--
                call.cancel()
            }

            override fun onFailure(call: Call<CharacterModel>, t: Throwable) {
                retroLaunchCount = 0
            }
        })
    }

    companion object {
        var episodeLatestID = 0
        fun newInstance(episodeId: Int) = EpisodesDetailsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_EPISODE, episodeId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_layout, menu)

        menu.findItem(R.id.menu_item_search).isVisible = false
        menu.findItem(R.id.menu_item_filter).isVisible = false
    }

    override fun toString(): String = EPISODES_DETAILS_STRING
}