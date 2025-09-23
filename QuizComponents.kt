/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

/**
 * Data class representing a quiz question with multiple choices.
 */
data class QuizQuestion(
    val text: String,
    val correctAnswer: String,
    val options: List<String>
)

/**
 * Composable that displays a quiz with a list of questions.
 *
 * @param userName Name of the user to personalize the quiz.
 * @param quizTitle Title of the quiz.
 * @param questions List of QuizQuestion objects.
 * @param resetTrigger Changing this value resets the quiz state.
 * @param onQuizComplete Callback invoked with the final score (0–100).
 */
@Composable
fun QuizScreen(
    userName: String,
    quizTitle: String,
    questions: List<QuizQuestion>,
    resetTrigger: Int = 0,
    onQuizComplete: (Int) -> Unit
) {
    var currentIndex by remember(resetTrigger) { mutableStateOf(0) }
    var selectedOption by remember(resetTrigger) { mutableStateOf("") }
    val results = remember(resetTrigger) { mutableStateListOf<Boolean>() }

    if (currentIndex < questions.size) {
        val question = questions[currentIndex]
        Card(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "$quizTitle: ${currentIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(12.dp))
                Text(question.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))

                // Show all options as buttons with highlight for the selected
                question.options.forEach { option ->
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            selectedOption = option
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedOption == option)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(option)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedOption.isNotBlank(),
                    onClick = {
                        results.add(selectedOption == question.correctAnswer)
                        selectedOption = ""
                        if (currentIndex < questions.size - 1) {
                            currentIndex++
                        } else {
                            val score = (results.count { it } * 100) / questions.size
                            onQuizComplete(score)
                        }
                    }
                ) {
                    Text("Next")
                }
            }
        }
    }
}


/**
 * Composable that shows the quiz results with retry and home options.
 *
 * @param userName Name of the user to personalize feedback.
 * @param score Final score (0–100).
 * @param messages Map of score thresholds to feedback messages.
 * @param onTryAgain Callback to restart the quiz.
 * @param onHome Callback to return to the home screen.
 */
@Composable
fun ResultsScreen(
    userName: String,
    score: Int,
    messages: Map<Int, String>,
    onTryAgain: () -> Unit,
    onHome: () -> Unit
) {
    val message = messages
        .entries
        .sortedByDescending { it.key }
        .firstOrNull { score >= it.key }
        ?.value
        ?: "Keep it up!"

    Card(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$message $userName!", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Your Score: $score%", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onTryAgain, modifier = Modifier.weight(1f)) {
                    Text("Try Again")
                }
                Button(onClick = onHome, modifier = Modifier.weight(1f)) {
                    Text("Home")
                }
            }
        }
    }
}

/**
 * Returns a list of solar energy quiz questions.
 */
fun getSolarQuizQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion(
        text = "What does a DIY solar oven use to trap heat?",
        correctAnswer = "Plastic wrap",
        options = listOf("Plastic wrap", "A fan", "Ice", "A dark cloth")
    ),
    QuizQuestion(
        text = "Which color best absorbs sunlight inside the oven?",
        correctAnswer = "Black",
        options = listOf("Black", "White", "Yellow", "Green")
    ),
    QuizQuestion(
        text = "What part of the solar oven reflects sunlight into the box?",
        correctAnswer = "Aluminum foil flap",
        options = listOf("Aluminum foil flap", "The black paper", "The plastic", "The pizza")
    ),
    QuizQuestion(
        text = "Is solar energy a renewable resource?",
        correctAnswer = "Yes",
        options = listOf("Yes", "No")
    ),
    QuizQuestion(
        text = "When do solar panels work best?",
        correctAnswer = "A sunny afternoon",
        options = listOf("A sunny afternoon", "Night time", "A rainy morning", "Inside a box")
    )
)

/**
 * Returns a list of wind energy quiz questions.
 */
fun getWindQuizQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion(
        text = "What device converts wind energy into electricity in our DIY project?",
        correctAnswer = "A motor",
        options = listOf("A motor", "A battery", "An LED", "Paper")
    ),
    QuizQuestion(
        text = "What part of the pinwheel catches the wind?",
        correctAnswer = "The blades",
        options = listOf("The blades", "The pin", "The stick", "The motor")
    ),
    QuizQuestion(
        text = "Is wind energy a renewable resource?",
        correctAnswer = "Yes",
        options = listOf("Yes", "No")
    ),
    QuizQuestion(
        text = "Does wind power create pollution?",
        correctAnswer = "No",
        options = listOf("Yes", "No")
    ),
    QuizQuestion(
        text = "Where should you place a wind turbine for the best effect?",
        correctAnswer = "In a windy place",
        options = listOf("In a windy place", "In the shade", "Inside a building", "Underwater")
    )
)
