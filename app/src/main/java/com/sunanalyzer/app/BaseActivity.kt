package com.sunanalyzer.app

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.sunanalyzer.app.utility.*
import kotlinx.coroutines.runBlocking
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


open class BaseActivity : AppCompatActivity() {

    private var firstTimeBackPressed = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViews();
    }

    override fun onNightModeChanged(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onNightModeChanged(mode)
    }

    private fun initializeViews() {

    }


    fun setUpToolbarWithBackArrow(toolbar: Toolbar, strTitle: String, isBackArrow: Boolean) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayHomeAsUpEnabled(isBackArrow)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_left_arrow)
            val title = toolbar.findViewById<TextView>(R.id.titleToolbar)
            title.text = strTitle
        }
    }

    fun setUpToolbarWithBackText(toolbar: Toolbar, strTitle: String, isBackArrow: Boolean) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayHomeAsUpEnabled(false)
            val title = toolbar.findViewById<TextView>(R.id.titleToolbar)
            val llBack = toolbar.findViewById<LinearLayout>(R.id.llBack)
            title.text = strTitle
            llBack.setOnClickListener {
                onBackPressed()
            }
            if (!isBackArrow) {
                llBack.gone()
            } else {
                llBack.visible()
            }
        }
    }

    override fun onBackPressed() {
        if (firstTimeBackPressed) {
            finish()
            super.onBackPressed()
        } else {
            this.toast("Press again to exit")
            firstTimeBackPressed = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun askPermission(): Boolean {
        if (!RuntimeAppPermission.checkGrantedPermission(this, Manifest.permission.CAMERA)
            || !RuntimeAppPermission.checkGrantedPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            || !RuntimeAppPermission.checkGrantedPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

        ) {
            var permissionsList: ArrayList<String> = ArrayList()
            permissionsList!!.add(Manifest.permission.CAMERA)
            permissionsList!!.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsList!!.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            RuntimeAppPermission.getPermissions(this, permissionsList!!)
            return false
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()) {
            var rejectedPemissionCount = 0
            var success = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    success = false
                    rejectedPemissionCount++
                }
            }
            if (!success) {
                var permissionsList: java.util.ArrayList<String> = java.util.ArrayList()
                permissionsList.add(Manifest.permission.CAMERA)
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                RuntimeAppPermission.getRuntimePermissionSnackBar(
                    window.decorView.rootView,
                    rejectedPemissionCount,
                    permissionsList,
                    this
                )
            }
        }
    }


    private fun getFileNameByUri(uri: Uri): String {
        var fileName = System.currentTimeMillis().toString()
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            fileName =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            cursor.close()
        }
        return fileName
    }

    private fun copyUriToExternalFilesDir(uri: Uri, fileName: String): String {
        var filePath = ""
        runBlocking {
            val inputStream = contentResolver.openInputStream(uri)
            val tempDir = getExternalFilesDir("temp")
            if (inputStream != null && tempDir != null) {
                val file = File("$tempDir/$fileName")
                filePath = file.absolutePath
                val fos = FileOutputStream(file)
                val bis = BufferedInputStream(inputStream)
                val bos = BufferedOutputStream(fos)
                val byteArray = ByteArray(1024)
                var bytes = bis.read(byteArray)
                while (bytes > 0) {
                    bos.write(byteArray, 0, bytes)
                    bos.flush()
                    bytes = bis.read(byteArray)
                }
                bos.close()
                fos.close()
            }
        }
        return filePath

    }


    /**
     * This function is used to show pdf file with external app
     * @author Dixit Panchal
     * @since 21/1/2021
     * @param path as url of pdf file
     * */
    fun openPdfWithExternalApp(path: String) {
        if (path.startsWith("http")) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
                startActivity(browserIntent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val file: File = File(path)
                if (file.exists()) {
                    val path = FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName.toString() + ".provider",
                        file.absoluteFile
                    )
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(path, "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        this.toast("No Application Available to View PDF")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * This function is used to dial a mobile number
     * @author Dixit Panchal
     * @since 22/1/2021
     * @param phoneNumber as contact number of user
     * */
    fun actionCall(phoneNumber: String) {
        try {
            if (!phoneNumber.isNullOrEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    startActivity(intent)
                } catch (e: Exception) {
                }
            } else {
                toast("Contact number is not available.")
            }
        } catch (e: Exception) {
        }
    }

    /**
     * This function is used to dial a mobile number
     * @author Dixit Panchal
     * @since 22/1/2021
     * @param email as contact number of user
     * */
    fun sendEmail(email: String) {
        try {
            val emailIntent = Intent()
            emailIntent.type = "text/plain"
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This function is used to close the keyboard
     * @author Dixit Panchal
     * @since 09/02/2021
     * */
    fun closeSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * This function is used to show alert to user with yes or no option
     * @author Dixit Panchal
     * @since 09/02/2021
     * */
    fun showAlertDialog(msg: String, positiveListener: DialogInterface.OnClickListener) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert !")
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("Yes", positiveListener)
        alertDialogBuilder.setNegativeButton("No", null)
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    /**
     * This function is used to show alert to user with yes or no option
     * @author Dixit Panchal
     * @since 09/02/2021
     * */
    fun showAlertDialog(
        msg: String,
        positiveListener: DialogInterface.OnClickListener,
        negativeListener: DialogInterface.OnClickListener,
    ) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert !")
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("Yes", positiveListener)
        alertDialogBuilder.setNegativeButton("No", negativeListener)
        alertDialogBuilder.setCancelable(false)
        val alertDialog: AlertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * This function is used to show alert to user
     * @author Dixit Panchal
     * @since 09/02/2021
     * */
    fun showAlertDialog(
        msg: String,
        positiveString: String,
        negativeString: String,
        positiveListener: DialogInterface.OnClickListener,
    ) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert !")
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton(positiveString, positiveListener)
        alertDialogBuilder.setNegativeButton(negativeString, null)
        val alertDialog: AlertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * This function is used to show alert to user
     * @author Dixit Panchal
     * @since 09/02/2021
     * */
    fun showAlert(msg: String, positiveListener: DialogInterface.OnClickListener) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert !")
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("Okay", positiveListener)
        val alertDialog: AlertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * This function is used to redirect user to google map using geo location
     * @author Dixit Panchal
     * @since 11/03/2021
     * */
    fun openGoogleMap(location: String) {
        if (location != "," && location.isNotEmpty()) {
            try {
                val geoUri =
                    Uri.parse("http://maps.google.com/maps?q=loc:$location (Your Location)")
                val intent = Intent(Intent.ACTION_VIEW, geoUri)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            toast("Location is not available")
        }
    }

    /**
     * This function is used to get meter to ft or ft to meter
     * @author Dixit Panchal
     * @since 06/01/2022
     * */
    fun meterToFtSquare(value: Double, inMeter: Boolean): String {
        var result = 0.0
        val meter = 10.764
        result = if (inMeter) {
            value
        } else {
            value * meter
        }

        return result.toTwoDecimal()
    }

    /**
     * This function is used to get meter to ft or ft to meter
     * @author Dixit Panchal
     * @since 06/01/2022
     * */
    fun meterToFt(value: Double, inMeter: Boolean): String {
        var result = 0.0
        val meter = 3.281
        result = if (inMeter) {
            value
        } else {
            value * meter
        }

        return result.toTwoDecimal()
    }


}