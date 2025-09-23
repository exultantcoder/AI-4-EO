/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.customtasks.interactivelearning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween

// --- Reusable UI Components ---

@Composable
fun OnboardingScreen(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNext,
                enabled = value.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    onContinueLearning: () -> Unit,
    onResetProfile: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Welcome back, ${userProfile.name}! 👋", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Login Count: ${userProfile.loginCount}", style = MaterialTheme.typography.bodyMedium)
            Text("Language: ${userProfile.language}", style = MaterialTheme.typography.bodyLarge)
            Text("Favorite Topic: ${userProfile.favoriteTopic}", style = MaterialTheme.typography.bodyLarge)

            if (userProfile.solarScore > 0) Text("🌞 Solar Energy Score: ${userProfile.solarScore}%")
            if (userProfile.windEnergyScore > 0) Text("💨 Wind Energy Score: ${userProfile.windEnergyScore}%")
            if (userProfile.customProjectScore > 0) Text("🛠️ Custom Project Score: ${userProfile.customProjectScore}%")

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onContinueLearning, modifier = Modifier.weight(1f)) {
                    Text("Continue Learning")
                }
                Button(onClick = onResetProfile, modifier = Modifier.weight(1f)) {
                    Text("Reset Profile")
                }
            }
        }
    }
}

@Composable
fun ConfirmationScreen(
    userProfile: UserProfile,
    onConfirm: () -> Unit,
    onEdit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Please confirm your details:", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Language: ${userProfile.language}")
            Text("Name: ${userProfile.name}")
            Text("Favorite Topic: ${userProfile.favoriteTopic}")
            Text("Motivation: ${userProfile.motivation}")
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                    Text("Yes, correct")
                }
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Edit details")
                }
            }
        }
    }
}

@Composable
fun InfoScreen(title: String, buttonText: String, onButtonClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onButtonClick, modifier = Modifier.fillMaxWidth()) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun LearningLevelScreen(onSelectLevel: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Choose your learning level:", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSelectLevel, modifier = Modifier.fillMaxWidth()) {
                Text("Hands-On / Práctico ✓")
            }
        }
    }
}

@Composable
fun ActivitySelectionScreen(
    userProfile: UserProfile,
    onSelectActivity: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Choose your activity:", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            val activities = listOf("Harvest Solar Energy", "Harvest Wind Energy", "Custom Project","TalkToMe")
            activities.forEach { activity ->
                val translated = when (activity) {
                    "Harvest Solar Energy" -> if (userProfile.language.contains("Español", ignoreCase = true)) "Cosechar Energía Solar" else activity
                    "Harvest Wind Energy" -> if (userProfile.language.contains("Español", ignoreCase = true)) "Cosechar Energía Eólica" else activity
                    "Custom Project" -> if (userProfile.language.contains("Español", ignoreCase = true)) "Proyecto Personalizado" else activity

                    else -> activity
                }

                Button(
                    onClick = { onSelectActivity(activity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("$activity / $translated")
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SolarIntroScreen(onStartQuiz: () -> Unit) {
    var currentStepIndex by remember { mutableStateOf(0) }

    val steps = listOf(
        "Find a pizza box, black paper, clear plastic wrap, and aluminum foil." to "🏠",
        "Cut a flap into the box's lid and cover the inside of the flap with foil." to "✂️",
        "Line the box bottom with the black paper to absorb heat." to "📄",
        "Seal the opening with clear plastic wrap to trap the sun's heat." to "🌡️",
        "Place a marshmallow inside and point the foil flap toward the sun." to "🍡",
        "Watch your simple oven use solar energy to cook your treat!" to "🔥"
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("🌞 Harvest Solar Energy", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text(
                "Let's learn how to build a simple DIY solar oven!",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(24.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = (currentStepIndex + 1) / steps.size.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Current step display
            AnimatedContent(
                targetState = currentStepIndex,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) with slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    )
                }
            ) { stepIndex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = steps[stepIndex].second,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Step ${stepIndex + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = steps[stepIndex].first,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous button
                Button(
                    onClick = { currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0) },
                    enabled = currentStepIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Previous")
                }

                Spacer(Modifier.width(12.dp))

                // Next/Quiz button
                Button(
                    onClick = {
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                        } else {
                            onStartQuiz()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    if (currentStepIndex < steps.size - 1) {
                        Text("Next")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    } else {
                        Text("Ready for Quiz! 🎯")
                    }
                }
            }

            // Step indicator dots
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(steps.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (index == currentStepIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { currentStepIndex = index }
                    )
                    if (index < steps.size - 1) {
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WindIntroScreen(onStartQuiz: () -> Unit) {
    var currentStepIndex by remember { mutableStateOf(0) }

    val steps = listOf(
        "Build a small pinwheel from paper and a pin." to "📌",
        "Attach the pinwheel to a small DC motor (from a toy car)." to "🔧",
        "Connect the motor's wires to a small LED light." to "💡",
        "Take it outside on a windy day or use a fan." to "🌪️",
        "Watch the wind spin the pinwheel, turning the motor, which acts as a generator to light up the LED!" to "⚡"
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("💨 Harvest Wind Energy", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text(
                "Let's learn how to see wind power in action!",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(24.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = (currentStepIndex + 1) / steps.size.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Current step display
            AnimatedContent(
                targetState = currentStepIndex,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) with slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    )
                }
            ) { stepIndex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = steps[stepIndex].second,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Step ${stepIndex + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = steps[stepIndex].first,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous button
                Button(
                    onClick = { currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0) },
                    enabled = currentStepIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Previous")
                }

                Spacer(Modifier.width(12.dp))

                // Next/Quiz button
                Button(
                    onClick = {
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                        } else {
                            onStartQuiz()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    if (currentStepIndex < steps.size - 1) {
                        Text("Next")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    } else {
                        Text("Ready for Quiz! 🎯")
                    }
                }
            }

            // Step indicator dots
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(steps.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (index == currentStepIndex)
                                    MaterialTheme.colorScheme.secondary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { currentStepIndex = index }
                    )
                    if (index < steps.size - 1) {
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun CustomProjectCreator(onStart: (String) -> Unit) {
    var userInput by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("🛠️ Custom Project Creator", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text("Tell me what you want to create, and our AI guide will help you learn how!", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("e.g., 'Build a robot' or 'Create a volcano'") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onStart(userInput.trim()) },
                enabled = userInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Start My Project!") }
        }
    }
}



@Composable
fun ProjectLearningScreen(
    projectName: String,
    onComplete: () -> Unit
) {
    InfoScreen(
        title = "Project: $projectName",
        buttonText = "Complete Project",
        onButtonClick = onComplete
    )
}
