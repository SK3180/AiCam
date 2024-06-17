package com.sksingh.radr

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sksingh.radr.screens.CameraPreview
import com.sksingh.radr.screens.PhotoSheetContent
import com.sksingh.radr.ui.theme.RadRTheme
import com.sksingh.radr.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class Camera : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (!hasRequiredPermission()) {
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSION, 0)
        }

        setContent {

            val font = FontFamily(
                Font(R.font.dontnew)
            )
            val scope = rememberCoroutineScope()
            val scaffoldState = rememberBottomSheetScaffoldState()
           var ans by remember {
                mutableStateOf("First Capture the picture then Click on Tick Button And Wait for 3 Seconds")
           }
            RadRTheme {
                val viewModel: MainViewModel = viewModel()
                val bitmaps by viewModel.bitmaps.collectAsState()

                val context = this

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoSheetContent(
                            bitmaps = bitmaps,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                       //     .fillMaxSize()
                            .padding(padding)
                    ) {
                        val controller = remember {
                            LifecycleCameraController(context).apply {
                                setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
                            }
                        }

                        CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp)
                            ,
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(20.dp,Alignment.CenterHorizontally)

                        ) {
                            IconButton(onClick = {
                                scope.launch {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.PhotoAlbum,
                                    contentDescription = "Gallery",
                                    modifier = Modifier
                                        .size(70.dp)
                                        .padding(),
                                    tint = Color.White
                                )
                            }

                            IconButton(onClick = {
                                takePhoto(controller, onPhotoTaken = viewModel::onTakePhoto)
                                Toast.makeText(context, "Captured Successfully", Toast.LENGTH_SHORT).show()

                            }) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Capture",
                                    modifier = Modifier
                                        .size(70.dp)
                                        .padding(),
                                            tint = Color.White
                                )
                            }

                            IconButton(onClick = {
                                Toast.makeText(context, "Processing Image..", Toast.LENGTH_SHORT).show()
                                if (bitmaps.isNotEmpty()) {
                                    val bitmap = bitmaps.last()
                                    viewModel.processImage(bitmap) { response ->
                                        ans = response
                                        // Handle the response
                                        Log.d("Camera", "Image processed: $response")
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Send",
                                    modifier = Modifier
                                        .size(70.dp)
                                        .padding(),
                                    tint = Color.Green
                                )
                            }
                        }
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                            .clip(shape = RoundedCornerShape(20.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
                        ) {

                            Text(modifier = Modifier

                                .background(Color(47, 49, 51),
                                    shape = RoundedCornerShape(20.dp))
                                .padding(10.dp),
                                text = ans ,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = font
                            )


                        }
                    }
                }
            }
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("RestrictedApi")
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    onPhotoTaken(image.toBitmap())

                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo", exception)
                }
            }
        )
    }

    private fun hasRequiredPermission(): Boolean {
        return CAMERAX_PERMISSION.all {
            ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSION = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}


