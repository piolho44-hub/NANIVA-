package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.JewelryDatabase
import com.example.data.JewelryItem
import com.example.data.JewelryRepository
import com.example.data.Sale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class JewelryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: JewelryRepository

    val allItems: StateFlow<List<JewelryItem>>
    val allSales: StateFlow<List<Sale>>

    init {
        val database = JewelryDatabase.getDatabase(application)
        val dao = database.jewelryDao()
        repository = JewelryRepository(dao)

        allItems = repository.allItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSales = repository.allSales.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed database if empty
        viewModelScope.launch {
            val currItems = repository.allItems.first()
            if (currItems.isEmpty()) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() {
        val item1 = JewelryItem(
            name = "Anel Solitário Ouro Amarelo",
            category = "Anel",
            material = "Prata",
            price = 3499.00,
            costPrice = 1300.00,
            stockQuantity = 8,
            minStockAlert = 3,
            sku = "AN-SOL-PRATA",
            description = "Clássico anel solitário em prata italiana trabalhada, cravejado de um exuberante diamante central de 20 pontos."
        )
        val item2 = JewelryItem(
            name = "Gargantilha Ponto de Luz",
            category = "Colar",
            material = "Banhado",
            price = 1450.00,
            costPrice = 600.00,
            stockQuantity = 4,
            minStockAlert = 2,
            sku = "CL-PTL-BANHADO",
            description = "Suave veneziana fina com pendente brilhante redondo extremamente radiante, banhado a ouro."
        )
        val item3 = JewelryItem(
            name = "Pulseira Riviera Zircônia",
            category = "Pulseira",
            material = "Prata",
            price = 289.00,
            costPrice = 95.00,
            stockQuantity = 22,
            minStockAlert = 5,
            sku = "PL-RIV-PRATA",
            description = "Pulseira luxuosa estilo Riviera, confeccionada em prata de lei legítima, inteiramente cravejada de zircônias."
        )
        val item4 = JewelryItem(
            name = "Brinco de Pérola Natural Cultivada",
            category = "Brincos",
            material = "Pérolas",
            price = 899.00,
            costPrice = 380.00,
            stockQuantity = 12,
            minStockAlert = 4,
            sku = "BR-PER-PEROLA",
            description = "Clássico refinado: brincos com pérolas naturais selecionadas cultivadas em água doce e base de alta qualidade."
        )
        val item5 = JewelryItem(
            name = "Anel Duplo Cravejado",
            category = "Anel",
            material = "Bijuteria",
            price = 180.00,
            costPrice = 60.00,
            stockQuantity = 2, // low stock!
            minStockAlert = 3,
            sku = "AN-DUP-BIJU",
            description = "Anel duplo geométrico bijuteria refinada com detalhes em mini-zircônias brilhantes."
        )

        repository.insertItem(item1)
        repository.insertItem(item2)
        repository.insertItem(item3)
        repository.insertItem(item4)
        repository.insertItem(item5)

        // Seed sales over three distinct months to showcase monthly bar charts
        val calendar = Calendar.getInstance()

        // 60 days ago: March (approx)
        calendar.set(Calendar.MONTH, Calendar.MARCH)
        calendar.set(Calendar.DAY_OF_MONTH, 15)
        repository.insertSale(Sale(
            jewelryItemId = 1,
            jewelryItemName = "Anel Solitário Ouro Amarelo",
            category = "Anel",
            quantity = 1,
            unitPrice = 3499.00,
            discount = 199.00,
            totalPrice = 3300.00,
            paymentMethod = "Cartão de Crédito",
            salesChannel = "Nuvemshop",
            timestamp = calendar.timeInMillis
        ))

        // 30 days ago: April (approx)
        calendar.set(Calendar.MONTH, Calendar.APRIL)
        calendar.set(Calendar.DAY_OF_MONTH, 18)
        repository.insertSale(Sale(
            jewelryItemId = 3,
            jewelryItemName = "Pulseira Riviera Zircônia",
            category = "Pulseira",
            quantity = 4,
            unitPrice = 289.00,
            discount = 56.0,
            totalPrice = 1100.00,
            paymentMethod = "Pix",
            salesChannel = "WhatsApp",
            timestamp = calendar.timeInMillis
        ))

        // 5 days ago: Early May
        calendar.set(Calendar.MONTH, Calendar.MAY)
        calendar.set(Calendar.DAY_OF_MONTH, 10)
        repository.insertSale(Sale(
            jewelryItemId = 4,
            jewelryItemName = "Brinco de Pérola Natural Cultivada",
            category = "Brincos",
            quantity = 2,
            unitPrice = 899.00,
            discount = 98.00,
            totalPrice = 1700.00,
            paymentMethod = "Pix",
            salesChannel = "Nuvemshop",
            timestamp = calendar.timeInMillis
        ))

        // Today: May
        repository.insertSale(Sale(
            jewelryItemId = 2,
            jewelryItemName = "Gargantilha Ponto de Luz",
            category = "Colar",
            quantity = 1,
            unitPrice = 1450.00,
            discount = 50.0,
            totalPrice = 1400.00,
            paymentMethod = "Pix",
            salesChannel = "Loja Física",
            timestamp = System.currentTimeMillis()
        ))
    }

    // Interaction CRUD wrappers
    fun addItem(
        name: String,
        category: String,
        material: String,
        price: Double,
        costPrice: Double,
        stockQuantity: Int,
        minStockAlert: Int,
        sku: String,
        description: String,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            repository.insertItem(
                JewelryItem(
                    name = name,
                    category = category,
                    material = material,
                    price = price,
                    costPrice = costPrice,
                    stockQuantity = stockQuantity,
                    minStockAlert = minStockAlert,
                    sku = sku.ifEmpty { "JOIAS-${System.currentTimeMillis() % 10000}" },
                    description = description,
                    imageUri = imageUri
                )
            )
        }
    }

    fun updateItem(item: JewelryItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: JewelryItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun registerSale(item: JewelryItem, quantity: Int, discount: Double, paymentMethod: String, salesChannel: String) {
        viewModelScope.launch {
            val totalPrice = (item.price * quantity) - discount
            val sale = Sale(
                jewelryItemId = item.id,
                jewelryItemName = item.name,
                category = item.category,
                quantity = quantity,
                unitPrice = item.price,
                discount = discount,
                totalPrice = Math.max(0.0, totalPrice),
                paymentMethod = paymentMethod,
                salesChannel = salesChannel.ifEmpty { "Loja Física" },
                timestamp = System.currentTimeMillis()
            )
            repository.registerSale(sale, item)
        }
    }

    fun registerMultiItemSale(
        itemsWithDetails: List<Triple<JewelryItem, Int, Double>>, // Item, quantity, discount
        paymentMethod: String,
        salesChannel: String
    ) {
        viewModelScope.launch {
            itemsWithDetails.forEach { (item, qty, itemDiscount) ->
                val totalItemPrice = (item.price * qty) - itemDiscount
                val sale = Sale(
                    jewelryItemId = item.id,
                    jewelryItemName = item.name,
                    category = item.category,
                    quantity = qty,
                    unitPrice = item.price,
                    discount = itemDiscount,
                    totalPrice = Math.max(0.0, totalItemPrice),
                    paymentMethod = paymentMethod,
                    salesChannel = salesChannel.ifEmpty { "Loja Física" },
                    timestamp = System.currentTimeMillis()
                )
                repository.registerSale(sale, item)
            }
        }
    }
}
