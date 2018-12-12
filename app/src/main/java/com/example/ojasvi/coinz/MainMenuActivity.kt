package com.example.ojasvi.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class MainMenuActivity : AppCompatActivity() {

    //intilise the links to the options
    private var playGame: TextView? = null
    private var leaderBoard: TextView? = null
    private var shop: TextView? = null
    private var bank: TextView? = null
    private var quitGame: TextView? = null
    private var logOut: TextView? = null

    //initialise firebase auth
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        //sets the layout to be activity_main_menu
        setContentView(R.layout.activity_main_menu)

        //set the clickable fields according to the views in the relevant layout
        playGame = findViewById(R.id.Play)
        leaderBoard = findViewById(R.id.scoreboard)
        shop = findViewById(R.id.shop)
        bank = findViewById(R.id.bank)
        quitGame = findViewById(R.id.quit)
        logOut = findViewById(R.id.logout)

        //Fields are clickable == true
        playGame!!.isEnabled = true
        leaderBoard!!.isEnabled = true
        shop!!.isEnabled = true
        bank!!.isEnabled = true
        quitGame!!.isEnabled = true
        logOut!!.isEnabled = true

        //set the firebase auth
        mAuth = FirebaseAuth.getInstance()

        //when user clicks on Play
        playGame!!.setOnClickListener{
            Log.d(TAG,"User clicked on Play")
            startGame()
        }

        //when user clicks on LeaderBoard
        leaderBoard!!.setOnClickListener{
            Log.d(TAG,"User clicked on \"LeaderBoard\"")
            openLeaderBoard()
        }

        //when user clicks on Shop
        shop!!.setOnClickListener{
            Log.d(TAG,"User clicked on \"shop\"")
            goToShop()
        }

        //when user clicks on Bank
        bank!!.setOnClickListener{
            Log.d(TAG,"User clicked on \"bank\"")
            goToBank()
        }

        //when user clicks on Quit
        quitGame!!.setOnClickListener{
            Log.d(TAG,"User clicked on \"quit\"")
            quit()
        }

        //when user clicks on LogOut
        logOut!!.setOnClickListener{
            Log.d(TAG,"User clicked on \"logOut\"")
            openLoginPage()
        }

    }

    private fun startGame(){
        //switch to the main activity i.e. the map activity
        Log.d(TAG,"Starting the game")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun openLoginPage(){
        //switch to the login activity
        Log.d(TAG,"Opening the login screen")
        val intent = Intent(this, LoginActivity::class.java)
        //USer signs out from firebase as well
        mAuth?.signOut()
        mAuth = null
        startActivity(intent)
    }

    fun quit(){
        //Quit the application and open the home screen
        Log.d(TAG,"Quitting the application")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun openLeaderBoard(){
        //switch to the leaderBoard activity
        Log.d(TAG,"LeaderBoard")
        val intent = Intent(this, LeaderboardActivity::class.java)
        startActivity(intent)
    }

    private fun goToBank(){
        //switch to the bank activity
        Log.d(TAG,"Bank")
        val intent = Intent(this,BankActivity::class.java)
        startActivity(intent)
    }

    private fun goToShop(){
        //switch to the shop activity
        Log.d(TAG,"Shop")
        val intent = Intent(this,ShoppingActivity::class.java)
        startActivity(intent)
    }
    companion object {
        var TAG = "Main Menu"
    }
}
