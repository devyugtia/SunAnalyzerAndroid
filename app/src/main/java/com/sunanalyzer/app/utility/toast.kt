package com.sunanalyzer.app.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.*
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.sunanalyzer.app.R
import java.sql.Timestamp
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

fun Context.noPermission() {
    Toast.makeText(this, "You don't have a permission.", Toast.LENGTH_LONG).show()
}

fun Activity.changeActivity(SecondActivity: Class<*>, isFinish: Boolean) {
    val intent = Intent(this, SecondActivity)
    this.startActivity(intent)
    //this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    if (isFinish) {
        this.finish()
    }
}


fun Activity.changeActivity(
    SecondActivity: Class<*>,
    isFinish: Boolean,
    intentData: Intent,
    flags: Int,
) {
    val intent = Intent(this, SecondActivity)
    intent.putExtras(intentData)
    intent.flags = flags
    this.startActivity(intent)
    //this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    if (isFinish) {
        this.finish()
    }
}

fun Activity.changeActivityForResult(SecondActivity: Class<*>, activityRequestCode: Int) {
    val intent = Intent(this, SecondActivity)
    this.startActivityForResult(intent, activityRequestCode)
    //this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
}

fun Activity.changeActivityForResultData(
    SecondActivity: Class<*>,
    activityRequestCode: Int,
    intentData: Intent,
) {
    val intent = Intent(this, SecondActivity)
    intent.putExtras(intentData)
    this.startActivityForResult(intent, activityRequestCode)
    //this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
}

fun View.showSnackbar(msg: String) {
   Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

@BindingAdapter("isSelected")
fun isSelected(imageView: ImageView, isSelected: Boolean) {
    if (isSelected) {
        imageView.setImageResource(R.drawable.ic_check_mark)
    } else {
        imageView.setImageResource(R.drawable.ic_check_mark_unselected)
    }
}

fun TextView.isSelected(isSelected: Boolean) {
    if (isSelected) {
        this.setTextColor(resources.getColor(R.color.white))
        this.setBackgroundResource(R.drawable.bg_textview_selected)
    } else {
        this.setTextColor(resources.getColor(R.color.colorTextHint))
        this.setBackgroundResource(R.drawable.bg_textview_default)
    }
}

@SuppressLint("SimpleDateFormat")
fun Context.changeDateFormat(
    inputDate: String,
    inputPattern: String,
    outputPattern: String,
): String {
    val inputFormat = SimpleDateFormat(inputPattern)
    val outputFormat = SimpleDateFormat(outputPattern)

    var date: Date? = null
    var str: String? = null
    try {
        date = inputFormat.parse(inputDate)
        str = outputFormat.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return str!!
}

fun convertDateToMili(inputDate: String, inputPattern: String): Long {
    val sdf = SimpleDateFormat(inputPattern)
    var timeInMilliseconds: Long = 0
    try {
        val mDate = sdf.parse(inputDate)
        timeInMilliseconds = mDate.time
    } catch (e: Exception) {
        Log.e("exception", e.localizedMessage)
    }
    return timeInMilliseconds
}

fun convertMiliToDate(mili: Long, inputPattern: String): String {
    val tStamp = Timestamp(mili)
    val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(inputPattern)
    return simpleDateFormat.format(tStamp)
}



fun EditText.setErrorMessage(errorMessage: String) {
    if (!errorMessage.isNullOrEmpty()) {
        this.requestFocus()
        this.error = errorMessage
    }
}

fun String.isAlphaNumeric(): Boolean {
    val n: Pattern = Pattern.compile(".*[0-9].*")
    val ac: Pattern = Pattern.compile("^.*[A-Z].*")
    val asm: Pattern = Pattern.compile("^.*[a-z].*")
    return (n.matcher(this).find() && (ac.matcher(this).find() || asm.matcher(this).find()))
}

fun EditText.Text(): String {
    return this.text.toString().trim()
}

fun TextView.Text(): String {
    return this.text.toString().trim()
}

fun Double.toMyString(): String {
    return try {
        (this.toInt()).toString()
    } catch (e: Exception) {
        this.toString()
    }
}

fun Double.toTwoDecimal(): String {
    return try {
        String.format("%.2f", this)
    } catch (e: Exception) {
        this.toString()
    }
}

fun String.toMyInt(): Int {
    return try {
        this.toDouble().toInt()
    } catch (e: Exception) {
        -1
    }
}

fun String.toMyDouble(): Double {
    return try {
        this.toDouble()
    } catch (e: Exception) {
        0.0
    }
}

fun Double.toThreeDecimal(): String {
    return try {
        String.format("%.3f", this)
    } catch (e: Exception) {
        this.toString()
    }
}

fun Double.toZeroDecimal(): String {
    return try {
        String.format("%.0f", this)
    } catch (e: Exception) {
        this.toString()
    }
}

fun EditText.setValue(value: String) {
    try {
        if (!value.isNullOrEmpty()) {
            this.setText(value)
        }
    } catch (e: Exception) {
        Log.e("exception", e.localizedMessage)
    }
}

fun TextView.setValueAdapter(value: String) {
    try {
        if (!value.isNullOrEmpty()) {
            this.text = value
        } else {
            this.text = ""
        }
    } catch (e: Exception) {
        Log.e("exception", e.localizedMessage)
    }
}

@BindingAdapter("formatPrice")
fun TextView.formatPrice(price: String) {
    if (!price.isNullOrEmpty()) {
        val number: Double = price.toDouble()
        this.text = "\u20B9 " + DecimalFormat("##,##,##,##,##,##,##0.00").format(number)
    } else {
        this.text = "\u20B9 " + "0.0"
    }
}

fun TextView.setValue(value: String) {
    try {
        if (!value.isNullOrEmpty()) {
            this.text = value
        } else {
            this.text = ""
        }
    } catch (e: Exception) {
        Log.e("exception", e.localizedMessage)
    }
}

fun <T> ArrayList<T>.arrayValue(arrayList: ArrayList<T>) {
    try {
        if (!arrayList.isNullOrEmpty()) {
            this.addAll(arrayList)
        }
    } catch (e: Exception) {
        Log.e("exception", e.localizedMessage)
    }
}

fun String.isValidGSTNo(): Boolean {
    return if (this.length == 15) {
        false
    } else {
        val regex = ("^[0-9]{2}[A-Z]{5}[0-9]{4}"
                + "[A-Z]{1}[1-9A-Z]{1}"
                + "Z[0-9A-Z]{1}$")
        val p = Pattern.compile(regex)
        val m = p.matcher(this)
        m.matches()
    }
}

fun String.isValidPANNO(): Boolean {
    return if (this.length == 10) {
        false
    } else {
        val regex = ("[A-Z]{5}[0-9]{4}[A-Z]{1}")
        val p = Pattern.compile(regex)
        val m = p.matcher(this)
        m.matches()
    }
}

@BindingAdapter("loadImage")
fun loadImage(view: ImageView, url: String) {

        Glide.with(view.context).load(url).placeholder(R.drawable.ic_image_place_holder)
            .error(R.drawable.ic_image_place_holder).into(view)
}


private val random = Random(Date().time)

fun generateRandomString(length: Int): String {
    val values = charArrayOf(
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9'
    )

    var out = ""
    for (i in 0 until length) {
        val idx: Int = random.nextInt(values.size)
        out += values[idx]
    }
    return out
}

@BindingAdapter("visibilityOnFlag")
fun visibilityOnFlag(view: ImageView, flag: Boolean) {
    if (flag) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.INVISIBLE
    }
}



@BindingAdapter("visibilityOnString")
fun visibilityOnString(view: LinearLayout, flag: String) {
    if (flag.isNullOrEmpty()) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
    }
}


@BindingAdapter("setVisibility")
fun setVisibility(view: View, isVisible: Boolean) {
    if (isVisible) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("setHtmlText")
fun setHtmlText(view: TextView, text: String) {
    if (!text.isNullOrEmpty()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            view.text = Html.fromHtml(text);
        }
    } else {
        view.text = ""
    }

}

@BindingAdapter("setRating")
fun setRating(ratingBar: RatingBar, rating: Int) {
    if (rating > 0) {
        ratingBar.rating = rating.toFloat()
    }
}


