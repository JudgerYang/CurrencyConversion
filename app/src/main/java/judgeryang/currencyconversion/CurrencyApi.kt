package judgeryang.currencyconversion

import android.content.Context
import android.net.Uri
import androidx.core.util.Consumer
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson

const val BASE_URL = "https://apilayer.net/"
const val CACHE_EXPIRE = 30000  // 30 seconds

class CurrencyApi(context: Context, private val accessKey: String) {
    private val gson = Gson()
    private var requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private val exchangeCache: MutableMap<String, ExchangeCache> = mutableMapOf()

    fun requestList(consumer: Consumer<Map<String, CurrencyData>>, errorHandler: Consumer<Throwable>) {
        val url = buildApiUrl("list").build().toString()

        val stringRequest = StringRequest(url, Response.Listener<String> { json ->
            val result = gson.fromJson(json, CurrencyListResult::class.java)
            val map = result.currencies.mapValues { entry ->
                CurrencyData(entry.key, entry.value, null)
            }
            consumer.accept(map)
        }, Response.ErrorListener { error ->
            errorHandler.accept(error)
        })

        requestQueue.add(stringRequest)
    }

    fun requestExchange(source: String, consumer: Consumer<List<CurrencyData>>, errorHandler: Consumer<Throwable>) {
        val url = buildApiUrl("live").appendQueryParameter("source", source).build().toString()

        // Use cache to reduce user network usage
        val cache = exchangeCache[url]
        if (cache != null) {
            val now = System.currentTimeMillis()
            if (now - cache.timestamp < CACHE_EXPIRE) {
                // Cache not expired, use cache
                consumer.accept(cache.exchangeList)
                return
            }

            // Cache expired, remove cache entry
            exchangeCache.remove(url)
        }

        // No cache, send a live request
        val stringRequest = StringRequest(url, Response.Listener<String> { json ->
            val result = gson.fromJson(json, LiveResult::class.java)
            val exchangeList = result.quotes.entries.flatMap { (quote, rate) ->
                val to = quote.removePrefix(source)
                listOf(CurrencyData(to, to, rate))
            }

            // Add result list to cache
            exchangeCache[url] = ExchangeCache(System.currentTimeMillis(), exchangeList)

            consumer.accept(exchangeList)
        }, Response.ErrorListener { error ->
            errorHandler.accept(error)
        })

        requestQueue.add(stringRequest)
    }

    private fun buildApiUrl(apiName: String): Uri.Builder {
        return Uri.parse(BASE_URL).buildUpon()
            .path("api").appendPath(apiName)
            .appendQueryParameter("access_key", accessKey)
    }
}

data class CurrencyListResult(
    val success: Boolean,
    val currencies: Map<String, String>
)

data class CurrencyData(
    val shortName: String,
    val fullName: String,
    var rate: Float?
)

data class LiveResult(
    val quotes: Map<String, Float>
)

data class ExchangeCache(
    val timestamp: Long,
    val exchangeList: List<CurrencyData>
)
