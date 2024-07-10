package com.example.prophet.page

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.prophet.bluetoothUtil.loopRead
import com.example.prophet.bluetoothUtil.write
import com.example.prophet.sqlite.PatientDB
import com.example.prophet.sqlite.PatientDao
import com.example.prophet.sqlite.PatientEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


@Preview
@Composable
fun PreviewAddUser() {
    addUser({})
}

//private lateinit var db: PatientDatabase

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun addUser(
    navigateUp: () -> Unit,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var bmi by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            addUserAppBar(
                name,
                age,
                weight,
                height,
                bmi,
                navigateUp
            )
        }
    ) {innerPadding ->
//        db = initDatabase()

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Text(
                modifier = Modifier.padding(16.dp, 30.dp, 16.dp, 30.dp),
                color = MaterialTheme.colorScheme.primary,
                text = "输入用户信息至\n" +
                        "2K1000LA",
                fontSize = 30.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            userInput(innerPadding, context, name, "姓名", false, {newValue ->
                name = newValue
            }, keyboardType = KeyboardType.Text)
            userInput(innerPadding, context, age, "年龄", false, {newValue ->
                age = newValue
            }, keyboardType = KeyboardType.Number)
            userInput(innerPadding, context, weight, "体重", false, { newValue ->
                weight = newValue
                bmi = calculateBmi(newValue, height)
            }, keyboardType = KeyboardType.Number)
            userInput(innerPadding, context, height, "身高", false, { newValue ->
                height = newValue
                bmi = calculateBmi(weight, newValue)
            }, keyboardType = KeyboardType.Number)
            userInput(innerPadding, context, bmi, bmi, true,
                keyboardType = KeyboardType.Number)
        }


    }

}

private fun calculateBmi(weight: String, height: String): String {
    val weightInKg = weight.toFloatOrNull()
    val heightInM = height.toFloatOrNull()?.div(100)  // convert cm to m
    return if (weightInKg != null && heightInM != null && heightInM != 0f) {
        val bmiValue = weightInKg / (heightInM * heightInM)
        "%.2f".format(bmiValue)
    } else {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addUserAppBar(
    name: String,
    age: String,
    weight: String,
    height: String,
    bmi: String,
    navigateUp: () -> Unit
) {

    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    TopAppBar(
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        title = {
            Text(
                color = MaterialTheme.colorScheme.primary,
                text = "添加新用户"
            )
        },
        actions = {
                  IconButton(onClick = {
                      Log.d("name", name)
                        Log.d("weight", weight)
                        Log.d("height", height)
                        Log.d("bmi", bmi)
                      Log.d("age", age)
                      showDialog.value = true
                      val patientDao: PatientDao = PatientDB.getDB(context).patientDao()
                      CoroutineScope(Dispatchers.IO).launch {
                          if (updateUserData(bmi, context, showDialog)) {
                              val patient = PatientEntity(
                                  id = 1,
                                  name = name,
                                  weight = weight.toDouble(),
                                  height = height.toDouble(),
                                  age = age.toInt(),
                                  bmi = bmi.toDouble()
                              )
                              Log.d("patient", patient.toString())
                              if (patientDao.getPatient(1) != null) {
                                  patientDao.updatePatient(patient)
                              } else {
                                  patientDao.insertPatient(patient)
                              }
                              withContext(Dispatchers.Main) {
                                  navigateUp()
                              }

                          }
                      }

                  }) {
                      Icon(Icons.Default.Send, contentDescription = "send_button")
                  }
        },
        navigationIcon = {
            IconButton(onClick = { navigateUp() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "back_button"
                )
            }
        },
    )

    if (showDialog.value) {
        LoadPatientDialog(showDialog)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun userInput(
    innerPadding: PaddingValues,
    context: Context,
    content: String,
    hint: String,
    readOnly: Boolean,
    onValueChange: (String) -> Unit = {},
    keyboardType: KeyboardType,
) {
    var temp by remember { mutableStateOf(content) }
    Card (
        modifier = Modifier
            .padding(16.dp, 10.dp, 16.dp, 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(100.dp),
        ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = temp,
            textStyle = TextStyle(fontWeight = FontWeight.Bold),
            onValueChange = {newValue ->
                temp = newValue
                onValueChange(newValue)
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            placeholder = {
                Text(
                    text = hint,
                )
            },
            singleLine = true,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(
                keyboardType =  keyboardType
            )
        )
    }
}

suspend fun updateUserData(bmi: String, context: Context, showDialog: MutableState<Boolean>): Boolean {
    var flag = false
    val job = CoroutineScope(Dispatchers.IO).launch {
        val data = "#$bmi;#$bmi;#$bmi;#$bmi;#$bmi;#$bmi;".toByteArray()
        var i = 0
        while (i < 3) {
            Log.d("sendData", data.toString(Charsets.UTF_8))
            if (write(data)) {
                loopRead().collect {
                    Log.d("updateUserData", it.toString(Charsets.UTF_8))
                    if (it.toString(Charsets.UTF_8) == "1") {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "用户数据上传成功", Toast.LENGTH_SHORT)
                                .show()
                        }
                        showDialog.value = false
                        flag = true
                    }
                }
            }
            i++
        }
        Log.d("updateUserData111", "flag is $flag")
        if (flag) {
            Log.d("updateUserData11111", "flag is true")
            return@launch
        }
        withContext(Dispatchers.Main) {
            showDialog.value = false
            Toast.makeText(context, "用户数据上传失败", Toast.LENGTH_SHORT).show()
        }
    }
    try {
        withTimeout(10000) {
            job.join()
            return@withTimeout true
        }
    } catch (e: TimeoutCancellationException) {
        // 处理超时的情况
        withContext(Dispatchers.Main) {
            showDialog.value = false
            Toast.makeText(context, "接收数据超时", Toast.LENGTH_SHORT).show()
        }
    }
    return flag
}



@Composable
fun LoadPatientDialog(showDialog: MutableState<Boolean>) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        Dialog(
            onDismissRequest = { showDialog.value = false },
            content = {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.wrapContentSize(Alignment.Center),
                            text = "正在上传数据",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }

            },
        )
    }
}
