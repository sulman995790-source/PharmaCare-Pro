package com.example.ui.screens.khata

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerTransaction
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
import com.example.ui.viewmodel.PharmacyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UdharKhataScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val allCustomers by viewModel.allCustomers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val transactions by viewModel.selectedCustomerTransactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(searchQuery, allCustomers) {
        if (searchQuery.isBlank()) allCustomers
        else allCustomers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalOutstanding = allCustomers.sumOf { it.currentOutstanding }
    val duesCount = allCustomers.count { it.currentOutstanding > 0 }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedCustomer == null) {
                FloatingActionButton(
                    onClick = { showAddCustomerDialog = true },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_customer")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Customer")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Customer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
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

            if (selectedCustomer != null) {
                // Customer Detail Ledger View
                CustomerLedgerView(
                    customer = selectedCustomer!!,
                    transactions = transactions,
                    onBack = { viewModel.selectCustomer(null) },
                    onRecordPaymentClick = { showPaymentDialog = true }
                )
            } else {
                // Overview Udhar Khata Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL OUTSTANDING (UDHAR)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹%.2f".format(totalOutstanding),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = StatusAmber
                            )
                            Text(
                                text = "$duesCount customers with pending balance",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(StatusAmberBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = StatusAmber,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search customer name or phone number...", fontSize = 13.sp) },
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
                        .testTag("khata_search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Customer Ledgers (${filteredCustomers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Customer List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectCustomer(customer) }
                                .testTag("customer_card_${customer.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(TealPrimary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = customer.phoneNumber,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹%.0f".format(customer.currentOutstanding),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (customer.currentOutstanding > 0) StatusAmber else StatusGreen
                                    )
                                    StatusBadge(
                                        text = if (customer.currentOutstanding > 0) "DUE" else "SETTLED",
                                        textColor = if (customer.currentOutstanding > 0) StatusAmber else StatusGreen,
                                        bgColor = if (customer.currentOutstanding > 0) StatusAmberBg else StatusGreenBg
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Customer Dialog
        if (showAddCustomerDialog) {
            AddCustomerDialog(
                onDismiss = { showAddCustomerDialog = false },
                onSave = { name, phone, limit ->
                    viewModel.addNewCustomer(name, phone, limit)
                    showAddCustomerDialog = false
                }
            )
        }

        // Record Payment Dialog
        if (showPaymentDialog && selectedCustomer != null) {
            RecordPaymentDialog(
                customer = selectedCustomer!!,
                onDismiss = { showPaymentDialog = false },
                onSave = { amount, mode, note ->
                    viewModel.recordCustomerPayment(amount, mode, note)
                    showPaymentDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomerLedgerView(
    customer: Customer,
    transactions: List<CustomerTransaction>,
    onBack: () -> Unit,
    onRecordPaymentClick: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_to_khata_list")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = customer.phoneNumber, fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Current Due Balance", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "₹%.2f".format(customer.currentOutstanding),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = if (customer.currentOutstanding > 0) StatusAmber else StatusGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Credit Limit", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "₹%.0f".format(customer.creditLimit), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                val limitRatio = (customer.currentOutstanding / customer.creditLimit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { limitRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (limitRatio > 0.8f) StatusRed else TealPrimary,
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Record Payment & WhatsApp Reminder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRecordPaymentClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("record_payment_btn")
                    ) {
                        Icon(Icons.Default.PriceCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receive ₹", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            sendWhatsAppPaymentReminder(context, customer)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("send_whatsapp_reminder_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp Reminder", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Ledger History (${transactions.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { tx ->
                val isCredit = tx.type == "CREDIT_SALE"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isCredit) "Bill on Credit (Udhar)" else "Payment Received (${tx.paymentMode})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (tx.notes.isNotBlank()) {
                                Text(text = tx.notes, fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                text = dateFormat.format(Date(tx.timestamp)),
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }

                        Text(
                            text = if (isCredit) "+₹%.2f".format(tx.amount) else "-₹%.2f".format(tx.amount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (isCredit) StatusAmber else StatusGreen
                        )
                    }
                }
            }
        }
    }
}

fun sendWhatsAppPaymentReminder(context: Context, customer: Customer) {
    val message = """
        Namaste ${customer.name},
        
        Greetings from *LocalWell Pharmacy*.
        Your outstanding medical store credit bill balance is *₹%.2f*.
        
        Kindly settle the dues via UPI (GPay/PhonePe/Paytm) or visit our counter.
        Thank you for trusting LocalWell!
    """.trimIndent().format(customer.currentOutstanding)

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Send Payment Reminder via"))
}

@Composable
fun RecordPaymentDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onSave: (amount: Double, paymentMode: String, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("%.0f".format(customer.currentOutstanding)) }
    var paymentMode by remember { mutableStateOf("Cash") }
    var note by remember { mutableStateOf("Full settlement") }

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
                    text = "Receive Payment from ${customer.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Text(
                    text = "Current outstanding: ₹%.2f".format(customer.currentOutstanding),
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount Received (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "UPI", "Bank").forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Transaction Reference") },
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
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onSave(amt, paymentMode, note)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.testTag("save_payment_btn")
                    ) {
                        Text("Record Payment")
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, creditLimit: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 ") }
    var limitStr by remember { mutableStateOf("5000") }

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
                    text = "Add Khata Customer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name (e.g. Ramesh Sharma)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_cust_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_cust_phone"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Credit Limit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            if (name.isNotBlank()) {
                                onSave(name, phone, limitStr.toDoubleOrNull() ?: 5000.0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.testTag("save_new_cust_btn")
                    ) {
                        Text("Save Customer")
                    }
                }
            }
        }
    }
}
