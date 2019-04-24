package judgeryang.currencyconversion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.recyclerview.widget.RecyclerView


class CurrencyAdapter(val listener: Listener) : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {
    private val currencyList: ArrayList<CurrencyData> = ArrayList()

    @UiThread
    fun setCurrencyList(list: List<CurrencyData>) {
        currencyList.clear()
        currencyList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.currency_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = currencyList.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currencyList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val currencyShort = itemView.findViewById<TextView>(R.id.currency_short)
        private val currencyLong = itemView.findViewById<TextView>(R.id.currency_long)

        fun bind(data: CurrencyData) {
            currencyShort.text = data.shortName
            currencyLong.text = data.longName
            itemView.setOnClickListener {
                listener.onCurrencyClick(data)
            }
        }
    }
}

abstract class Listener {
    abstract fun onCurrencyClick(data: CurrencyData)
}
