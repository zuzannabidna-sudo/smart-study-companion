package com.example.obrona

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Question(val title: String, val shortDesc: String, val detailedDesc: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF625b71)) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    // Stan wybranego pliku i wybranego pytania
    var selectedFile by remember { mutableStateOf("arkusz1.txt") }
    var selectedQuestion by remember { mutableStateOf<Question?>(null) }

    // Obliczanie indeksu zakładki na podstawie nazwy pliku
    val tabIndex = when(selectedFile) {
        "arkusz1.txt" -> 0
        "arkusz2.txt" -> 1
        "arkusz3.txt" -> 2
        "arkusz4.txt" -> 3
        else -> 0
    }


    var questions by remember { mutableStateOf(listOf<Question>()) }

    LaunchedEffect(selectedFile) {
        // Przełączamy się na wątek IO (tło)
        withContext(Dispatchers.IO) {
            val list = mutableListOf<Question>()
            try {
                context.assets.open(selectedFile).bufferedReader(Charsets.UTF_8).useLines { lines ->
                    lines.forEach { line ->
                        val cleanLine = line.trim().replace("\uFEFF", "")
                        if (cleanLine.isNotEmpty()) {
                            val parts = cleanLine.split("\t")
                            if (parts.size >= 3) {
                                val formattedDesc = parts[2].trim()
                                    .replace("\\n", "\n")
                                    .replace("•", "\n•")

                                list.add(
                                    Question(
                                        title = parts[0].trim(),
                                        shortDesc = parts[1].trim(),
                                        detailedDesc = formattedDesc
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            questions = list
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Górne menu wyboru arkusza
        ScrollableTabRow(
            selectedTabIndex = tabIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF228822),
            divider = {Color(0xFF228822)}
        ) {
            listOf("Arkusz 1", "Arkusz 2", "Arkusz 3").forEachIndexed { index, name ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { selectedFile = "arkusz${index + 1}.txt" },
                    text = { Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF625b71)) }
                )
            }
        }

        // Lista pytań
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            items(questions) { question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedQuestion = question },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question.title,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.shortDesc,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Okno szczegółów
    selectedQuestion?.let { q ->
        AlertDialog(
            onDismissRequest = { selectedQuestion = null },
            title = {
                Text(
                    text = q.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = q.detailedDesc,
                        fontSize = 16.sp,
                        lineHeight = 26.sp, // Większy odstęp dla wektorów i ułamków
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedQuestion = null },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6650a4))
                ) {
                    Text("Rozumiem")
                }
            }
        )
    }
}