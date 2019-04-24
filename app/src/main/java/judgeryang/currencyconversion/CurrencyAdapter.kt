package judgeryang.currencyconversion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.recyclerview.widget.RecyclerView


class CurrencyAdapter(val listener: Listener) : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {
    var showExchangeRate: Boolean = false
    private val currencyMap: MutableMap<String, CurrencyData> = mutableMapOf()

    @UiThread
    fun setCurrencyList(map: Map<String, CurrencyData>) {
        currencyMap.clear()
        currencyMap.putAll(map)
        notifyDataSetChanged()
    }

    @UiThread
    fun setExchangeList(exchangeList: List<CurrencyData>) {
        exchangeList.forEach { data ->
            currencyMap[data.shortName]?.rate = data.rate
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.currency_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = currencyMap.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currencyMap.values.elementAt(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val currencyShort = itemView.findViewById<TextView>(R.id.currency_short)
        private val currencyFull = itemView.findViewById<TextView>(R.id.currency_full)
        private val exchangeRate = itemView.findViewById<TextView>(R.id.exchange_rate)

        fun bind(data: CurrencyData) {
            currencyShort.text = data.shortName
            currencyFull.text = data.fullName

            if (showExchangeRate && data.rate != null) {
                exchangeRate.visibility = View.VISIBLE
                exchangeRate.text = data.rate.toString()
            } else {
                exchangeRate.visibility = View.GONE
            }

            itemView.setOnClickListener {
                listener.onCurrencyClick(data)
            }
        }
    }
}

abstract class Listener {
    abstract fun onCurrencyClick(data: CurrencyData)
}
