package com.kumar.imagelabeller.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kumar.imagelabeller.api.RestaurantRepository
import com.kumar.imagelabeller.api.RestaurantViewModel
import com.kumar.imagelabeller.api.RestaurantViewModelFactory
import com.kumar.imagelabeller.api.restaurantDataStore
import com.kumar.imagelabeller.model.Restaurant
import kotlinx.coroutines.launch

@Composable
fun RestaurantScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { RestaurantRepository(context.restaurantDataStore) }
    val viewModel = viewModel<RestaurantViewModel>(
        factory = RestaurantViewModelFactory(repository)
    )
    val restaurants by viewModel.restaurants.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Restaurant Data Entry",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Restaurant entry form
        RestaurantEntryForm(
            name = viewModel.name,
            onNameChange = { viewModel.name = it },
            address = viewModel.address,
            onAddressChange = { viewModel.address = it },
            cuisine = viewModel.cuisine,
            onCuisineChange = { viewModel.cuisine = it },
            rating = viewModel.rating,
            onRatingChange = { viewModel.rating = it },
            onSubmit = {
                coroutineScope.launch {
                    viewModel.addRestaurant()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Restaurant list
        Text(
            text = "Restaurant List",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn {
            items(restaurants.size) { index ->
                RestaurantItem(restaurant = restaurants[index])
            }
        }
    }
}

@Composable
fun RestaurantEntryForm(
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    cuisine: String,
    onCuisineChange: (String) -> Unit,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Column {
        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Restaurant Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        // Address field
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        // Cuisine dropdown
        CuisineDropdown(
            selectedCuisine = cuisine,
            onCuisineSelected = onCuisineChange
        )

        // Rating stars
        RatingBar(
            maxRating = 5,
            currentRating = rating,
            onRatingChanged = onRatingChange,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Submit button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add Restaurant")
        }
    }
}

@Composable
fun CuisineDropdown(
    selectedCuisine: String,
    onCuisineSelected: (String) -> Unit
) {
    val cuisines =
        listOf("Italian", "Chinese", "Indian", "Mexican", "Japanese", "Thai", "American", "French")
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = selectedCuisine,
            onValueChange = {},
            readOnly = true,
            label = { Text("Cuisine") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            cuisines.forEach { cuisine ->
                DropdownMenuItem(
                    text = { Text(cuisine) },
                    onClick = {
                        onCuisineSelected(cuisine)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RatingBar(
    maxRating: Int = 5,
    currentRating: Int,
    onRatingChanged: (Int) -> Unit,
    starsColor: Color = Color.Yellow,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = "Rating: ",
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= currentRating) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = if (i <= currentRating) starsColor else Color.Unspecified,
                modifier = Modifier
                    .clickable { onRatingChanged(i) }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun RestaurantItem(restaurant: Restaurant) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = restaurant.address,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cuisine: ${restaurant.cuisine}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                // Display rating stars
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= restaurant.rating) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = null,
                            tint = if (i <= restaurant.rating) Color.Yellow else Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}