package com.example.ojasvi.coinz

import android.os.Bundle
import android.app.Activity
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_leaderboard.*
import org.w3c.dom.Text

class LeaderboardActivity : Activity() {


    var currentUser: TextView?= null
    var curUserScore: TextView? = null
    private var userAndScores: MutableMap<TextView,TextView> = hashMapOf()

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

    }

}
