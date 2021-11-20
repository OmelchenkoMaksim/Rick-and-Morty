package com.android.andersenrickandmorty.activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.DataBase.resetAllPagingAttributes
import com.android.andersenrickandmorty.fragments.details.CharactersDetailsFragment
import com.android.andersenrickandmorty.fragments.details.EpisodesDetailsFragment
import com.android.andersenrickandmorty.fragments.details.LocationsDetailsFragment
import com.android.andersenrickandmorty.fragments.main.*
import com.android.andersenrickandmorty.room.RickApplication
import com.android.andersenrickandmorty.viewmodels.PagingViewModel
import com.android.andersenrickandmorty.viewmodels.PagingViewModelFactory
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(R.layout.activity_main),
    NavigationFragment.OnClickUpdateContainer {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private val mainViewModel: PagingViewModel by viewModels {
        PagingViewModelFactory((application as RickApplication).repository, DataBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressBar = findViewById(R.id.progress_bar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        mainViewModel.initDatabase()

        alwaysCheckInternetConnection()
        onStartUpdateContainers()
        pullToRefresh()

    }

    private fun onStartUpdateContainers() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_navigation, NavigationFragment.newInstance())
            .commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_main, CharactersFragment.newInstance())
            .commit()
    }

    private fun pullToRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light
        )
        swipeRefreshLayout.setOnRefreshListener { updateContainerPull(swipeRefreshLayout) }
    }

    private fun updateContainerPull(swipeRefreshLayout: SwipeRefreshLayout) {

        DataBase.useCharacterFilters = false
        DataBase.clearCharacterOptions()
        DataBase.filteredDataCharacter.clear()
        DataBase.useLocationFilters = false
        DataBase.clearLocationOptions()
        DataBase.filteredDataLocation.clear()
        DataBase.useEpisodeFilters = false
        DataBase.clearEpisodeOptions()
        DataBase.filteredDataEpisode.clear()

        val visibleFragments =
            supportFragmentManager.fragments.filter { it.isVisible }
        // check open current fragment now and replaced
        progressBar.visibility = View.VISIBLE
        checkContainer(
            visibleFragments,
            supportFragmentManager,
            CharactersFragment.newInstance()
        )
        checkContainer(
            visibleFragments,
            supportFragmentManager,
            LocationsFragment.newInstance()
        )
        checkContainer(visibleFragments, supportFragmentManager, EpisodesFragment.newInstance())
        checkContainer(
            visibleFragments,
            supportFragmentManager,
            CharactersDetailsFragment.newInstance(CharactersDetailsFragment.characterLatestID)
        )
        checkContainer(
            visibleFragments,
            supportFragmentManager,
            EpisodesDetailsFragment.newInstance(EpisodesDetailsFragment.episodeLatestID)
        )
        checkContainer(
            visibleFragments,
            supportFragmentManager,
            LocationsDetailsFragment.newInstance(LocationsDetailsFragment.locationLatestID)
        )
        CoroutineScope(Dispatchers.Main).launch {
            swipeRefreshLayout.isRefreshing = false
            progressBar.visibility = View.GONE
            Toast.makeText(
                applicationContext,
                getString(R.string.refreshed),
                Toast.LENGTH_SHORT
            )
                .show()
            this.cancel()
        }
    }

    private fun checkContainer(
        visibleFragments: List<Fragment>,
        supportFragmentManager: FragmentManager,
        fragment: Fragment
    ) {
        if (visibleFragments.indexOfFirst { it.toString() == fragment.toString() } != -1) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                .commit()
        }
    }


    // same logic as bottom back arrow
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_back) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun updateContainer(fragment: Fragment) {
        resetAllPagingAttributes()
        val visibleFragments =
            supportFragmentManager.fragments.filter { it.isVisible }
        // check the index of the element with override toString
        // if toString first == toString in parameter
        // it means this is it itself
        // короче это проверка открыт ли этот фрагмент в данный момент
        if (visibleFragments.indexOfFirst { it.toString() == fragment.toString() } != -1) {
            return
        }
        if (fragment.toString() == CHARACTERS_STRING ||
            fragment.toString() == EPISODES_STRING ||
            fragment.toString() == LOCATIONS_STRING
        ) {
            progressBar.visibility = View.VISIBLE
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_main, fragment)
            .addToBackStack(fragment.toString())
            .commit()
        CoroutineScope(Dispatchers.Main).launch {
            progressBar.visibility = View.GONE
            this.cancel()
        }
    }

    // all time internet checking
    private fun alwaysCheckInternetConnection() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (!online) {
                    online = isNetworkAvailable(applicationContext)
                    delay(300)
                    if (online) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.connection_restored),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
                delay(500)
                if (online) {
                    online = isNetworkAvailable(applicationContext)
                    delay(300)
                    if (!online) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.connection_lost),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
                delay(500)
            }
        }
    }

    // check device internet adapter turn on
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // for other device how are able to connect with Ethernet
                //networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                // for check internet over Bluetooth
                //networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return false
        }
    }
}