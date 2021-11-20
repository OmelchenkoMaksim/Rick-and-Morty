package com.android.andersenrickandmorty.fragments.alerts

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.FiltersOnline

class AlertDialogEpisodeEpisodesFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val episode: Array<String> = if (online) {
            FiltersOnline.episodesEpisodesOnline.toTypedArray()
        } else {
            DataBase.episodesEpisodesOffline.toTypedArray()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.episode_episodes_alert_title_fragment))
                .setSingleChoiceItems(
                    episode, -1
                ) { _, item ->
                    DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_EPISODE] = episode[item]
                }
                .setPositiveButton(
                    getString(R.string.positive_alert_button_fragment)
                ) { _, _ ->
                }
                .setNegativeButton(getString(R.string.negative_alert_button_fragment)) { _, _ ->
                    DataBase.filterEpisodesOptions[DataBase.EPISODE_OPTION_EPISODE] = ""
                }
            builder.create()
        } ?: throw IllegalStateException(getString(R.string.alert_throw_activity_cannot_be_null))
    }
}