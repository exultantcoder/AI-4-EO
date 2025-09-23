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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.*
import com.google.ai.edge.gallery.customtasks.common.CustomTask
import com.google.ai.edge.gallery.customtasks.common.CustomTaskData
import com.google.ai.edge.gallery.data.CategoryInfo
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.data.createLlmChatConfigs
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * An Interactive Learning custom task implementation that provides personalized educational
 * experiences with Solar Energy, Wind Energy, and Custom Project modules.
 *
 * This class demonstrates:
 * - Real Gemma 3n model integration (E2B and E4B variants)
 * - Persistent user progress tracking using SharedPreferences and file storage
 * - Multi-step onboarding flow with user personalization
 * - Educational modules with quizzes and scoring
 * - Multilingual support for Spanish and English
 * - Custom project creation with AI guidance simulation
 *
 * Key features:
 * - Task Definition: Defines the Interactive Learning Hub with educational category
 * - Model Configuration: Uses actual Gemma 3n models from HuggingFace
 * - User Data Management: Tracks user profiles, scores, and login history
 * - Module Navigation: Solar, Wind, and Custom project learning paths
 * - Quiz System: Interactive questions with scoring and feedback
 * - Results Tracking: Persistent storage of user achievements and progress
 */
class InteractiveLearning @Inject constructor() : CustomTask {

    override val task: Task =
        Task(
            id = "interactive_learning",
            label = "Interactive Learning Hub",
            category = CategoryInfo(id = "learning", label = "Learning"),
            icon = Icons.Outlined.Language,
            description = "Complete personalized learning with Solar, Wind, and Custom projects. " +
                    "Features multilingual support, persistent progress tracking, and interactive " +
                    "educational modules powered by Gemma 3n AI models for hands-on learning experiences.",
            docUrl = "https://github.com/google-ai-edge/gallery/Android/src/app/src/main/java/" +
                    "com/google/ai/edge/gallery/customtasks/common/CustomTask.kt",
            sourceCodeUrl = "https://github.com/google-ai-edge/gallery/Android/src/app/src/main/java/" +
                    "com/google/ai/edge/gallery/customtasks/interactivelearning/InteractiveLearning.kt",
            models = mutableListOf(
                Model(
                    name = "Gemma-3n-E2B-IT",
                    displayName = "Gemma 3n E2B (Recommended)",
                    info = "Efficient 2B parameter Gemma 3n model optimized for educational content. " +
                            "Supports text and image understanding for interactive learning modules. " +
                            "Faster inference with lower memory usage, ideal for mobile learning experiences.",
                    url = "https://huggingface.co/google/gemma-3n-E2B-it-litert-preview/resolve/main/gemma-3n-E2B-it-int4.task?download=true",
                    sizeInBytes = 3136226711L,
                    downloadFileName = "gemma-3n-E2B-it-int4.task",
                    version = "main",
                    configs = createLlmChatConfigs(
                        defaultMaxToken = 2048,
                        defaultTopK = 64,
                        defaultTopP = 0.95f,
                        defaultTemperature = 0.7f
                    ),
                    bestForTaskIds = listOf("interactive_learning_first"),
                    llmSupportImage = true,
                    llmSupportAudio = true,
                    learnMoreUrl = "https://huggingface.co/google/gemma-3n-E2B-it-litert-preview",
                    minDeviceMemoryInGb = 6,
                    showBenchmarkButton = false,
                    showRunAgainButton = false
                ),
                Model(
                    name = "Gemma-3n-E4B-IT",
                    displayName = "Gemma 3n E4B (Advanced)",
                    info = "Enhanced 4B parameter Gemma 3n model for advanced educational interactions. " +
                            "Provides superior understanding and generation capabilities for complex learning scenarios. " +
                            "Best for devices with higher memory capacity and processing power.",
                    url = "https://huggingface.co/google/gemma-3n-E4B-it-litert-preview/resolve/main/gemma-3n-E4B-it-int4.task?download=true",
                    sizeInBytes = 4405655031L,
                    downloadFileName = "gemma-3n-E4B-it-int4.task",
                    version = "main",
                    configs = createLlmChatConfigs(
                        defaultMaxToken = 2048,
                        defaultTopK = 64,
                        defaultTopP = 0.95f,
                        defaultTemperature = 0.7f
                    ),
                    bestForTaskIds = listOf(),
                    llmSupportImage = true,
                    llmSupportAudio = true,
                    learnMoreUrl = "https://huggingface.co/google/gemma-3n-E4B-it-litert-preview",
                    minDeviceMemoryInGb = 8,
                    showBenchmarkButton = false,
                    showRunAgainButton = false
                )
            )
        )

    override fun initializeModelFn(
        context: Context,
        coroutineScope: CoroutineScope,
        model: Model,
        onDone: (String) -> Unit,
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            model.instance = Unit
            onDone("")
            }
        }

    override fun cleanUpModelFn(
        context: Context,
        coroutineScope: CoroutineScope,
        model: Model,
        onDone: () -> Unit,
    ) {
        // Clean up the model instance
        model.instance = null
        // Notify cleanup is complete
        onDone()
    }

    @Composable
    override fun MainScreen(data: Any) {
        // Extract the ModelManagerViewModel from the data parameter
        val myData = data as CustomTaskData
        val modelManagerViewModel: ModelManagerViewModel = myData.modelManagerViewModel

        // Render the Interactive Learning screen
        InteractiveLearningScreen(modelManagerViewModel = modelManagerViewModel)
    }
}

