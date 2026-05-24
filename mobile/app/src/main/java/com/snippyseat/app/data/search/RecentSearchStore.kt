package com.snippyseat.app.data.search

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.searchDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "snippy_seat_search",
)

@Singleton
class RecentSearchStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.searchDataStore

    val recentSearches: Flow<List<String>> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            preferences[RECENT_SEARCHES]
                ?.split("|")
                ?.filter { it.isNotBlank() }
                .orEmpty()
        }

    suspend fun addSearch(query: String) {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return

        dataStore.edit { preferences ->
            val current = preferences[RECENT_SEARCHES]
                ?.split("|")
                ?.filter { it.isNotBlank() && !it.equals(cleaned, ignoreCase = true) }
                .orEmpty()
            preferences[RECENT_SEARCHES] = (listOf(cleaned) + current).take(10).joinToString("|")
        }
    }

    private companion object {
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    }
}
