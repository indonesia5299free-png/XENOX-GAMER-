package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydroTrackApp(viewModel: WaterViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hydro Track") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addWater(250) }) { // Quick add 250ml
                Icon(Icons.Default.Add, contentDescription = "Add Water")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DailyProgress(uiState = uiState)
            Spacer(modifier = Modifier.height(24.dp))
            QuickAddButtons(viewModel)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Today's History", 
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            IntakeHistory(
                intakes = uiState.intakes,
                onRemove = { viewModel.removeWater(it) }
            )
        }

        if (showSettings) {
            SettingsDialog(
                uiState = uiState,
                onDismiss = { showSettings = false },
                onGoalChange = { viewModel.setDailyGoal(it) },
                onNotificationToggle = { viewModel.setNotificationsEnabled(it) }
            )
        }
    }
}

@Composable
fun DailyProgress(uiState: WaterUiState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        CircularProgressIndicator(
            progress = { uiState.progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 16.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${uiState.totalIntakeMl} ml",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "of ${uiState.dailyGoalMl} ml",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickAddButtons(viewModel: WaterViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickAddButton(150, "Cup", viewModel)
        QuickAddButton(250, "Glass", viewModel)
        QuickAddButton(500, "Bottle", viewModel)
    }
}

@Composable
fun QuickAddButton(amount: Int, label: String, viewModel: WaterViewModel) {
    OutlinedButton(
        onClick = { viewModel.addWater(amount) },
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("+$amount ml", style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun IntakeHistory(
    intakes: List<com.example.data.local.WaterIntake>,
    onRemove: (Int) -> Unit
) {
    if (intakes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No water logged today yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(intakes) { intake ->
                ListItem(
                    headlineContent = { Text("${intake.amountMl} ml") },
                    supportingContent = { 
                        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text(format.format(Date(intake.timestampMillis))) 
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WaterDrop, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
                    trailingContent = {
                        TextButton(onClick = { onRemove(intake.id) }) {
                            Text("Remove")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun SettingsDialog(
    uiState: WaterUiState,
    onDismiss: () -> Unit,
    onGoalChange: (Int) -> Unit,
    onNotificationToggle: (Boolean) -> Unit
) {
    var goalText by remember { mutableStateOf(uiState.dailyGoalMl.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Settings", style = MaterialTheme.typography.headlineSmall)
                
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Goal (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Reminders")
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { onNotificationToggle(it) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val parsedGoal = goalText.toIntOrNull()
                        if (parsedGoal != null && parsedGoal > 0) {
                            onGoalChange(parsedGoal)
                        }
                        onDismiss()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
