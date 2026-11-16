
package com.meghmala.cosmetics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

data class Product(
    val name: String,
    val code: String,
    val buy: Double,
    val sell: Double,
    val stock: Int
)

class MainActivity : ComponentActivity() {

    private val products = mutableStateListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("shop_data", MODE_PRIVATE)

        fun loadProducts() {
            products.clear()
            val array = JSONArray(prefs.getString("products", "[]"))
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                products.add(
                    Product(
                        item.getString("name"),
                        item.getString("code"),
                        item.getDouble("buy"),
                        item.getDouble("sell"),
                        item.getInt("stock")
                    )
                )
            }
        }

        fun saveProducts() {
            val array = JSONArray()
            products.forEach {
                array.put(JSONObject().apply {
                    put("name", it.name)
                    put("code", it.code)
                    put("buy", it.buy)
                    put("sell", it.sell)
                    put("stock", it.stock)
                })
            }
            prefs.edit().putString("products", array.toString()).apply()
        }

        loadProducts()

        setContent {
            var name by remember { mutableStateOf("") }
            var code by remember { mutableStateOf("") }
            var buy by remember { mutableStateOf("") }
            var sell by remember { mutableStateOf("") }
            var stock by remember { mutableStateOf("") }
            var search by remember { mutableStateOf("") }
            var message by remember { mutableStateOf("") }

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("মেঘমালা কসমেটিকস") }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        Text("পণ্য যোগ করুন", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("পণ্যের নাম") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("বারকোড বা পণ্য কোড") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = buy,
                            onValueChange = { buy = it },
                            label = { Text("ক্রয়মূল্য") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = sell,
                            onValueChange = { sell = it },
                            label = { Text("বিক্রয়মূল্য") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("স্টকের পরিমাণ") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val b = buy.toDoubleOrNull()
                                val s = sell.toDoubleOrNull()
                                val q = stock.toIntOrNull()

                                if (name.isBlank() || b == null || s == null ||
                                    q == null || b < 0 || s < 0 || q < 0
                                ) {
                                    message = "সব তথ্য সঠিকভাবে পূরণ করো"
                                } else if (products.any {
                                        it.code == code.trim() && code.isNotBlank()
                                    }
                                ) {
                                    message = "এই পণ্য কোড আগে থেকেই আছে"
                                } else {
                                    products.add(
                                        Product(name.trim(), code.trim(), b, s, q)
                                    )
                                    saveProducts()
                                    name = ""
                                    code = ""
                                    buy = ""
                                    sell = ""
                                    stock = ""
                                    message = "পণ্য যোগ হয়েছে"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("পণ্য সংরক্ষণ")
                        }

                        if (message.isNotBlank()) {
                            Text(message)
                        }

                        Spacer(Modifier.height(12.dp))

                        Text("পণ্যের তালিকা", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            label = { Text("নামের প্রথম অক্ষর বা কোড দিয়ে খুঁজুন") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        val filtered = products.filter {
                            it.name.contains(search, ignoreCase = true) ||
                                it.code.contains(search, ignoreCase = true)
                        }

                        Text("মোট পণ্য: ${products.size}")

                        LazyColumn {
                            items(filtered) { product ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                                        Text("কোড: ${product.code}")
                                        Text("ক্রয়মূল্য: ৳${product.buy}")
                                        Text("বিক্রয়মূল্য: ৳${product.sell}")
                                        Text("স্টক: ${product.stock}")
                                        Text("সম্ভাব্য লাভ/পিস: ৳${product.sell - product.buy}")
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
