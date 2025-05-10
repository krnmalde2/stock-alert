package com.example.stockalert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stockalert.ui.theme.StockAlertTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class WatchRequest(val companies: List<String>, val keywords: List<String>)
data class Announcement(val company: String, val subject: String, val date: String)
data class WatchResponse(val matches: List<Announcement>)

interface StockApi {
    @POST("check-announcements")
    suspend fun checkAnnouncements(@Body request: WatchRequest): WatchResponse
}

class MainActivity : ComponentActivity() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val stockApi = retrofit.create(StockApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockAlertTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WatchlistScreen(stockApi)
                }
            }
        }
    }
}

@Composable
fun WatchlistScreen(stockApi: StockApi) {
    var result by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Watchlist", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val request = WatchRequest(
                companies = listOf("RELIANCE", "TCS"),
                keywords = listOf("financial result", "dividend", "Outcome of Board meeting")
            )
            scope.launch {
                try {
                    val response = stockApi.checkAnnouncements(request)
                    result = response.matches
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }) {
            Text("Check Announcements")
        }

        Spacer(modifier = Modifier.height(16.dp))

        result.forEach {
            Text("${it.date} - ${it.company}: ${it.subject}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}