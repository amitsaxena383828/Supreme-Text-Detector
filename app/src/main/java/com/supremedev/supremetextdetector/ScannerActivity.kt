package com.supremedev.supremetextdetector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.supremedev.supremetextdetector.databinding.ActivityScannerBinding

class ScannerActivity : AppCompatActivity() {
    private lateinit var activityScannerBinding: ActivityScannerBinding
    private val MY_RESULT_FOR_IMAGE = 102
    private lateinit var pic: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityScannerBinding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(activityScannerBinding.root)
        setupActionBar()
        setListener()
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.custom_action_bar)
    }

    private fun setListener() {
        activityScannerBinding.buttonDetect.setOnClickListener {
            extractText()
        }

        activityScannerBinding.buttonSnap.setOnClickListener {
            if (checkPermission()) {
                captureImage()
            } else {
                requestPermission()
            }

        }
    }

    private fun checkPermission(): Boolean {
        val cameraPermission: Int =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val PERMISSION_CODE = 100
        requestPermissions(
            this, arrayOf<String>(Manifest.permission.CAMERA), PERMISSION_CODE
        )
    }

    @Deprecated("Deprecated in Java")
    @ExperimentalGetImage
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MY_RESULT_FOR_IMAGE -> {
                pic = data?.extras?.get("data") as Bitmap
                activityScannerBinding.image.setImageBitmap(pic)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("Amit", "onRequestPermissionsResult: Permission check")
        if (grantResults.isNotEmpty()) {
            val cameraPermission: Boolean = grantResults.get(0) == PackageManager.PERMISSION_GRANTED
            if (cameraPermission) {
                captureImage()
            } else {
                Toast.makeText(this, "PermissionDenied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, MY_RESULT_FOR_IMAGE)
    }

    private fun extractText() {
        val imageBitmap = InputImage.fromBitmap(pic, 0)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        textRecognizer.process(imageBitmap).addOnSuccessListener {
            if (it != null) {
                activityScannerBinding.textView.text = it.text
                Toast.makeText(this, "text detected ${it.text}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "text can not be detected", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Unable to detect text", Toast.LENGTH_LONG).show()
        }
    }
}