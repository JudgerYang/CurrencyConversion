package judgeryang.currencyconversion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private var api: CurrencyApi? = null
    private var adapter: CurrencyAdapter? = null
    private var currentData: CurrencyData? = null

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
            adapter?.showExchangeRate = false
        }

        currency_to.setOnClickListener {
            currency_from.isSelected = false
            currency_to.isSelected = true
            currency_list.visibility = View.VISIBLE
            adapter?.showExchangeRate = true
        }

        edit_amount.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                exchange()
            }
        })

        adapter = CurrencyAdapter(currencyAdapterListener)
        currency_list.adapter = adapter
        currency_list.layoutManager = LinearLayoutManager(this)
    }

    private val currencyAdapterListener: Listener = object : Listener() {
        override fun onCurrencyClick(data: CurrencyData) {
            currency_list.visibility = View.GONE

            if (currency_from.isSelected) {
                currency_from.text = data.shortName
                currency_from.isSelected = false

                edit_result.setText(R.string.default_amount)

                requestExchange(data)
            } else if (currency_to.isSelected) {
                currency_to.text = data.shortName
                currency_to.isSelected = false

                currentData = data
                exchange()
            }
        }
    }

    private fun exchange() {
        try {
            val rate: Float = currentData?.rate ?: 0f
            val amount: Float = edit_amount.text.toString().toFloat()
            edit_result.setText((amount * rate).toString())
        } catch (e: NumberFormatException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            edit_result.setText(R.string.default_amount)
        }
    }

    private fun requestCurrencyList() {
        currency_from.text = getString(R.string.loading)
        currency_from.isEnabled = false

        api?.requestList(Consumer { currencyList ->
            runOnUiThread {
                currency_from.text = getString(R.string.default_from)
                currency_from.isEnabled = true
                adapter?.setCurrencyList(currencyList)
            }
        }, Consumer { error ->
            Log.e(TAG, error.toString())
        })
    }

    private fun requestExchange(data: CurrencyData) {
        currency_to.text = getString(R.string.loading)
        currency_to.isEnabled = false

        api?.requestExchange(data.shortName, Consumer { exchangeList ->
            runOnUiThread {
                currency_to.text = getString(R.string.default_to)
                currency_to.isEnabled = true
                adapter?.setExchangeList(exchangeList)
            }
        }, Consumer {error ->
            Log.e(TAG, error.toString())
        })
    }
}
