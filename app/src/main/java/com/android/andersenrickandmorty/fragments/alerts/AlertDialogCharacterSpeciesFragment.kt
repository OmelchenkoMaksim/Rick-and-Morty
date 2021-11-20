package com.android.andersenrickandmorty.fragments.alerts

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.FiltersOnline

class AlertDialogCharacterSpeciesFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val species: Array<String> = if (online) {
            FiltersOnline.charactersSpeciesOnline.toTypedArray()
        } else {
            DataBase.charactersSpeciesOffline.toTypedArray()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.species_characters_alert_title_fragment))
                .setSingleChoiceItems(
                    species, -1
                ) { _, item ->
                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_SPECIES] =
                        species[item]
                }
                .setPositiveButton(
                    getString(R.string.positive_alert_button_fragment)
                ) { _, _ ->
                }
                .setNegativeButton(getString(R.string.negative_alert_button_fragment)) { _, _ ->
                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_SPECIES] = ""
                }
            builder.create()
        } ?: throw IllegalStateException(getString(R.string.alert_throw_activity_cannot_be_null))
    }
}