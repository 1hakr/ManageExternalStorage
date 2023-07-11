package com.vinnamaral.manageexternalstorage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    // UI Views
    private lateinit var folderNameEt:EditText
    private lateinit var createFolderBtn: MaterialButton

    private companion object {
        // PERMISSION request constant, assign any value
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init UI Views
        folderNameEt = findViewById(R.id.folderNameEt)
        createFolderBtn = findViewById(R.id.createFolderBtn)

        // Handle click, create folder
        createFolderBtn.setOnClickListener {
            if (checkPermission()) {
                Log.d(TAG, "onCreate: Permission already granted, create folder")
                createFolder()
            }
            else {
                Log.d(TAG, "onCreate: Permission was not granted, request")
                requestPermission()
            }
        }
    }

    private fun createFolder() {
        //folder name
        val folderName = folderNameEt.text.toString().trim()

        //create folder using name we just input
        val file = File("${Environment.getExternalStorageDirectory()}/$folderName")

        //create folder
        val folderCreated = file.mkdir()

        //show if folder created or not
        if (folderCreated) {
            toast("Folder Created: ${file.absolutePath}")
        }
        else {
            toast("Folder not created!")
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e(TAG, "requestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        }
        else {
            //Android is below 11 (R)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, "storageActivityResultLauncher: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is granted")
                createFolder()
            }
            else {
                //Manage External Storage permission is denied
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is denied")
                toast("Manage External Storage Permission is denied")
            }
        }
        else {
            //Android is below 11 (R)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            Environment.isExternalStorageManager()
        }
        else {
            //Android is below 11 (R)
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {

                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")
                    createFolder()
                }
                else {
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied")
                    toast("External Storage Permission denied")
                }
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}