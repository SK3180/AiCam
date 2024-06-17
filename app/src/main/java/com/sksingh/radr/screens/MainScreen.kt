package com.sksingh.radr.screens

import android.content.Intent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sksingh.radr.R

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreen(){
    val font = FontFamily(
        Font(R.font.dontnew)
    )

    val qr = painterResource(id = R.drawable.qr)
    val context = LocalContext.current
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(51, 51, 51))
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly) {
        Row {

            Text(text = "AI CAM", modifier = Modifier
                .padding(horizontal = 20.dp)
                .border(1.dp, Color.Green, RoundedCornerShape(20.dp))
                .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 50.sp,
            fontFamily = font,
            )
        }


            Image(painter = qr,
                contentDescription = "radR Logo",
                modifier = Modifier.size(200.dp)

            )
        Column(modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    val intent = Intent(context, com.sksingh.radr.Camera::class.java)
                    context.startActivity(intent)

                          },
                modifier = Modifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color(254, 182, 36))
            ) {
                Text(text = "Scan Product",
                    color = Color.Black,
                    fontSize = 20.sp
                )
            }

        }
    }


}





