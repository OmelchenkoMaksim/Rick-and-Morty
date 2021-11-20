package com.android.andersenrickandmorty.fragments.alerts

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.FiltersOnline

class AlertDialogLocationTypesFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val type: Array<String> = if (online) {
            FiltersOnline.locationsTypesOnline.toTypedArray()
        } else {
            DataBase.locationsTypesOffline.toTypedArray()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.type_locations_alert_title_fragment))
                .setSingleChoiceItems(
                    type, -1
                ) { _, item ->
                    DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_TYPE] = type[item]
                }
                .setPositiveButton(
                    getString(R.string.positive_alert_button_fragment)
                ) { _, _ ->
                }
                .setNegativeButton(getString(R.string.negative_alert_button_fragment)) { _, _ ->
                    DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_TYPE] = ""
                }
            builder.create()
        } ?: throw IllegalStateException(getString(R.string.alert_throw_activity_cannot_be_null))
    }
}