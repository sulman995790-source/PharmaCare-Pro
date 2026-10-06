package com.example.ui.screens.substitutes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusBlueBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondaryContainer
import com.example.ui.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstituteFinderScreen(
    viewModel: PharmacyViewModel,
    onNavigateToBilling: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("Paracetamol 650mg") }
    val inventoryList by viewModel.inventoryList.collectAsState()

    // Find medicines sharing similar salt words
    val saltKeywords = remember(searchQuery) {
        searchQuery.split("+", " ", "mg", "mcg", "-")
            .map { it.trim().lowercase() }
            .filter { it.length > 2 }
    }

    val matchedMedicines = remember(searchQuery, inventoryList) {
        if (searchQuery.isBlank()) emptyList()
        else {
            inventoryList.filter { item ->
                val medName = item.medicine.name.lowercase()
                val genName = item.medicine.genericName.lowercase()
                val q = searchQuery.trim().lowercase()

                medName.contains(q) || genName.contains(q) ||
                    saltKeywords.any { kw -> genName.contains(kw) }
            }.sortedBy { it.earliestBatch?.sellingPrice ?: Double.MAX_VALUE }
        }
    }

    val quickSalts = listOf(
        "Paracetamol 650",
        "Amoxicillin + Clav",
        "Pantoprazole 40",
        "Azithromycin 500",
        "Telmisartan 40",
        "Montelukast",
        "Metformin 500"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Info Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TealSecondaryContainer.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Smart Generic & Substitute Finder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TealPrimary
                    )
                    Text(
                        text = "Suggest identical-salt medicines to patients to save costs or prevent lost sales when out-of-stock.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search medicine name or active salt...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("substitute_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Salt suggestion chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickSalts) { salt ->
                SuggestionChip(
                    onClick = { searchQuery = salt },
                    label = { Text(salt, fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Substitutes (${matchedMedicines.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Sorted by lowest price",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Results
        if (matchedMedicines.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching substitutes found",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Try searching by generic salt (e.g. Paracetamol, Pantoprazole)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            val lowestPrice = matchedMedicines.mapNotNull { it.earliestBatch?.sellingPrice }.minOrNull() ?: 1.0

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(matchedMedicines) { item ->
                    val batch = item.earliestBatch
                    val price = batch?.sellingPrice ?: 0.0
                    val mrp = batch?.mrp ?: 0.0
                    val isLowest = price == lowestPrice

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("substitute_item_${item.medicine.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLowest) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.medicine.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (isLowest) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusBadge(
                                                text = "Best Value",
                                                textColor = StatusGreen,
                                                bgColor = StatusGreenBg
                                            )
                                        }
                                    }
                                    Text(
                                        text = item.medicine.genericName,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${item.medicine.manufacturer} • ${item.medicine.rackLocation}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹%.1f".format(price),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = TealPrimary
                                    )
                                    if (mrp > price) {
                                        val savePct = ((mrp - price) / mrp * 100).toInt()
                                        Text(
                                            text = "MRP ₹%.1f ($savePct% OFF)".format(mrp),
                                            fontSize = 10.sp,
                                            color = StatusGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadge(
                                        text = if (item.totalStock > 0) "${item.totalStock} in stock" else "Out of stock",
                                        textColor = if (item.totalStock > 0) StatusGreen else StatusRed,
                                        bgColor = if (item.totalStock > 0) StatusGreenBg else StatusRedBg
                                    )
                                    if (batch != null) {
                                        Text(
                                            text = "Exp: ${batch.expiryDate}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.addToCart(item.medicine)
                                        onNavigateToBilling()
                                    },
                                    enabled = item.totalStock > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("bill_substitute_btn_${item.medicine.id}")
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to Bill", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
