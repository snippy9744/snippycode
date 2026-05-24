package com.snippyseat.app.feature.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.snippyseat.app.R
import com.snippyseat.app.ui.theme.Divider
import com.snippyseat.app.ui.theme.Error
import com.snippyseat.app.ui.theme.JetBrainsMono
import com.snippyseat.app.ui.theme.LightRed
import com.snippyseat.app.ui.theme.Primary
import com.snippyseat.app.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    onNavigate: (String) -> Unit,
    autoStartGoogle: Boolean = false,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val googleClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.google_web_client_id))
            .build(),
    )
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            viewModel.continueWithGoogle(task.result?.idToken, onNavigate)
        }
    }

    LaunchedEffect(autoStartGoogle) {
        if (autoStartGoogle) {
            googleLauncher.launch(googleClient.signInIntent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 34.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.PhoneIphone,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier
                .size(58.dp)
                .background(LightRed, CircleShape)
                .padding(13.dp),
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Login to Snippy Seat",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Use your Indian mobile number or Google account.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = viewModel::updatePhone,
            label = { Text("Mobile number") },
            leadingIcon = { Text(text = "+91", fontWeight = FontWeight.Bold, color = Primary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.otpSent) {
            OtpInput(
                value = uiState.otp,
                onValueChange = viewModel::updateOtp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            CountdownRow(
                countdown = uiState.countdown,
                onResend = viewModel::sendOtp,
            )
        }
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = error, color = Error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = {
                if (uiState.otpSent) {
                    viewModel.verifyOtp(onNavigate)
                } else {
                    viewModel.sendOtp()
                }
            },
            enabled = !uiState.loading,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(text = if (uiState.otpSent) "Verify OTP" else "Send OTP")
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        GoogleSignInButton(
            enabled = !uiState.loading,
            onClick = { googleLauncher.launch(googleClient.signInIntent) },
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = { viewModel.enterGuest(onNavigate) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = "Explore as Guest →",
                color = Primary,
                fontSize = 13.sp,
            )
        }
        Text(
            text = "No account needed to browse salons",
            color = Color(0xFF9E9E9E),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val focusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        textStyle = TextStyle(
            color = Color.Transparent,
            fontFamily = JetBrainsMono,
        ),
        modifier = Modifier.focusRequester(focusRequester),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(6) { index ->
                    val char = value.getOrNull(index)?.toString().orEmpty()
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = if (char.isNotEmpty()) Primary else Divider,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun CountdownRow(
    countdown: Int,
    onResend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator(
                progress = { countdown / 30f },
                color = Primary,
                trackColor = LightRed,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
            )
            Text(text = "${countdown}s", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(enabled = countdown == 0, onClick = onResend) {
            Text(text = "Resend OTP")
        }
    }
}

@Composable
private fun GoogleSignInButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "G",
                color = Primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(text = "Continue with Google", style = MaterialTheme.typography.titleMedium)
        }
    }
}
