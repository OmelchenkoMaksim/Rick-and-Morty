package com.android.andersenrickandmorty.fragments.main

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.adapters.EpisodesAdapterPaging
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.DataBase.resetAllPagingAttributes
import com.android.andersenrickandmorty.fragments.alerts.AlertDialogEpisodeEpisodesFragment
import com.android.andersenrickandmorty.models.AllEpisodes
import com.android.andersenrickandmorty.retrofit.RetrofitInstance
import com.android.andersenrickandmorty.room.RickApplication
import com.android.andersenrickandmorty.viewmodels.PagingViewModel
import com.android.andersenrickandmorty.viewmodels.PagingViewModelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val EXTRA_EPISODE = "EXTRA_EPISODE"
const val EPISODES_STRING = "EpisodesFragment"

class EpisodesFragment : Fragment() {

    private lateinit var recyclerViewEpisodes: RecyclerView
    private lateinit var episodesAdapterPaging: EpisodesAdapterPaging
    private lateinit var pagingViewModel: PagingViewModel

    private lateinit var filtersLayout: ConstraintLayout
    private lateinit var inputNameEpisodes: EditText
    private lateinit var chooseEpisodeEpisodes: Button
    private lateinit var applyFilters: Button
    private lateinit var closeFilters: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_episodes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findAllViews()
        setAdapter()
        // DataBase.resetAllPagingAttributes()
        initViewModel()
        setFilters()

        setHasOptionsMenu(true)
    }

    private fun findAllViews() {
        view?.let { view ->
            recyclerViewEpisodes = view.findViewById(R.id.recyclerViewEpisodes)
            filtersLayout = view.findViewById(R.id.filters_layout_episodes)
            inputNameEpisodes = view.findViewById(R.id.input_name_episode)
            closeFilters = view.findViewById(R.id.button_close_filters_episode)
            applyFilters = view.findViewById(R.id.button_apply_filters_episode)
            chooseEpisodeEpisodes = view.findViewById(R.id.choose_episodes_episode)
            progressBar = view.findViewById(R.id.progress_bar_episode)
        }
    }

    private fun setAdapter() {
        episodesAdapterPaging = EpisodesAdapterPaging(requireContext())
        recyclerViewEpisodes.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerViewEpisodes.adapter = episodesAdapterPaging
    }

    private fun initViewModel() {
        DataBase.useEpisodeFilters = false
        DataBase.useEpisodeForDetails = false
        pagingViewModel = ViewModelProvider(
            this,
            PagingViewModelFactory(
                (requireActivity().application as RickApplication).repository,
                DataBase
            )
        )[PagingViewModel::class.java]
        CoroutineScope(Dispatchers.Main).launch {
            pagingViewModel.getEpisodesListData().collectLatest {
                episodesAdapterPaging.submitData(it)
            }
            this.cancel()
        }
    }

    private fun setFilters() {
        setFilterAlerts()
        setFilterButtons()
    }

    private fun setFilterButtons() {
        closeFilters.setOnClickListener {
            filtersLayout.visibility = View.GONE
            DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_NAME] = ""
        }
        applyFilters.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (inputNameEpisodes.text.toString()
                    .isEmpty()
            ) {
                DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_NAME] = ""
            } else {
                DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_NAME] =
                    inputNameEpisodes.text.toString()
            }
            applyFiltersAndLaunch()

            filtersLayout.visibility = View.GONE
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                if (DataBase.filteredDataEpisode.isEmpty()) {
                    Toast.makeText(
                        activity, context?.getString(R.string.after_filtering_found_nothing),
                        Toast.LENGTH_LONG
                    ).show()
                }
                this.cancel()
            }
            inputNameEpisodes.setText("")
        }
    }

    private fun setFilterAlerts() {
        chooseEpisodeEpisodes.setOnClickListener {
            val dialogFragment = AlertDialogEpisodeEpisodesFragment()
            val manager = (context as FragmentActivity).supportFragmentManager
            dialogFragment.show(manager, getString(R.string.episode_alert_tag))
        }
    }

    private fun applyFiltersAndLaunch() {
        DataBase.useEpisodeFilters = true
        if (!online) {
            DataBase.filteredDataEpisode.clear()
            DataBase.filteredDataEpisode.addAll(
                DataBase.getEpisodesByOptions(DataBase.filterEpisodesOptions)
            )

            lifecycleScope.launch {
                delay(200)
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
                pagingViewModel.getEpisodesListData().collectLatest {
                    episodesAdapterPaging.submitData(it)
                }
                this.cancel()
            }
        }
        if (online) {
            for (i in 1..3) {
                launchRetrofitAndGet(
                    RetrofitInstance.retrofitGetInstance().getFilteredEpisodesCall(
                        i,
                        DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_NAME]!!,
                        DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_EPISODE]!!
                    )
                )
            }
            lifecycleScope.launch {
                delay(1500)
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
                pagingViewModel.getEpisodesListData().collectLatest {
                    episodesAdapterPaging.submitData(it)
                }
                this.cancel()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_layout, menu)
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint_episodes)
        menu.findItem(R.id.menu_item_back).isVisible = true
        menu.findItem(R.id.menu_item_search).isVisible = true
        menu.findItem(R.id.menu_item_filter).isVisible = true

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(queryText: String): Boolean {
                    resetAllPagingAttributes()
                    if (queryText.length == 1 && online) {
                        Toast.makeText(
                            activity,
                            context.getString(R.string.search_toast),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    DataBase.useEpisodeFilters = queryText.length > 1

                    DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_NAME] = queryText
                    DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_EPISODE] = ""

                    DataBase.filteredDataEpisode.clear()
                    DataBase.filteredDataEpisode.addAll(DataBase.getEpisodesByOptions(DataBase.filterEpisodesOptions))

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        if (DataBase.filteredDataEpisode.isEmpty() && queryText.isNotEmpty()) {
                            Toast.makeText(
                                activity, context.getString(R.string.after_filtering_found_nothing),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        this.cancel()
                    }
                    lifecycleScope.launch {
                        pagingViewModel.getEpisodesListData().collectLatest {
                            episodesAdapterPaging.submitData(it)
                        }
                        this.cancel()
                    }
                    return true
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        DataBase.updateFilters()
        DataBase.useCharacterForDetails = false
        if (item.itemId == R.id.menu_item_filter) {
            DataBase.filteredDataEpisode.clear()
            DataBase.clearEpisodeOptions()
            if (filtersLayout.visibility == View.VISIBLE) filtersLayout.visibility = View.GONE
            else filtersLayout.visibility = View.VISIBLE
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchRetrofitAndGet(call: Call<AllEpisodes>) {
        call.enqueue(object : Callback<AllEpisodes> {
            override fun onResponse(call: Call<AllEpisodes>, response: Response<AllEpisodes>) {
                if (!response.isSuccessful) {
                    return
                }
                DataBase.filteredDataEpisode.addAll(response.body()!!.results)
            }

            override fun onFailure(call: Call<AllEpisodes>, t: Throwable) {
            }
        })
    }

    companion object {
        fun newInstance() = EpisodesFragment()
    }

    override fun toString(): String = EPISODES_STRING
}