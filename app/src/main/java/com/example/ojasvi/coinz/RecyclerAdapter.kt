package com.example.ojasvi.coinz

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.FieldPosition

class RecyclerAdapter(private val coins: ArrayList<Coin>) : RecyclerView.Adapter<RecyclerAdapter.CoinHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.CoinHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return CoinHolder(inflatedView)
    }

    override fun getItemCount(): Int = coins.size

    override fun onBindViewHolder(holder: RecyclerAdapter.CoinHolder, position: Int) {
        val coinType = holder.itemView.findViewById<TextView>(R.id.coinType)
        val coinValue = holder.itemView.findViewById<TextView>(R.id.coinVal)
        val  coin = coins[position]
        coinType.text = coin.currency
        coinValue.text = coin.value
    }


    class CoinHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
            //2
            private var view: View = v
            private var coin: Coin? = null

            //3
            init {
                v.setOnClickListener(this)
            }

            //4
            override fun onClick(v: View) {
                Log.d("RecyclerView", "CLICK!")
            }

            companion object {
                //5
                private val COIN_KEY = "COIN"
            }
        }
}
