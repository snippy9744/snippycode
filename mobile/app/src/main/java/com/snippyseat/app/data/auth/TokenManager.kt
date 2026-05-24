package com.snippyseat.app.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.snippyseat.app.core.model.UserRole
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.snippySeatDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "snippy_seat_tokens",
)

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.snippySeatDataStore

    val session: Flow<AuthSession> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            AuthSession(
                accessToken = preferences[ACCESS_TOKEN],
                refreshToken = preferences[REFRESH_TOKEN],
                temporaryToken = preferences[TEMPORARY_TOKEN],
                userRole = UserRole.from(preferences[USER_ROLE]),
            )
        }

    suspend fun saveTokens(accessToken: String, refreshToken: String, userRole: UserRole) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[USER_ROLE] = userRole.name
            preferences.remove(TEMPORARY_TOKEN)
        }
    }

    suspend fun saveTemporaryToken(temporaryToken: String) {
        dataStore.edit { preferences ->
            preferences[TEMPORARY_TOKEN] = temporaryToken
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    fun bearerTokenBlocking(): String? = runBlocking {
        session.map { it.accessToken ?: it.temporaryToken }.first()
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val TEMPORARY_TOKEN = stringPreferencesKey("temporary_token")
        val USER_ROLE = stringPreferencesKey("user_role")
    }
}
