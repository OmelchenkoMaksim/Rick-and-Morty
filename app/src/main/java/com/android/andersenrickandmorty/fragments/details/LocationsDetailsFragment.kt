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
import com.android.andersenrickandmorty.fragments.main.EXTRA_LOCATION
import com.android.andersenrickandmorty.fragments.main.NavigationFragment
import com.android.andersenrickandmorty.models.CharacterModel
import com.android.andersenrickandmorty.models.LocationModel
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

const val LOCATIONS_DETAILS_STRING = "LocationsDetailsFragment"

class LocationsDetailsFragment : Fragment() {

    private lateinit var locationRecyclerViewDetails: RecyclerView
    private lateinit var charactersAdapterPaging: CharactersAdapterPaging
    private lateinit var onClickUpdateContainer: NavigationFragment.OnClickUpdateContainer
    private lateinit var viewModel: PagingViewModel
    private var locationId: Int = 0
    private var characterNumbersList = linkedSetOf<String>()
    private var retroLaunchCount = 0

    private lateinit var locationImageDetails: ImageView
    private lateinit var locationIdDetails: TextView
    private lateinit var locationNameDetails: TextView
    private lateinit var locationTypeDetails: TextView
    private lateinit var locationDimensionDetails: TextView
    private lateinit var locationCreatedDetails: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationId = requireArguments().getSerializable(EXTRA_LOCATION) as Int
        onClickUpdateContainer = context as NavigationFragment.OnClickUpdateContainer
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_locations_details, container, false)
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
        locationImageDetails = view.findViewById(R.id.image_location_details)
        locationIdDetails = view.findViewById(R.id.location_id_details)
        locationNameDetails = view.findViewById(R.id.location_name_details)
        locationTypeDetails = view.findViewById(R.id.location_type_details)
        locationDimensionDetails = view.findViewById(R.id.location_dimension_details)
        locationCreatedDetails = view.findViewById(R.id.location_created_details)
        locationRecyclerViewDetails = view.findViewById(R.id.recyclerViewLocationDetails)
        progressBar = view.findViewById(R.id.progress_bar_location_details)
    }

    private fun initViewModel() {
        DataBase.useCharacterFilters = false
        DataBase.useLocationFilters = false
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
        locationRecyclerViewDetails.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        locationRecyclerViewDetails.adapter = charactersAdapterPaging
    }

    private fun fillDetails() {
        DataBase.findLocationById(locationId).apply {
            Glide.with(locationImageDetails)
                .load(R.drawable.rick_and_morty_location)
                .placeholder(R.drawable.rick_and_morty_location)
                .error(R.drawable.rick_and_morty_location)
                .into(locationImageDetails)
            locationIdDetails.text = id.toString()
            locationNameDetails.text = name
            locationTypeDetails.text = type
            locationDimensionDetails.text = dimension
            locationCreatedDetails.text = created
            characterNumbersList.addAll(residents)

            launchAdapter(this)
        }
    }

    private fun launchAdapter(locationModel: LocationModel) {
        progressBar.visibility = View.VISIBLE
        DataBase.detailsDataCharacter.clear()
        DataBase.useCharacterForDetails = true

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
                    locationModel.residents.forEach { url ->
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_layout, menu)
        menu.findItem(R.id.menu_item_back).isVisible = true
        menu.findItem(R.id.menu_item_search).isVisible = false
        menu.findItem(R.id.menu_item_filter).isVisible = false
    }

    companion object {
        var locationLatestID = 0
        fun newInstance(locationId: Int) =
            LocationsDetailsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_LOCATION, locationId)
                }
            }
    }

    override fun toString(): String = LOCATIONS_DETAILS_STRING
}