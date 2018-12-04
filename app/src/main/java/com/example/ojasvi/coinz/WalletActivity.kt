package com.example.ojasvi.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_wallet.*

class WalletActivity : AppCompatActivity() {

    private  lateinit var adapter: RecyclerAdapter
    private lateinit var  linearLayoutManager: LinearLayoutManager
    val fakeData = ArrayList<Coin>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        val recyclerView = findViewById<RecyclerView>(R.id.listOfCoins)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        fakeData.add(Coin("SHIL", "1.2","h"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))
        fakeData.add(Coin("SHIL", "1.3","k"))



        adapter = RecyclerAdapter(fakeData)
        recyclerView.adapter = adapter

    }

}
