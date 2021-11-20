package com.android.andersenrickandmorty.fragments.alerts

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.FiltersOnline

class AlertDialogCharacterGendersFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val gender: Array<String> = if (online) {
            FiltersOnline.charactersGendersOnline.toTypedArray()
        } else {
            DataBase.charactersGendersOffline.toTypedArray()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.gender_characters_alert_title_fragment))
                .setSingleChoiceItems(
                    gender, -1
                ) { _, item ->
                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_GENDER] =
                        gender[item]
                }
                .setPositiveButton(
                    getString(R.string.positive_alert_button_fragment)
                ) { _, _ ->
                }
                .setNegativeButton(getString(R.string.negative_alert_button_fragment)) { _, _ ->
                    DataBase.filterCharactersOptions[DataBase.CHARACTER_OPTION_GENDER] = ""
                }
            builder.create()
        } ?: throw IllegalStateException(getString(R.string.alert_throw_activity_cannot_be_null))
    }
}