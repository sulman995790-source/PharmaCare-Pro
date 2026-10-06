package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val genericName: String, // Salt / Composition (e.g. Paracetamol 650mg)
    val manufacturer: String,
    val category: String, // Tablet, Syrup, Ointment, Capsule, Injection, Drops
    val hsnCode: String,
    val gstRate: Double, // 5.0, 12.0, 18.0
    val rackLocation: String, // e.g. "Rack A-02", "Cold Chain 2-8°C"
    val isScheduleH: Boolean = false,
    val isScheduleH1: Boolean = false,
    val minStockAlert: Int = 15
)

@Entity(
    tableName = "medicine_batches",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId"), Index("expiryTimestamp")]
)
data class MedicineBatch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val batchNumber: String,
    val expiryDate: String, // e.g. "12/2026"
    val expiryTimestamp: Long, // timestamp for accurate FEFO sorting
    val quantity: Int, // Current units/strips in stock
    val purchasePrice: Double,
    val mrp: Double,
    val sellingPrice: Double,
    val supplierName: String = "Direct Wholesale"
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val creditLimit: Double = 5000.0,
    val currentOutstanding: Double = 0.0, // Udhar dues
    val lastTransactionDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customer_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class CustomerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: String, // "CREDIT_SALE" (Udhar added) or "PAYMENT_RECEIVED" (Udhar cleared)
    val amount: Double,
    val invoiceId: Long? = null,
    val paymentMode: String = "Udhar", // "Cash", "UPI", "Udhar"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_invoices")
data class SaleInvoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String, // e.g. LW-2026-1001
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String,
    val customerPhone: String = "",
    val doctorName: String = "",
    val paymentMode: String, // "Cash", "UPI", "Card", "Udhar"
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val gstAmount: Double,
    val totalAmount: Double,
    val itemsCount: Int,
    val hasScheduleH1: Boolean = false
)

@Entity(
    tableName = "sale_invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleInvoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class SaleInvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val medicineId: Long,
    val batchId: Long,
    val medicineName: String,
    val batchNumber: String,
    val expiryDate: String,
    val quantity: Int,
    val unitPrice: Double,
    val mrp: Double,
    val discountPercent: Double,
    val gstRate: Double,
    val totalPrice: Double
)

@Entity(tableName = "purchase_bills")
data class PurchaseBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billNumber: String,
    val distributorName: String,
    val distributorGstin: String,
    val invoiceDate: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val itemsCount: Int,
    val status: String = "Verified"
)
