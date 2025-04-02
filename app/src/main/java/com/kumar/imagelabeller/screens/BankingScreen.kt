package com.kumar.imagelabeller.screens

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var showOtpField by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isAuthenticated by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(60) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalActivity.current

    // Phone number validation pattern
    val phonePattern = Pattern.compile("^\\+?[0-9]{10,13}\$")

    fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    fun startCountdown() {
        coroutineScope.launch {
            remainingTime = 60
            while (remainingTime > 0) {
                delay(1000)
                remainingTime--
            }
        }
    }

    if (isAuthenticated) {
        SuccessScreen()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Banking Authentication",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!showOtpField) {
                // User Details Form
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneNumber.isNotEmpty() && !phonePattern.matcher(phoneNumber)
                            .matches()
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isEmpty() || accountNumber.isEmpty() || phoneNumber.isEmpty()) {
                                errorMessage = "Please fill all fields"
                            } else if (!phonePattern.matcher(phoneNumber).matches()) {
                                errorMessage = "Invalid phone number"
                            } else {
                                isLoading = true
                                errorMessage = ""
                                generatedOtp = generateOtp()
                                // Simulate sending OTP (In real app, integrate SMS gateway here)
                                println("Generated OTP: $generatedOtp")
                                Toast.makeText(context, generatedOtp, Toast.LENGTH_SHORT).show()
                                coroutineScope.launch {
                                    delay(1500) // Simulate network delay
                                    isLoading = false
                                    showOtpField = true
                                    startCountdown()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Send OTP")
                        }
                    }
                }
            } else {
                // OTP Verification
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter OTP sent to\n$phoneNumber",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 6) enteredOtp = it
                        },
                        label = { Text("6-digit OTP") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier.width(200.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (enteredOtp.length != 6) {
                                errorMessage = "Please enter 6-digit OTP"
                            } else if (enteredOtp != generatedOtp) {
                                errorMessage = "Invalid OTP entered"
                            } else {
                                isLoading = true
                                errorMessage = ""
                                coroutineScope.launch {
                                    delay(1000) // Simulate verification
                                    isLoading = false
                                    isAuthenticated = true
                                }
                            }
                        },
                        modifier = Modifier.width(200.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Verify OTP")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (remainingTime > 0) "Resend OTP in $remainingTime s"
                        else "Resend OTP",
                        modifier = Modifier.clickable(enabled = remainingTime == 0) {
                            if (remainingTime == 0) {
                                generatedOtp = generateOtp()
                                startCountdown()
                                // Resend OTP logic here
                                println("Resent OTP: $generatedOtp")
                            }
                        },
                        color = if (remainingTime == 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Success",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Authentication Successful!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "You can now proceed with your transactions",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
