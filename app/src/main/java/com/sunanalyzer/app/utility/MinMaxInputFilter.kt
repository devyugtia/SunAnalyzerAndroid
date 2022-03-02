package com.sunanalyzer.app.utility

import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText


/**
 *  MinMaxInputFilter class is used to allowed integer value in between min and max value
 *  to input box
 * @author Dixit Panchal
 * @since 07/06/2021
 * @param min Pass Minimum value to allow in inputbox
 * @param max Pass Minimum value to allow in inputbox
 * */
class MinMaxInputFilter(private val min: Double=0.0, private val max: Double=99999999999999.0) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toDouble()
            if (isInRange(min, max, input)) {
               return null
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun isInRange(a: Double, b: Double, c: Double): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}