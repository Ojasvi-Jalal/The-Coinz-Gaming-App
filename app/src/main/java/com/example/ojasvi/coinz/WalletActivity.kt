package com.example.ojasvi.coinz

import android.content.Context
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

class WalletActivity : AppCompatActivity() {

    private  lateinit var adapter: RecyclerAdapter
    private lateinit var  linearLayoutManager: LinearLayoutManager
    //val fakeData = ArrayList<Coin>()
    private val coins = ArrayList<Coin>()
    private val displayCoins = ArrayList<Coin>()
    //Buttons: choosing the type of coins to be displayed

    private var shil_Button: ImageView? = null
    private var dolr_Button: ImageView? = null
    private var quid_Button: ImageView? = null
    private var peny_Button: ImageView? = null
    private var wallet_Button: ImageView? = null
    private var deposit_Button: ImageView? = null

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
        deposit_Button = findViewById(R.id.depositOrGift)

        //get all the coins in the wallet
        wallet?.collection(WalletActivity.COLLECTION_KEY)?.document(mAuth?.uid!!)?.collection("wallet")?.get()?.addOnCompleteListener { task ->
            if (task.result != null)
                for (document in task.result!!)
                    coins.add(document.toObject(Coin::class.java))
        }

        //get the recycler view ready
        val recyclerView = findViewById<RecyclerView>(R.id.listOfCoins)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager


        //show all the coins in the wallet
        wallet_Button!!.setOnClickListener{
            displayCoins.clear()
            Log.d(TAG,"Displaying all coins")
            for(coin in coins)
                displayCoins.add(coin)
            adapter = RecyclerAdapter(displayCoins)
            recyclerView.adapter = adapter
        }
        //filter the wallet and show the relevant coins
        shil_Button!!.setOnClickListener {
            displayCoins.clear()
            Log.d(TAG, "Displaying shils")
            for(coin in coins)
                if (coin.currency == "SHIL")
                    displayCoins.add(coin)
            adapter = RecyclerAdapter(displayCoins)
            recyclerView.adapter = adapter
        }


        dolr_Button!!.setOnClickListener {

            displayCoins.clear()
            Log.d(TAG, "Displaying dolrs")
            for(coin in coins)
                if (coin.currency == "DOLR")
                    displayCoins.add(coin)
            adapter = RecyclerAdapter(displayCoins)
            recyclerView.adapter = adapter
        }


        quid_Button!!.setOnClickListener {

            displayCoins.clear()
            Log.d(TAG, "Displaying quids")
            for(coin in coins)
                if (coin.currency == "QUID")
                    displayCoins.add(coin)
            adapter = RecyclerAdapter(displayCoins)
            recyclerView.adapter = adapter
        }


        peny_Button!!.setOnClickListener {

            displayCoins.clear()
            Log.d(TAG, "Displaying penys")
            for(coin in coins)
                if (coin.currency == "PENY")
                    displayCoins.add(coin)
            adapter = RecyclerAdapter(displayCoins)
            recyclerView.adapter = adapter
        }


    }
}
