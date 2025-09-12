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

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.ai.edge.gallery.data.ConfigKey
import com.google.ai.edge.gallery.data.NumberSliderConfig
import com.google.ai.edge.gallery.data.ValueType
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel
import com.google.ai.edge.gallery.ui.common.chat.ChatMessage
import com.google.ai.edge.gallery.ui.common.chat.ChatMessageText
import com.google.ai.edge.gallery.ui.common.chat.ChatView
import com.google.ai.edge.gallery.ui.llmchat.LlmChatModelHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// --- Data & Config Keys ---

/** Holds a chat message bitmap for image responses. */
data class LearningImage(val bitmap: Bitmap)

/**
 * Configuration key for number of questions per session.
 */
val INTERACTIVE_LEARNING_CONFIG_QUESTION_COUNT =
  ConfigKey(id = "question_count", label = "Questions per Session")

/** Configuration options for the task (if any future sliders). */
val INTERACTIVE_LEARNING_CONFIGS =
  listOf(
    NumberSliderConfig(
      key = INTERACTIVE_LEARNING_CONFIG_QUESTION_COUNT,
      sliderMin = 1f,
      sliderMax = 10f,
      defaultValue = 3f,
      valueType = ValueType.INT,
      needReinitialization = true
    )
  )

// --- Composable Screen ---

/**
 * Main screen for AI Interactive Learning.
 *
 * Uses the built-in ChatView for streaming multi-modal AI responses (text, images, audio).
 */
@Composable
fun AIInteractiveLearningScreen(
  modelManagerViewModel: ModelManagerViewModel,
  viewModel: AIInteractiveLearningViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val uiState by modelManagerViewModel.uiState.collectAsState()
  val model = uiState.selectedModel

  // Stream of chat messages (text & images).
  val messages by viewModel.messages.collectAsState()

  Column(modifier = Modifier.fillMaxSize()) {
    // ChatView handles the scrolling list of ChatMessage/Text/Image items.
    ChatView(
      task = modelManagerViewModel.getTaskById("ai_interactive_learning")!!,
      viewModel = viewModel,
      modelManagerViewModel = modelManagerViewModel,
      onSendMessage = { model, newMessages ->
        newMessages.forEach { viewModel.addMessage(model, it) }
        // If last message is text, trigger AI response.
        (newMessages.lastOrNull() as? ChatMessageText)?.let { userMsg ->
          viewModel.generateLearningResponse(model, userMsg.content)
        }
      },
      onRunAgainClicked = { model, msg ->
        if (msg is ChatMessageText) {
          viewModel.regenerateResponse(model, msg)
        }
      },
      onResetSessionClicked = { model ->
        viewModel.resetLearningSession(model)
      },
      onBenchmarkClicked = { _, _, _, _ -> },
      navigateUp = {}
    )

    // Footer: loading spinner while AI is processing
    if (!uiState.isModelInitialized(model)) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}
