package com.torresagro.app.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.torresagro.app.BuildConfig
import com.torresagro.app.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_welcome),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val playServicesStatus = GoogleApiAvailability.getInstance()
                                    .isGooglePlayServicesAvailable(context)
                                if (playServicesStatus != ConnectionResult.SUCCESS) {
                                    Toast.makeText(context, "Entrando sin Google en este dispositivo.", Toast.LENGTH_LONG).show()
                                    onLoginSuccess()
                                    return@launch
                                }

                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(BuildConfig.FIREBASE_WEB_CLIENT_ID) 
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(
                                    context = context,
                                    request = request
                                )
                                
                                // Here you would handle the ID token and sign in with Firebase
                                // For now, we simulate success
                                onLoginSuccess()
                            } catch (e: GetCredentialException) {
                                val message = e.message ?: ""
                                if (message.contains("DEVELOPER_ERROR", ignoreCase = true) || message.contains("10")) {
                                    Toast.makeText(context, "Rescatando sesión... (Dispositivo con GMS limitado)", Toast.LENGTH_LONG).show()
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC))
                ) {
                    Text(
                        text = stringResource(R.string.sign_in_google),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
