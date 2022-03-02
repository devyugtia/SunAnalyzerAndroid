package com.sunanalyzer.app.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuickSiteSurveyBuilding(
    @SerializedName("ageOfBuildings")
    var ageOfBuildings: String,
    @SerializedName("anyOtherSpecificRequirementFromCustomer")
    var anyOtherSpecificRequirementFromCustomer: String,
    @SerializedName("buildingId")
    var buildingId: String,
    @SerializedName("buildingName")
    var buildingName: String,
    @SerializedName("energyConsumption")
    var energyConsumption: String,
    @SerializedName("avgMonthlyBill")
    var avgMonthlyBill: String,
    @SerializedName("heightOfParapet")
    var heightOfParapet: String,
    @SerializedName("ladderToRoof")
    var ladderToRoof: String,
    @SerializedName("noOfFloor")
    var noOfFloor: String,
    @SerializedName("recommendedCapacity")
    var recommendedCapacity: String,
    @SerializedName("sanctionedLoad")
    var sanctionedLoad: String,
    @SerializedName("totalArea")
    var totalArea: String,
    @SerializedName("totalUsableArea")
    var totalUsableArea: String,
    @SerializedName("totalUsableAreaInPercentage")
    var totalUsableAreaInPercentage: String,
    @SerializedName("typeOfRoof")
    var typeOfRoof: String,
    @SerializedName("otherTypeOfRoof")
    var otherTypeOfRoof: String,
    @SerializedName("tiltAngleOfRoof")
    var tiltAngleOfRoof: String,
    @SerializedName("voltageLevelPhase")
    var voltageLevelPhase: String,
    @SerializedName("mapPoint")
    var mapPoint: ArrayList<MapPoint>,
    @SerializedName("measurementUnit")
    var measurementUnit: String
) : Parcelable