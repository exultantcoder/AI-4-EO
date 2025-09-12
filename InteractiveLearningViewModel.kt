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

import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.google.ai.edge.gallery.ui.common.chat.ChatMessage

/**
 * Holds the UI state for the AI Interactive Learning screen.
 *
 * Tracks:
 * - List of chat messages (text & images)
 * - Current learning phase
 * - Loading status
 */
data class AIInteractiveLearningUiState(
  val messages: List<ChatMessage> = emptyList(),
  val isLoading: Boolean = false,
  val currentPhase: LearningPhase = LearningPhase.ONBOARDING,
  val textColor: Color = Color.Unspecified
)

/** ViewModel for the AI Interactive Learning screen. */
@HiltViewModel
class AIInteractiveLearningViewModel @Inject constructor() : ViewModel() {
  private val _uiState = MutableStateFlow(AIInteractiveLearningUiState())
  val uiState = _uiState.asStateFlow()

  /** Add a new ChatMessage to the state. */
  fun addMessage(modelId: String, message: ChatMessage) {
    _uiState.update { state ->
      state.copy(messages = state.messages + message)
    }
  }

  /** Generate a learning response via LlmChatModelHelper. */
  fun generateLearningResponse(model: String, input: String) {
    _uiState.update { it.copy(isLoading = true) }
    // Call into MediaPipe/LlmChatModelHelper, simplified here
    // On response:
    //   addMessage(model, ChatMessageText(...))
    //   addMessage(model, ChatMessageImage(...)) for image responses
    _uiState.update { it.copy(isLoading = false) }
  }

  /** Regenerate the last response for the given message. */
  fun regenerateResponse(model: String, message: ChatMessage) {
    // Implementation similar to generateLearningResponse
  }

  /** Reset the learning session to initial state. */
  fun resetLearningSession(model: String) {
    _uiState.update { AIInteractiveLearningUiState() }
  }

  /** Advance to the next learning phase. */
  fun nextPhase() {
    _uiState.update { state ->
      val next = when (state.currentPhase) {
        LearningPhase.ONBOARDING -> LearningPhase.TOPIC_SELECTION
        LearningPhase.TOPIC_SELECTION -> LearningPhase.ASSESSMENT
        LearningPhase.ASSESSMENT -> LearningPhase.ADAPTIVE_LEARNING
        LearningPhase.ADAPTIVE_LEARNING -> LearningPhase.REINFORCEMENT
        LearningPhase.REINFORCEMENT -> LearningPhase.EVALUATION
        LearningPhase.EVALUATION -> LearningPhase.ONBOARDING
      }
      state.copy(currentPhase = next)
    }
  }
}
