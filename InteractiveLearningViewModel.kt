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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * UI state for Interactive Learning.
 */
data class InteractiveLearningUiState(
    val step: Int = 1,
    val userProfile: UserProfile = UserProfile(),
    val languageInput: String = "",
    val nameInput: String = "",
    val favoriteTopicInput: String = "",
    val motivationInput: String = "",
    val chosenActivity: String = "",
    val customProjectName: String = ""
)

@HiltViewModel
class InteractiveLearningViewModel @Inject constructor(
    private val userDataManager: UserDataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InteractiveLearningUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initializeUser()
    }

    private fun initializeUser() {
        val profile = userDataManager.updateLoginInfo()
        _uiState.update {
            it.copy(
                userProfile = profile,
                step = if (userDataManager.isUserRegistered()) 0 else 1,
                languageInput = profile.language,
                nameInput = profile.name,
                favoriteTopicInput = profile.favoriteTopic,
                motivationInput = profile.motivation
            )
        }
    }

    fun updateStep(newStep: Int) {
        _uiState.update { it.copy(step = newStep) }
    }

    fun updateLanguage(input: String) {
        _uiState.update { it.copy(languageInput = input) }
    }

    fun updateName(input: String) {
        _uiState.update { it.copy(nameInput = input) }
    }

    fun updateFavoriteTopic(input: String) {
        _uiState.update { it.copy(favoriteTopicInput = input) }
    }

    fun updateMotivation(input: String) {
        _uiState.update { it.copy(motivationInput = input) }
    }

    fun updateActivity(activity: String) {
        _uiState.update { it.copy(chosenActivity = activity) }
    }

    fun updateCustomProject(name: String) {
        _uiState.update { it.copy(customProjectName = name) }
    }

    fun saveUserProfile() {
        val state = _uiState.value
        val profile = state.userProfile.copy(
            language = state.languageInput.trim(),
            name = state.nameInput.trim(),
            favoriteTopic = state.favoriteTopicInput.trim(),
            motivation = state.motivationInput.trim()
        )
        userDataManager.saveUserProfile(profile)
        _uiState.update { it.copy(userProfile = profile) }
    }

    fun resetProfile() {
        userDataManager.clearUserData()
        _uiState.update {
            InteractiveLearningUiState(step = 1)
        }
    }

    fun setSolarScore(score: Int) {
        val profile = _uiState.value.userProfile.copy(solarScore = score)
        userDataManager.saveUserProfile(profile)
        _uiState.update { it.copy(userProfile = profile) }
    }

    fun setWindScore(score: Int) {
        val profile = _uiState.value.userProfile.copy(windEnergyScore = score)
        userDataManager.saveUserProfile(profile)
        _uiState.update { it.copy(userProfile = profile) }
    }

    fun setCustomProjectScore(score: Int) {
        val profile = _uiState.value.userProfile.copy(customProjectScore = score)
        userDataManager.saveUserProfile(profile)
        _uiState.update { it.copy(userProfile = profile) }
    }
}
