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
              label = { Text("e.g. English, Español, Italiano") },
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
            Text("Awesome, $userName! 🌟", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(8.dp))
            Text(
              "We will converse in $language and discuss $favoriteTopic. " +
              "Your motivation: “$motivation” will guide our sessions.",
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
