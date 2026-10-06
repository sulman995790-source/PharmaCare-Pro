package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.SaleInvoice
import com.example.data.local.entity.SaleInvoiceItem
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusBadge(
    text: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun InvoiceSlipModal(
    invoice: SaleInvoice,
    items: List<SaleInvoiceItem>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(invoice.timestamp))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("invoice_slip_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LOCALWELL PHARMACY",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = TealPrimary
                            )
                            Text(
                                text = "GSTIN: 07AABCL1234F1Z8",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_invoice_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 1.dp, color = Color(0xFFE2E8F0))

                // Invoice meta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Tax Invoice: ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Date: $formattedDate", fontSize = 11.sp, color = Color.DarkGray)
                    }
                    StatusBadge(
                        text = invoice.paymentMode.uppercase(),
                        textColor = if (invoice.paymentMode == "Udhar") StatusAmber else StatusGreen,
                        bgColor = if (invoice.paymentMode == "Udhar") StatusAmberBg else StatusGreenBg
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Patient: ${invoice.customerName} ${if (invoice.customerPhone.isNotBlank()) "(${invoice.customerPhone})" else ""}", fontSize = 11.sp)
                Text(text = "Doctor: ${invoice.doctorName}", fontSize = 11.sp, color = Color.DarkGray)

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 1.dp, color = Color(0xFFE2E8F0))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text(text = "Item & Batch", modifier = Modifier.weight(1.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Exp", modifier = Modifier.weight(0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Qty", modifier = Modifier.weight(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(text = "Rate", modifier = Modifier.weight(0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    Text(text = "Total", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                }

                // Items list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.8f)) {
                                Text(
                                    text = item.medicineName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "B:${item.batchNumber}",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(text = item.expiryDate, modifier = Modifier.weight(0.9f), fontSize = 10.sp, color = Color.DarkGray)
                            Text(text = "${item.quantity}", modifier = Modifier.weight(0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
                            Text(text = "₹%.1f".format(item.unitPrice), modifier = Modifier.weight(0.8f), fontSize = 10.sp, textAlign = TextAlign.End)
                            Text(
                                text = "₹%.2f".format(item.totalPrice),
                                modifier = Modifier.weight(1f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF1F5F9))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color(0xFFE2E8F0))

                // Totals
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Subtotal", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "₹%.2f".format(invoice.subtotal), fontSize = 11.sp)
                    }
                    if (invoice.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Discount", fontSize = 11.sp, color = StatusGreen)
                            Text(text = "-₹%.2f".format(invoice.discountAmount), fontSize = 11.sp, color = StatusGreen)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "GST (CGST+SGST included)", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "₹%.2f".format(invoice.gstAmount), fontSize = 11.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = Color(0xFFE2E8F0))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Net Payable", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "₹%.2f".format(invoice.totalAmount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TealPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Share & WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareInvoiceSummary(context, invoice, items)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_invoice_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("done_invoice_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Done", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun shareInvoiceSummary(context: Context, invoice: SaleInvoice, items: List<SaleInvoiceItem>) {
    val itemsSummary = items.joinToString("\n") {
        "• ${it.medicineName} (Qty: ${it.quantity}) - ₹%.2f".format(it.totalPrice)
    }

    val text = """
        *LOCALWELL PHARMACY*
        Invoice: ${invoice.invoiceNumber}
        Customer: ${invoice.customerName}
        Mode: ${invoice.paymentMode}
        --------------------------
        $itemsSummary
        --------------------------
        *Grand Total: ₹%.2f*
        Thank you for choosing LocalWell!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Pharmacy Bill ${invoice.invoiceNumber}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Invoice via"))
}

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        generic: String,
        manufacturer: String,
        category: String,
        rackLocation: String,
        isScheduleH: Boolean,
        isScheduleH1: Boolean,
        batchNumber: String,
        expiryDate: String,
        quantity: Int,
        purchasePrice: Double,
        mrp: Double,
        sellingPrice: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var generic by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tablets") }
    var rack by remember { mutableStateOf("Rack A-01") }
    var isScheduleH by remember { mutableStateOf(false) }
    var isScheduleH1 by remember { mutableStateOf(false) }

    var batchNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("12/2026") }
    var quantityStr by remember { mutableStateOf("50") }
    var purchasePriceStr by remember { mutableStateOf("25.0") }
    var mrpStr by remember { mutableStateOf("35.0") }
    var sellingPriceStr by remember { mutableStateOf("31.5") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_medicine_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                item {
                    Text(
                        text = "Add Medicine to Catalog",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Brand / Medicine Name (e.g. Dolo 650)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = generic,
                        onValueChange = { generic = it },
                        label = { Text("Active Salt / Generic Composition") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_generic_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = manufacturer,
                            onValueChange = { manufacturer = it },
                            label = { Text("Company") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Form") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rack,
                        onValueChange = { rack = it },
                        label = { Text("Rack / Shelf Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isScheduleH = !isScheduleH },
                            modifier = Modifier.weight(1f),
                            colors = if (isScheduleH) ButtonDefaults.outlinedButtonColors(containerColor = StatusAmberBg) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(text = if (isScheduleH) "✓ Schedule H" else "Schedule H", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { isScheduleH1 = !isScheduleH1 },
                            modifier = Modifier.weight(1f),
                            colors = if (isScheduleH1) ButtonDefaults.outlinedButtonColors(containerColor = StatusRedBg) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(text = if (isScheduleH1) "✓ Schedule H1" else "Schedule H1", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Initial Stock Batch (Optional)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

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
                            label = { Text("Units / Qty") },
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
                            label = { Text("Sale Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

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
                                if (name.isNotBlank() && generic.isNotBlank()) {
                                    onSave(
                                        name,
                                        generic,
                                        manufacturer.ifBlank { "Pharma Generic" },
                                        category,
                                        rack,
                                        isScheduleH,
                                        isScheduleH1,
                                        batchNumber,
                                        expiryDate,
                                        quantityStr.toIntOrNull() ?: 0,
                                        purchasePriceStr.toDoubleOrNull() ?: 0.0,
                                        mrpStr.toDoubleOrNull() ?: 0.0,
                                        sellingPriceStr.toDoubleOrNull() ?: 0.0
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier.testTag("save_medicine_button")
                        ) {
                            Text("Save Medicine")
                        }
                    }
                }
            }
        }
    }
}
