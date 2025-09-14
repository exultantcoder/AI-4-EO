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

package com.google.ai.edge.gallery.customtasks.interactive

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.customtasks.common.CustomTask
import com.google.ai.edge.gallery.customtasks.common.CustomTaskData
import com.google.ai.edge.gallery.data.CategoryInfo
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.SharedPreferences
import androidx.compose.ui.Alignment
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import androidx.compose.runtime.SideEffect
import androidx.compose.material.ButtonDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color

**
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
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    companion object {
        private const val USER_PROFILE_KEY = "user_profile"
        private const val USER_DATA_FILE = "interactive_learning_data.json"
    }
    
    /**
     * Save user profile to both SharedPreferences and file storage
     */
    fun saveUserProfile(profile: UserProfile) {
        // Save to SharedPreferences for quick access
        prefs.edit()
            .putString(USER_PROFILE_KEY, json.encodeToString(profile))
            .apply()
        
        // Save to internal file storage for backup
        try {
            val file = File(context.filesDir, USER_DATA_FILE)
            file.writeText(json.encodeToString(profile))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Load user profile with fallback mechanisms
     */
    fun loadUserProfile(): UserProfile {
        return try {
            // Try to load from SharedPreferences first
            val profileJson = prefs.getString(USER_PROFILE_KEY, null)
            if (profileJson != null) {
                json.decodeFromString<UserProfile>(profileJson)
            } else {
                // Fallback to file storage
                val file = File(context.filesDir, USER_DATA_FILE)
                if (file.exists()) {
                    val fileContent = file.readText()
                    json.decodeFromString<UserProfile>(fileContent)
                } else {
                    UserProfile() // Return default profile
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UserProfile() // Return default profile on error
        }
    }
    
    /**
     * Check if user has completed onboarding
     */
    fun isUserRegistered(): Boolean {
        val profile = loadUserProfile()
        return profile.name.isNotEmpty() && profile.language.isNotEmpty()
    }
    
    /**
     * Update login count and date
     */
    fun updateLoginInfo(): UserProfile {
        val profile = loadUserProfile()
        val updatedProfile = profile.copy(
            loginCount = profile.loginCount + 1,
            lastLoginDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
        )
        saveUserProfile(updatedProfile)
        return updatedProfile
    }
    
    /**
     * Clear all user data
     */
    fun clearUserData() {
        prefs.edit().clear().apply()
        val file = File(context.filesDir, USER_DATA_FILE)
        if (file.exists()) {
            file.delete()
        }
    }
}

/**
 * InteractiveLearningFirstTask demonstrates:
 * - Selection of conversation language (text + audio)
 * - Initialization of Gemma3n-E2B-IT model for text + speech
 * - Personalized question flow to get user profile and motivation
 * - Saving user responses and progress to a local file
 */
class InteractiveLearningFirstTask @Inject constructor() : CustomTask {

  override val task: Task = Task(
    id = "interactive_learning_first",
    label = "Interactive Learning (Step 1)",
    category = CategoryInfo(id = "learning", label = "Learning"),
    icon = Icons.Outlined.Language,
    description = "Choose your language and tell us about yourself to personalize your learning journey.",
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
      // Preload Gemma3n-E2B-IT for text and audio
      // (Assuming ModelManager handles download & setup)
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
    // Nothing to clean up for this step
    onDone()
  }

  @Composable
  override fun MainScreen(data: Any) {
    val myData = data as CustomTaskData
    val viewModel: ModelManagerViewModel = myData.modelManagerViewModel

    var step by remember { mutableStateOf(1) }
    var language by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var favoriteTopic by remember { mutableStateOf("") }
    var motivation by remember { mutableStateOf("") }
    val responses = remember { mutableMapOf<String, String>() }

    Column(modifier = Modifier.padding(16.dp)) {
      when (step) {

        // Step 1: Select language
        1 -> Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Which language would you like to use?", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
              value = language,
              onValueChange = { language = it },
              label = { Text("e.g. English, EspaÃ±ol, Italiano") },
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
              onClick = {
                if (language.isNotBlank()) {
                  responses["language"] = language.trim()
                  step = 2
                }
              },
              enabled = language.isNotBlank(),
              modifier = Modifier.fillMaxWidth()
            ) { Text("Next") }
          }
        }

        // Step 2: Ask name
        2 -> Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Hello! What's your name?", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
              value = userName,
              onValueChange = { userName = it },
              label = { Text("Enter your name") },
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
              onClick = {
                if (userName.isNotBlank()) {
                  responses["name"] = userName.trim()
                  step = 3
                }
              },
              enabled = userName.isNotBlank(),
              modifier = Modifier.fillMaxWidth()
            ) { Text("Next") }
          }
        }

        // Step 3: Favorite Topic
        3 -> Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Great, $userName. What topic excites you most?", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
              value = favoriteTopic,
              onValueChange = { favoriteTopic = it },
              label = { Text("e.g. Renewable Energy") },
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
              onClick = {
                if (favoriteTopic.isNotBlank()) {
                  responses["favoriteTopic"] = favoriteTopic.trim()
                  step = 4
                }
              },
              enabled = favoriteTopic.isNotBlank(),
              modifier = Modifier.fillMaxWidth()
            ) { Text("Next") }
          }
        }
        // Step 5: Show summary for confirmation BEFORE saving
5 -> Card(modifier = Modifier.fillMaxWidth()) {
  Column(modifier = Modifier.padding(16.dp)) {
    Text("Please confirm your details:", style = MaterialTheme.typography.h6)
    Spacer(Modifier.height(8.dp))
    Text("Language: ${language}", style = MaterialTheme.typography.body1)
    Text("Name: ${userName}", style = MaterialTheme.typography.body1)
    Text("Favorite Topic: ${favoriteTopic}", style = MaterialTheme.typography.body1)
    Text("Motivation: ${motivation}", style = MaterialTheme.typography.body1)
    Spacer(Modifier.height(16.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Button(
        onClick = {
          // Save responses & progress if confirmed
          responses["language"] = language.trim()
          responses["name"] = userName.trim()
          responses["favoriteTopic"] = favoriteTopic.trim()
          responses["motivation"] = motivation.trim()
          val file = context.getFileStreamPath("interactive_first_responses.json")
          file.writeText(gson.toJson(responses))
          step = 6 // Continue to next logical step/module
        },
        modifier = Modifier.weight(1f)
      ) { Text("Yes, details are correct") }

      Button(
        onClick = {
          // Go back to step 1 for re-entry
          step = 1
          // Optionally clear fields
          language = ""
          userName = ""
          favoriteTopic = ""
          motivation = ""
          responses.clear()
        },
        modifier = Modifier.weight(1f)
      ) { Text("No, go back and edit") }
    }
  }
}

// Step 6: Final confirmation and continue
6 -> Card(modifier = Modifier.fillMaxWidth()) {
  Column(modifier = Modifier.padding(16.dp)) {
    Text("Details saved! You're ready to start learning!", style = MaterialTheme.typography.h5)
    Spacer(Modifier.height(16.dp))
    Button(
      onClick = { /* navigate to next interactive module */ },
      modifier = Modifier.fillMaxWidth()
    ) { Text("Continue Learning") }
  }
}

        // Step 4: Motivation
        4 -> Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Why do you want to learn about $favoriteTopic?", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
              value = motivation,
              onValueChange = { motivation = it },
              label = { Text("Your motivation") },
              modifier = Modifier.fillMaxWidth(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(16.dp))
            Button(
              onClick = {
                if (motivation.isNotBlank()) {
                  responses["motivation"] = motivation.trim()
                  // Save responses & progress
                  val file = context.getFileStreamPath("interactive_first_responses.json")
                  file.writeText(gson.toJson(responses))
                  step = 5
                }
              },
              enabled = motivation.isNotBlank(),
              modifier = Modifier.fillMaxWidth()
            ) { Text("Finish") }
          }
        }

        // Step 5: Confirmation
        5 -> Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Awesome, $userName! ðŸŒŸ", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(8.dp))
            Text(
              "We will converse in $language and discuss $favoriteTopic. " +
              "Your motivation: â€œ$motivationâ€ will guide our sessions.",
              style = MaterialTheme.typography.body1
            )
            Spacer(Modifier.height(16.dp))
            Button(
              onClick = { /* navigate to next interactive module */ },
              modifier = Modifier.fillMaxWidth()
            ) { Text("Continue Learning") }
          }
        }
      }
    }
  }
}
// When transitioning to the main learning chat after onboarding:
val userSelectedLanguage = language.trim()

// Prepare a system prompt to ensure Gemma 3n replies in the correct language
val systemPrompt = """
You are an educational AI assistant.
From now on, please answer and ask all questions in $userSelectedLanguage.
If the user replies in another language, gently remind them to use $userSelectedLanguage for this session.
""".trimIndent()

// Send the system prompt to the model as the first system message
val initialMessage = ChatMessageText(
    content = systemPrompt,
    sender = ChatMessage.Sender.MODEL // Or SYSTEM, depending on message classes in your repo
)

// Add initialMessage to your chat sequence, then start subsequent question/answer logic
viewModel.addMessage(modelId = model.name, message = initialMessage)

// Now, when you call viewModel.generateLearningResponse for any new input, prefix the prompt:
fun generateLearningResponse(model: Model, input: String) {
    val fullPrompt = "$systemPrompt\nUser: $input"
    // Use fullPrompt for LLM inference
    // ... Gemma 3n API call here ...
}
// Step 7: Choose the level of understanding (dual-language buttons)
7 -> Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Choose your level of understanding:",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))

        // List of all levels (buttons shown in both languages)
        val levels = listOf(
            Pair("Beginner", userSelectedLanguageTranslation["Beginner"] ?: "Beginner"),
            Pair("Intermediate", userSelectedLanguageTranslation["Intermediate"] ?: "Intermediate"),
            Pair("Advanced", userSelectedLanguageTranslation["Advanced"] ?: "Advanced"),
            Pair("Hands-On", userSelectedLanguageTranslation["Hands-On"] ?: "Hands-On")
        )

        // Only 'Hands-On' is enabled
        for ((levelEn, levelUserLang) in levels) {
            Button(
                onClick = {
                    if (levelEn == "Hands-On") step = 8
                },
                enabled = (levelEn == "Hands-On"),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("$levelEn / $levelUserLang")
            }
        }
    }
}

///////////////////////////////////////////
// Step 8: Hands-On Project Choices
8 -> Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            userSelectedLanguageTranslation["ChooseActivity"] ?: "Choose an activity:",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))

        val activities = listOf(
            Pair("Harvest Solar Energy", userSelectedLanguageTranslation["HarvestSolar"] ?: "Harvest Solar Energy"),
            Pair("Harvest Wind Energy", userSelectedLanguageTranslation["HarvestWind"] ?: "Harvest Wind Energy"),
            Pair("Tell me what you want to create", userSelectedLanguageTranslation["TellMeCustomProject"] ?: "Tell me what you want to create")
        )

        for ((activityEn, activityUserLang) in activities) {
            Button(
                onClick = {
                    chosenActivity = activityEn
                    step = 9 // move to module creation/learning curve step
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("$activityEn / $activityUserLang")
            }
        }
    }
}

///////////////////////////////////////////
// Step 9: Start Learning Module (simplified)
9 -> Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Let's get started: $chosenActivity",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))
        // Example - in real code, you integrate Gemma 3n API, images, audio, etc.
        Text(
            "Learning is fun! We will guide you step by step, with images and audio in your chosen language ($userSelectedLanguage).",
            style = MaterialTheme.typography.bodyLarge
        )
        // AUTO: Send initial prompt to Gemma 3n with language instruction and chosen activity, display answer (with images/audio)
        // ...
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { /* Continue learning, show image/audio, ask first interactive question, etc. */ },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continue / ${userSelectedLanguageTranslation["Continue"] ?: "Continue"}") }
    }
}
@Composable
fun SolarEnergyLearningModule(
    userName: String,
    userLanguage: String, // e.g., "English", "EspaÃ±ol"
    translate: suspend (String, String) -> String, // translation lambda, AI or MLKit
    tts: suspend (String, String) -> Unit, // text-to-speech lambda: (text, lang)
    saveProgress: (Int) -> Unit // store quiz score
) {
    var step by remember { mutableStateOf(1) }
    var quizStep by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var userAnswer by remember { mutableStateOf("") }

    // Questions grouped by difficulty
    val quizQuestions = remember {
        listOf(
            // 5 easy
            Triple("What does a solar panel capture?", "Sunlight", listOf("Sunlight", "Rain", "Wind", "Soil")),
            Triple("Which color best absorbs sunlight?", "Black", listOf("Black", "White", "Yellow", "Green")),
            Triple("What device stores solar energy?", "Battery", listOf("Battery", "Lamp", "Fan", "Wire")),
            Triple("Which star is the source of solar energy?", "The Sun", listOf("The Sun", "Moon", "Venus", "Mars")),
            Triple("Can you cook with solar energy?", "Yes", listOf("Yes", "No")),
            // 3 moderate
            Triple("Which material is popular for DIY solar cookers?", "Aluminum foil", listOf("Aluminum foil", "Iron sheet", "Glass shards", "Cotton")),
            Triple("Solar cells convert sunlight into what kind of energy?", "Electricity", listOf("Electricity", "Sound", "Magnetism", "Heat only")),
            Triple("What is the clear covering on a solar oven called?", "Plastic wrap", listOf("Plastic wrap", "Steel sheet", "Wool", "Bricks")),
            // 2 hard
            Triple("Why is black paper used inside a solar oven?", "It absorbs more heat", listOf("It absorbs more heat", "Looks nice", "Reflects light", "It's cheap")),
            Triple("Which factor increases the efficiency of a solar cooker?", "Pointing at sun", listOf("Pointing at sun", "Putting it in shade", "Cover with blanket", "Painting white"))
        )
    }

    // Feedback messages for each score bracket (to be localized)
    val feedbackByScore = listOf(
        100 to "Excellent ðŸŒž! You are a Solar Pro, $userName!",
        80 to "Great job, $userName! You know a lot about the sun.",
        50 to "Good effort, $userName! Keep practicing and try again.",
        0 to "Don't worry, $userName! Try again and you'll shine!"
    )

    // Main step logic
    when (step) {
        1 -> { // Brief explanation
            LaunchedEffect(Unit) {
                val intro = "Solar energy is energy we get from sunlight. We can use solar energy to make electricity or heat things, even cook food!"
                val localized = translate(intro, userLanguage)
                tts(localized, userLanguage)
            }
            Text("...", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { step++ }) { Text("Continue") }
        }
        2 -> { // DIY hands-on steps!
            val steps = listOf(
                "1. Find a pizza box, black paper, clear plastic wrap, and aluminum foil.",
                "2. Cut a flap into the pizza boxâ€™s lidâ€”cover inside with foil.",
                "3. Line the box bottom with black paper. This absorbs sunlight.",
                "4. Seal the opening with clear plastic to keep heat in.",
                "5. Place a marshmallow or chocolate inside and close. Point the flap toward the sun.",
                "6. Watch as sunlight heats up your treat. This simple oven uses solar energy to cook!"
            )
            LazyColumn {
                items(steps) { raw ->
                    LaunchedEffect(raw) {
                        val localized = translate(raw, userLanguage)
                        tts(localized, userLanguage)
                    }
                    Card(
                        backgroundColor = Color(0xFFFDE68A),
                        modifier = Modifier.padding(8.dp)
                    ) { Text(raw, modifier = Modifier.padding(12.dp)) }
                }
            }
            Button(onClick = { step++ }) { Text("Start Quiz") }
        }
        3 -> { // Quiz
            if (quizStep < quizQuestions.size) {
                val (q, correct, opts) = quizQuestions[quizStep]
                Text("${quizStep + 1}. $q", style = MaterialTheme.typography.titleMedium)
                opts.forEach { opt ->
                    Button(
                        modifier = Modifier.padding(6.dp),
                        enabled = !answered,
                        onClick = {
                            answered = true
                            userAnswer = opt
                            if (opt == correct) correctCount++
                        }
                    ) { Text(opt) }
                }
                if (answered) {
                    Text(if (userAnswer == correct) "âœ”ï¸ Correct!" else "âŒ " + "Correct: $correct")
                    Button(onClick = {
                        answered = false
                        userAnswer = ""
                        quizStep++
                    }) { Text("Next") }
                }
            } else {
                step = 4
            }
        }
        4 -> { // Score and feedback
            val percent = (correctCount * 100) / quizQuestions.size
            val feedback = feedbackByScore.firstOrNull { percent >= it.first }?.second ?: ""
            val localized = runBlocking { translate("Congratulations! $userName, Score: $percent%. $feedback", userLanguage) }
            // Save to file in background (hide from the user)
            LaunchedEffect(Unit) { saveProgress(percent) }
            Column {
                Text(localized, style = MaterialTheme.typography.headlineSmall)
                Button(onClick = { /* goto main menu, or redo */ }) { Text("Restart / Learn Again") }
            }
        }
    }
}
// Assume userSelectedLanguage, userSelectedLanguageTranslation, and userName variables from previous steps

var step by remember { mutableStateOf(10) }
var solarQuizAnswers by remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }
var currentQuestion by remember { mutableStateOf(0) }
val solarQuizQuestions = listOf(
    // Easy (5)
    Pair("What is solar energy?", userSelectedLanguageTranslation["Q1"] ?: "Â¿QuÃ© es la energÃ­a solar?"),
    Pair("What object converts sunlight into electricity?", userSelectedLanguageTranslation["Q2"] ?: "Â¿QuÃ© objeto convierte la luz solar en electricidad?"),
    Pair("Is solar energy renewable?", userSelectedLanguageTranslation["Q3"] ?: "Â¿Es la energÃ­a solar renovable?"),
    Pair("In which part of the day is solar energy most available?", userSelectedLanguageTranslation["Q4"] ?: "Â¿En quÃ© parte del dÃ­a hay mÃ¡s energÃ­a solar disponible?"),
    Pair("Does cloudy weather reduce solar panel output?", userSelectedLanguageTranslation["Q5"] ?: "Â¿El clima nublado reduce la producciÃ³n de paneles solares?"),
    // Moderate (3)
    Pair("Which material is often used for making solar panels?", userSelectedLanguageTranslation["Q6"] ?: "Â¿QuÃ© material se usa para fabricar paneles solares?"),
    Pair("What simple device can store the electricity from solar panels?", userSelectedLanguageTranslation["Q7"] ?: "Â¿QuÃ© dispositivo sencillo almacena la electricidad de los paneles solares?"),
    Pair("How can you measure the efficiency of a solar panel?", userSelectedLanguageTranslation["Q8"] ?: "Â¿CÃ³mo se mide la eficiencia de un panel solar?"),
    // Hard (2)
    Pair("Explain the photovoltaic effect.", userSelectedLanguageTranslation["Q9"] ?: "Explica el efecto fotovoltaico."),
    Pair("Name one challenge of integrating solar energy into a cityâ€™s power grid.", userSelectedLanguageTranslation["Q10"] ?: "Nombra un reto de integrar la energÃ­a solar en la red elÃ©ctrica de la ciudad.")
)
val solarQuizOptions = listOf(
    listOf("Energy from the sun", "Energy from wind", "Energy from water", "Energy from soil"),                      // Q1
    listOf("Solar panel", "Battery", "Motor", "Plastic bottle"),                                                     // Q2
    listOf("Yes", "No"),                                                                                            // Q3
    listOf("Night", "Afternoon", "Morning", "Evening"),                                                             // Q4
    listOf("Yes", "No"),                                                                                            // Q5
    listOf("Silicon", "Iron", "Wood", "Paper"),                                                                     // Q6
    listOf("Battery", "Bulb", "Switch", "Fan"),                                                                     // Q7
    listOf("By its output compared to input", "By its color", "By its weight", "By its length"),                    // Q8
    listOf("Explains how sunlight creates electricity in panel", "A method of cooling", "A problem with batteries", "A plastic effect"), // Q9
    listOf("Storing excess energy", "Making electricity", "Cleaning panels", "Using batteries")                     // Q10
)
val solarQuizCorrectAnswers = listOf(0,0,0,1,0,0,0,0,0,0) // Indices of correct options

/////////////////////////
// Step 10: Solar Energy Learning Module
if (step == 10) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                userSelectedLanguageTranslation["SolarIntro"] ?:
                        "Solar energy is the energy from the sun. It is captured using solar panels and can be used for electricity and heating. Solar energy is clean, renewable, and available almost everywhere on Earth.",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            Text(
                userSelectedLanguageTranslation["HarvestGuide"] ?:
                        "Let's learn how to harvest solar energy with simple, low-cost things:\n" +
                        "1. Find a small solar panel (can be purchased cheaply, or from a toy/solar garden light).\n" +
                        "2. Place it outside where it gets direct sunlight (avoid shade).\n" +
                        "3. Connect the solar panel's wires to a small battery (like AA rechargeable battery) using clips or tape.\n" +
                        "4. After a few hours, disconnect the battery and use it to power a small LED light.\n" +
                        "5. You've now harvested solar energy!\nAlways be careful with wires and ask an adult for help if needed.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { step = 11 },
                modifier = Modifier.fillMaxWidth()
            ) { Text(userSelectedLanguageTranslation["StartQuiz"] ?: "Start Solar Energy Quiz") }
        }
    }
}

/////////////////////////
// Step 11: Solar Energy Quiz
if (step == 11) {
    val questionText = solarQuizQuestions[currentQuestion].second
    val options = solarQuizOptions[currentQuestion]
    var selectedOption by remember { mutableStateOf(-1) }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Quiz: ${currentQuestion+1} / ${solarQuizQuestions.size}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(questionText, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        options.forEachIndexed { idx, opt ->
            Button(
                onClick = { selectedOption = idx },
                enabled = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedOption == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            ) { Text(opt) }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                solarQuizAnswers[currentQuestion] = (selectedOption == solarQuizCorrectAnswers[currentQuestion])
                if (currentQuestion < solarQuizQuestions.size - 1) {
                    currentQuestion += 1
                    selectedOption = -1
                } else {
                    step = 12  // Go to score
                }
            },
            enabled = (selectedOption != -1),
            modifier = Modifier.fillMaxWidth()
        ) { Text(userSelectedLanguageTranslation["Next"] ?: "Next") }
    }
}

/////////////////////////
// Step 12: Score & Congrats
if (step == 12) {
    val score = solarQuizAnswers.values.count { it }
    val percentScore = (score * 100 / solarQuizQuestions.size)
    val congratText = when {
        percentScore == 100 -> userSelectedLanguageTranslation["CongratsPerfect"] ?: "Congratulations $userName! ðŸŒž You got a perfect score of $percentScore%. You're a Solar Superstar!"
        percentScore >= 70 -> userSelectedLanguageTranslation["CongratsGood"] ?: "Great job $userName! You scored $percentScore%. You're on your way to mastering solar energy."
        percentScore >= 50 -> userSelectedLanguageTranslation["CongratsOkay"] ?: "Nice try $userName! You scored $percentScore%. Keep learning and you'll shine even brighter!"
        else -> userSelectedLanguageTranslation["CongratsTryAgain"] ?: "Don't worry, $userName! You scored $percentScore%. Review the steps and try again for a higher score!"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(congratText, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { step = 10; currentQuestion = 0; solarQuizAnswers.clear() }, // Retake quiz
                modifier = Modifier.fillMaxWidth()
            ) { Text(userSelectedLanguageTranslation["RetakeQuiz"] ?: "Retake Quiz") }
            // Save result silently
            SideEffect {
                responses["solarScore"] = percentScore.toString()
                val file = context.getFileStreamPath("interactive_first_responses.json")
                file.writeText(gson.toJson(responses))
            }
        }
    }
}
if (step == 100) { // after user chooses "Harvest Solar Energy"
    // Introduction
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(
                userSelectedLanguageTranslation["SolarIntro"]
                    ?: "Solar energy comes from the sun. We can collect this light energy and turn it into electricity using solar panels. Solar energy is clean, renewable, and helps us protect our planet.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                userSelectedLanguageTranslation["SolarDIY"]
                    ?: "Let's learn how to harvest solar energy, even with simple items!\n1. Get a small solar panel (from a garden light or hobby store)\n2. Place it in sunlight on a clear day\n3. Connect wires from the panel to a small battery using clips or tape\n4. After a few hours, disconnect the battery and connect it to a tiny LED.\nSee it glow? You've collected solar power!\nAlways ask an adult to help and never touch loose wires alone.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
            // Here you can include a fun, educational image
            // Image(painter = ..., contentDescription = ...)
            Button(
                onClick = { step = 101 },
                modifier = Modifier.fillMaxWidth()
            ) { Text(userSelectedLanguageTranslation["ReadyForQuiz"] ?: "Ready for Quiz!") }
        }
    }
}

//////////////////////////////////////////////////////////////////
// Quiz logic: Variables
val solarQuizQuestions = listOf(
    Triple("What is solar energy?", "Energy from the sun", listOf("Energy from the sun", "Energy from wind", "Energy from water", "Energy from plants")),
    Triple("What device changes sunlight into electricity?", "Solar panel", listOf("Solar panel", "Battery", "Motor", "Plastic wrap")),
    Triple("Is solar energy renewable?", "Yes", listOf("Yes", "No")),
    Triple("When do solar panels work best?", "Sunny afternoon", listOf("Sunny afternoon", "Night time", "Rainy morning", "Inside a box")),
    Triple("Does solar energy pollute the earth?", "No", listOf("Yes", "No")),
    // Moderate
    Triple("What material is used in most solar panels?", "Silicon", listOf("Silicon", "Steel", "Glass", "Wood")),
    Triple("What stores electricity from a solar panel?", "Battery", listOf("Battery", "Light bulb", "Fan", "Glass jar")),
    Triple("What is needed for a solar system to power your house at night?", "Battery", listOf("Battery", "Fan", "Water", "Colorful wires")),
    // Hard
    Triple("Explain in your own words why solar energy is important.", "", emptyList()), // open-ended
    Triple("Name one problem with using only solar energy for a whole city.", "", emptyList())
)
var currentQuizIndex by remember { mutableStateOf(0) }
var userQuizAnswers by remember { mutableStateOf(mutableListOf<Boolean>()) }
val totalQuestions = solarQuizQuestions.size

// Quiz steps
if (step == 101 && currentQuizIndex < totalQuestions) {
    val (qText, correct, options) = solarQuizQuestions[currentQuizIndex]
    var selectedIdx by remember { mutableStateOf(-1) }
    Column(Modifier.padding(16.dp)) {
        Text("${userSelectedLanguageTranslation["Quiz"] ?: "Quiz"}: ${currentQuizIndex + 1} / $totalQuestions", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Text(qText, style = MaterialTheme.typography.bodyLarge)

        // For multiple choice
        if (options.isNotEmpty()) {
            options.forEachIndexed { idx, ans ->
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    onClick = { selectedIdx = idx }
                ) {
                    Text(ans)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedIdx >= 0,
                onClick = {
                    userQuizAnswers.add(options[selectedIdx] == correct)
                    if (currentQuizIndex < totalQuestions - 1) {
                        currentQuizIndex++
                        selectedIdx = -1
                    } else {
                        step = 102
                    }
                }
            ) { Text(userSelectedLanguageTranslation["Next"] ?: "Next") }
        } else { // For open-ended
            var userText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                label = { Text(userSelectedLanguageTranslation["TypeYourAnswer"] ?: "Type your answer") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = userText.isNotBlank(),
                onClick = {
                    userQuizAnswers.add(userText.isNotBlank()) // Accept any answer as correct for open-ended
                    if (currentQuizIndex < totalQuestions - 1) {
                        currentQuizIndex++
                        userText = ""
                    } else {
                        step = 102
                    }
                }
            ) { Text(userSelectedLanguageTranslation["Next"] ?: "Next") }
        }
    }
}

//////////////////////////////////////////////////////////////////
// Show score
if (step == 102) {
    val correctCount = userQuizAnswers.count { it }
    val percent = (correctCount * 100) / totalQuestions
    val message = when {
        percent == 100 -> userSelectedLanguageTranslation["CongratsPerfect"] ?: "Congratulations, $userName! 100%â€”Solar Genius! â˜€ï¸"
        percent >= 80 -> userSelectedLanguageTranslation["CongratsGreat"] ?: "Well done, $userName! $percent%â€”You're getting brilliant with solar power."
        percent >= 70 -> userSelectedLanguageTranslation["CongratsGood"] ?: "Nice, $userName! $percent%â€”Keep learning and you'll be a solar hero."
        else -> userSelectedLanguageTranslation["CongratsTryAgain"] ?: "Keep trying, $userName! $percent%. Review the steps and you'll get even better next time!"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(message, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    // Reset for retake or navigate to other module/finish
                    step = 100
                    currentQuizIndex = 0
                    userQuizAnswers = mutableListOf()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(userSelectedLanguageTranslation["TryAgain"] ?: "Try Again") }

            // Save the result in user file, silently
            SideEffect {
                responses["solar_score"] = percent.toString()
                val file = context.getFileStreamPath("interactive_first_responses.json")
                file.writeText(gson.toJson(responses))
            }
        }
    }
}
if (step == 200) { // User picked "Harvest Wind Energy"
    // Introduction
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(
                userSelectedLanguageTranslation["WindIntro"]
                    ?: "Wind energy comes from the movement of air. Using wind turbines, we convert this movement into electricity. It's clean, renewable, and an important part of sustainable energy.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                userSelectedLanguageTranslation["WindDIY"]
                    ?: "Letâ€™s learn how to harvest wind energy with simple, low-cost materials:\n" +
                    "1. Build a small wind turbine using cardboard, a small motor, and a fan blade.\n" +
                    "2. Connect the motor to a small LED or battery to capture the electricity.\n" +
                    "3. Place the turbine outdoors in a windy area for the best effect.\n" +
                    "4. Observe how the wind turns the blades and generates power.\n" +
                    "Be safe and ask an adult for help with sharp tools or wiring.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { step = 201 },
                modifier = Modifier.fillMaxWidth()
            ) { Text(userSelectedLanguageTranslation["ReadyForQuiz"] ?: "Ready for Quiz!") }
        }
    }
}

//////////////////////////////////////////////////////////////////
// Variables for quiz
val windQuizQuestions = listOf(
    Triple("What causes wind energy?", "Movement of air", listOf("Movement of air", "Sunlight", "Water flow", "Heat")),
    Triple("What device converts wind energy into electricity?", "Wind turbine", listOf("Wind turbine", "Solar panel", "Battery", "Generator")),
    Triple("Is wind energy renewable?", "Yes", listOf("Yes", "No")),
    Triple("Where should you place a wind turbine?", "In a windy place", listOf("In a windy place", "In the shade", "Inside a building", "In water")),
    Triple("Does wind power create pollution?", "No", listOf("Yes", "No")),
    // Moderate
    Triple("What part of the turbine moves first?", "Blades", listOf("Blades", "Base", "Tower", "Generator")),
    Triple("How is electricity generated by a wind turbine?", "By spinning a generator", listOf("By spinning a generator", "By heating air", "By storing sunlight", "By turning a battery")),
    Triple("What can store electricity from wind turbines?", "Battery", listOf("Battery", "Light bulb", "Switch", "Transformer")),
    // Hard
    Triple("Explain why wind energy is an important natural resource.", "", emptyList()),
    Triple("Name one challenge in using wind energy in cities.", "", emptyList())
)
var currentWindQuizIndex by remember { mutableStateOf(0) }
var userWindQuizAnswers by remember { mutableStateOf(mutableListOf<Boolean>()) }
val totalWindQuestions = windQuizQuestions.size

// Quiz flow
if (step == 201 && currentWindQuizIndex < totalWindQuestions) {
    val (qText, correct, options) = windQuizQuestions[currentWindQuizIndex]
    var selectedIdx by remember { mutableStateOf(-1) }
    Column(Modifier.padding(16.dp)) {
        Text("${userSelectedLanguageTranslation["Quiz"] ?: "Quiz"}: ${currentWindQuizIndex + 1} / $totalWindQuestions", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(qText, style = MaterialTheme.typography.bodyLarge)

        if (options.isNotEmpty()) {
            options.forEachIndexed { idx, ans ->
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    onClick = { selectedIdx = idx }
                ) {
                    Text(ans)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedIdx >= 0,
                onClick = {
                    userWindQuizAnswers.add(options[selectedIdx] == correct)
                    if (currentWindQuizIndex < totalWindQuestions - 1) {
                        currentWindQuizIndex++
                        selectedIdx = -1
                    } else {
                        step = 202
                    }
                }
            ) { Text(userSelectedLanguageTranslation["Next"] ?: "Next") }
        } else {
            var userText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                label = { Text(userSelectedLanguageTranslation["TypeYourAnswer"] ?: "Type your answer") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = userText.isNotBlank(),
                onClick = {
                    userWindQuizAnswers.add(userText.isNotBlank()) // Accept any answer
                    if (currentWindQuizIndex < totalWindQuestions - 1) {
                        currentWindQuizIndex++
                        userText = ""
                    } else {
                        step = 202
                    }
                }
            ) { Text(userSelectedLanguageTranslation["Next"] ?: "Next") }
        }
    }
}

//////////////////////////////////////////////////////////////////
// Score display and save
if (step == 202) {
    val correctCount = userWindQuizAnswers.count { it }
    val percent = (correctCount * 100) / totalWindQuestions
    val congratsMsg = when {
        percent == 100 -> userSelectedLanguageTranslation["CongratsPerfect"] ?: "Congratulations, $userName! ðŸ’¨ You aced the wind energy quiz!"
        percent >= 80 -> userSelectedLanguageTranslation["CongratsGreat"] ?: "Great work, $userName! $percent% â€“ You're a wind energy expert!"
        percent >= 70 -> userSelectedLanguageTranslation["CongratsGood"] ?: "Good job, $userName! $percent% â€“ Keep mastering wind energy."
        else -> userSelectedLanguageTranslation["CongratsTryAgain"] ?: "Don't worry, $userName! You scored $percent%. Try again and learn more!"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(congratsMsg, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    step = 200  // Reset to start wind module again if they want
                    currentWindQuizIndex = 0
                    userWindQuizAnswers.clear()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(userSelectedLanguageTranslation["TryAgain"] ?: "Try Again") }
            // Silent save of score to user's response file
            SideEffect {
                responses["wind_energy_score"] = percent.toString()
                val file = context.getFileStreamPath("interactive_first_responses.json")
                file.writeText(gson.toJson(responses))
            }
        }
    }
}
// Closing with Home button in solar/wind or any scored module
Card(modifier = Modifier.fillMaxWidth()) {
    Column(
        Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(congratsMsg, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                step = 0  // Or your app's home step/flow
                // Reset other states as needed
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(userSelectedLanguageTranslation["Home"] ?: "Home") }
    }
}
// Step 300: User inputs their custom project idea
300 -> Card(modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
        Text(
            userSelectedLanguageTranslation["CustomProjectPrompt"] ?: "Tell me what you want to create!",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))
        var userInput by remember { mutableStateOf("") }
        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text(userSelectedLanguageTranslation["EnterProject"] ?: "Enter your project idea") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                chosenCustomProject = userInput.trim()
                if (chosenCustomProject.isNotEmpty()) {
                    step = 301
                    customProjectScore = 0
                    customProjectQuestionsAsked = 0
                }
            },
            enabled = userInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(userSelectedLanguageTranslation["StartLearning"] ?: "Start Learning") }
    }
}

// Step 301: Guided interactive learning for custom project
301 -> Card(modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
        Text(
            (userSelectedLanguageTranslation["CustomProjectIntro"] ?: "Great! Let's learn how to create: ") + "$chosenCustomProject",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(
            userSelectedLanguageTranslation["CustomProjectGuide"] ?: "We will guide you step by step with explanations, images, and audio.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                // TODO: Integrate real AI interaction and progress
                // Mock advancing question count / scoring as example
                customProjectQuestionsAsked++
                customProjectScore += 1  // Assume correct for demo

                if (customProjectQuestionsAsked >= 10) {
                    step = 302  // Go to score summary
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(userSelectedLanguageTranslation["NextStep"] ?: "Next Step") }
    }
}

// Step 302: Custom project score summary + Home button
302 -> Card(modifier = Modifier.fillMaxWidth()) {
    Column(
        Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val percentScore = (customProjectScore * 10)  // Assuming 10 questions
        val congratsMessage = when {
            percentScore >= 90 ->
                userSelectedLanguageTranslation["CongratsPerfect"] ?: "Fantastic! You scored $percentScore% on your project learning."
            percentScore >= 70 ->
                userSelectedLanguageTranslation["CongratsGreat"] ?: "Great job! You scored $percentScore% on your project."
            else ->
                userSelectedLanguageTranslation["KeepTrying"] ?: "Good effort! Your score is $percentScore%. Keep learning!"
        }
        Text(congratsMessage, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                step = 0  // Back to home or main menu
                // Reset states if necessary
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(userSelectedLanguageTranslation["Home"] ?: "Home")
        }

        // Save results silently
        SideEffect {
            responses["custom_project_score"] = percentScore.toString()
            val file = context.getFileStreamPath("interactive_first_responses.json")
            file.writeText(gson.toJson(responses))
        }
    }
}
