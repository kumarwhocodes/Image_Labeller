package com.kumar.imagelabeller.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kumar.imagelabeller.model.Restaurant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.restaurantDataStore by preferencesDataStore(name = "restaurant_preferences")

class RestaurantRepository(private val dataStore: DataStore<Preferences>) {

    // Keys for preferences
    companion object {
        val RESTAURANT_DATA = stringSetPreferencesKey("restaurant_data")
    }

    // Get all restaurants
    val restaurants: Flow<List<Restaurant>> = dataStore.data.map { preferences ->
        val restaurantSet = preferences[RESTAURANT_DATA] ?: emptySet()
        restaurantSet.map { restaurantString ->
            val parts = restaurantString.split("|||")
            Restaurant(
                id = parts[0],
                name = parts[1],
                address = parts[2],
                cuisine = parts[3],
                rating = parts[4].toInt()
            )
        }
    }

    // Add a restaurant
    suspend fun addRestaurant(restaurant: Restaurant) {
        dataStore.edit { preferences ->
            val currentSet = preferences[RESTAURANT_DATA] ?: emptySet()
            val restaurantString =
                "${restaurant.id}|||${restaurant.name}|||${restaurant.address}|||${restaurant.cuisine}|||${restaurant.rating}"
            preferences[RESTAURANT_DATA] = currentSet + restaurantString
        }
    }
}