package com.example.prophet.page

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prophet.R
import com.example.prophet.sqlite.PatientDB
import com.example.prophet.sqlite.PatientDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Preview
@Composable
fun MyPreview() {
    My(
        innerPadding = PaddingValues(0.dp)
    )
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun My(
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(innerPadding),
    ) {

//        db = initDatabase()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp, 70.dp, 30.dp, 10.dp),
        ) {
            UserImage()
            val patientDao: PatientDao = PatientDB.getDB(context).patientDao()
            val name = remember { mutableStateOf("") }
            val age = remember { mutableStateOf("") }
            val weight = remember { mutableStateOf("") }
            val height = remember { mutableStateOf("") }
            val bmi = remember { mutableStateOf("") }
            CoroutineScope(Dispatchers.IO).launch {
                val patient = patientDao.getPatient(1)
                withContext(Dispatchers.Main) {
                    if (patient != null) {
                        Log.d("My", "Patient: $patient")
                        name.value = patient.name
                        age.value = patient.age.toString()
                        weight.value = patient.weight.toString()
                        height.value = patient.height.toString()
                        bmi.value = patient.bmi.toString()
                    } else {
                        Log.d("My", "Patient is null")
                        name.value = "信息未录入"
                        age.value = "信息未录入"
                        weight.value = "信息未录入"
                        height.value = "信息未录入"
                        bmi.value = "信息未录入"
                    }
                }
            }

            //姓名
            Text(
                text = "姓名：${name.value}",
                modifier = Modifier
                    .padding(10.dp, 50.dp, 0.dp, 0.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            //年龄
            Text(
                text = "年龄：${age.value}",
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            //身高
            Text(
                text = "身高：${height.value}cm",
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            //体重
            Text(
                text = "体重：${weight.value}kg",
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            //BMI
            Text(
                text = "BMI：${bmi.value}",
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
        }
    }

}

@Composable
fun UserImage() {
    Surface (
        shape = CircleShape,
        modifier = Modifier
            .shadow(20.dp, shape = CircleShape)
    ){
        Image(
            painter = painterResource(id = R.drawable.avatar),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = "User Image",
            modifier = Modifier
                .size(70.dp),
            contentScale = ContentScale.Crop,
        )
    }

}

//@Composable
//private fun initDatabase(): PatientDatabase {
//    val applicationContext = LocalContext.current.applicationContext
//
//    val db = Room.databaseBuilder(
//        applicationContext,
//        PatientDatabase::class.java, "patient"
//    ).build()
//
//    return db
//}