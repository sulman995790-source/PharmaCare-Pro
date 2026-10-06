package com.example.ui.screens.reports

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SaleInvoice
import com.example.data.local.entity.SaleInvoiceItem
import com.example.ui.components.InvoiceSlipModal
import com.example.ui.components.MetricStatCard
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
fun ReportsScreen(
    viewModel: PharmacyViewModel,
    modifier: Modifier = Modifier
) {
    val allInvoices by viewModel.allInvoices.collectAsState()
    val scheduleH1Invoices by viewModel.scheduleH1Invoices.collectAsState()
    val allBatches by viewModel.allBatches.collectAsState()
    val allPurchases by viewModel.allPurchases.collectAsState()
    val inventoryList by viewModel.inventoryList.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedInvoiceForPreview by remember { mutableStateOf<SaleInvoice?>(null) }
    var invoiceItemsForPreview by remember { mutableStateOf<List<SaleInvoiceItem>>(emptyList()) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    // Calculations
    val totalRevenue = allInvoices.sumOf { it.totalAmount }
    val totalGst = allInvoices.sumOf { it.gstAmount }
    val invoicesCount = allInvoices.size

    val cashSales = allInvoices.filter { it.paymentMode == "Cash" }.sumOf { it.totalAmount }
    val upiSales = allInvoices.filter { it.paymentMode == "UPI" }.sumOf { it.totalAmount }
    val udharSales = allInvoices.filter { it.paymentMode == "Udhar" }.sumOf { it.totalAmount }

    // Expiry at risk (next 90 days)
    val now = System.currentTimeMillis()
    val ninetyDaysLater = now + (90L * 24 * 60 * 60 * 1000)
    val nearExpiryBatches = allBatches.filter { it.expiryTimestamp in now..ninetyDaysLater && it.quantity > 0 }
    val nearExpiryCapitalAtRisk = nearExpiryBatches.sumOf { it.purchasePrice * it.quantity }

    val tabs = listOf("Overview", "Schedule H1 Register", "Expiry Risk", "Purchases")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = TealPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Overview
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricStatCard(
                                title = "Total Sales",
                                value = "₹%.2f".format(totalRevenue),
                                subtitle = "$invoicesCount bills issued",
                                icon = Icons.Default.AttachMoney,
                                iconColor = TealPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "GST Output",
                                value = "₹%.2f".format(totalGst),
                                subtitle = "Ready for GSTR-1",
                                icon = Icons.Default.Receipt,
                                iconColor = StatusBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        // Payment Split Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Collection by Payment Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Cash", fontSize = 11.sp, color = Color.Gray)
                                        Text("₹%.1f".format(cashSales), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusGreen)
                                    }
                                    Column {
                                        Text("UPI / Digital", fontSize = 11.sp, color = Color.Gray)
                                        Text("₹%.1f".format(upiSales), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusBlue)
                                    }
                                    Column {
                                        Text("Udhar (Credit)", fontSize = 11.sp, color = Color.Gray)
                                        Text("₹%.1f".format(udharSales), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusAmber)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Recent Sales Invoices (${allInvoices.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(allInvoices) { inv ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedInvoiceForPreview = inv
                                    // Sample invoice items
                                    invoiceItemsForPreview = listOf(
                                        SaleInvoiceItem(
                                            invoiceId = inv.id,
                                            medicineId = 1,
                                            batchId = 1,
                                            medicineName = "Medicine (Dispensed)",
                                            batchNumber = "B-AUTO",
                                            expiryDate = "12/2026",
                                            quantity = inv.itemsCount,
                                            unitPrice = inv.totalAmount / inv.itemsCount.coerceAtLeast(1),
                                            mrp = inv.totalAmount * 1.1,
                                            discountPercent = 10.0,
                                            gstRate = 12.0,
                                            totalPrice = inv.totalAmount
                                        )
                                    )
                                },
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        StatusBadge(
                                            text = inv.paymentMode,
                                            textColor = if (inv.paymentMode == "Udhar") StatusAmber else StatusGreen,
                                            bgColor = if (inv.paymentMode == "Udhar") StatusAmberBg else StatusGreenBg
                                        )
                                    }
                                    Text(text = "Customer: ${inv.customerName}", fontSize = 11.sp, color = Color.DarkGray)
                                    Text(text = dateFormat.format(Date(inv.timestamp)), fontSize = 10.sp, color = Color.Gray)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹%.2f".format(inv.totalAmount),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = TealPrimary
                                    )
                                    Text(text = "${inv.itemsCount} items", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Schedule H1 Register (Audit & Drug Compliance)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = StatusRedBg.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Statutory Schedule H1 Dispense Register",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StatusRed
                                    )
                                    Text(
                                        text = "Mandatory log of restricted antibiotics & 3rd gen cephalosporins under Drugs & Cosmetics Rules.",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    if (scheduleH1Invoices.isEmpty()) {
                        item {
                            Text(
                                text = "No Schedule H1 prescriptions logged yet.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(scheduleH1Invoices) { inv ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Bill #${inv.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = dateFormat.format(Date(inv.timestamp)), fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Patient: ${inv.customerName} (${inv.customerPhone})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(text = "Prescribing Doctor: ${inv.doctorName}", fontSize = 11.sp, color = TealPrimary)
                                    Text(text = "Amount: ₹%.2f (${inv.paymentMode})".format(inv.totalAmount), fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Expiry Risk Audit
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = StatusAmberBg.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Capital at Expiry Risk (< 90 Days)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusAmber
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹%.2f".format(nearExpiryCapitalAtRisk),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = StatusAmber
                                )
                                Text(
                                    text = "${nearExpiryBatches.size} batches need immediate return to distributor or clearance sale.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    items(nearExpiryBatches) { batch ->
                        val med = inventoryList.find { it.medicine.id == batch.medicineId }?.medicine
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
                                    Text(text = med?.name ?: "Medicine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Batch: ${batch.batchNumber} • Qty: ${batch.quantity} units", fontSize = 11.sp)
                                    Text(text = "Supplier: ${batch.supplierName}", fontSize = 10.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    StatusBadge(
                                        text = "Exp: ${batch.expiryDate}",
                                        textColor = StatusAmber,
                                        bgColor = StatusAmberBg
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Value: ₹%.1f".format(batch.purchasePrice * batch.quantity),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = StatusRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Purchases
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Distributor Purchase Bills (${allPurchases.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(allPurchases) { bill ->
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
                                    Text(text = bill.distributorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Invoice: ${bill.billNumber} • GSTIN: ${bill.distributorGstin}", fontSize = 11.sp, color = Color.Gray)
                                    Text(text = dateFormat.format(Date(bill.invoiceDate)), fontSize = 10.sp, color = Color.LightGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹%.2f".format(bill.totalAmount),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = TealPrimary
                                    )
                                    StatusBadge(
                                        text = bill.status,
                                        textColor = StatusGreen,
                                        bgColor = StatusGreenBg
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Preview modal
        selectedInvoiceForPreview?.let { inv ->
            InvoiceSlipModal(
                invoice = inv,
                items = invoiceItemsForPreview,
                onDismiss = { selectedInvoiceForPreview = null }
            )
        }
    }
}
