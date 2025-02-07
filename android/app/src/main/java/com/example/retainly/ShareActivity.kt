package com.example.retainly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ShareActivity"

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val flashcardDao: FlashcardDao
) : ViewModel() {
    fun saveCard(englishText: String, polishTranslation: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                flashcardDao.insertCard(
                    Flashcard(
                        englishText = englishText,
                        polishTranslation = polishTranslation
                    )
                )
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving card", e)
            }
        }
    }
}

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShareScreen(
                        sharedText = sharedText,
                        onComplete = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun ShareScreen(
    sharedText: String,
    onComplete: () -> Unit,
    viewModel: ShareViewModel = hiltViewModel()
) {
    var englishText by remember { mutableStateOf(sharedText) }
    var polishTranslation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = englishText,
            onValueChange = { englishText = it },
            label = { Text("English Text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = polishTranslation,
            onValueChange = { polishTranslation = it },
            label = { Text("Polish Translation") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onComplete() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    isLoading = true
                    viewModel.saveCard(
                        englishText = englishText,
                        polishTranslation = polishTranslation
                    ) {
                        isLoading = false
                        onComplete()
                    }
                },
                enabled = !isLoading && englishText.isNotBlank() && polishTranslation.isNotBlank()
            ) {
                Text("Save Card")
            }
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}