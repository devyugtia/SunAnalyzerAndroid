package com.sunanalyzer.app.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class SpinnerOptionModel(
        @SerializedName("id")
        val id: Int = 0,
        @SerializedName("value")
        val value: String = "",
        @SerializedName(value = "name", alternate = ["text", "title", "template_name"])
        var name: String = "",
        @SerializedName("isSelected")
        var isSelected: Boolean = false,
        @SerializedName("month")
        val month: Int = 0,
        @SerializedName("option")
        val arrayListChild: ArrayList<SpinnerOptionModel> = ArrayList(),
        val strId: String = "",
        @SerializedName("logo")
        val logo: String = ""

) : Parcelable