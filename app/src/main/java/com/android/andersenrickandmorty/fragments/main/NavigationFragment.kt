package com.android.andersenrickandmorty.fragments.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.android.andersenrickandmorty.R

const val NAVIGATION_STRING = "NavigationFragment"

class NavigationFragment : Fragment() {

    interface OnClickUpdateContainer {
        fun updateContainer(fragment: Fragment)
    }

    private lateinit var buttonCharacter: Button
    private lateinit var buttonLocation: Button
    private lateinit var buttonEpisodes: Button

    private lateinit var onClickUpdateContainer: OnClickUpdateContainer

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onClickUpdateContainer = context as OnClickUpdateContainer
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonCharacter = view.findViewById(R.id.button_characters)
        buttonCharacter.setOnClickListener {
            onClickUpdateContainer.updateContainer(
                CharactersFragment.newInstance()
            )
        }

        buttonLocation = view.findViewById(R.id.button_locations)
        buttonLocation.setOnClickListener {
            onClickUpdateContainer.updateContainer(
                LocationsFragment.newInstance()
            )
        }

        buttonEpisodes = view.findViewById(R.id.button_episodes)
        buttonEpisodes.setOnClickListener {
            onClickUpdateContainer.updateContainer(
                EpisodesFragment.newInstance()
            )
        }
    }

    companion object {
        fun newInstance() = NavigationFragment()
    }

    override fun toString(): String = NAVIGATION_STRING
}