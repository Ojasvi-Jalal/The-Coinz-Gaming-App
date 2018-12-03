package com.example.ojasvi.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.find

class MainMenuActivity : AppCompatActivity() {

    private var play_game: TextView? = null
    private var leaderBoard: TextView? = null
    private var shop: TextView? = null
    private var bank: TextView? = null
    private var quitGame: TextView? = null
    private var logOut: TextView? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        play_game = findViewById(R.id.Play)
        leaderBoard = findViewById(R.id.scoreboard)
        shop = findViewById(R.id.shop)
        bank = findViewById(R.id.bank)
        quitGame = findViewById(R.id.quit)
        logOut = findViewById(R.id.logout)

        play_game!!.isEnabled = true
        leaderBoard!!.isEnabled = true
        shop!!.isEnabled = true
        bank!!.isEnabled = true
        quitGame!!.isEnabled = true
        logOut!!.isEnabled = true

        mAuth = FirebaseAuth.getInstance()

        play_game!!.setOnClickListener(){
            Log.d(TAG,"User clicked on Play")
            startGame()
        }

        leaderBoard!!.setOnClickListener(){
            Log.d(TAG,"User clicked on \"LeaderBoard\"")
            openLeaderBoard()
        }

        shop!!.setOnClickListener(){
            Log.d(TAG,"User clicked on \"shop\"")
        }

        bank!!.setOnClickListener(){
            Log.d(TAG,"User clicked on \"bank\"")
        }

        quitGame!!.setOnClickListener(){
            Log.d(TAG,"User clicked on \"quit\"")
            quit()
        }

        logOut!!.setOnClickListener(){
            Log.d(TAG,"User clicked on \"logOut\"")
            openLoginPage()
        }

    }

    fun startGame(){
        Log.d(TAG,"Starting the game")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun openLoginPage(){
        Log.d(TAG,"Opening the login screen")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun quit(){
        Log.d(TAG,"Quitting the application")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun openLeaderBoard(){
        Log.d(TAG,"LeaderBoard")
        val intent = Intent(this, LeaderboardActivity::class.java)
        startActivity(intent)
    }

    companion object {
        var TAG = "Main Menu"
    }
}
