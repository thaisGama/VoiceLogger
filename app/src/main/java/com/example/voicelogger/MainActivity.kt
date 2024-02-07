package com.example.voicelogger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.voicelogger.ui.theme.VoiceLoggerTheme
import androidx.compose.runtime.*
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoiceLoggerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VoiceInputUI()
                }
            }
        }
    }
}

@Composable
fun VoiceInputUI() {
    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf("") }

    // Launcher for the voice input activity result
    val voiceInputLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            recognizedText = data?.get(0) ?: ""
            // Call writeToFile here to automatically log the recognized text
            if (recognizedText.isNotEmpty()) {
                appendToCsvFile(recognizedText, context)
            }
        }
    }

    // UI layout remains the same
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            voiceInputLauncher.launch(intent)
        }) {
            Text(text = "Start Voice Input")
        }

        Text(text = "Recognized Text: $recognizedText", modifier = Modifier.padding(top = 16.dp))
    }
}


// Example function to append data to a CSV file on external storage
fun appendToCsvFile(dosage: String, context: Context) {
    // Format for the timestamp
    val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault())
    // Get the current timestamp
    val timestamp = dateFormat.format(Date())

    // Data line to write, including the timestamp
    val dataLine = "$timestamp, $dosage\n"

    try {
        // File path to the external storage directory specific to your app
        val file = File(context.getExternalFilesDir(null), "dosageLog.csv")
        // Append the data line to the file
        file.appendText(dataLine)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}


@Preview(showBackground = true)
@Composable
fun VoiceLoggerPreview() {
    VoiceLoggerTheme {
        VoiceInputUI()
    }
}