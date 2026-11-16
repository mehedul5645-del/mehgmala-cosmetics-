earentpBaielielieldthttoarcifiro

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
    val id: Long,
    val name: String,
    val code: String,
    val buy: Double,
    val sell: Double,
    val stock: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("meghmala_data", MODE_PRIVATE)
        val products = mutableStateListOf<Product>()
        var totalSales by mutableDoubleStateOf(
            prefs.getFloat("sales", 0f).toDouble()
        )
        var totalProfit by mutableDoubleStateOf(
            prefs.getFloat("profit", 0f).toDouble()
        )

        fun save() {
            val array = JSONArray()
            products.forEach {
                array.put(JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("code", it.code)
                    put("buy", it.buy)
                    put("sell", it.sell)
                    put("stock", it.stock)
                })
            }
            prefs.edit()
                .putString("products", array.toString())
                .putFloat("sales", totalSales.toFloat())
                .putFloat("profit", totalProfit.toFloat())
                .apply()
        }

        val saved = JSONArray(prefs.getString("products", "[]"))
        for (i in 0 until saved.length()) {
            val p = saved.getJSONObject(i)
            products.add(
                Product(
                    p.getLong("id"),
                    p.getString("name"),
                    p.getString("code"),
                    p.getDouble("buy"),
                    p.getDouble("sell"),
                    p.getInt("stock")
                )
            )
        }

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
                        TopAppBar(title = { Text("মেঘমালা কসমেটিকস") })
                    }
                ) { padding ->
                    Column(
                        Modifier
                            .padding(padding)
                            .padding(12.dp)
                            .fillMaxSize()
                    ) {
                        Text("মোট বিক্রি: ৳${"%.2f".format(totalSales)}")
                        Text("মোট লাভ: ৳${"%.2f".format(totalProfit)}")

                        HorizontalDivider()

                        Text("নতুন পণ্য", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            name, { name = it },
                            label = { Text("পণ্যের নাম") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            code, { code = it },
                            label = { Text("পণ্য কোড / বারকোড") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            buy, { buy = it },
                            label = { Text("ক্রয়মূল্য") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            sell, { sell = it },
                            label = { Text("বিক্রয়মূল্য") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            stock, { stock = it },
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
                                    message = "সঠিক তথ্য দাও"
                                } else if (code.isNotBlank() &&
                                    products.any { it.code == code.trim() }
                                ) {
                                    message = "এই কোড আগে থেকেই আছে"
                                } else {
                                    products.add(
                                        Product(
                                            System.currentTimeMillis(),
                                            name.trim(),
                                            code.trim(),
                                            b, s, q
                                        )
                                    )
                                    save()
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

                        Text(message)

                        HorizontalDivider()

                        Text("পণ্য খুঁজুন", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            search, { search = it },
                            label = { Text("নামের প্রথম অক্ষর বা কোড") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        LazyColumn {
                            items(products.filter {
                                it.name.contains(search, true) ||
                                    it.code.contains(search, true)
                            }) { product ->
                                var quantity by remember(product.id) {
                                    mutableStateOf("1")
                                }

                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                ) {
                                    Column(Modifier.padding(10.dp)) {
                                        Text(
                                            product.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("কোড: ${product.code}")
                                        Text("বিক্রয়মূল্য: ৳${product.sell}")
                                        Text("বর্তমান স্টক: ${product.stock}")

                                        OutlinedTextField(
                                            quantity,
                                            { quantity = it },
                                            label = { Text("বিক্রির পরিমাণ") }
                                        )

                                        Button(
                                            onClick = {
                                                val q = quantity.toIntOrNull()
                                                if (q == null || q <= 0) {
                                                    message = "সঠিক পরিমাণ দাও"
                                                } else if (q > product.stock) {
                                                    message = "পর্যাপ্ত স্টক নেই"
                                                } else {
                                                    val index = products.indexOfFirst {
                                                        it.id == product.id
                                                    }
                                                    if (index >= 0) {
                                                        val updated = product.copy(
                                                            stock = product.stock - q
                                                        )
                                                        products[index] = updated
                                                        totalSales += product.sell * q
                                                        totalProfit +=
                                                            (product.sell - product.buy) * q
                                                        save()
                                                        message = "বিক্রি সফল হয়েছে"
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("বিক্রি করুন")
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
}
