package com.devquest.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.devquest.model.Preferences
import kotlinx.coroutines.flow.map

private val Context.questPreferences by preferencesDataStore("devquest")

class PreferencesStore(context: Context) {
    private val store = context.applicationContext.questPreferences
    private val nickname = stringPreferencesKey("nickname")
    private val goal = intPreferencesKey("goal")
    private val theme = stringPreferencesKey("theme")
    private val reduced = booleanPreferencesKey("reduced")
    private val onboarded = booleanPreferencesKey("onboarded")
    private val goals = stringSetPreferencesKey("goals")
    val flow =
        store.data.map {
            Preferences(
                it[nickname] ?: "Explorer",
                it[goal] ?: 20,
                it[theme] ?: "Dark",
                it[reduced] ?: false,
                it[onboarded] ?: false,
                it[goals] ?: emptySet(),
            )
        }

    suspend fun update(p: Preferences) {
        store.edit {
            it[nickname] = p.nickname.trim().take(30).ifBlank { "Explorer" }
            it[goal] = p.goal.coerceIn(5, 120)
            it[theme] = p.theme
            it[reduced] = p.reducedMotion
            it[onboarded] = p.onboarded
            it[goals] = p.goals
        }
    }
}
