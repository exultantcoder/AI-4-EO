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
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Data class for persistent user profile storage.
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
 * Utility class for managing persistent user data storage.
 */
@Singleton
class UserDataManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("interactive_learning_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    companion object {
        private const val USER_PROFILE_KEY = "user_profile"
        private const val USER_DATA_FILE = "interactive_learning_data.json"
    }

    /**
     * Saves the user profile both to SharedPreferences and to a file.
     */
    fun saveUserProfile(profile: UserProfile) {
        val profileJson = json.encodeToString(profile)
        prefs.edit { putString(USER_PROFILE_KEY, profileJson) }
        runCatching {
            File(context.filesDir, USER_DATA_FILE).writeText(profileJson)
        }
    }

    /**
     * Loads the user profile, preferring SharedPreferences, then file backup, else defaults.
     */
    fun loadUserProfile(): UserProfile = runCatching {
        prefs.getString(USER_PROFILE_KEY, null)
            ?.let { json.decodeFromString<UserProfile>(it) }
            ?: File(context.filesDir, USER_DATA_FILE).takeIf { it.exists() }
                ?.readText()?.let { json.decodeFromString<UserProfile>(it) }
            ?: UserProfile()
    }.getOrDefault(UserProfile())

    /**
     * Returns true if the user has completed onboarding.
     */
    fun isUserRegistered(): Boolean {
        val p = loadUserProfile()
        return p.name.isNotBlank() && p.language.isNotBlank()
    }

    /**
     * Increments login count, updates last login timestamp, and saves.
     */
    fun updateLoginInfo(): UserProfile {
        val profile = loadUserProfile()
        val updated = profile.copy(
            loginCount = profile.loginCount + 1,
            lastLoginDate = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
            ).format(java.util.Date())
        )
        saveUserProfile(updated)
        return updated
    }

    /**
     * Clears all stored user data.
     */
    fun clearUserData() {
        prefs.edit { clear() }
        File(context.filesDir, USER_DATA_FILE).takeIf { it.exists() }?.delete()
    }
}
