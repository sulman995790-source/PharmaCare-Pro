package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.PharmacyDao
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerTransaction
import com.example.data.local.entity.Medicine
import com.example.data.local.entity.MedicineBatch
import com.example.data.local.entity.PurchaseBill
import com.example.data.local.entity.SaleInvoice
import com.example.data.local.entity.SaleInvoiceItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        Medicine::class,
        MedicineBatch::class,
        Customer::class,
        CustomerTransaction::class,
        SaleInvoice::class,
        SaleInvoiceItem::class,
        PurchaseBill::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pharmacyDao(): PharmacyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "localwell_pharmacy_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialPharmacyData(database.pharmacyDao())
                    }
                }
            }
        }

        private suspend fun populateInitialPharmacyData(dao: PharmacyDao) {
            val cal = Calendar.getInstance()

            // 1. Initial Medicines (Real Indian & Global pharmacy bestsellers with true salt compositions & substitutes)
            val medicines = listOf(
                Medicine(
                    id = 1,
                    name = "Dolo 650",
                    genericName = "Paracetamol 650mg",
                    manufacturer = "Micro Labs Ltd",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack A-01",
                    isScheduleH = false,
                    isScheduleH1 = false,
                    minStockAlert = 30
                ),
                Medicine(
                    id = 2,
                    name = "Calpol 650",
                    genericName = "Paracetamol 650mg",
                    manufacturer = "GlaxoSmithKline (GSK)",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack A-02",
                    isScheduleH = false,
                    isScheduleH1 = false,
                    minStockAlert = 25
                ),
                Medicine(
                    id = 3,
                    name = "Pacimol 650",
                    genericName = "Paracetamol 650mg",
                    manufacturer = "Ipca Laboratories",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack A-03",
                    isScheduleH = false,
                    isScheduleH1 = false,
                    minStockAlert = 20
                ),
                Medicine(
                    id = 4,
                    name = "Augmentin 625 Duo",
                    genericName = "Amoxicillin 500mg + Potassium Clavulanate 125mg",
                    manufacturer = "GSK Pharmaceuticals",
                    category = "Tablets",
                    hsnCode = "30041010",
                    gstRate = 12.0,
                    rackLocation = "Rack B-04",
                    isScheduleH = true,
                    isScheduleH1 = true,
                    minStockAlert = 15
                ),
                Medicine(
                    id = 5,
                    name = "Moxikind-CV 625",
                    genericName = "Amoxicillin 500mg + Potassium Clavulanate 125mg",
                    manufacturer = "Mankind Pharma",
                    category = "Tablets",
                    hsnCode = "30041010",
                    gstRate = 12.0,
                    rackLocation = "Rack B-05",
                    isScheduleH = true,
                    isScheduleH1 = true,
                    minStockAlert = 20
                ),
                Medicine(
                    id = 6,
                    name = "Pan 40",
                    genericName = "Pantoprazole 40mg",
                    manufacturer = "Alkem Laboratories",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack C-01",
                    isScheduleH = true,
                    isScheduleH1 = false,
                    minStockAlert = 25
                ),
                Medicine(
                    id = 7,
                    name = "Pantocid 40",
                    genericName = "Pantoprazole 40mg",
                    manufacturer = "Sun Pharma",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack C-02",
                    isScheduleH = true,
                    isScheduleH1 = false,
                    minStockAlert = 20
                ),
                Medicine(
                    id = 8,
                    name = "Azithral 500",
                    genericName = "Azithromycin 500mg",
                    manufacturer = "Alembic Pharmaceuticals",
                    category = "Tablets",
                    hsnCode = "30042010",
                    gstRate = 12.0,
                    rackLocation = "Rack B-10",
                    isScheduleH = true,
                    isScheduleH1 = true,
                    minStockAlert = 10
                ),
                Medicine(
                    id = 9,
                    name = "Azee 500",
                    genericName = "Azithromycin 500mg",
                    manufacturer = "Cipla Ltd",
                    category = "Tablets",
                    hsnCode = "30042010",
                    gstRate = 12.0,
                    rackLocation = "Rack B-11",
                    isScheduleH = true,
                    isScheduleH1 = true,
                    minStockAlert = 15
                ),
                Medicine(
                    id = 10,
                    name = "Telma 40",
                    genericName = "Telmisartan 40mg",
                    manufacturer = "Glenmark Pharmaceuticals",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack D-02",
                    isScheduleH = true,
                    isScheduleH1 = false,
                    minStockAlert = 15
                ),
                Medicine(
                    id = 11,
                    name = "Glycomet 500 SR",
                    genericName = "Metformin Hydrochloride 500mg (SR)",
                    manufacturer = "USV Private Limited",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack D-07",
                    isScheduleH = true,
                    isScheduleH1 = false,
                    minStockAlert = 20
                ),
                Medicine(
                    id = 12,
                    name = "Montair-LC",
                    genericName = "Montelukast 10mg + Levocetirizine 5mg",
                    manufacturer = "Cipla Ltd",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack E-03",
                    isScheduleH = true,
                    isScheduleH1 = false,
                    minStockAlert = 20
                ),
                Medicine(
                    id = 13,
                    name = "Allegra 120mg",
                    genericName = "Fexofenadine 120mg",
                    manufacturer = "Sanofi India",
                    category = "Tablets",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack E-05",
                    isScheduleH = false,
                    isScheduleH1 = false,
                    minStockAlert = 10
                ),
                Medicine(
                    id = 14,
                    name = "Becosules Z",
                    genericName = "Vitamin B-Complex + Vitamin C + Zinc",
                    manufacturer = "Pfizer Ltd",
                    category = "Capsules",
                    hsnCode = "30045020",
                    gstRate = 12.0,
                    rackLocation = "Rack F-01",
                    isScheduleH = false,
                    isScheduleH1 = false,
                    minStockAlert = 30
                ),
                Medicine(
                    id = 15,
                    name = "Shelcal 500",
                    genericName = "Elemental Calcium 500mg + Vitamin D3 250IU",
                    manufacturer = "Torrent Pharmaceuticals",
                    category = "Tablets",
                    hsnCode = "30045020",
                    gstRate = 12.0,
                    rackLocation = "Rack F-04",
                    isScheduleH = false,
                    isScheduleH1 = false,
                    minStockAlert = 25
                ),
                Medicine(
                    id = 16,
                    name = "Asthalin 100 Inhaler",
                    genericName = "Salbutamol 100mcg CFC Free",
                    manufacturer = "Cipla Ltd",
                    category = "Inhaler",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Cold Chain Rack G-02",
                    isScheduleH = true,
                    isScheduleH1 = false,
                    minStockAlert = 8
                ),
                Medicine(
                    id = 17,
                    name = "Betadine 10% Ointment",
                    genericName = "Povidone Iodine 10% w/w",
                    manufacturer = "Win-Medicare",
                    category = "Ointment",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack H-01",
                    isScheduleH = false,
                    isScheduleH1 = false,
                    minStockAlert = 12
                ),
                Medicine(
                    id = 18,
                    name = "Benadryl Cough Syrup",
                    genericName = "Diphenhydramine + Ammonium Chloride",
                    manufacturer = "Johnson & Johnson",
                    category = "Syrup",
                    hsnCode = "30049099",
                    gstRate = 12.0,
                    rackLocation = "Rack S-03",
                    isScheduleH = false,
                    isScheduleH1 = false,
                    minStockAlert = 10
                )
            )
            dao.insertMedicines(medicines)

            // 2. Batches (FEFO Demonstrative - fresh, regular, near-expiry, low-stock)
            fun timestampForMonthsAhead(months: Int): Long {
                val c = Calendar.getInstance()
                c.add(Calendar.MONTH, months)
                return c.timeInMillis
            }

            fun formatMonthYear(months: Int): String {
                val c = Calendar.getInstance()
                c.add(Calendar.MONTH, months)
                val m = c.get(Calendar.MONTH) + 1
                val y = c.get(Calendar.YEAR)
                return "%02d/%d".format(m, y)
            }

            val batches = listOf(
                // Dolo 650 (2 batches: 1 earlier expiring for FEFO, 1 later)
                MedicineBatch(
                    medicineId = 1,
                    batchNumber = "DL24K12",
                    expiryDate = formatMonthYear(2), // Near expiry: 2 months
                    expiryTimestamp = timestampForMonthsAhead(2),
                    quantity = 22,
                    purchasePrice = 24.50,
                    mrp = 34.00,
                    sellingPrice = 30.60,
                    supplierName = "Cipla Wholesale Dist"
                ),
                MedicineBatch(
                    medicineId = 1,
                    batchNumber = "DL25A05",
                    expiryDate = formatMonthYear(16),
                    expiryTimestamp = timestampForMonthsAhead(16),
                    quantity = 80,
                    purchasePrice = 25.00,
                    mrp = 34.00,
                    sellingPrice = 30.60,
                    supplierName = "Apollo Pharma Agency"
                ),
                // Calpol 650 (Substitute to Dolo)
                MedicineBatch(
                    medicineId = 2,
                    batchNumber = "CP25E10",
                    expiryDate = formatMonthYear(14),
                    expiryTimestamp = timestampForMonthsAhead(14),
                    quantity = 45,
                    purchasePrice = 23.80,
                    mrp = 33.50,
                    sellingPrice = 29.50,
                    supplierName = "GSK C&F Agency"
                ),
                // Pacimol 650 (Cheaper generic substitute)
                MedicineBatch(
                    medicineId = 3,
                    batchNumber = "PC25B02",
                    expiryDate = formatMonthYear(18),
                    expiryTimestamp = timestampForMonthsAhead(18),
                    quantity = 60,
                    purchasePrice = 18.00,
                    mrp = 26.00,
                    sellingPrice = 22.00,
                    supplierName = "National Pharma Hub"
                ),
                // Augmentin 625 Duo
                MedicineBatch(
                    medicineId = 4,
                    batchNumber = "AG25F01",
                    expiryDate = formatMonthYear(12),
                    expiryTimestamp = timestampForMonthsAhead(12),
                    quantity = 18,
                    purchasePrice = 168.00,
                    mrp = 223.50,
                    sellingPrice = 205.00,
                    supplierName = "GSK C&F Agency"
                ),
                // Moxikind-CV 625 (Affordable substitute for Augmentin)
                MedicineBatch(
                    medicineId = 5,
                    batchNumber = "MK25D08",
                    expiryDate = formatMonthYear(15),
                    expiryTimestamp = timestampForMonthsAhead(15),
                    quantity = 35,
                    purchasePrice = 135.00,
                    mrp = 185.00,
                    sellingPrice = 165.00,
                    supplierName = "Mankind Trade Depot"
                ),
                // Pan 40
                MedicineBatch(
                    medicineId = 6,
                    batchNumber = "PN24Z11",
                    expiryDate = formatMonthYear(1), // Near expiry: 1 month!
                    expiryTimestamp = timestampForMonthsAhead(1),
                    quantity = 15,
                    purchasePrice = 110.00,
                    mrp = 162.00,
                    sellingPrice = 145.00,
                    supplierName = "Alkem Medico"
                ),
                MedicineBatch(
                    medicineId = 6,
                    batchNumber = "PN25C03",
                    expiryDate = formatMonthYear(13),
                    expiryTimestamp = timestampForMonthsAhead(13),
                    quantity = 50,
                    purchasePrice = 112.00,
                    mrp = 162.00,
                    sellingPrice = 145.00,
                    supplierName = "Alkem Medico"
                ),
                // Pantocid 40
                MedicineBatch(
                    medicineId = 7,
                    batchNumber = "PT25G09",
                    expiryDate = formatMonthYear(14),
                    expiryTimestamp = timestampForMonthsAhead(14),
                    quantity = 40,
                    purchasePrice = 120.00,
                    mrp = 175.00,
                    sellingPrice = 158.00,
                    supplierName = "Sun Pharma Distributor"
                ),
                // Azithral 500 (Schedule H1)
                MedicineBatch(
                    medicineId = 8,
                    batchNumber = "AZ25J04",
                    expiryDate = formatMonthYear(11),
                    expiryTimestamp = timestampForMonthsAhead(11),
                    quantity = 6, // Low stock alert!
                    purchasePrice = 95.00,
                    mrp = 138.00,
                    sellingPrice = 125.00,
                    supplierName = "Alembic C&F"
                ),
                // Azee 500
                MedicineBatch(
                    medicineId = 9,
                    batchNumber = "ZE25K07",
                    expiryDate = formatMonthYear(15),
                    expiryTimestamp = timestampForMonthsAhead(15),
                    quantity = 28,
                    purchasePrice = 92.00,
                    mrp = 135.00,
                    sellingPrice = 120.00,
                    supplierName = "Cipla Wholesale Dist"
                ),
                // Telma 40
                MedicineBatch(
                    medicineId = 10,
                    batchNumber = "TL25H02",
                    expiryDate = formatMonthYear(18),
                    expiryTimestamp = timestampForMonthsAhead(18),
                    quantity = 42,
                    purchasePrice = 175.00,
                    mrp = 245.00,
                    sellingPrice = 220.00,
                    supplierName = "Glenmark Central"
                ),
                // Glycomet 500 SR
                MedicineBatch(
                    medicineId = 11,
                    batchNumber = "GM25L06",
                    expiryDate = formatMonthYear(20),
                    expiryTimestamp = timestampForMonthsAhead(20),
                    quantity = 90,
                    purchasePrice = 32.00,
                    mrp = 48.00,
                    sellingPrice = 42.00,
                    supplierName = "USV Distributors"
                ),
                // Montair-LC
                MedicineBatch(
                    medicineId = 12,
                    batchNumber = "ML25M01",
                    expiryDate = formatMonthYear(14),
                    expiryTimestamp = timestampForMonthsAhead(14),
                    quantity = 30,
                    purchasePrice = 180.00,
                    mrp = 255.00,
                    sellingPrice = 230.00,
                    supplierName = "Cipla Wholesale Dist"
                ),
                // Allegra 120
                MedicineBatch(
                    medicineId = 13,
                    batchNumber = "AL25P03",
                    expiryDate = formatMonthYear(17),
                    expiryTimestamp = timestampForMonthsAhead(17),
                    quantity = 24,
                    purchasePrice = 160.00,
                    mrp = 228.00,
                    sellingPrice = 205.00,
                    supplierName = "Sanofi India Agency"
                ),
                // Becosules Z
                MedicineBatch(
                    medicineId = 14,
                    batchNumber = "BZ25R08",
                    expiryDate = formatMonthYear(22),
                    expiryTimestamp = timestampForMonthsAhead(22),
                    quantity = 110,
                    purchasePrice = 38.00,
                    mrp = 54.00,
                    sellingPrice = 48.00,
                    supplierName = "Pfizer Healthcare"
                ),
                // Shelcal 500
                MedicineBatch(
                    medicineId = 15,
                    batchNumber = "SH25S09",
                    expiryDate = formatMonthYear(19),
                    expiryTimestamp = timestampForMonthsAhead(19),
                    quantity = 55,
                    purchasePrice = 98.00,
                    mrp = 145.00,
                    sellingPrice = 130.00,
                    supplierName = "Torrent Pharma Dist"
                ),
                // Asthalin Inhaler
                MedicineBatch(
                    medicineId = 16,
                    batchNumber = "AS25T01",
                    expiryDate = formatMonthYear(10),
                    expiryTimestamp = timestampForMonthsAhead(10),
                    quantity = 14,
                    purchasePrice = 118.00,
                    mrp = 165.00,
                    sellingPrice = 150.00,
                    supplierName = "Cipla Wholesale Dist"
                ),
                // Betadine
                MedicineBatch(
                    medicineId = 17,
                    batchNumber = "BT25U04",
                    expiryDate = formatMonthYear(24),
                    expiryTimestamp = timestampForMonthsAhead(24),
                    quantity = 25,
                    purchasePrice = 85.00,
                    mrp = 125.00,
                    sellingPrice = 112.00,
                    supplierName = "Win-Medicare Direct"
                ),
                // Benadryl
                MedicineBatch(
                    medicineId = 18,
                    batchNumber = "BN25V02",
                    expiryDate = formatMonthYear(16),
                    expiryTimestamp = timestampForMonthsAhead(16),
                    quantity = 18,
                    purchasePrice = 95.00,
                    mrp = 139.00,
                    sellingPrice = 128.00,
                    supplierName = "City Healthcare Wholesale"
                )
            )
            dao.insertBatches(batches)

            // 3. Customers (Digital Udhar Khata)
            val customers = listOf(
                Customer(
                    id = 1,
                    name = "Rajesh Kumar (Gupta Ji)",
                    phoneNumber = "+91 98234 11223",
                    creditLimit = 5000.0,
                    currentOutstanding = 1420.0,
                    lastTransactionDate = System.currentTimeMillis() - 86400000L * 2
                ),
                Customer(
                    id = 2,
                    name = "Sunita Sharma",
                    phoneNumber = "+91 94150 99881",
                    creditLimit = 3000.0,
                    currentOutstanding = 650.0,
                    lastTransactionDate = System.currentTimeMillis() - 86400000L * 4
                ),
                Customer(
                    id = 3,
                    name = "Dr. Amit Patel (Clinic)",
                    phoneNumber = "+91 98765 43210",
                    creditLimit = 15000.0,
                    currentOutstanding = 3850.0,
                    lastTransactionDate = System.currentTimeMillis() - 86400000L * 1
                ),
                Customer(
                    id = 4,
                    name = "Vikas Verma",
                    phoneNumber = "+91 98112 55667",
                    creditLimit = 2500.0,
                    currentOutstanding = 0.0,
                    lastTransactionDate = System.currentTimeMillis() - 86400000L * 8
                ),
                Customer(
                    id = 5,
                    name = "Meena Joshi",
                    phoneNumber = "+91 97654 33221",
                    creditLimit = 4000.0,
                    currentOutstanding = 940.0,
                    lastTransactionDate = System.currentTimeMillis() - 86400000L * 5
                )
            )
            dao.insertCustomers(customers)

            // Customer Transactions
            val transactions = listOf(
                CustomerTransaction(
                    customerId = 1,
                    type = "CREDIT_SALE",
                    amount = 1420.0,
                    invoiceId = 1,
                    paymentMode = "Udhar",
                    notes = "Monthly diabetes & BP medicines on credit",
                    timestamp = System.currentTimeMillis() - 86400000L * 2
                ),
                CustomerTransaction(
                    customerId = 2,
                    type = "CREDIT_SALE",
                    amount = 1150.0,
                    invoiceId = 2,
                    paymentMode = "Udhar",
                    notes = "Antibiotic course bill",
                    timestamp = System.currentTimeMillis() - 86400000L * 7
                ),
                CustomerTransaction(
                    customerId = 2,
                    type = "PAYMENT_RECEIVED",
                    amount = 500.0,
                    invoiceId = null,
                    paymentMode = "UPI",
                    notes = "GPay received partial payment",
                    timestamp = System.currentTimeMillis() - 86400000L * 4
                ),
                CustomerTransaction(
                    customerId = 3,
                    type = "CREDIT_SALE",
                    amount = 3850.0,
                    invoiceId = 3,
                    paymentMode = "Udhar",
                    notes = "Clinic weekly emergency stock",
                    timestamp = System.currentTimeMillis() - 86400000L * 1
                ),
                CustomerTransaction(
                    customerId = 5,
                    type = "CREDIT_SALE",
                    amount = 940.0,
                    invoiceId = null,
                    paymentMode = "Udhar",
                    notes = "Asthma inhaler and vitamins",
                    timestamp = System.currentTimeMillis() - 86400000L * 5
                )
            )
            dao.insertCustomerTransactions(transactions)

            // 4. Sample Sales Invoices for reporting & POS verification
            val now = System.currentTimeMillis()
            val invoices = listOf(
                SaleInvoice(
                    id = 1,
                    invoiceNumber = "LW-2026-1001",
                    timestamp = now - 3600000L * 4,
                    customerName = "Rajesh Kumar (Gupta Ji)",
                    customerPhone = "+91 98234 11223",
                    doctorName = "Dr. R. K. Saxena (MD)",
                    paymentMode = "Udhar",
                    subtotal = 1450.0,
                    discountAmount = 182.0,
                    gstAmount = 152.0,
                    totalAmount = 1420.0,
                    itemsCount = 3,
                    hasScheduleH1 = false
                ),
                SaleInvoice(
                    id = 2,
                    invoiceNumber = "LW-2026-1002",
                    timestamp = now - 3600000L * 3,
                    customerName = "Anil Tiwari",
                    customerPhone = "+91 99345 67890",
                    doctorName = "Dr. Mehra (ENT)",
                    paymentMode = "UPI",
                    subtotal = 380.0,
                    discountAmount = 30.0,
                    gstAmount = 42.0,
                    totalAmount = 392.0,
                    itemsCount = 2,
                    hasScheduleH1 = true
                ),
                SaleInvoice(
                    id = 3,
                    invoiceNumber = "LW-2026-1003",
                    timestamp = now - 3600000L * 1,
                    customerName = "Pooja Singhania",
                    customerPhone = "+91 98110 33445",
                    doctorName = "Self / OTC",
                    paymentMode = "Cash",
                    subtotal = 210.0,
                    discountAmount = 20.0,
                    gstAmount = 22.8,
                    totalAmount = 212.8,
                    itemsCount = 2,
                    hasScheduleH1 = false
                )
            )
            dao.insertInvoice(invoices[0])
            dao.insertInvoice(invoices[1])
            dao.insertInvoice(invoices[2])

            val invoiceItems = listOf(
                SaleInvoiceItem(
                    invoiceId = 1,
                    medicineId = 10,
                    batchId = 12,
                    medicineName = "Telma 40",
                    batchNumber = "TL25H02",
                    expiryDate = formatMonthYear(18),
                    quantity = 2,
                    unitPrice = 220.0,
                    mrp = 245.0,
                    discountPercent = 10.0,
                    gstRate = 12.0,
                    totalPrice = 440.0
                ),
                SaleInvoiceItem(
                    invoiceId = 1,
                    medicineId = 11,
                    batchId = 13,
                    medicineName = "Glycomet 500 SR",
                    batchNumber = "GM25L06",
                    expiryDate = formatMonthYear(20),
                    quantity = 4,
                    unitPrice = 42.0,
                    mrp = 48.0,
                    discountPercent = 12.5,
                    gstRate = 12.0,
                    totalPrice = 168.0
                ),
                SaleInvoiceItem(
                    invoiceId = 2,
                    medicineId = 8,
                    batchId = 10,
                    medicineName = "Azithral 500",
                    batchNumber = "AZ25J04",
                    expiryDate = formatMonthYear(11),
                    quantity = 1,
                    unitPrice = 125.0,
                    mrp = 138.0,
                    discountPercent = 9.4,
                    gstRate = 12.0,
                    totalPrice = 125.0
                ),
                SaleInvoiceItem(
                    invoiceId = 3,
                    medicineId = 1,
                    batchId = 1,
                    medicineName = "Dolo 650",
                    batchNumber = "DL24K12",
                    expiryDate = formatMonthYear(2),
                    quantity = 2,
                    unitPrice = 30.60,
                    mrp = 34.0,
                    discountPercent = 10.0,
                    gstRate = 12.0,
                    totalPrice = 61.20
                )
            )
            dao.insertInvoiceItems(invoiceItems)

            // 5. Purchase Bills
            val purchaseBills = listOf(
                PurchaseBill(
                    billNumber = "PB-CIPLA-8921",
                    distributorName = "Cipla Wholesale Dist",
                    distributorGstin = "07AAAAA0000A1Z5",
                    invoiceDate = now - 86400000L * 3,
                    totalAmount = 14850.0,
                    itemsCount = 6,
                    status = "Verified"
                ),
                PurchaseBill(
                    billNumber = "PB-APOLLO-4412",
                    distributorName = "Apollo Pharma Agency",
                    distributorGstin = "07BBBBB1111B2Z6",
                    invoiceDate = now - 86400000L * 10,
                    totalAmount = 28400.0,
                    itemsCount = 12,
                    status = "Verified"
                )
            )
            dao.insertPurchaseBills(purchaseBills)
        }
    }
}
