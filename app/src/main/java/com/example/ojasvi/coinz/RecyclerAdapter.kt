package com.example.ojasvi.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecyclerAdapter(private val coins: ArrayList<Coin>, context: Context) : RecyclerView.Adapter<RecyclerAdapter.CoinHolder>() {

    //firebase and firestore objects to access the database
    private var mAuth: FirebaseAuth? = null
    private var depositCoin: FirebaseFirestore? = null

    //keep track of how many coins deposited
    private var depositCount = 0


    private val preferencesFile = "MyPrefsFile" // for storing preferences

    private var lastDownloadDate = ""

    //the context to be able to get access to the sharedPreferences file
    private var walletContext = context

    //gets the current date
    private val date = LocalDate.now()
    private val formatDate = DateTimeFormatter.ofPattern("uuuu/MM/dd")
    private val formattedDate = date.format(formatDate)

    //keep track of the balance
    private var addBalance = BigDecimal(0)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.CoinHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return CoinHolder(inflatedView)
    }

    override fun getItemCount(): Int = coins.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerAdapter.CoinHolder, position: Int) {
       //sets the fields to the relevant views in the layout
        val coinType = holder.itemView.findViewById<TextView>(R.id.coinType)
        val coinValue = holder.itemView.findViewById<TextView>(R.id.coinVal)
        val depositButton = holder.itemView.findViewById<Button>(R.id.deposit)

        val  coin = coins[position]

        //get the coin currency and it's value
        coinType.text = coin.currency
        coinValue.text = "%.5f".format(coin.value.toDouble())

        //get the firestore and firebase instances
        depositCoin = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val ref = depositCoin?.collection("wallets")?.document(mAuth?.currentUser?.email!!)

        //Restore preferences and get the rates to do conversion
        val prefSettings = walletContext.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

        lastDownloadDate = prefSettings?.getString("lastDownloadDate", "")!!
        val shilRate = prefSettings.getString("shilRate","0")?.toBigDecimal()
        Log.d(TAG,shilRate.toString())
        val dolrRate = prefSettings.getString("dolrRate","0")?.toBigDecimal()
        Log.d(TAG,dolrRate.toString())
        val penyRate = prefSettings.getString("penyRate","0")?.toBigDecimal()
        Log.d(TAG,penyRate.toString())
        val quidRate = prefSettings.getString("quidRate","0")?.toBigDecimal()
        Log.d(TAG,quidRate.toString())

        //set the deposit count to 0 if it's a new day
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

        //user wants to deposit the coin
        depositButton.setOnClickListener{
            //if deposit count > 25 and it's the same day don't let the user deposit the coin
            if(depositCount<=25&&lastDownloadDate==formattedDate) {
                    ref?.collection("account")?.add(coin)
                    depositCount++
                    ref?.collection("wallet")?.get()?.addOnCompleteListener { task ->
                        if (task.result != null) {
                            for (document in task.result!!) {
                                val coinFirestore = document.toObject(Coin::class.java)
                                //remove the coin from the wallet, convert it into gold and add it
                                //to the current balance
                                if (coin.id == coinFirestore.id)
                                    document.reference.delete()
                                if (shilRate != null) {
                                    if (coin.currency == "SHIL")
                                        addBalance += shilRate * coinFirestore.value.toBigDecimal()
                                    Log.d(TAG, addBalance.toString())
                                }
                                if (penyRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "PENY")
                                        addBalance += penyRate * coinFirestore.value.toBigDecimal()
                                    Log.d(TAG, addBalance.toString())
                                }
                                if (dolrRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "DOLR")
                                        addBalance += dolrRate * coinFirestore.value.toBigDecimal()
                                    Log.d(TAG, addBalance.toString())
                                }
                                if (quidRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "QUID")
                                        addBalance += quidRate * coinFirestore.value.toBigDecimal()
                                    Log.d(TAG, addBalance.toString())
                                }
                            }
                        }

                        //update the available funds file with the new added balance
                        val netWorth = HashMap<String, Any?>()
                        netWorth["Account Balance"] = addBalance.toLong()
                        ref.collection("User info")
                            .document("Available funds")
                            .set(netWorth)
                            .addOnSuccessListener { Log.d(TAG, "Account Balance successfully written!") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)}
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

    companion object {
        var TAG = "depositingCoin/RecyclerAdapter"
    }


    class CoinHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
            init {
                v.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                Log.d("RecyclerView", "CLICK!")
            }
        }
}
