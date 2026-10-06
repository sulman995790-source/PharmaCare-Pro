package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerTransaction
import com.example.data.local.entity.Medicine
import com.example.data.local.entity.MedicineBatch
import com.example.data.local.entity.PurchaseBill
import com.example.data.local.entity.SaleInvoice
import com.example.data.local.entity.SaleInvoiceItem
import kotlinx.coroutines.flow.Flow

data class MedicineWithStock(
    val id: Long,
    val name: String,
    val genericName: String,
    val manufacturer: String,
    val category: String,
    val hsnCode: String,
    val gstRate: Double,
    val rackLocation: String,
    val isScheduleH: Boolean,
    val isScheduleH1: Boolean,
    val minStockAlert: Int,
    val totalStock: Int,
    val earliestExpiry: Long?,
    val lowestSellingPrice: Double?
)

@Dao
interface PharmacyDao {
    // --- Medicines ---
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    suspend fun getMedicineById(id: Long): Medicine?

    @Query("""
        SELECT * FROM medicines 
        WHERE name LIKE '%' || :query || '%' 
           OR genericName LIKE '%' || :query || '%'
           OR manufacturer LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchMedicines(query: String): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE genericName LIKE '%' || :genericName || '%' ORDER BY name ASC")
    fun getMedicinesByGeneric(genericName: String): Flow<List<Medicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<Medicine>): List<Long>

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    // --- Batches ---
    @Query("SELECT * FROM medicine_batches ORDER BY expiryTimestamp ASC")
    fun getAllBatches(): Flow<List<MedicineBatch>>

    @Query("SELECT * FROM medicine_batches WHERE medicineId = :medicineId AND quantity > 0 ORDER BY expiryTimestamp ASC")
    fun getBatchesForMedicine(medicineId: Long): Flow<List<MedicineBatch>>

    @Query("SELECT * FROM medicine_batches WHERE medicineId = :medicineId AND quantity > 0 ORDER BY expiryTimestamp ASC")
    suspend fun getBatchesForMedicineFEFO(medicineId: Long): List<MedicineBatch>

    @Query("SELECT * FROM medicine_batches WHERE id = :batchId LIMIT 1")
    suspend fun getBatchById(batchId: Long): MedicineBatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: MedicineBatch): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<MedicineBatch>): List<Long>

    @Update
    suspend fun updateBatch(batch: MedicineBatch)

    @Query("UPDATE medicine_batches SET quantity = quantity - :qty WHERE id = :batchId")
    suspend fun decrementBatchStock(batchId: Long, qty: Int)

    @Query("UPDATE medicine_batches SET quantity = quantity + :qty WHERE id = :batchId")
    suspend fun incrementBatchStock(batchId: Long, qty: Int)

    @Delete
    suspend fun deleteBatch(batch: MedicineBatch)

    // --- Customers & Udhar Khata ---
    @Query("SELECT * FROM customers ORDER BY currentOutstanding DESC, name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>): List<Long>

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET currentOutstanding = :newBalance, lastTransactionDate = :timestamp WHERE id = :customerId")
    suspend fun updateCustomerOutstanding(customerId: Long, newBalance: Double, timestamp: Long)

    @Query("SELECT * FROM customer_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getCustomerTransactions(customerId: Long): Flow<List<CustomerTransaction>>

    @Query("SELECT * FROM customer_transactions ORDER BY timestamp DESC")
    fun getAllCustomerTransactions(): Flow<List<CustomerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerTransaction(transaction: CustomerTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerTransactions(transactions: List<CustomerTransaction>): List<Long>

    // --- Sales & Invoices ---
    @Query("SELECT * FROM sale_invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<SaleInvoice>>

    @Query("SELECT * FROM sale_invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): SaleInvoice?

    @Query("SELECT * FROM sale_invoices WHERE hasScheduleH1 = 1 ORDER BY timestamp DESC")
    fun getScheduleH1Invoices(): Flow<List<SaleInvoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: SaleInvoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<SaleInvoiceItem>): List<Long>

    @Query("SELECT * FROM sale_invoice_items WHERE invoiceId = :invoiceId")
    fun getInvoiceItems(invoiceId: Long): Flow<List<SaleInvoiceItem>>

    @Query("SELECT * FROM sale_invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getInvoiceItemsSync(invoiceId: Long): List<SaleInvoiceItem>

    @Query("SELECT * FROM sale_invoice_items ORDER BY id DESC")
    fun getAllInvoiceItems(): Flow<List<SaleInvoiceItem>>

    // --- Purchase Bills ---
    @Query("SELECT * FROM purchase_bills ORDER BY invoiceDate DESC")
    fun getAllPurchaseBills(): Flow<List<PurchaseBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseBill(bill: PurchaseBill): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseBills(bills: List<PurchaseBill>): List<Long>

    // Complete Checkout Transaction
    @Transaction
    suspend fun executeSaleCheckout(
        invoice: SaleInvoice,
        items: List<SaleInvoiceItem>,
        customerId: Long? = null
    ): Long {
        val invoiceId = insertInvoice(invoice)
        val itemsWithInvoice = items.map { it.copy(invoiceId = invoiceId) }
        insertInvoiceItems(itemsWithInvoice)

        // Decrement stock in batches
        for (item in itemsWithInvoice) {
            decrementBatchStock(item.batchId, item.quantity)
        }

        // If Udhar, add to customer outstanding
        if (invoice.paymentMode.equals("Udhar", ignoreCase = true) && customerId != null) {
            val customer = getCustomerById(customerId)
            if (customer != null) {
                val newOutstanding = customer.currentOutstanding + invoice.totalAmount
                updateCustomerOutstanding(customerId, newOutstanding, invoice.timestamp)
                insertCustomerTransaction(
                    CustomerTransaction(
                        customerId = customerId,
                        type = "CREDIT_SALE",
                        amount = invoice.totalAmount,
                        invoiceId = invoiceId,
                        paymentMode = "Udhar",
                        notes = "Bill #${invoice.invoiceNumber}",
                        timestamp = invoice.timestamp
                    )
                )
            }
        }

        return invoiceId
    }
}
