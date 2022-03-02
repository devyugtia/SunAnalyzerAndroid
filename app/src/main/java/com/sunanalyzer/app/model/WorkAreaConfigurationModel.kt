package com.sunanalyzer.app.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WorkAreaConfigurationModel(
    @SerializedName("workAreaId")
    var workAreaId: String = "",
    @SerializedName("azimuthAngle")
    var azimuthAngle: Double = 0.0,
    @SerializedName("offset")
    var offset: Double = 0.0,
    @SerializedName("panelTilt")
    var panelTilt: Double= 0.0,
    @SerializedName("pitchDistance")
    var pitchDistance: Double= 0.0,
    @SerializedName("wpPerPanel")
    var wpPerPanel: Double= 0.0,
    @SerializedName("totalWorkArea")
    var totalWorkArea: Double= 0.0,
    @SerializedName("layoutType")
    var layoutType: String= "Layout 1 (2x1)",
    @SerializedName("layoutTypeId")
    var layoutTypeId: Int= 1,
    @SerializedName("panelType")
    var panelType: Boolean= true,
    @SerializedName("panelW")
    var panelW: Double= 2.0,
    @SerializedName("panelH")
    var panelH: Double= 1.0
) : Parcelable