package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.billing.BillingScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.khata.UdharKhataScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.substitutes.SubstituteFinderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondaryContainer
import com.example.ui.viewmodel.PharmacyViewModel

enum class LocalWellTab(val label: String, val icon: ImageVector, val tag: String) {
    BILLING("Billing", Icons.Default.ReceiptLong, "nav_billing"),
    INVENTORY("Inventory", Icons.Default.Inventory2, "nav_inventory"),
    SUBSTITUTES("Substitutes", Icons.Default.CompareArrows, "nav_substitutes"),
    KHATA("Udhar Khata", Icons.Default.AccountBalanceWallet, "nav_khata"),
    REPORTS("Reports", Icons.Default.Analytics, "nav_reports")
}

class MainActivity : ComponentActivity() {

    private val viewModel: PharmacyViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(LocalWellTab.BILLING) }
                val cartItems by viewModel.cartItems.collectAsState()
                val selectedCustomer by viewModel.selectedCustomer.collectAsState()

                // Hardware back press handling
                BackHandler(enabled = currentTab != LocalWellTab.BILLING || selectedCustomer != null) {
                    if (selectedCustomer != null) {
                        viewModel.selectCustomer(null)
                    } else if (currentTab != LocalWellTab.BILLING) {
                        currentTab = LocalWellTab.BILLING
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(TealPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalPharmacy,
                                            contentDescription = "Logo",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    androidx.compose.foundation.layout.Column {
                                        Text(
                                            text = "LocalWell",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = TealPrimary
                                        )
                                        Text(
                                            text = "Pharmacy Billing & Stock POS",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            LocalWellTab.values().forEach { tab ->
                                val isSelected = currentTab == tab

                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (currentTab == LocalWellTab.KHATA && tab != LocalWellTab.KHATA) {
                                            viewModel.selectCustomer(null)
                                        }
                                        currentTab = tab
                                    },
                                    icon = {
                                        if (tab == LocalWellTab.BILLING && cartItems.isNotEmpty()) {
                                            BadgedBox(badge = {
                                                Badge(containerColor = TealPrimary) {
                                                    Text("${cartItems.size}")
                                                }
                                            }) {
                                                Icon(tab.icon, contentDescription = tab.label)
                                            }
                                        } else {
                                            Icon(tab.icon, contentDescription = tab.label)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TealPrimary,
                                        selectedTextColor = TealPrimary,
                                        indicatorColor = TealSecondaryContainer
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            LocalWellTab.BILLING -> BillingScreen(viewModel = viewModel)
                            LocalWellTab.INVENTORY -> InventoryScreen(
                                viewModel = viewModel,
                                onNavigateToBilling = { currentTab = LocalWellTab.BILLING }
                            )
                            LocalWellTab.SUBSTITUTES -> SubstituteFinderScreen(
                                viewModel = viewModel,
                                onNavigateToBilling = { currentTab = LocalWellTab.BILLING }
                            )
                            LocalWellTab.KHATA -> UdharKhataScreen(viewModel = viewModel)
                            LocalWellTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
