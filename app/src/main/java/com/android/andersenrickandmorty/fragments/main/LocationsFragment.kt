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
import com.android.andersenrickandmorty.adapters.LocationsAdapterPaging
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.DataBase.resetAllPagingAttributes
import com.android.andersenrickandmorty.fragments.alerts.AlertDialogLocationDimensionsFragment
import com.android.andersenrickandmorty.fragments.alerts.AlertDialogLocationTypesFragment
import com.android.andersenrickandmorty.models.AllLocations
import com.android.andersenrickandmorty.retrofit.RetrofitInstance
import com.android.andersenrickandmorty.room.RickApplication
import com.android.andersenrickandmorty.viewmodels.PagingViewModel
import com.android.andersenrickandmorty.viewmodels.PagingViewModelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val EXTRA_LOCATION = "EXTRA_LOCATION"
const val LOCATIONS_STRING = "LocationsFragment"

class LocationsFragment : Fragment() {

    private lateinit var recyclerViewLocations: RecyclerView
    private lateinit var locationsAdapterPaging: LocationsAdapterPaging
    private lateinit var pagingViewModel: PagingViewModel

    private lateinit var filtersLayout: ConstraintLayout
    private lateinit var inputNameLocations: EditText
    private lateinit var chooseTypesLocations: Button
    private lateinit var chooseDimensionLocations: Button
    private lateinit var applyFilters: Button
    private lateinit var closeFilters: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_locations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findAllViews()
        setAdapter()
        initViewModel()
        setFilters()
        setHasOptionsMenu(true)
    }

    private fun findAllViews() {
        view?.let { view ->
            recyclerViewLocations = view.findViewById(R.id.recyclerViewLocations)
            filtersLayout = view.findViewById(R.id.filters_layout_locations)
            inputNameLocations = view.findViewById(R.id.input_name_location)
            closeFilters = view.findViewById(R.id.button_close_filters_locations)
            applyFilters = view.findViewById(R.id.button_apply_filters_locations)
            chooseTypesLocations = view.findViewById(R.id.choose_type_location)
            chooseDimensionLocations = view.findViewById(R.id.choose_dimension_location)
            progressBar = view.findViewById(R.id.progress_bar_locations)
        }
    }

    private fun setAdapter() {
        locationsAdapterPaging = LocationsAdapterPaging(requireContext())
        recyclerViewLocations.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerViewLocations.adapter = locationsAdapterPaging
    }

    private fun initViewModel() {
        DataBase.useLocationFilters = false
        pagingViewModel = ViewModelProvider(
            this,
            PagingViewModelFactory(
                (requireActivity().application as RickApplication)
                    .repository, DataBase
            )
        )[PagingViewModel::class.java]

        CoroutineScope(Dispatchers.Main).launch {
            pagingViewModel.getLocationsListData().collectLatest {
                locationsAdapterPaging.submitData(it)
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
            DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_NAME] = ""
        }
        applyFilters.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (inputNameLocations.text.toString()
                    .isEmpty()
            ) {
                DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_NAME] = ""
            } else {
                DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_NAME] =
                    inputNameLocations.text.toString()
            }
            applyFiltersAndLaunch()

            filtersLayout.visibility = View.GONE
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                if (DataBase.filteredDataLocation.isEmpty()) {
                    Toast.makeText(
                        activity, context?.getString(R.string.after_filtering_found_nothing),
                        Toast.LENGTH_LONG
                    ).show()
                }
                this.cancel()
            }
            inputNameLocations.setText("")
        }
    }

    private fun setFilterAlerts() {
        chooseTypesLocations.setOnClickListener {
            val dialogFragment = AlertDialogLocationTypesFragment()
            val manager = (context as FragmentActivity).supportFragmentManager
            dialogFragment.show(manager, getString(R.string.location_alert_tag))
        }
        chooseDimensionLocations.setOnClickListener {
            val dialogFragment = AlertDialogLocationDimensionsFragment()
            val manager = (context as FragmentActivity).supportFragmentManager
            dialogFragment.show(manager, getString(R.string.location_alert_tag))
        }
    }

    private fun applyFiltersAndLaunch() {
        DataBase.useLocationFilters = true
        DataBase.useCharacterForDetails = false
        if (!online) {
            DataBase.filteredDataLocation.clear()
            DataBase.filteredDataLocation.addAll(
                DataBase.getLocationsByOptions(DataBase.filterLocationsOptions)
            )

            lifecycleScope.launch {
                delay(200)
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
                pagingViewModel.getLocationsListData().collectLatest {
                    locationsAdapterPaging.submitData(it)
                }
                this.cancel()
            }
        }
        if (online) {
            for (i in 1..3) {
                launchRetrofitAndGet(
                    RetrofitInstance.retrofitGetInstance().getFilteredLocationsCall(
                        i,
                        DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_NAME]!!,
                        DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_TYPE]!!,
                        DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_DIMENSION]!!
                    )
                )
            }
            lifecycleScope.launch {
                delay(1500)
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
                pagingViewModel.getLocationsListData().collectLatest {
                    locationsAdapterPaging.submitData(it)
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
        searchView.queryHint = getString(R.string.search_hint_locations)
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

                    DataBase.useLocationFilters = queryText.length > 1

                    DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_NAME] = queryText
                    DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_TYPE] = ""
                    DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_DIMENSION] = ""

                    DataBase.filteredDataLocation.clear()
                    DataBase.filteredDataLocation.addAll(DataBase.getLocationsByOptions(DataBase.filterLocationsOptions))

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        if (DataBase.filteredDataLocation.isEmpty() && queryText.isNotEmpty()) {
                            Toast.makeText(
                                activity, context.getString(R.string.after_filtering_found_nothing),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        this.cancel()
                    }
                    lifecycleScope.launch {
                        pagingViewModel.getLocationsListData().collectLatest {
                            locationsAdapterPaging.submitData(it)
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
        if (item.itemId == R.id.menu_item_filter) {
            DataBase.filteredDataLocation.clear()
            DataBase.clearLocationOptions()
            if (filtersLayout.visibility == View.VISIBLE) filtersLayout.visibility = View.GONE
            else filtersLayout.visibility = View.VISIBLE
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchRetrofitAndGet(call: Call<AllLocations>) {
        call.enqueue(object : Callback<AllLocations> {
            override fun onResponse(call: Call<AllLocations>, response: Response<AllLocations>) {
                if (!response.isSuccessful) {
                    return
                }
                DataBase.filteredDataLocation.addAll(response.body()!!.results)
            }

            override fun onFailure(call: Call<AllLocations>, t: Throwable) {
            }
        })
    }

    companion object {
        fun newInstance() = LocationsFragment()
    }

    override fun toString(): String = LOCATIONS_STRING
}