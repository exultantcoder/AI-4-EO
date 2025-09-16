Interactive learning task 



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


package com.google.ai.edge.gallery.customtasks.interactive

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.customtasks.common.CustomTask
import com.google.ai.edge.gallery.customtasks.common.CustomTaskData
import com.google.ai.edge.gallery.data.CategoryInfo
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data class for persistent user storage
 */
@Serializable
data class UserProfile(
    val language: String = "",
    val name: String = "",
    val favoriteTopic: String = "",
    val motivation: String = "",
    val solarScore: Int = 0,
    val windEnergyScore: Int = 0,
    val customProjectScore: Int = 0,
    val loginCount: Int = 0,
    val lastLoginDate: String = ""
)

/**
 * Utility class for managing persistent user data
 */
class UserDataManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("interactive_learning_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    companion object {
        private const val USER_PROFILE_KEY = "user_profile"
        private const val USER_DATA_FILE = "interactive_learning_data.json"
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit().putString(USER_PROFILE_KEY, json.encodeToString(profile)).apply()
        try {
            val file = File(context.filesDir, USER_DATA_FILE)
            file.writeText(json.encodeToString(profile))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadUserProfile(): UserProfile {
        return try {
            val profileJson = prefs.getString(USER_PROFILE_KEY, null)
            if (profileJson != null) {
                json.decodeFromString<UserProfile>(profileJson)
            } else {
                val file = File(context.filesDir, USER_DATA_FILE)
                if (file.exists()) {
                    json.decodeFromString<UserProfile>(file.readText())
                } else {
                    UserProfile()
                }
            }
        } catch (e: Exception) {
            UserProfile()
        }
    }

    fun isUserRegistered(): Boolean {
        val profile = loadUserProfile()
        return profile.name.isNotEmpty() && profile.language.isNotEmpty()
    }

    fun updateLoginInfo(): UserProfile {
        val profile = loadUserProfile()
        val updatedProfile = profile.copy(
            loginCount = profile.loginCount + 1,
            lastLoginDate = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())
        )
        saveUserProfile(updatedProfile)
        return updatedProfile
    }

    fun clearUserData() {
        prefs.edit().clear().apply()
        val file = File(context.filesDir, USER_DATA_FILE)
        if (file.exists()) file.delete()
    }
}

class InteractiveLearningFirstTask @Inject constructor() : CustomTask {
    override val task: Task = Task(
        id = "interactive_learning_first",
        label = "Interactive Learning Hub",
        category = CategoryInfo(id = "learning", label = "Learning"),
        icon = Icons.Outlined.Language,
        description = "Complete personalized learning with Solar, Wind, and Custom projects.",
        docUrl = "",
        sourceCodeUrl = "",
        models = mutableListOf(
            Model(
                name = "Gemma3n-E2B-IT",
                info = "On-device multilingual LLM with text & audio support",
                url = "",
                sizeInBytes = 0L,
                bestForTaskIds = listOf("interactive_learning_first"),
                configs = emptyList()
            )
        )
    )

    override fun initializeModelFn(
        context: Context,
        coroutineScope: CoroutineScope,
        model: Model,
        onDone: (String) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            model.instance = null
            onDone("")
        }
    }

    override fun cleanUpModelFn(
        context: Context,
        coroutineScope: CoroutineScope,
        model: Model,
        onDone: () -> Unit
    ) {
        onDone()
    }

    @Composable
    override fun MainScreen(data: Any) {
        val myData = data as CustomTaskData
        val viewModel: ModelManagerViewModel = myData.modelManagerViewModel
        val context = LocalContext.current

        val userDataManager = remember { UserDataManager(context) }

        var userProfile by remember { mutableStateOf(UserProfile()) }
        var step by remember { mutableStateOf(1) }
        var language by remember { mutableStateOf("") }
        var userName by remember { mutableStateOf("") }
        var favoriteTopic by remember { mutableStateOf("") }
        var motivation by remember { mutableStateOf("") }
        var chosenActivity by remember { mutableStateOf("") }
        var chosenCustomProject by remember { mutableStateOf("") }

        // --- Integration from Code 2: Multilingual Support ---
        val userSelectedLanguageTranslation = remember { mutableStateMapOf<String, String>() }

        // --- Helper function to save progress ---
        val saveUserProgress = { profile: UserProfile ->
            userDataManager.saveUserProfile(profile)
            userProfile = profile
        }

        LaunchedEffect(language) {
            // In a real app, you would load these from string resources (strings.xml)
            // This is a simplified demo for translation.
            if (language.equals("Español", ignoreCase = true)) {
                userSelectedLanguageTranslation["Hands-On"] = "Práctico"
                userSelectedLanguageTranslation["Harvest Solar Energy"] = "Cosechar Energía Solar"
                userSelectedLanguageTranslation["Harvest Wind Energy"] = "Cosechar Energía Eólica"
                userSelectedLanguageTranslation["Custom Project"] = "Proyecto Personalizado"
                userSelectedLanguageTranslation["Start Learning"] = "Empezar a Aprender"
                // ... add other translations
            } else {
                userSelectedLanguageTranslation.clear() // Default to English
            }
        }

        LaunchedEffect(Unit) {
            userProfile = userDataManager.updateLoginInfo()
            if (userDataManager.isUserRegistered()) {
                language = userProfile.language
                userName = userProfile.name
                favoriteTopic = userProfile.favoriteTopic
                motivation = userProfile.motivation
                step = 0 // Welcome back screen
            } else {
                step = 1 // New user onboarding
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            when (step) {
                // --- HOME SCREEN ---
                0 -> Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Welcome back, ${userProfile.name}! 👋", style = MaterialTheme.typography.h5)
                        Spacer(Modifier.height(8.dp))
                        Text("Login Count: ${userProfile.loginCount}", style = MaterialTheme.typography.body2)
                        Text("Language: ${userProfile.language}", style = MaterialTheme.typography.body1)
                        Text("Favorite Topic: ${userProfile.favoriteTopic}", style = MaterialTheme.typography.body1)

                        if (userProfile.solarScore > 0) Text("🌞 Solar Energy Score: ${userProfile.solarScore}%")
                        if (userProfile.windEnergyScore > 0) Text("💨 Wind Energy Score: ${userProfile.windEnergyScore}%")
                        if (userProfile.customProjectScore > 0) Text("🛠️ Custom Project Score: ${userProfile.customProjectScore}%")
                        
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { step = 7 }, modifier = Modifier.weight(1f)) { Text("Continue Learning") }
                            Button(onClick = { userDataManager.clearUserData(); step = 1 }, modifier = Modifier.weight(1f)) { Text("Reset Profile") }
                        }
                    }
                }

                // --- ONBOARDING STEPS 1-6 ---
                1 -> OnboardingScreen("Which language would you like to use?", language, { language = it }, "e.g. English, Español", { if (language.isNotBlank()) step = 2 })
                2 -> OnboardingScreen("Hello! What's your name?", userName, { userName = it }, "Enter your name", { if (userName.isNotBlank()) step = 3 })
                3 -> OnboardingScreen("Great, $userName. What topic excites you most?", favoriteTopic, { favoriteTopic = it }, "e.g. Renewable Energy", { if (favoriteTopic.isNotBlank()) step = 4 })
                4 -> OnboardingScreen("Why do you want to learn about $favoriteTopic?", motivation, { motivation = it }, "Your motivation", { if (motivation.isNotBlank()) step = 5 })

                5 -> Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Please confirm your details:", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(8.dp))
                        Text("Language: $language")
                        Text("Name: $userName")
                        Text("Favorite Topic: $favoriteTopic")
                        Text("Motivation: $motivation")
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = {
                                    val newProfile = userProfile.copy(
                                        language = language.trim(),
                                        name = userName.trim(),
                                        favoriteTopic = favoriteTopic.trim(),
                                        motivation = motivation.trim()
                                    )
                                    saveUserProgress(newProfile)
                                    step = 6
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Yes, correct") }
                            Button(onClick = { step = 1 }, modifier = Modifier.weight(1f)) { Text("Edit details") }
                        }
                    }
                }
                6 -> InfoScreen("Details saved! Ready to learn!", "Start Learning") { step = 7 }

                // --- MODULE & ACTIVITY SELECTION ---
                7 -> Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Choose your learning level:", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(16.dp))
                        val handsOnTranslated = userSelectedLanguageTranslation["Hands-On"] ?: "Hands-On"
                        Button(onClick = { step = 8 }, modifier = Modifier.fillMaxWidth()) {
                            Text("Hands-On / $handsOnTranslated ✓")
                        }
                    }
                }
                8 -> Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Choose your activity:", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(16.dp))
                        val activities = listOf("Harvest Solar Energy", "Harvest Wind Energy", "Custom Project")
                        activities.forEach { activity ->
                            val translated = userSelectedLanguageTranslation[activity] ?: activity
                            Button(
                                onClick = {
                                    chosenActivity = activity
                                    step = when (activity) {
                                        "Harvest Solar Energy" -> 100
                                        "Harvest Wind Energy" -> 200
                                        "Custom Project" -> 300
                                        else -> 0
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) { Text("$activity / $translated") }
                        }
                    }
                }

                // --- SOLAR ENERGY MODULE (100-102) ---
                100 -> SolarIntroScreen { step = 101 }
                101 -> QuizScreen(
                    userName = userName,
                    quizTitle = "🌞 Solar Quiz",
                    questions = solarQuizQuestions,
                    onQuizComplete = { score ->
                        val updatedProfile = userProfile.copy(solarScore = score)
                        saveUserProgress(updatedProfile)
                        step = 102
                    }
                )
                102 -> ResultsScreen(
                    userName = userName,
                    score = userProfile.solarScore,
                    messages = mapOf(
                        100 to "🌟 Perfect! You're a Solar Genius!",
                        80 to "🎉 Excellent! You're brilliant with solar power!",
                        70 to "👍 Great job! Keep learning about solar energy!",
                        0 to "💪 Keep trying! Review and try again!"
                    ),
                    onTryAgain = { step = 101 },
                    onHome = { step = 0 }
                )

                // --- WIND ENERGY MODULE (200-202) ---
                200 -> WindIntroScreen { step = 201 }
                201 -> QuizScreen(
                    userName = userName,
                    quizTitle = "💨 Wind Quiz",
                    questions = windQuizQuestions,
                    onQuizComplete = { score ->
                        val updatedProfile = userProfile.copy(windEnergyScore = score)
                        saveUserProgress(updatedProfile)
                        step = 202
                    }
                )
                202 -> ResultsScreen(
                    userName = userName,
                    score = userProfile.windEnergyScore,
                    messages = mapOf(
                        100 to "🌟 Amazing! You're a Wind Expert!",
                        80 to "🎉 Fantastic! You understand wind power!",
                        70 to "👍 Good work! Keep mastering wind energy!",
                        0 to "💪 Don't give up! Every expert was a beginner."
                    ),
                    onTryAgain = { step = 201 },
                    onHome = { step = 0 }
                )
                
                // --- CUSTOM PROJECT MODULE (300-302) ---
                 300 -> CustomProjectCreator(
                    onStart = { projectName ->
                        chosenCustomProject = projectName
                        // Here you would prepare the AI prompt
                        // val systemPrompt = """You are an educational AI assistant..."""
                        step = 301
                    }
                )
                301 -> InfoScreen("Project: $chosenCustomProject", "Start Guided Learning") { 
                    // This is where the interactive chat with the AI would happen.
                    // For this demo, we'll simulate completion and go to results.
                    val simulatedScore = (70..100).random() // Simulate a good score
                    val updatedProfile = userProfile.copy(customProjectScore = simulatedScore)
                    saveUserProgress(updatedProfile)
                    step = 302
                }
                302 -> ResultsScreen(
                    userName = userName,
                    score = userProfile.customProjectScore,
                    messages = mapOf(
                        90 to "🏆 Outstanding! You're a natural creator!",
                        70 to "🎉 Great work! Impressive project skills!",
                        50 to "👍 Good effort! Keep practicing!",
                        0 to "💪 Keep learning! Every project is a step forward."
                    ),
                    onTryAgain = { step = 300 },
                    onHome = { step = 0 }
                )
            }
        }
    }
}

// --- Reusable UI Components ---

@Composable
fun OnboardingScreen(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onNext: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNext, enabled = value.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text("Next")
            }
        }
    }
}

@Composable
fun InfoScreen(title: String, buttonText: String, onButtonClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onButtonClick, modifier = Modifier.fillMaxWidth()) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun SolarIntroScreen(onStartQuiz: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("🌞 Harvest Solar Energy", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(16.dp))
            Text(
                "Let's learn how to build a simple DIY solar oven!",
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(Modifier.height(8.dp))
            val steps = listOf(
                "1. Find a pizza box, black paper, clear plastic wrap, and aluminum foil.",
                "2. Cut a flap into the box’s lid and cover the inside of the flap with foil.",
                "3. Line the box bottom with the black paper to absorb heat.",
                "4. Seal the opening with clear plastic wrap to trap the sun's heat.",
                "5. Place a marshmallow inside and point the foil flap toward the sun.",
                "6. Watch your simple oven use solar energy to cook your treat!"
            )
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(steps) { step ->
                    Text(step, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
                Text("Ready for the Quiz!")
            }
        }
    }
}

@Composable
fun WindIntroScreen(onStartQuiz: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("💨 Harvest Wind Energy", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(16.dp))
            Text(
                "Let's learn how to see wind power in action!",
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "1. Build a small pinwheel from paper and a pin.\n" +
                "2. Attach the pinwheel to a small DC motor (from a toy car).\n" +
                "3. Connect the motor's wires to a small LED light.\n" +
                "4. Take it outside on a windy day or use a fan.\n" +
                "5. Watch the wind spin the pinwheel, turning the motor, which acts as a generator to light up the LED!",
                style = MaterialTheme.typography.body1
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
                Text("Ready for the Quiz!")
            }
        }
    }
}

@Composable
fun CustomProjectCreator(onStart: (String) -> Unit) {
    var userInput by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("🛠️ Custom Project Creator", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(12.dp))
            Text("Tell me what you want to create, and our AI guide will help you learn how!", style = MaterialTheme.typography.body1)
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

// --- Reusable Quiz and Results Logic ---

data class QuizQuestion(val text: String, val correctAnswer: String, val options: List<String>)


@Composable
fun QuizScreen(
    userName: String,
    quizTitle: String,
    questions: List<QuizQuestion>,
    resetTrigger: Int = 0, 
    onQuizComplete: (Int) -> Unit
) {
    var currentQuestionIndex by remember(resetTrigger) { mutableStateOf(0) }
    var selectedAnswer by remember(resetTrigger) { mutableStateOf("") }
    val correctAnswers = remember(resetTrigger) { mutableStateListOf<Boolean>() }

    if (currentQuestionIndex < questions.size) {
        val question = questions[currentQuestionIndex]
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("$quizTitle: ${currentQuestionIndex + 1} / ${questions.size}", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(12.dp))
                Text(question.text, style = MaterialTheme.typography.body1)
                Spacer(Modifier.height(16.dp))
                
                question.options.forEach { option ->
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        onClick = { selectedAnswer = option },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedAnswer == option) MaterialTheme.colors.primary else MaterialTheme.colors.surface
                        )
                    ) { Text(option) }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer.isNotBlank(),
                    onClick = {
                        correctAnswers.add(selectedAnswer == question.correctAnswer)
                        selectedAnswer = ""
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                        } else {
                            val score = (correctAnswers.count { it } * 100) / questions.size
                            onQuizComplete(score)
                        }
                    }
                ) { Text("Next Question") }
            }
        }
    }
}

@Composable
fun ResultsScreen(
    userName: String,
    score: Int,
    messages: Map<Int, String>,
    onTryAgain: () -> Unit,
    onHome: () -> Unit
) {
    val message = messages.entries.firstOrNull { score >= it.key }?.value ?: "Keep it up!"
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$message $userName!", style = MaterialTheme.typography.h5)
            Text("Your Score: $score%", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTryAgain, modifier = Modifier.weight(1f)) { Text("Try Again") }
                Button(onClick = onHome, modifier = Modifier.weight(1f)) { Text("Home") }
            }
        }
    }
}

// --- Quiz Data ---

val solarQuizQuestions = listOf(
    QuizQuestion("What does a DIY solar oven use to trap heat?", "Plastic wrap", listOf("Plastic wrap", "A fan", "Ice", "A dark cloth")),
    QuizQuestion("Which color best absorbs sunlight inside the oven?", "Black", listOf("Black", "White", "Yellow", "Green")),
    QuizQuestion("What part of the solar oven reflects sunlight into the box?", "Aluminum foil flap", listOf("Aluminum foil flap", "The black paper", "The plastic", "The pizza")),
    QuizQuestion("Is solar energy a renewable resource?", "Yes", listOf("Yes", "No")),
    QuizQuestion("When do solar panels work best?", "A sunny afternoon", listOf("A sunny afternoon", "Night time", "A rainy morning", "Inside a box"))
)

val windQuizQuestions = listOf(
    QuizQuestion("What device converts wind energy into electricity in our DIY project?", "A motor", listOf("A motor", "A battery", "An LED", "Paper")),
    QuizQuestion("What part of the pinwheel catches the wind?", "The blades", listOf("The blades", "The pin", "The stick", "The motor")),
    QuizQuestion("Is wind energy a renewable resource?", "Yes", listOf("Yes", "No")),
    QuizQuestion("Does wind power create pollution?", "No", listOf("Yes", "No")),
    QuizQuestion("Where should you place a wind turbine for the best effect?", "In a windy place", listOf("In a windy place", "In the shade", "Inside a building", "Underwater"))
)
