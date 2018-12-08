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
import java.math.BigDecimal
import java.text.FieldPosition
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecyclerAdapter(private val coins: ArrayList<Coin>, context: Context) : RecyclerView.Adapter<RecyclerAdapter.CoinHolder>() {

    private var mAuth: FirebaseAuth? = null
    private var depositCoin: FirebaseFirestore? = null

    private var depositCount = 0

    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private var lastDownloadDate = ""

    private var walletContext = context

    private val date = LocalDate.now()
    private val formatDate = DateTimeFormatter.ofPattern("uuuu/MM/dd")
    private val formattedDate = date.format(formatDate)

    private var addBalance = BigDecimal(0)

    private lateinit var  linearLayoutManager: LinearLayoutManager

    private var displayCoins = ArrayList<Coin>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.CoinHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return CoinHolder(inflatedView)
    }

    override fun getItemCount(): Int = coins.size

    override fun onBindViewHolder(holder: RecyclerAdapter.CoinHolder, position: Int) {
        val coinType = holder.itemView.findViewById<TextView>(R.id.coinType)
        val coinValue = holder.itemView.findViewById<TextView>(R.id.coinVal)
        val depositButton: Button = holder.itemView.findViewById<Button>(R.id.deposit)

        var TAG = "depositingCoin/RecyclerAdapter"
        val  coin = coins[position]
        coinType.text = coin.currency
        coinValue.text = "%.5f".format(coin.value.toDouble())

        depositCoin = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //get the recycler view ready
        val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.listOfCoins)

        val ref = depositCoin?.collection("wallets")?.document(mAuth?.currentUser?.email!!)

        //Restore preferences and get the rates to do conversion
        val prefSettings = walletContext.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

        lastDownloadDate = prefSettings?.getString("lastDownloadDate", "")!!
        val shilRate = prefSettings?.getString("shilRate","0")?.toBigDecimal()
        Log.d(TAG,shilRate.toString())
        val dolrRate = prefSettings?.getString("dolrRate","0")?.toBigDecimal()
        Log.d(TAG,dolrRate.toString())
        val penyRate = prefSettings?.getString("penyRate","0")?.toBigDecimal()
        Log.d(TAG,penyRate.toString())
        val quidRate = prefSettings?.getString("quidRate","0")?.toBigDecimal()
        Log.d(TAG,quidRate.toString())

        if(lastDownloadDate != formattedDate) {
            depositCount = 0
            depositButton.isEnabled = true
        }

        ref?.collection("User info")
                ?.document("Available funds")
                ?.get()
                ?.addOnSuccessListener {
                    if (it.exists() && it != null) {
                        addBalance = (it.getDouble("Account Balance")?.toLong())!!.toBigDecimal()
                    }
                }

        depositButton.setOnClickListener{
            if(depositCount<=25&&lastDownloadDate==formattedDate) {
                    ref?.collection("account")?.add(coin)
                    depositCount++
                    ref?.collection("wallet")?.get()?.addOnCompleteListener { task ->
                        if (task.result != null) {
                            for (document in task.result!!) {
                                var coinFirestore = document.toObject(Coin::class.java)
                                if (coin.id == coinFirestore.id)
                                    document.reference.delete()
                                if (shilRate != null) {
                                    if (coin.currency == "SHIL")
                                        addBalance += shilRate * coinFirestore.value?.toBigDecimal()
                                    Log.d(TAG, addBalance.toString())
                                }
                                if (penyRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "PENY")
                                        addBalance += penyRate * coinFirestore.value?.toBigDecimal()
                                    Log.d(TAG, addBalance.toString())
                                }
                                if (dolrRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "DOLR")
                                        addBalance += dolrRate * coinFirestore.value?.toBigDecimal()
                                    Log.d(TAG, addBalance.toString())
                                }
                                if (quidRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "QUID")
                                        addBalance += quidRate * coinFirestore.value?.toBigDecimal()
                                    Log.d(TAG, addBalance.toString())
                                }
                            }
                        }

                        val netWorth = HashMap<String, Any?>()
                        netWorth["Account Balance"] = addBalance.toLong()
                        ref?.collection("User info")
                            ?.document("Available funds")
                            ?.set(netWorth)
                            ?.addOnSuccessListener { Log.d(TAG, "Account Balance successfully written!") }
                            ?.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)}
                    }

                }
                else
                {
                    if(lastDownloadDate == formattedDate) {
                       depositButton.isEnabled = false
                        Log.d(TAG, "User's reached the daily deposit money")
                    }

                }

            }
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
