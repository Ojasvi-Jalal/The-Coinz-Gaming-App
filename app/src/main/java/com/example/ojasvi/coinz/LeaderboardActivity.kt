package com.example.ojasvi.coinz

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_leaderboard.*

class LeaderboardActivity : Activity() {

    private var mAuth: FirebaseAuth? = null
    private var userScore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        userScore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val refUser = userScore?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)

        refUser?.collection("User info")
                ?.document("Available funds")
                ?.get()
                ?.addOnSuccessListener {
                    if(it.exists() && it != null) {
                        currentUserScore.visibility = View.VISIBLE
                        currentUserScore.text = it.getDouble("Account Balance")?.toLong().toString()
                    }
                }

        val ref = userScore?.collection("wallets")?.document("scores")

        ref?.get()
                ?.addOnCompleteListener {
                    if(it.result!=null){
                            val result = it.result
                        @Suppress("UNCHECKED_CAST")
                        val data = result!!.data as HashMap<String,Long>
                        val scores = data.toList().sortedByDescending { (_,value) -> value }.toMap()
                        val leaderboard = listOf<Pair<TextView,TextView>>(Pair(user1,score1)
                                ,Pair(user2,score2),Pair(user3,score3),Pair(user4,score4))
                        var i = 0
                        loop@ for (score in scores){
                            leaderboard[i].first.visibility = View.VISIBLE
                            leaderboard[i].second.visibility = View.VISIBLE
                            leaderboard[i].first.text = score.key
                            leaderboard[i].second.text = score.value.toString()
                            i += 1
                            if (i==4){
                                break@loop
                            }

                        }

                    }
                    }

        }

    }
