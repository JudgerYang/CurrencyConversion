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

class CurrencyApi(context: Context, private val accessKey: String) {
    private val gson = Gson()
    private var requestQueue: RequestQueue = Volley.newRequestQueue(context)

    fun requestList(consumer: Consumer<List<CurrencyData>>, errorHandler: Consumer<Throwable>) {
        val url = buildApiUrl("list").build().toString()

        val stringRequest = StringRequest(url, Response.Listener<String> { json ->
            val result = gson.fromJson(json, CurrencyListResult::class.java)
            val currencyList = result.currencies.entries.flatMap { (shortName, longName) ->
                listOf(CurrencyData(shortName, longName))
            }
            consumer.accept(currencyList)
        }, Response.ErrorListener { error ->
            errorHandler.accept(error)
        })

        requestQueue.add(stringRequest)
    }

    fun requestExchange(source: String, consumer: Consumer<List<ExchangeData>>, errorHandler: Consumer<Throwable>) {
        val url = buildApiUrl("live").appendQueryParameter("source", source).build().toString()

        val stringRequest = StringRequest(url, Response.Listener<String> { json ->
            val result = gson.fromJson(json, LiveResult::class.java)
            val exchangeList = result.quotes.entries.flatMap { (quote, rate) ->
                val to = quote.removeSuffix(source)
                listOf(ExchangeData(to, rate))
            }
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
    val longName: String
)

data class LiveResult(
    val quotes: Map<String, Float>
)

data class ExchangeData(
    val to: String,
    val rate: Float
)