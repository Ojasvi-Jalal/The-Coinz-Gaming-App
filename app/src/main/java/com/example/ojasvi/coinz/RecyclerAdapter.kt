package com.example.ojasvi.coinz

import android.content.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.FieldPosition
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecyclerAdapter(private val coins: ArrayList<Coin>) : RecyclerView.Adapter<RecyclerAdapter.CoinHolder>() {

    private var mAuth: FirebaseAuth? = null
    private var depositCoin: FirebaseFirestore? = null

    private var depositCount = 0

    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private var lastDownloadDate = ""

    private val date = LocalDate.now()
    private val formatDate = DateTimeFormatter.ofPattern("uuuu/MM/dd")
    private val formattedDate = date.format(formatDate)

    private lateinit var  linearLayoutManager: LinearLayoutManager

    private var displayCoins = ArrayList<Coin>()

    fun RecyclerAdapter(displayCoins: ArrayList<Coin>, lastDownloadDate: String) {
        this.displayCoins = displayCoins
        this.lastDownloadDate = lastDownloadDate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.CoinHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return CoinHolder(inflatedView)
    }

    override fun getItemCount(): Int = coins.size

    override fun onBindViewHolder(holder: RecyclerAdapter.CoinHolder, position: Int) {
        val coinType = holder.itemView.findViewById<TextView>(R.id.coinType)
        val coinValue = holder.itemView.findViewById<TextView>(R.id.coinVal)
        val depositButton: Button = holder.itemView.findViewById<Button>(R.id.depositOrGift)
        val  coin = coins[position]
        coinType.text = coin.currency
        coinValue.text = "%.5f".format(coin.value.toDouble())

        var adapter: RecyclerAdapter = com.example.ojasvi.coinz.RecyclerAdapter(displayCoins)

        depositCoin = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //get the recycler view ready
        val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.listOfCoins)

        depositButton.setOnClickListener{
            //if(depositCount<=25&&lastDownloadDate==formattedDate) {
                    depositCoin?.collection("wallets")?.document(mAuth?.uid!!)?.collection("account")?.add(coin)
                    depositCount++
                    depositCoin?.collection("wallets")?.document(mAuth?.uid!!)?.collection("wallet")?.get()?.addOnCompleteListener { task ->
                        if (task.result != null)
                            for (document in task.result!!)
                                if (coin.id == document.toObject(Coin::class.java).id)
                                    document.reference.delete()
                    }
//                }
//                else
//                {
//                    if(lastDownloadDate!=formattedDate) {
//                        depositCoin?.collection("wallets")?.document(mAuth?.uid!!)?.collection("account")?.add(coin)
//                        depositCount++
//                        depositCoin?.collection("wallets")?.document(mAuth?.uid!!)?.collection("wallet")?.get()?.addOnCompleteListener { task ->
//                            if (task.result != null)
//                                for (document in task.result!!)
//                                    if (coin.id == document.toObject(Coin::class.java).id)
//                                        document.reference.delete()
//                        }
//                    }
//
//                }

            }
        displayCoins.remove(coin)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position,displayCoins.size)
        adapter = com.example.ojasvi.coinz.RecyclerAdapter(displayCoins)
        recyclerView?.adapter = adapter
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
