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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.adapters.CharactersAdapterPaging
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.DataBase.resetAllPagingAttributes
import com.android.andersenrickandmorty.fragments.alerts.AlertDialogCharacterGendersFragment
import com.android.andersenrickandmorty.fragments.alerts.AlertDialogCharacterSpeciesFragment
import com.android.andersenrickandmorty.fragments.alerts.AlertDialogCharacterStatusFragment
import com.android.andersenrickandmorty.fragments.alerts.AlertDialogCharacterTypesFragment
import com.android.andersenrickandmorty.models.AllCharacters
import com.android.andersenrickandmorty.retrofit.RetrofitInstance
import com.android.andersenrickandmorty.room.RickApplication
import com.android.andersenrickandmorty.viewmodels.PagingViewModel
import com.android.andersenrickandmorty.viewmodels.PagingViewModelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val EXTRA_CHARACTER = "EXTRA_CHARACTER"
const val CHARACTERS_STRING = "CharactersFragment"

class CharactersFragment : Fragment() {

    private lateinit var recyclerViewCharacters: RecyclerView
    private lateinit var charactersAdapterPaging: CharactersAdapterPaging
    private lateinit var pagingViewModel: PagingViewModel

    private lateinit var filtersLayout: ConstraintLayout
    private lateinit var inputNameCharacters: EditText
    private lateinit var chooseSpeciesCharacters: Button
    private lateinit var chooseTypesCharacters: Button
    private lateinit var chooseStatusesCharacters: Button
    private lateinit var chooseGenderCharacters: Button
    private lateinit var applyFilters: Button
    private lateinit var closeFilters: Button
    private lateinit var progressBar: ProgressBar

    private var database = DataBase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_characters, container, false)
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
            chooseSpeciesCharacters = view.findViewById(R.id.choose_species_characters)
            chooseTypesCharacters = view.findViewById(R.id.choose_type_characters)
            chooseStatusesCharacters = view.findViewById(R.id.choose_status_characters)
            chooseGenderCharacters = view.findViewById(R.id.choose_gender_characters)
            closeFilters = view.findViewById(R.id.button_close_filters_characters)
            applyFilters = view.findViewById(R.id.button_apply_filters_characters)
            progressBar = view.findViewById(R.id.progress_bar_character)
            filtersLayout = view.findViewById(R.id.filters_layout_characters)
            inputNameCharacters = view.findViewById(R.id.input_name_characters)
            recyclerViewCharacters = view.findViewById(R.id.recyclerViewCharacters)
        }
    }

    private fun setAdapter() {
        charactersAdapterPaging = CharactersAdapterPaging(requireContext())
        recyclerViewCharacters.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerViewCharacters.adapter = charactersAdapterPaging
    }

    private fun initViewModel() {
        DataBase.useCharacterFilters = false
        DataBase.useCharacterForDetails = false
        DataBase.useEpisodeForDetails = false
        pagingViewModel = ViewModelProvider(
            this,
            PagingViewModelFactory(
                (requireActivity().application as RickApplication).repository,
                DataBase
            )
        )[PagingViewModel::class.java]
        CoroutineScope(Dispatchers.Main).launch {
            pagingViewModel.getCharactersListData().collectLatest {
                charactersAdapterPaging.submitData(it)
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
            DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_NAME] = ""
        }
        applyFilters.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (inputNameCharacters.text.toString()
                    .isEmpty()
            ) {
                DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_NAME] = ""
            } else {
                DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_NAME] =
                    inputNameCharacters.text.toString()
            }
            applyFiltersAndLaunch()
            filtersLayout.visibility = View.GONE
            CoroutineScope(Dispatchers.Main).launch {
                delay(1500)
                if (DataBase.filteredDataCharacter.isEmpty()) {
                    Toast.makeText(
                        activity, context?.getString(R.string.after_filtering_found_nothing),
                        Toast.LENGTH_LONG
                    ).show()
                }
                this.cancel()
            }
            inputNameCharacters.setText("")
        }
    }

    private fun setFilterAlerts() {
        chooseStatusesCharacters.setOnClickListener {
            val dialogFragment = AlertDialogCharacterStatusFragment()
            dialogFragment.show(childFragmentManager, getString(R.string.character_alert_tag))
        }
        chooseSpeciesCharacters.setOnClickListener {
            val dialogFragment = AlertDialogCharacterSpeciesFragment()
            dialogFragment.show(childFragmentManager, getString(R.string.character_alert_tag))
        }
        chooseGenderCharacters.setOnClickListener {
            val dialogFragment = AlertDialogCharacterGendersFragment()
            dialogFragment.show(childFragmentManager, getString(R.string.character_alert_tag))
        }
        chooseTypesCharacters.setOnClickListener {
            val dialogFragment = AlertDialogCharacterTypesFragment()
            dialogFragment.show(childFragmentManager, getString(R.string.character_alert_tag))
        }
    }

    private fun applyFiltersAndLaunch() {

        DataBase.useCharacterFilters = true
        if (!online) {
            DataBase.filteredDataCharacter.clear()
            DataBase.filteredDataCharacter.addAll(DataBase.getCharactersByOptions(DataBase.filterCharactersOptions))
            lifecycleScope.launch {
                delay(200)
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
                pagingViewModel.getCharactersListData().collectLatest {
                    charactersAdapterPaging.submitData(it)
                }
                this.cancel()
            }
        }
        if (online) {
            // до 7 т.к. если запрос включает популярное имя, например рик то будет максимум 6 страниц
            // еще одна на случай расширения базы
            for (i in 1..7) {
                launchRetrofitAndGet(
                    RetrofitInstance.retrofitGetInstance().getFilteredCharactersCall(
                        i,
                        DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_NAME]!!,
                        DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_STATUS]!!,
                        DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_SPECIES]!!,
                        DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_TYPE]!!,
                        DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_GENDER]!!
                    )
                )
            }
            lifecycleScope.launch {
                delay(1500)
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
                pagingViewModel.getCharactersListData().collectLatest {
                    charactersAdapterPaging.submitData(it)
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
        searchView.queryHint = getString(R.string.search_hint_character)
        menu.findItem(R.id.menu_item_back).isVisible = false
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
                    // true or false
                    DataBase.useCharacterFilters = queryText.length > 1

                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_NAME] = queryText
                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_STATUS] = ""
                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_SPECIES] = ""
                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_TYPE] = ""
                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_GENDER] = ""

                    DataBase.filteredDataCharacter.clear()
                    DataBase.filteredDataCharacter.addAll(
                        DataBase.getCharactersByOptions(DataBase.filterCharactersOptions)
                    )

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        if (DataBase.filteredDataCharacter.isEmpty() && queryText.isNotEmpty())
                            Toast.makeText(
                                activity,
                                context.getString(R.string.after_filtering_found_nothing),
                                Toast.LENGTH_LONG
                            ).show()
                        this.cancel()
                    }
                    lifecycleScope.launch {
                        pagingViewModel.getCharactersListData().collectLatest {
                            charactersAdapterPaging.submitData(it)
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
        DataBase.useEpisodeForDetails = false
        if (item.itemId == R.id.menu_item_filter) {
            DataBase.filteredDataCharacter.clear()
            DataBase.clearCharacterOptions()
            if (filtersLayout.visibility == View.VISIBLE) filtersLayout.visibility = View.GONE
            else filtersLayout.visibility = View.VISIBLE
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchRetrofitAndGet(call: Call<AllCharacters>) {
        call.enqueue(object : Callback<AllCharacters> {
            override fun onResponse(call: Call<AllCharacters>, response: Response<AllCharacters>) {
                if (!response.isSuccessful) {
                    return
                }
                DataBase.filteredDataCharacter.addAll(response.body()!!.results)
                call.cancel()
            }

            override fun onFailure(call: Call<AllCharacters>, t: Throwable) {
            }
        })
    }

    companion object {
        fun newInstance() = CharactersFragment()
    }

    override fun toString(): String = CHARACTERS_STRING
}