package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerTransaction
import com.example.data.local.entity.Medicine
import com.example.data.local.entity.MedicineBatch
import com.example.data.local.entity.PurchaseBill
import com.example.data.local.entity.SaleInvoice
import com.example.data.local.entity.SaleInvoiceItem
import com.example.data.repository.PharmacyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CartItem(
    val medicine: Medicine,
    val batch: MedicineBatch,
    val quantity: Int,
    val unitPrice: Double,
    val discountPercent: Double = 10.0,
    val gstRate: Double = medicine.gstRate
) {
    val lineSubtotal: Double get() = unitPrice * quantity
    val lineDiscount: Double get() = lineSubtotal * (discountPercent / 100.0)
    val lineTaxable: Double get() = lineSubtotal - lineDiscount
    val lineGst: Double get() = lineTaxable * (gstRate / 100.0)
    val lineTotal: Double get() = lineTaxable + lineGst
}

enum class InventoryFilter {
    ALL,
    LOW_STOCK,
    EXPIRING_SOON,
    EXPIRED,
    SCHEDULE_H1
}

data class MedicineItemUi(
    val medicine: Medicine,
    val batches: List<MedicineBatch>,
    val totalStock: Int,
    val isLowStock: Boolean,
    val hasExpiringSoon: Boolean,
    val hasExpired: Boolean,
    val earliestBatch: MedicineBatch?
)

class PharmacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PharmacyRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = PharmacyRepository(database.pharmacyDao())
    }

    // --- Search Queries ---
    val medicineSearchQuery = MutableStateFlow("")
    val inventorySearchQuery = MutableStateFlow("")
    val inventoryFilter = MutableStateFlow(InventoryFilter.ALL)
    val substituteSearchQuery = MutableStateFlow("")
    val khataSearchQuery = MutableStateFlow("")

    // Raw sources
    val allMedicines: StateFlow<List<Medicine>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBatches: StateFlow<List<MedicineBatch>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<SaleInvoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleH1Invoices: StateFlow<List<SaleInvoice>> = repository.scheduleH1Invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchases: StateFlow<List<PurchaseBill>> = repository.allPurchaseBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Inventory Combined UI Flow ---
    val inventoryList: StateFlow<List<MedicineItemUi>> = combine(
        allMedicines,
        allBatches,
        inventorySearchQuery,
        inventoryFilter
    ) { meds, batches, query, filter ->
        val now = System.currentTimeMillis()
        val threeMonthsLater = now + (90L * 24 * 60 * 60 * 1000)

        val batchMap = batches.groupBy { it.medicineId }

        val items = meds.map { med ->
            val medBatches = batchMap[med.id] ?: emptyList()
            val totalStock = medBatches.sumOf { it.quantity }
            val isLowStock = totalStock <= med.minStockAlert
            val hasExpired = medBatches.any { it.expiryTimestamp <= now }
            val hasExpiringSoon = medBatches.any { it.expiryTimestamp in (now + 1)..threeMonthsLater }
            val earliest = medBatches.filter { it.quantity > 0 }.minByOrNull { it.expiryTimestamp }

            MedicineItemUi(
                medicine = med,
                batches = medBatches,
                totalStock = totalStock,
                isLowStock = isLowStock,
                hasExpiringSoon = hasExpiringSoon,
                hasExpired = hasExpired,
                earliestBatch = earliest
            )
        }

        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.medicine.name.contains(query, ignoreCase = true) ||
                item.medicine.genericName.contains(query, ignoreCase = true) ||
                item.medicine.manufacturer.contains(query, ignoreCase = true) ||
                item.medicine.rackLocation.contains(query, ignoreCase = true)

            if (!matchesQuery) return@filter false

            when (filter) {
                InventoryFilter.ALL -> true
                InventoryFilter.LOW_STOCK -> item.isLowStock
                InventoryFilter.EXPIRING_SOON -> item.hasExpiringSoon
                InventoryFilter.EXPIRED -> item.hasExpired
                InventoryFilter.SCHEDULE_H1 -> item.medicine.isScheduleH1
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- POS Billing Cart State ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val doctorName = MutableStateFlow("")
    val paymentMode = MutableStateFlow("Cash") // Cash, UPI, Card, Udhar
    val selectedCustomerId = MutableStateFlow<Long?>(null)

    // Last generated invoice for modal preview / sharing
    private val _lastGeneratedInvoice = MutableStateFlow<SaleInvoice?>(null)
    val lastGeneratedInvoice: StateFlow<SaleInvoice?> = _lastGeneratedInvoice.asStateFlow()

    private val _lastGeneratedItems = MutableStateFlow<List<SaleInvoiceItem>>(emptyList())
    val lastGeneratedItems: StateFlow<List<SaleInvoiceItem>> = _lastGeneratedItems.asStateFlow()

    val isInvoiceModalOpen = MutableStateFlow(false)

    fun addToCart(medicine: Medicine, chosenBatch: MedicineBatch? = null, qty: Int = 1) {
        viewModelScope.launch {
            val batch = chosenBatch ?: run {
                val fefoBatches = repository.getBatchesForMedicineFEFO(medicine.id)
                fefoBatches.firstOrNull { it.quantity > 0 }
            }

            if (batch == null) return@launch

            val current = _cartItems.value.toMutableList()
            val existingIndex = current.indexOfFirst { it.batch.id == batch.id }

            if (existingIndex >= 0) {
                val existing = current[existingIndex]
                val newQty = (existing.quantity + qty).coerceAtMost(batch.quantity)
                current[existingIndex] = existing.copy(quantity = newQty)
            } else {
                val initialQty = qty.coerceAtMost(batch.quantity)
                current.add(
                    CartItem(
                        medicine = medicine,
                        batch = batch,
                        quantity = initialQty,
                        unitPrice = batch.sellingPrice,
                        discountPercent = 10.0,
                        gstRate = medicine.gstRate
                    )
                )
            }
            _cartItems.value = current
        }
    }

    fun updateCartQuantity(index: Int, newQty: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                val capped = newQty.coerceAtMost(item.batch.quantity)
                current[index] = item.copy(quantity = capped)
            }
            _cartItems.value = current
        }
    }

    fun updateCartDiscount(index: Int, discount: Double) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(discountPercent = discount.coerceIn(0.0, 50.0))
            _cartItems.value = current
        }
    }

    fun removeFromCart(index: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        customerName.value = ""
        customerPhone.value = ""
        doctorName.value = ""
        paymentMode.value = "Cash"
        selectedCustomerId.value = null
    }

    fun checkout(onSuccess: (SaleInvoice) -> Unit = {}) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val totalSubtotal = items.sumOf { it.lineSubtotal }
        val totalDiscount = items.sumOf { it.lineDiscount }
        val totalGst = items.sumOf { it.lineGst }
        val totalFinal = items.sumOf { it.lineTotal }
        val hasH1 = items.any { it.medicine.isScheduleH1 }

        val invoiceNumber = "LW-${Calendar.getInstance().get(Calendar.YEAR)}-${(1000..9999).random()}"
        val custName = customerName.value.ifBlank { "Walk-in Customer" }

        val invoice = SaleInvoice(
            invoiceNumber = invoiceNumber,
            timestamp = System.currentTimeMillis(),
            customerName = custName,
            customerPhone = customerPhone.value,
            doctorName = doctorName.value.ifBlank { "Self / OTC" },
            paymentMode = paymentMode.value,
            subtotal = totalSubtotal,
            discountAmount = totalDiscount,
            gstAmount = totalGst,
            totalAmount = totalFinal,
            itemsCount = items.size,
            hasScheduleH1 = hasH1
        )

        val invoiceItems = items.map { cart ->
            SaleInvoiceItem(
                invoiceId = 0,
                medicineId = cart.medicine.id,
                batchId = cart.batch.id,
                medicineName = cart.medicine.name,
                batchNumber = cart.batch.batchNumber,
                expiryDate = cart.batch.expiryDate,
                quantity = cart.quantity,
                unitPrice = cart.unitPrice,
                mrp = cart.batch.mrp,
                discountPercent = cart.discountPercent,
                gstRate = cart.gstRate,
                totalPrice = cart.lineTotal
            )
        }

        viewModelScope.launch {
            val invoiceId = repository.executeCheckout(
                invoice = invoice,
                items = invoiceItems,
                customerId = selectedCustomerId.value
            )
            val completeInvoice = invoice.copy(id = invoiceId)
            _lastGeneratedInvoice.value = completeInvoice
            _lastGeneratedItems.value = invoiceItems
            isInvoiceModalOpen.value = true
            clearCart()
            onSuccess(completeInvoice)
        }
    }

    fun closeInvoiceModal() {
        isInvoiceModalOpen.value = false
    }

    // --- Substitutes / Generic Finder State ---
    val substituteSearchResults: StateFlow<List<MedicineItemUi>> = combine(
        substituteSearchQuery,
        inventoryList
    ) { query, items ->
        if (query.isBlank()) {
            items.take(8)
        } else {
            // Find matches by brand name OR active salt/generic
            val cleanQuery = query.trim().lowercase()
            items.filter { item ->
                item.medicine.name.lowercase().contains(cleanQuery) ||
                item.medicine.genericName.lowercase().contains(cleanQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun findSubstitutesForGeneric(genericName: String): List<MedicineItemUi> {
        val currentList = inventoryList.value
        val saltKeywords = genericName.split("+", " ", "mg", "mcg")
            .map { it.trim().lowercase() }
            .filter { it.length > 2 }

        return currentList.filter { item ->
            saltKeywords.any { kw -> item.medicine.genericName.lowercase().contains(kw) }
        }
    }

    // --- Udhar Khata State & Actions ---
    val selectedCustomer = MutableStateFlow<Customer?>(null)

    val selectedCustomerTransactions: StateFlow<List<CustomerTransaction>> = selectedCustomer
        .flatMapLatest { cust ->
            if (cust == null) MutableStateFlow(emptyList())
            else repository.getCustomerTransactions(cust.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCustomer(customer: Customer?) {
        selectedCustomer.value = customer
    }

    fun recordCustomerPayment(amount: Double, mode: String, note: String) {
        val cust = selectedCustomer.value ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            repository.recordCustomerPayment(
                customerId = cust.id,
                amount = amount,
                paymentMode = mode,
                notes = note
            )
            // Refresh selected customer
            val updated = repository.getCustomerById(cust.id)
            selectedCustomer.value = updated
        }
    }

    fun addNewCustomer(name: String, phone: String, creditLimit: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCustomer(
                Customer(
                    name = name.trim(),
                    phoneNumber = phone.trim(),
                    creditLimit = creditLimit,
                    currentOutstanding = 0.0,
                    lastTransactionDate = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Inventory Add / Edit Actions ---
    fun addNewMedicineWithBatch(
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
    ) {
        viewModelScope.launch {
            val medId = repository.addMedicine(
                Medicine(
                    name = name.trim(),
                    genericName = generic.trim(),
                    manufacturer = manufacturer.trim(),
                    category = category,
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = rackLocation.trim().ifBlank { "Rack A-01" },
                    isScheduleH = isScheduleH,
                    isScheduleH1 = isScheduleH1,
                    minStockAlert = 15
                )
            )

            if (batchNumber.isNotBlank() && quantity > 0) {
                // Parse expiry to timestamp
                val expiryTs = parseExpiryToTimestamp(expiryDate)
                repository.addBatch(
                    MedicineBatch(
                        medicineId = medId,
                        batchNumber = batchNumber.trim().uppercase(),
                        expiryDate = expiryDate.trim(),
                        expiryTimestamp = expiryTs,
                        quantity = quantity,
                        purchasePrice = purchasePrice,
                        mrp = mrp,
                        sellingPrice = sellingPrice
                    )
                )
            }
        }
    }

    fun addNewBatchForMedicine(
        medicineId: Long,
        batchNumber: String,
        expiryDate: String,
        quantity: Int,
        purchasePrice: Double,
        mrp: Double,
        sellingPrice: Double,
        supplierName: String = "Direct Wholesale"
    ) {
        viewModelScope.launch {
            val expiryTs = parseExpiryToTimestamp(expiryDate)
            repository.addBatch(
                MedicineBatch(
                    medicineId = medicineId,
                    batchNumber = batchNumber.trim().uppercase(),
                    expiryDate = expiryDate.trim(),
                    expiryTimestamp = expiryTs,
                    quantity = quantity,
                    purchasePrice = purchasePrice,
                    mrp = mrp,
                    sellingPrice = sellingPrice,
                    supplierName = supplierName.trim()
                )
            )
        }
    }

    fun adjustBatchStock(batchId: Long, newQuantity: Int) {
        viewModelScope.launch {
            repository.adjustBatchStock(batchId, newQuantity)
        }
    }

    // --- Purchase Bill Addition ---
    fun addPurchaseBill(
        distributor: String,
        billNumber: String,
        items: List<Pair<Long, MedicineBatch>>
    ) {
        viewModelScope.launch {
            val totalAmount = items.sumOf { it.second.purchasePrice * it.second.quantity }
            val bill = PurchaseBill(
                billNumber = billNumber.ifBlank { "PB-${System.currentTimeMillis() % 10000}" },
                distributorName = distributor.ifBlank { "Direct Distributor" },
                distributorGstin = "07AAAAA0000A1Z5",
                invoiceDate = System.currentTimeMillis(),
                totalAmount = totalAmount,
                itemsCount = items.size
            )
            repository.addPurchaseBill(bill, items.map { it.second })
        }
    }

    private fun parseExpiryToTimestamp(expiryStr: String): Long {
        return try {
            val parts = expiryStr.trim().split("/", "-")
            val month = if (parts.size >= 2) parts[0].toIntOrNull() ?: 1 else 1
            val year = if (parts.size >= 2) parts[1].toIntOrNull() ?: 2026 else 2026
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, if (year < 100) 2000 + year else year)
            cal.set(Calendar.MONTH, (month - 1).coerceIn(0, 11))
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
        }
    }
}
