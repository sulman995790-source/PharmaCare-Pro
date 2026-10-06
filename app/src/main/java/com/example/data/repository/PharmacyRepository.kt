package com.example.data.repository

import com.example.data.local.dao.PharmacyDao
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerTransaction
import com.example.data.local.entity.Medicine
import com.example.data.local.entity.MedicineBatch
import com.example.data.local.entity.PurchaseBill
import com.example.data.local.entity.SaleInvoice
import com.example.data.local.entity.SaleInvoiceItem
import kotlinx.coroutines.flow.Flow

class PharmacyRepository(private val dao: PharmacyDao) {

    // --- Medicines ---
    val allMedicines: Flow<List<Medicine>> = dao.getAllMedicines()

    fun searchMedicines(query: String): Flow<List<Medicine>> {
        return if (query.isBlank()) {
            dao.getAllMedicines()
        } else {
            dao.searchMedicines(query.trim())
        }
    }

    fun getMedicinesByGeneric(generic: String): Flow<List<Medicine>> {
        return dao.getMedicinesByGeneric(generic.trim())
    }

    suspend fun getMedicineById(id: Long): Medicine? = dao.getMedicineById(id)

    suspend fun addMedicine(medicine: Medicine): Long = dao.insertMedicine(medicine)

    suspend fun updateMedicine(medicine: Medicine) = dao.updateMedicine(medicine)

    suspend fun deleteMedicine(medicine: Medicine) = dao.deleteMedicine(medicine)

    // --- Batches ---
    val allBatches: Flow<List<MedicineBatch>> = dao.getAllBatches()

    fun getBatchesForMedicine(medicineId: Long): Flow<List<MedicineBatch>> {
        return dao.getBatchesForMedicine(medicineId)
    }

    suspend fun getBatchesForMedicineFEFO(medicineId: Long): List<MedicineBatch> {
        return dao.getBatchesForMedicineFEFO(medicineId)
    }

    suspend fun addBatch(batch: MedicineBatch): Long = dao.insertBatch(batch)

    suspend fun updateBatch(batch: MedicineBatch) = dao.updateBatch(batch)

    suspend fun deleteBatch(batch: MedicineBatch) = dao.deleteBatch(batch)

    suspend fun adjustBatchStock(batchId: Long, newQuantity: Int) {
        val existing = dao.getBatchById(batchId) ?: return
        dao.updateBatch(existing.copy(quantity = newQuantity))
    }

    // --- Customers & Udhar Khata ---
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<Customer>> {
        return if (query.isBlank()) {
            dao.getAllCustomers()
        } else {
            dao.searchCustomers(query.trim())
        }
    }

    suspend fun getCustomerById(id: Long): Customer? = dao.getCustomerById(id)

    suspend fun addCustomer(customer: Customer): Long = dao.insertCustomer(customer)

    fun getCustomerTransactions(customerId: Long): Flow<List<CustomerTransaction>> {
        return dao.getCustomerTransactions(customerId)
    }

    suspend fun recordCustomerPayment(
        customerId: Long,
        amount: Double,
        paymentMode: String,
        notes: String
    ) {
        val customer = dao.getCustomerById(customerId) ?: return
        val newOutstanding = (customer.currentOutstanding - amount).coerceAtLeast(0.0)
        val now = System.currentTimeMillis()
        dao.updateCustomerOutstanding(customerId, newOutstanding, now)
        dao.insertCustomerTransaction(
            CustomerTransaction(
                customerId = customerId,
                type = "PAYMENT_RECEIVED",
                amount = amount,
                paymentMode = paymentMode,
                notes = notes,
                timestamp = now
            )
        )
    }

    // --- Sales / Invoicing ---
    val allInvoices: Flow<List<SaleInvoice>> = dao.getAllInvoices()

    val scheduleH1Invoices: Flow<List<SaleInvoice>> = dao.getScheduleH1Invoices()

    fun getInvoiceItems(invoiceId: Long): Flow<List<SaleInvoiceItem>> {
        return dao.getInvoiceItems(invoiceId)
    }

    suspend fun executeCheckout(
        invoice: SaleInvoice,
        items: List<SaleInvoiceItem>,
        customerId: Long? = null
    ): Long {
        return dao.executeSaleCheckout(invoice, items, customerId)
    }

    // --- Purchase Bills ---
    val allPurchaseBills: Flow<List<PurchaseBill>> = dao.getAllPurchaseBills()

    suspend fun addPurchaseBill(
        bill: PurchaseBill,
        batches: List<MedicineBatch>
    ): Long {
        val billId = dao.insertPurchaseBill(bill)
        dao.insertBatches(batches)
        return billId
    }
}
