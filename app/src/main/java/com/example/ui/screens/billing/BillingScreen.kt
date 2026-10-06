package com.example.ui.screens.billing

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Medicine
import com.example.ui.components.InvoiceSlipModal
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
fun BillingScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val allMedicines by viewModel.allMedicines.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val isInvoiceOpen by viewModel.isInvoiceModalOpen.collectAsState()
    val lastInvoice by viewModel.lastGeneratedInvoice.collectAsState()
    val lastItems by viewModel.lastGeneratedItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }

    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val doctorName by viewModel.doctorName.collectAsState()
    val paymentMode by viewModel.paymentMode.collectAsState()
    val selectedCustomerId by viewModel.selectedCustomerId.collectAsState()

    var showCustomerPicker by remember { mutableStateOf(false) }

    val filteredMedicines = remember(searchQuery, allMedicines) {
        if (searchQuery.isBlank()) emptyList()
        else allMedicines.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.genericName.contains(searchQuery, ignoreCase = true)
        }.take(6)
    }

    val subtotal = cartItems.sumOf { it.lineSubtotal }
    val totalDiscount = cartItems.sumOf { it.lineDiscount }
    val totalGst = cartItems.sumOf { it.lineGst }
    val grandTotal = cartItems.sumOf { it.lineTotal }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Search & Add Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    searchFocused = true
                                },
                                placeholder = { Text("Search medicine, brand, salt...", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pos_medicine_search"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Search Results Dropdown List
                        if (filteredMedicines.isNotEmpty() && searchFocused) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    filteredMedicines.forEach { med ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.addToCart(med)
                                                    searchQuery = ""
                                                    searchFocused = false
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = med.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    if (med.isScheduleH1) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        StatusBadge(
                                                            text = "Sch H1",
                                                            textColor = StatusRed,
                                                            bgColor = StatusRedBg
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = med.genericName,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${med.manufacturer} • ${med.rackLocation}",
                                                    fontSize = 10.sp,
                                                    color = TealPrimary
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.addToCart(med)
                                                    searchQuery = ""
                                                    searchFocused = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("add_item_btn_${med.id}")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Add (FEFO)", fontSize = 11.sp)
                                            }
                                        }
                                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cart Items Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Current Bill Items (${cartItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (cartItems.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearCart() },
                            modifier = Modifier.testTag("clear_cart_button")
                        ) {
                            Text("Clear", color = StatusRed, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (cartItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Cart is empty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Search medicine above or select from inventory to add",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(cartItems) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_item_$index"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.medicine.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = item.medicine.genericName,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        StatusBadge(
                                            text = "B:${item.batch.batchNumber}",
                                            textColor = Color.DarkGray,
                                            bgColor = Color(0xFFF1F5F9)
                                        )
                                        StatusBadge(
                                            text = "Exp: ${item.batch.expiryDate}",
                                            textColor = StatusAmber,
                                            bgColor = StatusAmberBg
                                        )
                                        StatusBadge(
                                            text = "Stock: ${item.batch.quantity}",
                                            textColor = TealPrimary,
                                            bgColor = TealSecondaryContainer
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.removeFromCart(index) },
                                    modifier = Modifier.size(28.dp).testTag("delete_item_$index")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = StatusRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Quantity Stepper & Price Calculation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(index, item.quantity - 1) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        fontSize = 14.sp
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(index, item.quantity + 1) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Rate & Total
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Rate: ₹%.1f  (MRP: ₹%.1f)".format(item.unitPrice, item.batch.mrp),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Total: ₹%.2f".format(item.lineTotal),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TealPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Customer & Doctor Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Patient & Doctor Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Quick select Udhar customer
                            TextButton(onClick = { showCustomerPicker = !showCustomerPicker }) {
                                Text(
                                    text = if (selectedCustomerId != null) "Change Khata" else "Select Khata",
                                    fontSize = 12.sp,
                                    color = TealPrimary
                                )
                            }
                        }

                        if (showCustomerPicker) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Pick Registered Customer for Bill / Udhar:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                                        items(allCustomers) { cust ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.selectedCustomerId.value = cust.id
                                                        viewModel.customerName.value = cust.name
                                                        viewModel.customerPhone.value = cust.phoneNumber
                                                        showCustomerPicker = false
                                                    }
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(cust.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Text("Due: ₹%.0f".format(cust.currentOutstanding), fontSize = 11.sp, color = StatusAmber)
                                            }
                                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { viewModel.customerName.value = it },
                                label = { Text("Customer Name") },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("customer_name_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { viewModel.customerPhone.value = it },
                                label = { Text("Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = doctorName,
                            onValueChange = { viewModel.doctorName.value = it },
                            label = { Text("Doctor Name (Prescription)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Payment Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Cash", "UPI", "Card", "Udhar").forEach { mode ->
                                val isSelected = paymentMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.paymentMode.value = mode },
                                    label = {
                                        Text(
                                            text = if (mode == "Udhar") "Udhar (Credit)" else mode,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (mode == "Udhar") StatusAmberBg else TealPrimary,
                                        selectedLabelColor = if (mode == "Udhar") StatusAmber else Color.White
                                    ),
                                    modifier = Modifier.testTag("payment_mode_$mode")
                                )
                            }
                        }
                    }
                }
            }

            // Summary & Checkout Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bill Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 12.sp, color = Color.Gray)
                            Text("₹%.2f".format(subtotal), fontSize = 12.sp)
                        }
                        if (totalDiscount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Chemist Discount", fontSize = 12.sp, color = StatusGreen)
                                Text("-₹%.2f".format(totalDiscount), fontSize = 12.sp, color = StatusGreen)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST (Included)", fontSize = 12.sp, color = Color.Gray)
                            Text("₹%.2f".format(totalGst), fontSize = 12.sp)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color(0xFFE2E8F0))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(
                                text = "₹%.2f".format(grandTotal),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = TealPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.checkout() },
                            enabled = cartItems.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_bill_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (paymentMode == "Udhar") "Add to Udhar Khata (₹%.0f)".format(grandTotal) else "Generate GST Invoice (₹%.2f)".format(grandTotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Modal for generated bill
        if (isInvoiceOpen && lastInvoice != null) {
            InvoiceSlipModal(
                invoice = lastInvoice!!,
                items = lastItems,
                onDismiss = { viewModel.closeInvoiceModal() }
            )
        }
    }
}
