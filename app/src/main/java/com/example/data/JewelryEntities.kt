package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jewelry_items")
data class JewelryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., "Anel", "Colar", "Pulseira", "Brincos"
    val material: String, // e.g., "Bijuteria", "Prata", "Banhado" etc.
    val price: Double,    // Retail price
    val costPrice: Double, // Cost price (for reporting profit margins)
    val stockQuantity: Int,
    val minStockAlert: Int = 3,
    val sku: String = "",  // SKU for Nuvemshop e-commerce vibe
    val description: String = "",
    val imageUri: String? = null
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jewelryItemId: Int,
    val jewelryItemName: String, // cached in case item is deleted
    val category: String,        // cached category
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val totalPrice: Double,
    val paymentMethod: String, // "Pix", "Cartão de Crédito", "Cartão de Débito", "Dinheiro"
    val salesChannel: String = "Loja Física",
    val timestamp: Long = System.currentTimeMillis()
)
