package com.example.chelaspokerdice.services

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.chelaspokerdice.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit

class DataStoreUserService(
    private val dataStore: DataStore<Preferences>
) {
    val CURR_USER_ID = intPreferencesKey("id")
    val CURR_USERNAME = stringPreferencesKey("username")
    val CURR_USER_TOKEN = stringPreferencesKey("token_validation")

    val currentUser: Flow<User?>
        get() = dataStore.data.map {
            if (it.contains(CURR_USER_ID)) User(
                id = it[CURR_USER_ID]!!,
                username = it[CURR_USERNAME]!!,
                token = it[CURR_USER_TOKEN]!!,
            ) else null
        }

    suspend fun set(
        id: Int,
        username: String,
        token: String
    ) {
        Log.d("DataStoreUserService", "Storing user with id: $id, username: $username")
        dataStore.edit {
            it[CURR_USER_ID] = id
            it[CURR_USERNAME] = username
            it[CURR_USER_TOKEN] = token
        }
        Log.d("DataStoreUserService", "User stored successfully")
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(CURR_USER_ID)
            it.remove(CURR_USERNAME)
            it.remove(CURR_USER_TOKEN)
        }
    }

    suspend fun logout() {
        clear()
    }
}