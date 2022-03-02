package com.sunanalyzer.app.utility

object AppConstant {
    /*common API response constant*/

    const val STEP_ANIMATION_TIME: Long = 500
    const val TOKEN: String = "token"
    const val ACCOUNT_ID: String = "account_id"

    const val GOOGLE_MAP_URL = "https://maps.googleapis.com/maps/api/geocode/json"
    const val GOOGLE_MAP_DIRECTION_URL = "https://maps.googleapis.com/maps/api/directions/json?"

    /*login API constant*/
    const val LAT = "lat"
    const val LON = "lon"

    /*Important document state list*/
    const val RANDOM = "random"
    const val STATE = "state"
    const val CITY = "city"


    object AreaType {
        const val M2 = "2001"
        const val FT2 = "2002"
    }

    object DrawingType {
        const val WORK_AREA = "work_area"
        const val SHADOW = "shadow"
        const val FREE_AREA = "free_area"
    }
}