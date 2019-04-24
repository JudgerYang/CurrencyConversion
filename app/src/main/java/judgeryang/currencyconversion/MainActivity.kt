package judgeryang.currencyconversion

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private var api: CurrencyApi? = null
    private var adapter: CurrencyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        api = CurrencyApi(this, getString(R.string.api_access_key))

        initUi()
        requestCurrencyList()
    }

    private fun initUi() {
        currency_from.requestFocus()
        currency_from.setOnClickListener {
            currency_from.isSelected = true
            currency_to.isSelected = false
            currency_list.visibility = View.VISIBLE
        }

        currency_to.setOnClickListener {
            currency_from.isSelected = false
            currency_to.isSelected = true
            currency_list.visibility = View.VISIBLE
        }

        adapter = CurrencyAdapter(currencyAdapterListener)
        currency_list.adapter = adapter
        currency_list.layoutManager = LinearLayoutManager(this)
    }

    private fun showWaitingDialog() {

    }

    private val currencyAdapterListener: Listener = object : Listener() {
        override fun onCurrencyClick(data: CurrencyData) {
            currency_list.visibility = View.GONE

            if (currency_from.isSelected) {
                currency_from.text = data.shortName
                currency_from.isSelected = false
                requestExchange(data)
            } else if (currency_to.isSelected) {
                currency_to.text = data.shortName
                currency_to.isSelected = false
            }
        }
    }

    private fun requestCurrencyList() {
        api?.requestList(Consumer { currencyList ->
            runOnUiThread {
                currency_from.isEnabled = true
                currency_to.isEnabled = true
                adapter?.setCurrencyList(currencyList)
            }
        }, Consumer { error ->
            Log.e(TAG, error.toString())
        })
    }

    private fun requestExchange(data: CurrencyData) {
        api?.requestExchange(data.shortName, Consumer { exchangeList ->
            runOnUiThread {
                adapter?.setExchangeList(exchangeList)
            }
        }, Consumer {error ->
            Log.e(TAG, error.toString())
        })
    }
}
