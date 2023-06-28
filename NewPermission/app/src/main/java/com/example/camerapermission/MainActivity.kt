package com.example.camerapermission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camerapermission.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var imageUri: Uri
    var bottomDialog: BottomSheetDialog? = null
    var selectedImagePaths = ""

    companion object {
        //permission
        const val STORAGE_PERMISSION_CODE = 101
        const val CAMERA_PERMISSION_CODE = 100
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btCamera.setOnClickListener {
            openBottomSheet()
        }


    }

    private fun openBottomSheet() {

        bottomDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.image_dialog, null)
        bottomDialog!!.setContentView(view)
        val camera = view.findViewById<LinearLayout>(R.id.linear_camera)
        val gallery = view.findViewById<LinearLayout>(R.id.linear_gallery)
        val close = view.findViewById<ImageView>(R.id.img_close)

        close.setOnClickListener {
            bottomDialog!!.dismiss()
        }


        camera.setOnClickListener {

            checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)
            bottomDialog?.dismiss()

        }

        gallery.setOnClickListener {
            checkPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE
            )
            bottomDialog?.dismiss()

            //openGallery()
        }
        bottomDialog!!.show()
    }

    private fun checkPermissions(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            openGallery()
        }


    }
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraresultLauncher.launch(takePictureIntent)
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryresultLauncher.launch(intent)
    }

    var galleryresultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (bottomDialog != null) {
                    bottomDialog?.dismiss()
                }

                imageUri = data?.data!!
                binding.imageView.setImageURI(imageUri)

//                val selectedImageUri: String = attr.data.toString()
//                val projection = arrayOf(MediaStore.Images.Media.DATA)
//                uploaduserimage.setImageURI(data?.data) // handle chosen image
                //val selectedImageUri: Int = attr.data?.extras?.get("data")
                // selectedImagePaths = getRealPathFromURI(imageUri)
                // Log.e("Image File Path", "" + selectedImagePaths)

            }
        }
    var cameraresultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val imageBitmap = data?.extras?.get("data") as Bitmap
                if (bottomDialog != null) {
                    bottomDialog?.dismiss()
                }

                val tempUri: Uri = getImageUri(applicationContext, imageBitmap)!!

                selectedImagePaths = File(getRealPathsFromURI(tempUri).toString()).toString()

                System.out.println(selectedImagePaths)

                // Log.e("CAMERA", selectedImagePaths.toString())

                binding.imageView.setImageBitmap(imageBitmap)
            }
        }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            inContext.contentResolver, inImage, "Title", null
        )
        return Uri.parse(path)
    }

    fun getRealPathsFromURI(uri: Uri?): String {
        var path = ""
        if (contentResolver != null) {
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                // Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraresultLauncher.launch(takePictureIntent)
            } else {
                // Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
                openGallery()
            } else {
                //   Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}