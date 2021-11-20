package com.android.andersenrickandmorty.fragments.alerts

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.andersenrickandmorty.R
import com.android.andersenrickandmorty.common.DataBase
import com.android.andersenrickandmorty.common.DataBase.online
import com.android.andersenrickandmorty.common.FiltersOnline

class AlertDialogLocationDimensionsFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dimension: Array<String> = if (online) {
            FiltersOnline.locationsDimensionsOnline.toTypedArray()
        } else {
            DataBase.locationsDimensionsOffline.toTypedArray()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.dimension_locations_alert_title_fragment))
                .setSingleChoiceItems(
                    dimension, -1
                ) { _, item ->
                    DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_DIMENSION] =
                        dimension[item]
                }
                .setPositiveButton(
                    getString(R.string.positive_alert_button_fragment)
                ) { _, _ ->
                }
                .setNegativeButton(getString(R.string.negative_alert_button_fragment)) { _, _ ->
                    DataBase.filterLocationsOptions[DataBase.LOCATION_OPTION_DIMENSION] = ""
                }
            builder.create()
        } ?: throw IllegalStateException(getString(R.string.alert_throw_activity_cannot_be_null))
    }
}