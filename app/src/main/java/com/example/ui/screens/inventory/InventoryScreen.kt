package com.example.ui.screens.inventory

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.Medicine
import com.example.data.local.entity.MedicineBatch
import com.example.ui.components.AddMedicineDialog
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
import com.example.ui.viewmodel.InventoryFilter
import com.example.ui.viewmodel.MedicineItemUi
import com.example.ui.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: PharmacyViewModel,
    onNavigateToBilling: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val inventoryList by viewModel.inventoryList.collectAsState()
    val searchQuery by viewModel.inventorySearchQuery.collectAsState()
    val currentFilter by viewModel.inventoryFilter.collectAsState()

    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var selectedMedForNewBatch by remember { mutableStateOf<Medicine?>(null) }
    var expandedMedicineId by remember { mutableStateOf<Long?>(null) }

    // Quick stats
    val totalMeds = inventoryList.size
    val totalUnits = inventoryList.sumOf { it.totalStock }
    val nearExpiryCount = inventoryList.count { it.hasExpiringSoon }
    val lowStockCount = inventoryList.count { it.isLowStock }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMedicineDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_medicine")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Medicine")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Medicine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.inventorySearchQuery.value = it },
                placeholder = { Text("Search brand, salt, company, rack...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.inventorySearchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_search_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Medicines", fontSize = 10.sp, color = Color.Gray)
                        Text("$totalMeds", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Total Stock", fontSize = 10.sp, color = Color.Gray)
                        Text("$totalUnits units", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (nearExpiryCount > 0) StatusAmberBg else MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Expiring <90d", fontSize = 10.sp, color = if (nearExpiryCount > 0) StatusAmber else Color.Gray)
                        Text("$nearExpiryCount items", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (nearExpiryCount > 0) StatusAmber else Color.DarkGray)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (lowStockCount > 0) StatusRedBg else MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Low Stock", fontSize = 10.sp, color = if (lowStockCount > 0) StatusRed else Color.Gray)
                        Text("$lowStockCount items", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (lowStockCount > 0) StatusRed else Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = currentFilter == InventoryFilter.ALL,
                        onClick = { viewModel.inventoryFilter.value = InventoryFilter.ALL },
                        label = { Text("All (${totalMeds})", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == InventoryFilter.LOW_STOCK,
                        onClick = { viewModel.inventoryFilter.value = InventoryFilter.LOW_STOCK },
                        label = { Text("Low Stock ($lowStockCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusRedBg, selectedLabelColor = StatusRed)
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == InventoryFilter.EXPIRING_SOON,
                        onClick = { viewModel.inventoryFilter.value = InventoryFilter.EXPIRING_SOON },
                        label = { Text("Expiring Soon ($nearExpiryCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusAmberBg, selectedLabelColor = StatusAmber)
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == InventoryFilter.SCHEDULE_H1,
                        onClick = { viewModel.inventoryFilter.value = InventoryFilter.SCHEDULE_H1 },
                        label = { Text("Schedule H1", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Medicines List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(inventoryList) { item ->
                    val isExpanded = expandedMedicineId == item.medicine.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_item_${item.medicine.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header Row
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
                                        if (item.medicine.isScheduleH1) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusBadge(
                                                text = "Sch H1",
                                                textColor = StatusRed,
                                                bgColor = StatusRedBg
                                            )
                                        }
                                    }
                                    Text(
                                        text = item.medicine.genericName,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.padding(top = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Place,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${item.medicine.rackLocation} • ${item.medicine.manufacturer}",
                                            fontSize = 10.sp,
                                            color = TealPrimary
                                        )
                                    }
                                }

                                // Total Stock Badge
                                Column(horizontalAlignment = Alignment.End) {
                                    StatusBadge(
                                        text = "${item.totalStock} in stock",
                                        textColor = if (item.isLowStock) StatusRed else StatusGreen,
                                        bgColor = if (item.isLowStock) StatusRedBg else StatusGreenBg
                                    )
                                    if (item.hasExpiringSoon) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        StatusBadge(
                                            text = "Exp < 90d",
                                            textColor = StatusAmber,
                                            bgColor = StatusAmberBg
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))

                            // Action buttons & Accordion toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.clickable {
                                        expandedMedicineId = if (isExpanded) null else item.medicine.id
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.batches.size} Batches (FEFO)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TealPrimary
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { selectedMedForNewBatch = item.medicine },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("+ Batch", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.addToCart(item.medicine)
                                            onNavigateToBilling()
                                        },
                                        enabled = item.totalStock > 0,
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Bill", fontSize = 10.sp)
                                    }
                                }
                            }

                            // Expanded Batches View
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "FEFO BATCHES (First Expiry First Out)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )

                                    item.batches.forEachIndexed { bIndex, batch ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White, RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Batch: ${batch.batchNumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                    if (bIndex == 0) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        StatusBadge(
                                                            text = "FEFO Priority",
                                                            textColor = StatusGreen,
                                                            bgColor = StatusGreenBg
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "Exp: ${batch.expiryDate} • MRP: ₹%.1f • Rate: ₹%.1f".format(batch.mrp, batch.sellingPrice),
                                                    fontSize = 10.sp,
                                                    color = Color.DarkGray
                                                )
                                                Text(
                                                    text = "Distributor: ${batch.supplierName}",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            // Stock Adjuster
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        if (batch.quantity > 0) {
                                                            viewModel.adjustBatchStock(batch.id, batch.quantity - 1)
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                                }
                                                Text(
                                                    text = "${batch.quantity}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        viewModel.adjustBatchStock(batch.id, batch.quantity + 1)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Medicine Modal
        if (showAddMedicineDialog) {
            AddMedicineDialog(
                onDismiss = { showAddMedicineDialog = false },
                onSave = { name, generic, manufacturer, category, rack, isSchH, isSchH1, batchNum, expDate, qty, cost, mrp, rate ->
                    viewModel.addNewMedicineWithBatch(
                        name = name,
                        generic = generic,
                        manufacturer = manufacturer,
                        category = category,
                        rackLocation = rack,
                        isScheduleH = isSchH,
                        isScheduleH1 = isSchH1,
                        batchNumber = batchNum,
                        expiryDate = expDate,
                        quantity = qty,
                        purchasePrice = cost,
                        mrp = mrp,
                        sellingPrice = rate
                    )
                    showAddMedicineDialog = false
                }
            )
        }

        // Add Batch Modal
        selectedMedForNewBatch?.let { med ->
            AddBatchDialog(
                medicine = med,
                onDismiss = { selectedMedForNewBatch = null },
                onSave = { batchNum, expDate, qty, cost, mrp, rate, supplier ->
                    viewModel.addNewBatchForMedicine(
                        medicineId = med.id,
                        batchNumber = batchNum,
                        expiryDate = expDate,
                        quantity = qty,
                        purchasePrice = cost,
                        mrp = mrp,
                        sellingPrice = rate,
                        supplierName = supplier
                    )
                    selectedMedForNewBatch = null
                }
            )
        }
    }
}

@Composable
fun AddBatchDialog(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onSave: (
        batchNumber: String,
        expiryDate: String,
        quantity: Int,
        purchasePrice: Double,
        mrp: Double,
        sellingPrice: Double,
        supplierName: String
    ) -> Unit
) {
    var batchNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("12/2026") }
    var quantityStr by remember { mutableStateOf("50") }
    var purchasePriceStr by remember { mutableStateOf("25.0") }
    var mrpStr by remember { mutableStateOf("35.0") }
    var sellingPriceStr by remember { mutableStateOf("31.5") }
    var supplier by remember { mutableStateOf("Cipla Wholesale Dist") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add New Batch for ${medicine.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Text(
                    text = medicine.genericName,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Batch #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Exp (MM/YYYY)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it },
                        label = { Text("Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mrpStr,
                        onValueChange = { mrpStr = it },
                        label = { Text("MRP (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = { Text("Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Distributor / Supplier") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (batchNumber.isNotBlank()) {
                                onSave(
                                    batchNumber,
                                    expiryDate,
                                    quantityStr.toIntOrNull() ?: 1,
                                    purchasePriceStr.toDoubleOrNull() ?: 0.0,
                                    mrpStr.toDoubleOrNull() ?: 0.0,
                                    sellingPriceStr.toDoubleOrNull() ?: 0.0,
                                    supplier
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("Add Batch")
                    }
                }
            }
        }
    }
}
