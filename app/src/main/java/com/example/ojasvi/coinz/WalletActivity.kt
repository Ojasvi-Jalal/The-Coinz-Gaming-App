package com.example.ojasvi.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.android.synthetic.main.recyclerview_item_row.*

class WalletActivity : AppCompatActivity() {

    private  lateinit var adapter: RecyclerAdapter
    private lateinit var  linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView

    //val fakeData = ArrayList<Coin>()
    private var coins = ArrayList<Coin>()
    private val displayCoins = ArrayList<Coin>()
    //Buttons: choosing the type of coins to be displayed

    private var shil_Button: ImageView? = null
    private var dolr_Button: ImageView? = null
    private var quid_Button: ImageView? = null
    private var peny_Button: ImageView? = null
    private var wallet_Button: ImageView? = null
    private var gift_Button: ImageView? = null
    private var location:String? = ""

    //Firebase
    //wallet
    private var wallet: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    //private var depositCoin: FirebaseFirestore? = null

    private val preferencesFile = "MyPrefsFile" // for storing preferences


    companion object {
        private val TAG = "WalletActivity"
        private const val COLLECTION_KEY = "wallets"
        private const val DEPOSIT_KEY = "bank"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        wallet = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //Restore preferences and get the rates to do conversion
        val prefSettings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val downloadDate = prefSettings?.getString("lastDownloadDate","")

        //set the buttons

        wallet_Button = findViewById(R.id.wallet)
        shil_Button = findViewById(R.id.shilWallet)
        dolr_Button = findViewById(R.id.dolrWallet)
        quid_Button = findViewById(R.id.quidWallet)
        peny_Button = findViewById(R.id.penyWallet)
        gift_Button = findViewById(R.id.gift)

        //get all the coins in the wallet
        val ref = wallet?.collection(WalletActivity.COLLECTION_KEY)?.document(mAuth?.currentUser?.email!!)
//        ref?.collection("wallet")?.get()?.addOnCompleteListener { task ->
//            if (task.result != null)
//                for (document in task.result!!)
//                    coins.add(document.toObject(Coin::class.java))
//        }

        ref?.collection("wallet")?.addSnapshotListener{ snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
            }

            coins = ArrayList()
            for (document in snapshot!!)
                    coins.add(document.toObject(Coin::class.java))
            Log.d(TAG, "Wallet modified")

            if(location == "wallet"){
                displayWallet()
            }

            if(location == "peny"){
                displayPenys()
            }

            if(location == "dollar"){
                displayDolrs()
            }

            if(location == "quid"){
                displayQuids()
            }

            if(location == "shil"){
                displayShils()
            }


        }

        //get the recycler view ready
        recyclerView = findViewById<RecyclerView>(R.id.listOfCoins)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager


        //show all the coins in the wallet
        wallet_Button!!.setOnClickListener{
            displayCoins.clear()
            Log.d(TAG,"Displaying all coins")
            displayWallet()
        }
        //filter the wallet and show the relevant coins
        shil_Button!!.setOnClickListener {
            Log.d(TAG, "Displaying shils")
            displayShils()
        }

        dolr_Button!!.setOnClickListener {
            Log.d(TAG, "Displaying dolrs")
            displayDolrs()
        }

        quid_Button!!.setOnClickListener {
            Log.d(TAG, "Displaying quids")
            displayQuids()
        }

        peny_Button!!.setOnClickListener {
            Log.d(TAG, "Displaying penys")
            displayPenys()
        }

        gift_Button!!.setOnClickListener{
            Log.d(TAG,"User wants to gift the coins")
            intent = Intent(this,GiftActivity::class.java)
            startActivity(intent)
        }

    }

    fun displayPenys(){
        displayCoins.clear()
        for(coin in coins)
            if (coin.currency == "PENY")
                displayCoins.add(coin)
        adapter = RecyclerAdapter(displayCoins)
        recyclerView.adapter = adapter
        location = "peny"
    }
    fun displayDolrs(){
        displayCoins.clear()
        for(coin in coins)
            if (coin.currency == "DOLR")
                displayCoins.add(coin)
        adapter = RecyclerAdapter(displayCoins)
        recyclerView.adapter = adapter
        location = "dollar"
    }
    fun displayShils(){
        displayCoins.clear()
        for(coin in coins)
            if (coin.currency == "SHIL")
                displayCoins.add(coin)
        adapter = RecyclerAdapter(displayCoins)
        recyclerView.adapter = adapter
        location = "shil"
    }
    fun displayQuids(){
        displayCoins.clear()
        for(coin in coins)
            if (coin.currency == "QUID")
                displayCoins.add(coin)
        adapter = RecyclerAdapter(displayCoins)
        recyclerView.adapter = adapter
        location = "quid"
    }

    fun displayWallet(){
        displayCoins.clear()
        displayCoins.addAll(coins)
        adapter = RecyclerAdapter(displayCoins)
        recyclerView.adapter = adapter
        location = "wallet"
    }
}
