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

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel

@Composable
fun InteractiveLearningScreen(
    modelManagerViewModel: ModelManagerViewModel,
    viewModel: InteractiveLearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userDataManager = remember { UserDataManager(context) }

    // Save progress helper
    val saveProgress: () -> Unit = { viewModel.saveUserProfile() }

    Column(modifier = Modifier.padding(16.dp)) {
        when (uiState.step) {
            0 -> HomeScreen(
                userProfile = uiState.userProfile,
                onContinueLearning = { viewModel.updateStep(7) },
                onResetProfile = {
                    viewModel.resetProfile()
                }
            )
            1 -> OnboardingScreen(
                title = "Which language would you like to use?",
                value = uiState.languageInput,
                placeholder = "e.g. English, Español",
                onValueChange = viewModel::updateLanguage,
                onNext = {
                    if (uiState.languageInput.isNotBlank()) viewModel.updateStep(2)
                }
            )
            2 -> OnboardingScreen(
                title = "Hello! What's your name?",
                value = uiState.nameInput,
                placeholder = "Enter your name",
                onValueChange = viewModel::updateName,
                onNext = {
                    if (uiState.nameInput.isNotBlank()) viewModel.updateStep(3)
                }
            )
            3 -> OnboardingScreen(
                title = "Great, ${uiState.nameInput}. What topic excites you most?",
                value = uiState.favoriteTopicInput,
                placeholder = "e.g. Renewable Energy",
                onValueChange = viewModel::updateFavoriteTopic,
                onNext = {
                    if (uiState.favoriteTopicInput.isNotBlank()) viewModel.updateStep(4)
                }
            )
            4 -> OnboardingScreen(
                title = "Why do you want to learn about ${uiState.favoriteTopicInput}?",
                value = uiState.motivationInput,
                placeholder = "Your motivation",
                onValueChange = viewModel::updateMotivation,
                onNext = {
                    if (uiState.motivationInput.isNotBlank()) viewModel.updateStep(5)
                }
            )
            5 -> ConfirmationScreen(
                userProfile = uiState.userProfile.copy(
                    language = uiState.languageInput,
                    name = uiState.nameInput,
                    favoriteTopic = uiState.favoriteTopicInput,
                    motivation = uiState.motivationInput
                ),
                onConfirm = {
                    saveProgress()
                    viewModel.updateStep(6)
                },
                onEdit = { viewModel.updateStep(1) }
            )
            6 -> InfoScreen(
                title = "Details saved! Ready to learn!",
                buttonText = "Start Learning",
                onButtonClick = {
                    viewModel.updateStep(7)
                }
            )
            7 -> LearningLevelScreen(
                onSelectLevel = {
                    viewModel.updateStep(8)
                }
            )
            8 -> ActivitySelectionScreen(
                userProfile = uiState.userProfile,
                onSelectActivity = {
                    viewModel.updateActivity(it)
                    viewModel.updateStep(
                        when (it) {
                            "Harvest Solar Energy" -> 100
                            "Harvest Wind Energy" -> 200
                            "Custom Project" -> 300
                            "TalkToMe" -> 400
                            else -> 0
                        }
                    )
                }
            )

            100 -> SolarIntroScreen { viewModel.updateStep(101) }
            101 -> QuizScreen(
                userName = uiState.userProfile.name,
                quizTitle = "🌞 Solar Quiz",
                questions = getSolarQuizQuestions(),
                resetTrigger = 0,
                onQuizComplete = {
                    viewModel.setSolarScore(it)
                    viewModel.updateStep(102)
                }
            )
            102 -> ResultsScreen(
                userName = uiState.userProfile.name,
                score = uiState.userProfile.solarScore,
                messages = mapOf(
                    100 to "🌟 Perfect! You're a Solar Genius!",
                    80 to "🎉 Excellent! You're brilliant with solar power!",
                    70 to "👍 Great job! Keep learning about solar energy!",
                    0 to "💪 Keep trying! Review and try again!"
                ),
                onTryAgain = { viewModel.updateStep(101) },
                onHome = { viewModel.updateStep(0) }
            )
            200 -> WindIntroScreen { viewModel.updateStep(201) }
            201 -> QuizScreen(
                userName = uiState.userProfile.name,
                quizTitle = "💨 Wind Quiz",
                questions = getWindQuizQuestions(),
                resetTrigger = 0,
                onQuizComplete = {
                    viewModel.setWindScore(it)
                    viewModel.updateStep(202)
                }
            )
            202 -> ResultsScreen(
                userName = uiState.userProfile.name,
                score = uiState.userProfile.windEnergyScore,
                messages = mapOf(
                    100 to "🌟 Amazing! You're a Wind Expert!",
                    80 to "🎉 Fantastic! You understand wind power!",
                    70 to "👍 Good work! Keep mastering wind energy!",
                    0 to "💪 Don't give up! Every expert was a beginner."
                ),
                onTryAgain = { viewModel.updateStep(201) },
                onHome = { viewModel.updateStep(0) }
            )
            300 -> CustomProjectCreator {
                viewModel.updateCustomProject(it)
                viewModel.updateStep(301)
            }
            301 -> ProjectLearningScreen(
                projectName = uiState.customProjectName,
                onComplete = {
                    viewModel.setCustomProjectScore((70..100).random())
                    viewModel.updateStep(302)
                }
            )
            302 -> ResultsScreen(
                userName = uiState.userProfile.name,
                score = uiState.userProfile.customProjectScore,
                messages = mapOf(
                    90 to "🏆 Outstanding! You're a natural creator!",
                    70 to "🎉 Great work! Impressive project skills!",
                    50 to "👍 Good effort! Keep practicing!",
                    0 to "💪 Keep learning! Every project is a step forward."
                ),
                onTryAgain = { viewModel.updateStep(300) },
                onHome = { viewModel.updateStep(0) },
            )
            400 ->  {

            TalkToMeDialog(
                modelManagerViewModel = modelManagerViewModel,
                onDismissRequest = {
                    // Return to activity picker or other step as needed
                    viewModel.updateStep(8)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
            500 -> {
                // Show a back button to go to activity selection
                Column(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = { viewModel.updateStep(8) },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("← Back to Activities")
                    }
                    SolarAngleOptimizerGame()
                }
            }
            600 -> {
                // Show a back button to go to activity selection
                Column(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = { viewModel.updateStep(8) },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("← Back to Activities")
                    }
                    WindAlignmentGame()
                }
            }
        }
    }
}
// Helper function for language translations
private fun getLanguageTranslations(language: String): Map<String, String> {
    return if (language.equals("Español", ignoreCase = true)) {
        mapOf(
            "Hands-On" to "Práctico",
            "Harvest Solar Energy" to "Cosechar Energía Solar",
            "Harvest Wind Energy" to "Cosechar Energía Eólica",
            "Custom Project" to "Proyecto Personalizado",
            "Start Learning" to "Empezar a Aprender"
        )
    } else {
        emptyMap()
    }
}

