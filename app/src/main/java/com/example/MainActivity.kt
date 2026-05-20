package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.data.JewelryItem
import com.example.data.Sale
import com.example.ui.JewelryViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val app = context.applicationContext as Application
                val viewModel: JewelryViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JewelryAdminDashboard(viewModel = viewModel)
                }
            }
        }
    }
}

enum class AppTab(val label: String) {
    DASHBOARD("Painel"),
    CATALOG("Catálogo"),
    NEW_SALE("Nova Venda"),
    REPORTS("Relatórios")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JewelryAdminDashboard(viewModel: JewelryViewModel) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val sales by viewModel.allSales.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItemForEdit by remember { mutableStateOf<JewelryItem?>(null) }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Navigation and screen container layout
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "NANIVA",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("dashboard_top_bar")
            )
        },
        bottomBar = {
            if (!isTablet) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        val icon = when (tab) {
                            AppTab.DASHBOARD -> if (isSelected) Icons.Filled.Dashboard else Icons.Filled.Dashboard
                            AppTab.CATALOG -> if (isSelected) Icons.Filled.Loyalty else Icons.Filled.Loyalty
                            AppTab.NEW_SALE -> if (isSelected) Icons.Filled.AddShoppingCart else Icons.Filled.AddShoppingCart
                            AppTab.REPORTS -> if (isSelected) Icons.Filled.BarChart else Icons.Filled.BarChart
                        }
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            label = { Text(tab.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(icon, contentDescription = tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase(Locale.ROOT)}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isTablet) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Spacer(modifier = Modifier.height(16.dp))
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        val icon = when (tab) {
                            AppTab.DASHBOARD -> if (isSelected) Icons.Filled.Dashboard else Icons.Filled.Dashboard
                            AppTab.CATALOG -> if (isSelected) Icons.Filled.Loyalty else Icons.Filled.Loyalty
                            AppTab.NEW_SALE -> if (isSelected) Icons.Filled.AddShoppingCart else Icons.Filled.AddShoppingCart
                            AppTab.REPORTS -> if (isSelected) Icons.Filled.BarChart else Icons.Filled.BarChart
                        }
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            label = { Text(tab.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(icon, contentDescription = tab.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase(Locale.ROOT)}")
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Animate transition between screens
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tab_transitions"
                ) { tab ->
                    when (tab) {
                        AppTab.DASHBOARD -> DashboardTabScreen(
                            items = items,
                            sales = sales,
                            onNavigateToSale = { currentTab = AppTab.NEW_SALE },
                            viewModel = viewModel
                        )
                        AppTab.CATALOG -> CatalogTabScreen(
                            items = items,
                            onAddItemClick = { showAddDialog = true },
                            onEditItemClick = { selectedItemForEdit = it },
                            viewModel = viewModel
                        )
                        AppTab.NEW_SALE -> NewSaleTabScreen(
                            items = items,
                            viewModel = viewModel,
                            onSaleSuccess = { currentTab = AppTab.DASHBOARD }
                        )
                        AppTab.REPORTS -> ReportsTabScreen(
                            items = items,
                            sales = sales
                        )
                    }
                }

                // Dialogs
                if (showAddDialog) {
                    AddItemDialog(
                        onDismiss = { showAddDialog = false },
                        onConfirm = { name, category, material, price, costPrice, stock, alert, sku, desc, imgUri ->
                            viewModel.addItem(
                                name = name,
                                category = category,
                                material = material,
                                price = price,
                                costPrice = costPrice,
                                stockQuantity = stock,
                                minStockAlert = alert,
                                sku = sku,
                                description = desc,
                                imageUri = imgUri
                            )
                            showAddDialog = false
                        }
                    )
                }

                selectedItemForEdit?.let { item ->
                    EditItemDialog(
                        item = item,
                        onDismiss = { selectedItemForEdit = null },
                        onConfirm = { updatedItem ->
                            viewModel.updateItem(updatedItem)
                            selectedItemForEdit = null
                        }
                    )
                }
            }
        }
    }
}

// FORMAT HELPER
fun formatBRL(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(value)
}

@Composable
fun CategorySalesChart(sales: List<Sale>) {
    val categories = listOf("Anel", "Colar", "Pulseira", "Brincos", "Berloques", "Earcuff", "Bracelete", "Conjuntos", "Outros")
    val categorySalesList = remember(sales) {
        val totalsMap = sales.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.totalPrice } }
        
        categories.map { cat -> 
            cat to (totalsMap[cat] ?: 0.0)
        }.sortedByDescending { it.second }
    }

    val maxSaleValue = remember(categorySalesList) {
        val maxVal = categorySalesList.map { it.second }.maxOrNull() ?: 0.0
        if (maxVal == 0.0) 1.0 else maxVal
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categorySalesList.forEach { (cat, amount) ->
            val ratio = (amount / maxSaleValue).toFloat().coerceIn(0f, 1f)
            val animatedRatio by animateFloatAsState(
                targetValue = ratio,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "category_bar_width"
            )

            val catColor = when (cat) {
                "Anel" -> Color(0xFFDFB15B)
                "Colar" -> Color(0xFF3F51B5)
                "Pulseira" -> Color(0xFF009688)
                else -> Color(0xFFE91E63)
            }

            val catIcon = when (cat) {
                "Anel" -> Icons.Filled.Stars
                "Colar" -> Icons.Filled.Star
                "Pulseira" -> Icons.Filled.LocalActivity
                else -> Icons.Filled.Diamond
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Tag layout
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = catColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatBRL(amount),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Animated Horizontal Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedRatio)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(catColor, catColor.copy(alpha = 0.65f))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD TAB SCREEN
// ==========================================
@Composable
fun DashboardTabScreen(
    items: List<JewelryItem>,
    sales: List<Sale>,
    onNavigateToSale: () -> Unit,
    viewModel: JewelryViewModel
) {
    val scrollState = rememberScrollState()

    // Real-time calculations
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val todaySales = sales.filter { it.timestamp >= todayStart }
    val todayRevenue = todaySales.sumOf { it.totalPrice }
    val todaySalesCount = todaySales.size

    val lowStockItems = items.filter { it.stockQuantity <= it.minStockAlert }
    val totalStockPieces = items.sumOf { it.stockQuantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Olá, Nany!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Acompanhe os resultados da sua joalheria hoje",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            // Real-time indicator
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ao vivo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // Metrics Grid (2x2 Column Row equivalent)
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600

        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Vendas Hoje",
                    value = formatBRL(todayRevenue),
                    subtext = "$todaySalesCount transações",
                    icon = Icons.Filled.MonetizationOn,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Estoque Total",
                    value = "$totalStockPieces un.",
                    subtext = "${items.size} produtos",
                    icon = Icons.Filled.Inventory2,
                    color = Color(0xFF3F51B5),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Alertas de Peças",
                    value = "${lowStockItems.size}",
                    subtext = "Com estoque baixo",
                    icon = Icons.Filled.Warning,
                    color = if (lowStockItems.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Faturamento Geral",
                    value = formatBRL(sales.sumOf { it.totalPrice }),
                    subtext = "${sales.size} vendas salvas",
                    icon = Icons.Filled.TrendingUp,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Vendas Hoje",
                        value = formatBRL(todayRevenue),
                        subtext = "$todaySalesCount transações",
                        icon = Icons.Filled.MonetizationOn,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Estoque Total",
                        value = "$totalStockPieces un.",
                        subtext = "${items.size} produtos",
                        icon = Icons.Filled.Inventory2,
                        color = Color(0xFF3F51B5),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Alertas de Peças",
                        value = "${lowStockItems.size}",
                        subtext = "Com estoque baixo",
                        icon = Icons.Filled.Warning,
                        color = if (lowStockItems.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Faturamento Geral",
                        value = formatBRL(sales.sumOf { it.totalPrice }),
                        subtext = "${sales.size} vendas salvas",
                        icon = Icons.Filled.TrendingUp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Action Quick Access
        Button(
            onClick = onNavigateToSale,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("dashboard_quick_sale_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.AddShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Nova Venda Rápidamente", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column (Charts)
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bar Chart section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Acompanhamento de Vendas Mensais",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Simulando faturamento no padrão da Nuvemshop",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            CustomSalesBarChart(sales = sales)
                        }
                    }

                    // Category Sales Chart section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Vendas por Categoria",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Faturamento acumulado por categoria de item (do maior para o menor)",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            CategorySalesChart(sales = sales)
                        }
                    }
                }

                // Right Column (Low Stock Warnings)
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Low stock dynamic alerts
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Atenção ao Estoque Baixo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = if (lowStockItems.isNotEmpty()) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (lowStockItems.isNotEmpty()) "${lowStockItems.size} pendentes" else "Tudo em dia",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lowStockItems.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            if (lowStockItems.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Todas as peças possuem estoque suficiente!",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    lowStockItems.take(5).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.background,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1.3f)) {
                                                Text(
                                                    item.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "SKU: ${item.sku} | Estoque: ${item.stockQuantity}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.weight(0.7f),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.updateItem(item.copy(stockQuantity = item.stockQuantity + 5))
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primaryContainer,
                                                            CircleShape
                                                        )
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Add,
                                                        contentDescription = "Adicionar 5",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "+5 un.",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Bar Chart section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Acompanhamento de Vendas Mensais",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Simulando faturamento no padrão da Nuvemshop",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    CustomSalesBarChart(sales = sales)
                }
            }

            // Category Sales Chart section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vendas por Categoria",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Faturamento acumulado por categoria de item (do maior para o menor)",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    CategorySalesChart(sales = sales)
                }
            }

            // Low stock dynamic alerts
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Atenção ao Estoque Baixo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (lowStockItems.isNotEmpty()) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (lowStockItems.isNotEmpty()) "${lowStockItems.size} pendentes" else "Tudo em dia",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lowStockItems.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (lowStockItems.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Todas as peças possuem estoque suficiente!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lowStockItems.take(3).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.3f)) {
                                        Text(
                                            item.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "SKU: ${item.sku} | Estoque: ${item.stockQuantity}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.weight(0.7f),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                viewModel.updateItem(item.copy(stockQuantity = item.stockQuantity + 5))
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = "Adicionar 5",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "+5 un.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column {
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtext,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==========================================
// CUSTOM CANVAS SALES BAR CHART
// ==========================================
@Composable
fun CustomSalesBarChart(sales: List<Sale>) {
    // Generate monthly values based on current registered sales
    // We group values for Mar, Abr, Mai
    val monthlyTotals = remember(sales) {
        val totals = mutableMapOf("Mar" to 0.0, "Abr" to 0.0, "Mai" to 0.0)
        val calendar = Calendar.getInstance()

        sales.forEach { sale ->
            calendar.timeInMillis = sale.timestamp
            val month = calendar.get(Calendar.MONTH)
            val monthKey = when (month) {
                Calendar.MARCH -> "Mar"
                Calendar.APRIL -> "Abr"
                Calendar.MAY -> "Mai"
                else -> ""
            }
            if (monthKey.isNotEmpty()) {
                totals[monthKey] = (totals[monthKey] ?: 0.0) + sale.totalPrice
            }
        }
        totals
    }

    val chartMax = remember(monthlyTotals) {
        val maxVal = monthlyTotals.values.maxOrNull() ?: 1000.0
        if (maxVal == 0.0) 1000.0 else maxVal * 1.2 // 20% margin above max
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val goldAccent = Color(0xFFDFB15B)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            val months = listOf("Mar", "Abr", "Mai")
            months.forEach { m ->
                val amount = monthlyTotals[m] ?: 0.0
                // Calculate height percentage
                val heightPct = (amount / chartMax).toFloat().coerceIn(0.04f, 1f)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hover tooltip simulation
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = if (amount > 1000) "R$ %.1fk".format(amount / 1000.0) else "R$ %.0f".format(amount),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(heightPct)
                            .width(50.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor, goldAccent)
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = m,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Vendas consolidadas no Banco de Dados", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Text(
                "Total: ${formatBRL(monthlyTotals.values.sum())}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==========================================
// 2. CATALOG TAB SCREEN
// ==========================================
@Composable
fun CatalogTabScreen(
    items: List<JewelryItem>,
    onAddItemClick: () -> Unit,
    onEditItemClick: (JewelryItem) -> Unit,
    viewModel: JewelryViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Anel", "Colar", "Pulseira", "Brincos", "Berloques", "Earcuff", "Bracelete", "Conjuntos", "Outros")

    // Filter list in real time and sort out-of-stock items to the bottom
    val filteredItems = items.filter { item ->
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                item.sku.contains(searchQuery, ignoreCase = true) ||
                item.material.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "Todos" || item.category == selectedCategoryFilter
        matchesSearch && matchesCategory
    }.sortedBy { it.stockQuantity == 0 }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Catálogo de Peças",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Gerencie seus produtos e modifique o estoque em tempo real",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nome, material ou SKU...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_search_bar"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Category filter row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategoryFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Catalog list
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val isTablet = configuration.screenWidthDp >= 600

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CardGiftcard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Nenhuma joia encontrada",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Tente outro termo ou cadastre um novo item",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                if (isTablet) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            CatalogItemCard(
                                item = item,
                                onEditClick = { onEditItemClick(item) },
                                onDeleteClick = { viewModel.deleteItem(item) },
                                onStockChange = { delta ->
                                    val newQuantity = Math.max(0, item.stockQuantity + delta)
                                    viewModel.updateItem(item.copy(stockQuantity = newQuantity))
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            CatalogItemCard(
                                item = item,
                                onEditClick = { onEditItemClick(item) },
                                onDeleteClick = { viewModel.deleteItem(item) },
                                onStockChange = { delta ->
                                    val newQuantity = Math.max(0, item.stockQuantity + delta)
                                    viewModel.updateItem(item.copy(stockQuantity = newQuantity))
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add
        HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter))
        FloatingActionButton(
            onClick = onAddItemClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("catalog_fab_add"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = "Nova Peça")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nova Peça", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CatalogItemCard(
    item: JewelryItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStockChange: (Int) -> Unit
) {
    val isLowStock = item.stockQuantity <= item.minStockAlert

    val isOutOfStock = item.stockQuantity == 0
    val cardAlpha = if (isOutOfStock) 0.55f else 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = cardAlpha),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(if (isOutOfStock) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title & categories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Jewel Icon or Photo Representation
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (item.imageUri != null) Color.Transparent
                                else when (item.category) {
                                    "Anel" -> Color(0xFFDFB15B).copy(alpha = 0.15f)
                                    "Colar" -> Color(0xFF3F51B5).copy(alpha = 0.15f)
                                    "Pulseira" -> Color(0xFF009688).copy(alpha = 0.15f)
                                    else -> Color(0xFFE91E63).copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.imageUri != null) {
                            AsyncImage(
                                model = item.imageUri,
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = when (item.category) {
                                    "Anel" -> Icons.Filled.Stars
                                    "Colar" -> Icons.Filled.Star
                                    "Pulseira" -> Icons.Filled.LocalActivity
                                    else -> Icons.Filled.Diamond
                                },
                                contentDescription = null,
                                tint = when (item.category) {
                                    "Anel" -> Color(0xFFDFB15B)
                                    "Colar" -> Color(0xFF3F51B5)
                                    "Pulseira" -> Color(0xFF009688)
                                    else -> Color(0xFFE91E63)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.material,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SKU: ${item.sku}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Edit/Delete options
                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing details line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Preço de Venda", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        formatBRL(item.price),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Preço de Custo", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        formatBRL(item.costPrice),
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock controller line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.background,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Estoque: ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${item.stockQuantity} un.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (item.stockQuantity == 0) Color(0xFFEF4444) else if (isLowStock) Color(0xFFF59E0B) else Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Stock warning badge
                    if (item.stockQuantity == 0) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEF4444).copy(alpha = 0.15f)) {
                            Text(
                                "ESGOTADO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isLowStock) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF59E0B).copy(alpha = 0.15f)) {
                            Text(
                                "BAIXO ESTOQUE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Plus / Minus Adjusters right inline
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onStockChange(-1) },
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Diminuir Estoque", modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "${item.stockQuantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = { onStockChange(1) },
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Aumentar Estoque", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

data class CartItem(
    val item: JewelryItem,
    val quantity: Int,
    val discount: Double
)

// ==========================================
// 3. NEW SALE TAB SCREEN
// ==========================================
@Composable
fun NewSaleTabScreen(
    items: List<JewelryItem>,
    viewModel: JewelryViewModel,
    onSaleSuccess: () -> Unit
) {
    var searchItemQuery by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var cartItems by remember { mutableStateOf(emptyList<CartItem>()) }
    val typedDiscounts = remember { mutableStateMapOf<Int, String>() }

    var selectedPaymentMethod by remember { mutableStateOf("Pix") }
    var selectedSalesChannel by remember { mutableStateOf("WhatsApp") }
    var isCustomSalesChannel by remember { mutableStateOf(false) }
    var customSalesChannelText by remember { mutableStateOf("") }

    val paymentOptions = listOf("Pix", "Cartão de Crédito", "Cartão de Débito", "Dinheiro")
    val salesChannels = listOf("WhatsApp", "Shopee", "TikTok", "Instagram", "Loja Física", "+ Outro Canal")

    val dropdownItems = items.filter {
        it.name.contains(searchItemQuery, ignoreCase = true) ||
                it.sku.contains(searchItemQuery, ignoreCase = true)
    }

    // Totals calculations
    val subtotal = cartItems.sumOf { it.item.price * it.quantity }
    val totalDiscount = cartItems.sumOf { it.discount }
    val total = Math.max(0.0, subtotal - totalDiscount)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Frente de Caixa (PDV)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Selecione as joias, adicione quantos itens desejar e registre a venda",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600

        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column (Selections)
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Product Search and Selector
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("1. Selecione as Joias", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = searchItemQuery,
                                    onValueChange = {
                                        searchItemQuery = it
                                        expandedDropdown = true
                                    },
                                    placeholder = { Text("Buscar joias por nome ou SKU...") },
                                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("sale_product_field"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                DropdownMenu(
                                    expanded = expandedDropdown,
                                    onDismissRequest = { expandedDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    if (dropdownItems.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Nenhuma joia encontrada no estoque") },
                                            onClick = {}
                                        )
                                    } else {
                                        dropdownItems.forEach { item ->
                                            val cannotSell = item.stockQuantity == 0
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1.3f)
                                                        ) {
                                                            if (item.imageUri != null) {
                                                                AsyncImage(
                                                                    model = item.imageUri,
                                                                    contentDescription = item.name,
                                                                    contentScale = ContentScale.Crop,
                                                                    modifier = Modifier
                                                                        .size(32.dp)
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                )
                                                            } else {
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                                    modifier = Modifier.size(32.dp)
                                                                ) {
                                                                    Box(contentAlignment = Alignment.Center) {
                                                                        Icon(
                                                                            Icons.Filled.Diamond,
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.primary,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Text(
                                                                    item.name,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 13.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Text(
                                                                    formatBRL(item.price),
                                                                    fontSize = 11.sp,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = if (cannotSell) "Esgotado" else "${item.stockQuantity} un.",
                                                            fontSize = 12.sp,
                                                            color = if (cannotSell) Color(0xFFEF4444) else MaterialTheme.colorScheme.secondary,
                                                            modifier = Modifier.weight(0.7f),
                                                            textAlign = TextAlign.End
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    if (!cannotSell) {
                                                        val existingIndex = cartItems.indexOfFirst { it.item.id == item.id }
                                                        if (existingIndex >= 0) {
                                                            val existing = cartItems[existingIndex]
                                                            val newQty = Math.min(item.stockQuantity, existing.quantity + 1)
                                                            cartItems = cartItems.toMutableList().apply {
                                                                set(existingIndex, existing.copy(quantity = newQty))
                                                            }
                                                        } else {
                                                            cartItems = cartItems + CartItem(item = item, quantity = 1, discount = 0.0)
                                                        }
                                                        searchItemQuery = ""
                                                        expandedDropdown = false
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cart items view
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Itens do Pedido (${cartItems.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (cartItems.isNotEmpty()) {
                                    TextButton(onClick = { cartItems = emptyList(); typedDiscounts.clear() }) {
                                        Text("Limpar Carrinho", color = Color(0xFFEF4444), fontSize = 12.sp)
                                    }
                                }
                            }

                            if (cartItems.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingBag,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "O carrinho está vazio",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        "Selecione joias acima para iniciar o pedido",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    cartItems.forEach { cartItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (cartItem.item.imageUri != null) {
                                                AsyncImage(
                                                    model = cartItem.item.imageUri,
                                                    contentDescription = cartItem.item.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                )
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Filled.Diamond,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    cartItem.item.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "Material: ${cartItem.item.material}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Text(
                                                    "${formatBRL(cartItem.item.price)} un",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        cartItems = cartItems - cartItem
                                                        typedDiscounts.remove(cartItem.item.id)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Delete,
                                                        contentDescription = "Remover do carrinho",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (cartItem.quantity > 1) {
                                                                cartItems = cartItems.map {
                                                                    if (it.item.id == cartItem.item.id) it.copy(quantity = it.quantity - 1) else it
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(MaterialTheme.colorScheme.background, CircleShape)
                                                    ) {
                                                        Icon(Icons.Filled.Remove, contentDescription = "Menos", modifier = Modifier.size(14.dp))
                                                    }

                                                    Text(
                                                        text = "${cartItem.quantity}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        modifier = Modifier.width(20.dp),
                                                        textAlign = TextAlign.Center
                                                    )

                                                    IconButton(
                                                        onClick = {
                                                            if (cartItem.quantity < cartItem.item.stockQuantity) {
                                                                cartItems = cartItems.map {
                                                                    if (it.item.id == cartItem.item.id) it.copy(quantity = it.quantity + 1) else it
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(MaterialTheme.colorScheme.background, CircleShape)
                                                    ) {
                                                        Icon(Icons.Filled.Add, contentDescription = "Mais", modifier = Modifier.size(14.dp))
                                                    }
                                                }

                                                val currentDiscountInput = typedDiscounts[cartItem.item.id] ?: if (cartItem.discount > 0.0) cartItem.discount.toString() else ""
                                                OutlinedTextField(
                                                    value = currentDiscountInput,
                                                    onValueChange = { input ->
                                                        if (input.isEmpty() || input.toDoubleOrNull() != null) {
                                                            typedDiscounts[cartItem.item.id] = input
                                                            val discountVal = input.toDoubleOrNull() ?: 0.0
                                                            val maxAllowedDiscount = cartItem.item.price * cartItem.quantity
                                                            cartItems = cartItems.map {
                                                                if (it.item.id == cartItem.item.id) {
                                                                    it.copy(discount = Math.min(maxAllowedDiscount, discountVal))
                                                                } else it
                                                            }
                                                        }
                                                    },
                                                    placeholder = { Text("Desc. R$", fontSize = 10.sp) },
                                                    singleLine = true,
                                                    textStyle = TextStyle(fontSize = 11.sp),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier
                                                        .width(90.dp)
                                                        .height(44.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Column (Configurations & Summary)
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Financial & Payment details Cards
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("2. Configurar Pagamento", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                paymentOptions.forEach { method ->
                                    val isSelected = selectedPaymentMethod == method
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedPaymentMethod = method },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = method,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Canal de Vendas", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                                if (isCustomSalesChannel) {
                                    OutlinedTextField(
                                        value = customSalesChannelText,
                                        onValueChange = { customSalesChannelText = it },
                                        label = { Text("Digitar Canal de Vendas") },
                                        placeholder = { Text("Ex: TikTok Shop, Pinterest...") },
                                        trailingIcon = {
                                            IconButton(onClick = { isCustomSalesChannel = false }) {
                                                Icon(Icons.Filled.Close, contentDescription = "Lista")
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(salesChannels) { channel ->
                                            val isSelected = selectedSalesChannel == channel
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    if (channel == "+ Outro Canal") {
                                                        isCustomSalesChannel = true
                                                    } else {
                                                        selectedSalesChannel = channel
                                                    }
                                                },
                                                label = { Text(channel) },
                                                leadingIcon = if (isSelected) {
                                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                } else {
                                                    null
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sales Checkout calculation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Resumo do Pedido", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                Text(formatBRL(subtotal), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Desconto", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                Text("- " + formatBRL(totalDiscount), color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                            }

                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("VALOR TOTAL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                                Text(
                                    formatBRL(total),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Finalize transaction button
                    Button(
                        onClick = {
                            if (cartItems.isNotEmpty()) {
                                val finalSalesChannel = if (isCustomSalesChannel) customSalesChannelText.ifEmpty { "Outros" } else selectedSalesChannel
                                viewModel.registerMultiItemSale(
                                    itemsWithDetails = cartItems.map { Triple(it.item, it.quantity, it.discount) },
                                    paymentMethod = selectedPaymentMethod,
                                    salesChannel = finalSalesChannel
                                )
                                onSaleSuccess()
                            }
                        },
                        enabled = cartItems.isNotEmpty() && total >= 0.0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_sale_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Concluir e Emitir Venda", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Mobile (Portrait standard vertical stacking)
            // Product Search and Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Selecione as Joias", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = searchItemQuery,
                            onValueChange = {
                                searchItemQuery = it
                                expandedDropdown = true
                            },
                            placeholder = { Text("Buscar joias por nome ou SKU...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sale_product_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (dropdownItems.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Nenhuma joia encontrada no estoque") },
                                    onClick = {}
                                )
                            } else {
                                dropdownItems.forEach { item ->
                                    val cannotSell = item.stockQuantity == 0
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1.3f)
                                                ) {
                                                    if (item.imageUri != null) {
                                                        AsyncImage(
                                                            model = item.imageUri,
                                                            contentDescription = item.name,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                        )
                                                    } else {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                    Icons.Filled.Diamond,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            item.name,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 13.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            formatBRL(item.price),
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (cannotSell) "Esgotado" else "${item.stockQuantity} un.",
                                                    fontSize = 12.sp,
                                                    color = if (cannotSell) Color(0xFFEF4444) else MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.weight(0.7f),
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        },
                                        onClick = {
                                            if (!cannotSell) {
                                                val existingIndex = cartItems.indexOfFirst { it.item.id == item.id }
                                                if (existingIndex >= 0) {
                                                    val existing = cartItems[existingIndex]
                                                    val newQty = Math.min(item.stockQuantity, existing.quantity + 1)
                                                    cartItems = cartItems.toMutableList().apply {
                                                        set(existingIndex, existing.copy(quantity = newQty))
                                                    }
                                                } else {
                                                    cartItems = cartItems + CartItem(item = item, quantity = 1, discount = 0.0)
                                                }
                                                searchItemQuery = ""
                                                expandedDropdown = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cart items view
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Itens do Pedido (${cartItems.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (cartItems.isNotEmpty()) {
                            TextButton(onClick = { cartItems = emptyList(); typedDiscounts.clear() }) {
                                Text("Limpar Carrinho", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        }
                    }

                    if (cartItems.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "O carrinho está vazio",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Selecione joias acima para iniciar o pedido",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            cartItems.forEach { cartItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (cartItem.item.imageUri != null) {
                                        AsyncImage(
                                            model = cartItem.item.imageUri,
                                            contentDescription = cartItem.item.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Filled.Diamond,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            cartItem.item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Material: ${cartItem.item.material}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            "${formatBRL(cartItem.item.price)} un",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                cartItems = cartItems - cartItem
                                                typedDiscounts.remove(cartItem.item.id)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remover do carrinho",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (cartItem.quantity > 1) {
                                                        cartItems = cartItems.map {
                                                            if (it.item.id == cartItem.item.id) it.copy(quantity = it.quantity - 1) else it
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(MaterialTheme.colorScheme.background, CircleShape)
                                            ) {
                                                Icon(Icons.Filled.Remove, contentDescription = "Menos", modifier = Modifier.size(14.dp))
                                            }

                                            Text(
                                                text = "${cartItem.quantity}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.width(20.dp),
                                                textAlign = TextAlign.Center
                                            )

                                            IconButton(
                                                onClick = {
                                                    if (cartItem.quantity < cartItem.item.stockQuantity) {
                                                        cartItems = cartItems.map {
                                                            if (it.item.id == cartItem.item.id) it.copy(quantity = it.quantity + 1) else it
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(MaterialTheme.colorScheme.background, CircleShape)
                                            ) {
                                                Icon(Icons.Filled.Add, contentDescription = "Mais", modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        val currentDiscountInput = typedDiscounts[cartItem.item.id] ?: if (cartItem.discount > 0.0) cartItem.discount.toString() else ""
                                        OutlinedTextField(
                                            value = currentDiscountInput,
                                            onValueChange = { input ->
                                                if (input.isEmpty() || input.toDoubleOrNull() != null) {
                                                    typedDiscounts[cartItem.item.id] = input
                                                    val discountVal = input.toDoubleOrNull() ?: 0.0
                                                    val maxAllowedDiscount = cartItem.item.price * cartItem.quantity
                                                    cartItems = cartItems.map {
                                                        if (it.item.id == cartItem.item.id) {
                                                            it.copy(discount = Math.min(maxAllowedDiscount, discountVal))
                                                        } else it
                                                    }
                                                }
                                            },
                                            placeholder = { Text("Desc. R$", fontSize = 10.sp) },
                                            singleLine = true,
                                            textStyle = TextStyle(fontSize = 11.sp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .width(90.dp)
                                                .height(44.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Financial & Payment details Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("2. Configurar Pagamento", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        paymentOptions.forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPaymentMethod = method },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Canal de Vendas", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                        if (isCustomSalesChannel) {
                            OutlinedTextField(
                                value = customSalesChannelText,
                                		onValueChange = { customSalesChannelText = it },
                                label = { Text("Digitar Canal de Vendas") },
                                placeholder = { Text("Ex: TikTok Shop, Pinterest...") },
                                trailingIcon = {
                                    IconButton(onClick = { isCustomSalesChannel = false }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Lista")
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(salesChannels) { channel ->
                                    val isSelected = selectedSalesChannel == channel
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (channel == "+ Outro Canal") {
                                                isCustomSalesChannel = true
                                            } else {
                                                selectedSalesChannel = channel
                                            }
                                        },
                                        label = { Text(channel) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else {
                                            null
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sales Checkout calculation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resumo do Pedido", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Text(formatBRL(subtotal), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Desconto", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Text("- " + formatBRL(totalDiscount), color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                    }

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VALOR TOTAL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                        Text(
                            formatBRL(total),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Finalize transaction button
            Button(
                onClick = {
                    if (cartItems.isNotEmpty()) {
                        val finalSalesChannel = if (isCustomSalesChannel) customSalesChannelText.ifEmpty { "Outros" } else selectedSalesChannel
                        viewModel.registerMultiItemSale(
                            itemsWithDetails = cartItems.map { Triple(it.item, it.quantity, it.discount) },
                            paymentMethod = selectedPaymentMethod,
                            salesChannel = finalSalesChannel
                        )
                        onSaleSuccess()
                    }
                },
                enabled = cartItems.isNotEmpty() && total >= 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_sale_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Concluir e Emitir Venda", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 4. REPORTS TAB SCREEN
// ==========================================
@Composable
fun ReportsTabScreen(
    items: List<JewelryItem>,
    sales: List<Sale>
) {
    val scrollState = rememberScrollState()

    // Aggregate values
    val totalRevenue = sales.sumOf { it.totalPrice }

    // Let's calculate total inventory cost price & total potential revenue
    val inventoryTotalCost = items.sumOf { it.costPrice * it.stockQuantity }
    val inventoryPotentialRevenue = items.sumOf { it.price * it.stockQuantity }
    val potentialProfit = inventoryPotentialRevenue - inventoryTotalCost

    // Margin Calculations
    val calculatedRetailPriceOfSalesUnits = sales.sumOf { it.unitPrice * it.quantity }
    // Best matching item cost matching histories
    val calculatedCostOfSoldUnits = sales.sumOf { sale ->
        val originalItem = items.find { it.id == sale.jewelryItemId }
        val costPr = originalItem?.costPrice ?: (sale.unitPrice * 0.4) // fallback to 40% cost represent
        costPr * sale.quantity
    }
    val realProfit = totalRevenue - calculatedCostOfSoldUnits
    val profitPercentage = if (totalRevenue > 0) (realProfit / totalRevenue) * 100 else 0.0

    // Payment methods calculations
    val pCounts = sales.groupBy { it.paymentMethod }
    val pixValue = pCounts["Pix"]?.sumOf { it.totalPrice } ?: 0.0
    val creditValue = pCounts["Cartão de Crédito"]?.sumOf { it.totalPrice } ?: 0.0
    val debitValue = pCounts["Cartão de Débito"]?.sumOf { it.totalPrice } ?: 0.0
    val cashValue = pCounts["Dinheiro"]?.sumOf { it.totalPrice } ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Relatórios e Insights",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Estatísticas de performance e saúde financeira da sua loja",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600

        if (isTablet) {
            // First Row: Financial Overview & Potential Inventory Assets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Financial Overview Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Desempenho Comercial Total", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Faturamento Histórico", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(formatBRL(totalRevenue), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Lucratividade Real", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(formatBRL(realProfit), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF10B981))
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Margem de Lucro Média", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = "%.1f%%".format(profitPercentage),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFDFB15B)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = "Nuvemshop Analytics",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Potential Inventory assets Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Balanço Patrimonial do Estoque", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Custo Total Acumulado", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(formatBRL(inventoryTotalCost), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Faturamento Potencial", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(formatBRL(inventoryPotentialRevenue), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Bar meter
                        val progress = if (inventoryPotentialRevenue > 0) (inventoryTotalCost / inventoryPotentialRevenue).toFloat() else 0f
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )

                        Text(
                            text = "Suas joias valem ${formatBRL(potentialProfit)} a mais do que o custo de fabricação/compra.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Second Row: Payment split & Recent Transactions on tablet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Payment Methods split Card
                Card(
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Métodos de Pagamento Utilizados", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        val maxMethodVal = listOf(pixValue, creditValue, debitValue, cashValue).maxOrNull() ?: 1.0
                        val safeMax = if (maxMethodVal == 0.0) 1.0 else maxMethodVal

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PaymentProgressRow("Pix", pixValue, safeMax)
                            PaymentProgressRow("Cartão de Crédito", creditValue, safeMax)
                            PaymentProgressRow("Cartão de Débito", debitValue, safeMax)
                            PaymentProgressRow("Dinheiro", cashValue, safeMax)
                        }
                    }
                }

                // Recent Transactions
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Diário de Transações Recentes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (sales.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Nenhuma venda gravada até o momento.",
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        sales.forEach { sale ->
                            val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date(sale.timestamp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.3f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                sale.jewelryItemName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (sale.salesChannel) {
                                                    "Nuvemshop" -> Color(0xFFE0F2FE)
                                                    "WhatsApp" -> Color(0xFFDCFCE7)
                                                    "Instagram" -> Color(0xFFFCE7F3)
                                                    "Loja Física" -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFF3F4F6)
                                                },
                                                contentColor = when (sale.salesChannel) {
                                                    "Nuvemshop" -> Color(0xFF0369A1)
                                                    "WhatsApp" -> Color(0xFF15803D)
                                                    "Instagram" -> Color(0xFFBE185D)
                                                    "Loja Física" -> Color(0xFFB45309)
                                                    else -> Color(0xFF374151)
                                                }
                                            ) {
                                                Text(
                                                    text = sale.salesChannel,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Qtde: ${sale.quantity} un. | ${sale.paymentMethod}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            dateFormatted,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End) {
                                        Text(
                                            formatBRL(sale.totalPrice),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (sale.discount > 0.0) {
                                            Text(
                                                "Desconto: " + formatBRL(sale.discount),
                                                fontSize = 10.sp,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Standard Mobile (Vertical stream layout)
            // Financial Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Desempenho Comercial Total", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Faturamento Histórico", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(formatBRL(totalRevenue), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Lucratividade Real", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(formatBRL(realProfit), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF10B981))
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Margem de Lucro Média", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "%.1f%%".format(profitPercentage),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFFDFB15B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = "Nuvemshop Analytics",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Potential Inventory assets Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Balanço Patrimonial do Estoque", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Custo Total Acumulado", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(formatBRL(inventoryTotalCost), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Faturamento Potencial", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(formatBRL(inventoryPotentialRevenue), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Bar meter
                    val progress = if (inventoryPotentialRevenue > 0) (inventoryTotalCost / inventoryPotentialRevenue).toFloat() else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )

                    Text(
                        text = "Suas joias valem ${formatBRL(potentialProfit)} a mais do que o custo de fabricação ou compra.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Payment Methods split Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Métodos de Pagamento Utilizados", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    val maxMethodVal = listOf(pixValue, creditValue, debitValue, cashValue).maxOrNull() ?: 1.0
                    val safeMax = if (maxMethodVal == 0.0) 1.0 else maxMethodVal

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PaymentProgressRow("Pix", pixValue, safeMax)
                        PaymentProgressRow("Cartão de Crédito", creditValue, safeMax)
                        PaymentProgressRow("Cartão de Débito", debitValue, safeMax)
                        PaymentProgressRow("Dinheiro", cashValue, safeMax)
                    }
                }
            }

            // Historical Sales journal
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Diário de Transações Recentes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (sales.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Nenhuma venda gravada até o momento.",
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    sales.forEach { sale ->
                        val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date(sale.timestamp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            sale.jewelryItemName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (sale.salesChannel) {
                                                "Nuvemshop" -> Color(0xFFE0F2FE)
                                                "WhatsApp" -> Color(0xFFDCFCE7)
                                                "Instagram" -> Color(0xFFFCE7F3)
                                                "Loja Física" -> Color(0xFFFEF3C7)
                                                else -> Color(0xFFF3F4F6)
                                            },
                                            contentColor = when (sale.salesChannel) {
                                                "Nuvemshop" -> Color(0xFF0369A1)
                                                "WhatsApp" -> Color(0xFF15803D)
                                                "Instagram" -> Color(0xFFBE185D)
                                                "Loja Física" -> Color(0xFFB45309)
                                                else -> Color(0xFF374151)
                                            }
                                        ) {
                                            Text(
                                                text = sale.salesChannel,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "Qtde: ${sale.quantity} un. | ${sale.paymentMethod}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        dateFormatted,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatBRL(sale.totalPrice),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (sale.discount > 0.0) {
                                        Text(
                                            "Desconto: " + formatBRL(sale.discount),
                                            fontSize = 10.sp,
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentProgressRow(method: String, value: Double, maxLimit: Double) {
    val progress = (value / maxLimit).toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(method, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(formatBRL(value), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = Color(0xFFDFB15B), // Champagne gold accent indicators
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    }
}


// ==========================================
// DIALOGS: ADD / EDIT ITEMS
// ==========================================
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        category: String,
        metal: String,
        price: Double,
        costPrice: Double,
        stockQuantity: Int,
        minStockAlert: Int,
        sku: String,
        description: String,
        imageUri: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var costPriceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("") }
    var minStockText by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    var selectedCategory by remember { mutableStateOf("Anel") }

    var selectedMaterial by remember { mutableStateOf("Prata") }

    val categories = listOf("Anel", "Colar", "Pulseira", "Brincos", "Berloques", "Earcuff", "Bracelete", "Conjuntos", "Outros")
    val materials = listOf("Bijuteria", "Banhado", "Inox", "Prata", "Pérolas", "Outros")

    var categoryExpanded by remember { mutableStateOf(false) }
    var materialExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cadastrar Nova Joia", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .clickable {
                            launcher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Foto selecionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                contentColor = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    "Toque para alterar a foto",
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddAPhoto,
                                contentDescription = "Adicionar Foto",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Adicionar Imagem do Dispositivo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Para visualização no catálogo",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Peça") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_item_dialog_name")
                )

                // Category & Material dropdown lines
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Category
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria") },
                            trailingIcon = {
                                IconButton(onClick = { categoryExpanded = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        selectedCategory = c
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Material
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedMaterial,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Material") },
                            trailingIcon = {
                                IconButton(onClick = { materialExpanded = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = materialExpanded,
                            onDismissRequest = { materialExpanded = false }
                        ) {
                            materials.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        selectedMaterial = m
                                        materialExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Prices
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Preço Venda") },
                        prefix = { Text("R$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Preço Custo") },
                        prefix = { Text("R$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Stock details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Qtd Estoque") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Estoq Mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Custom Auto generated sku indicator
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("Código SKU (Opcional)") },
                    placeholder = { Text("Ex: AN-OU18-01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição Detalhada") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: 0.0
                    val cp = costPriceText.toDoubleOrNull() ?: 0.0
                    val st = stockText.toIntOrNull() ?: 0
                    val mst = minStockText.toIntOrNull() ?: 3

                    val finalCategory = selectedCategory
                    val finalMaterial = selectedMaterial

                    if (name.isNotEmpty() && p > 0) {
                        onConfirm(
                            name,
                            finalCategory,
                            finalMaterial,
                            p,
                            cp,
                            st,
                            mst,
                            sku,
                            description,
                            imageUri
                        )
                    }
                },
                enabled = name.isNotEmpty() && (priceText.toDoubleOrNull() != null),
                modifier = Modifier.testTag("confirm_add_product")
            ) {
                Text("CADASTRAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditItemDialog(
    item: JewelryItem,
    onDismiss: () -> Unit,
    onConfirm: (JewelryItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var priceText by remember { mutableStateOf(item.price.toString()) }
    var costPriceText by remember { mutableStateOf(item.costPrice.toString()) }
    var stockText by remember { mutableStateOf(item.stockQuantity.toString()) }
    var minStockText by remember { mutableStateOf(item.minStockAlert.toString()) }
    var sku by remember { mutableStateOf(item.sku) }
    var description by remember { mutableStateOf(item.description) }
    var imageUri by remember { mutableStateOf<String?>(item.imageUri) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    val categories = listOf("Anel", "Colar", "Pulseira", "Brincos", "Berloques", "Earcuff", "Bracelete", "Conjuntos", "Outros")
    val materials = listOf("Bijuteria", "Banhado", "Inox", "Prata", "Pérolas", "Outros")

    var selectedCategory by remember { mutableStateOf(if (item.category in categories) item.category else "Outros") }
    var selectedMaterial by remember { mutableStateOf(if (item.material in materials) item.material else "Outros") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var materialExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Peça", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .clickable {
                            launcher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Foto selecionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                contentColor = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    "Toque para alterar a foto",
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddAPhoto,
                                contentDescription = "Adicionar Foto",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Adicionar Imagem do Dispositivo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Para visualização no catálogo",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Peça") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Category Selection
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria") },
                            trailingIcon = {
                                IconButton(onClick = { categoryExpanded = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        selectedCategory = c
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Material Selection
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedMaterial,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Material") },
                            trailingIcon = {
                                IconButton(onClick = { materialExpanded = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = materialExpanded,
                            onDismissRequest = { materialExpanded = false }
                        ) {
                            materials.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        selectedMaterial = m
                                        materialExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Preço Venda") },
                        prefix = { Text("R$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Preço Custo") },
                        prefix = { Text("R$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Qtd Estoque") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Estoq Mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("Código SKU") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: item.price
                    val cp = costPriceText.toDoubleOrNull() ?: item.costPrice
                    val st = stockText.toIntOrNull() ?: item.stockQuantity
                    val mst = minStockText.toIntOrNull() ?: item.minStockAlert

                    val finalCategory = selectedCategory
                    val finalMaterial = selectedMaterial

                    if (name.isNotEmpty()) {
                        onConfirm(
                            item.copy(
                                name = name,
                                category = finalCategory,
                                material = finalMaterial,
                                price = p,
                                costPrice = cp,
                                stockQuantity = st,
                                minStockAlert = mst,
                                sku = sku,
                                description = description,
                                imageUri = imageUri
                            )
                        )
                    }
                },
                enabled = name.isNotEmpty()
            ) {
                Text("SALVAR ALTERAÇÕES")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
