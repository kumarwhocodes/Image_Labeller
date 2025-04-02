package com.kumar.imagelabeller.api

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kumar.imagelabeller.model.Restaurant

class RestaurantViewModel(private val repository: RestaurantRepository) : ViewModel() {

    // State for form fields
    var name by mutableStateOf("")
    var address by mutableStateOf("")
    var cuisine by mutableStateOf("")
    var rating by mutableStateOf(0)

    // State for restaurants list
    val restaurants = repository.restaurants

    // Add a restaurant
    suspend fun addRestaurant() {
        if (name.isNotBlank() && address.isNotBlank() && cuisine.isNotBlank()) {
            val restaurant = Restaurant(
                id = System.currentTimeMillis().toString(),
                name = name,
                address = address,
                cuisine = cuisine,
                rating = rating
            )
            repository.addRestaurant(restaurant)

            // Clear form fields
            name = ""
            address = ""
            cuisine = ""
            rating = 0
        }
    }
}