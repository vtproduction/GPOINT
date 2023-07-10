package com.jedmahonisgroup.gamepoint.adapters.leaderboard

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.leaderboard.EventLeaderboardModel
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

import java.text.DecimalFormat


class PointsViewHolder(inflater: LayoutInflater, private val parent: ViewGroup, private val schoolColor: String?):
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.leaderboard_list_card, parent, false)){

    private var TAG: String = PointsViewHolder::class.java.simpleName
    var rowNumber: TextView? = null
    var userName: TextView? = null
    private var numberOfPoints: TextView? = null
    private var userImage: CircleImageView? = null
    var separator1: View? = null
    var leaderboardPView: RelativeLayout? = null
    var separator2: View? = null
    var separator3: View? = null
    var view: View? = null


    init {
        rowNumber = itemView.findViewById(R.id.rowNumber)
        userName = itemView.findViewById(R.id.userName)
        numberOfPoints = itemView.findViewById(R.id.numberOfPoints)
        userImage = itemView.findViewById(com.jedmahonisgroup.gamepoint.R.id.userImage)
        leaderboardPView = itemView.findViewById(R.id.leaderboardPView)
        separator1 = itemView.findViewById(R.id.separator1)
        separator2 = itemView.findViewById(R.id.separator2)
        separator3 = itemView.findViewById(R.id.separator3)
        view = itemView


    }

    fun bind(data: EventLeaderboardModel, num: Int, response: List<EventLeaderboardModel>) {
        rowNumber?.text = num.toString()
//        val numberOf1s = numberForRank(1, response)
//        val numberOf2s = numberForRank(2, response)
//        val numberOf3s = numberForRank(3, response)
//        val cashForNumber1Rank = cashForNumberOneRankForNumberOfUsers(numberOf1s)
//        val formatter = DecimalFormat("#,###")
//        if (num == 1) {
//            rowNumber?.text = emojiSymbolsForInteger(cashForNumber1Rank/numberOf1s) + " $" + formatter.format(cashForNumber1Rank/numberOf1s)
//        } else if (num == 2) {
//            rowNumber?.text = cashForNumberTwoRankForNumberOfUsers(numberOf1s, numberOf2s, num)
//        } else if (num == 3) {
//            val onePlusTwoRanks = numberOf1s + numberOf2s
//            if (onePlusTwoRanks == 2) {
//                // There is only money for third because one user either got first and 2nd.
//                val number = 250/numberOf3s
//                rowNumber?.text = emojiSymbolsForInteger(number) + " $" + formatter.format(number) + "             "
//            }
//        }
        userName?.text = data.name
        //userName?.setTextColor(Color.parseColor(schoolColor))
        numberOfPoints?.text = data.this_month_event_points.toString()

        if (data.current_user){
            rowNumber?.setTextColor(Color.WHITE)

        }

        if (!data.avatar.isNullOrEmpty()) {
            Picasso.get()
                    .load(data.avatar)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.icon_feather_meh)
                    .into(userImage, object : com.squareup.picasso.Callback {
                        override fun onError(e: Exception?) {
                            if (!data.avatar.isNullOrEmpty()) {
                                Picasso.get()
                                        .load(data.avatar)
                                        .placeholder(R.drawable.icon_feather_meh)
                                        .into(userImage, object : com.squareup.picasso.Callback {
                                            override fun onError(e: Exception?) {
                                                Log.e(TAG, "Error loading user avatar a second time: $e")
                                                userImage!!.setImageResource(R.drawable.icon_feather_meh)
                                            }

                                            override fun onSuccess() {
                                                //there were images from the cache
                                            }
                                        })
                            }else{
                                userImage!!.setImageResource(R.drawable.icon_feather_meh)
                            }
                            Log.e(TAG, "Error loading user avatar a second time: $e")
                        }

                        override fun onSuccess() {
                            //there were images from the cache
                        }
                    })
        }else{
            userImage!!.setImageResource(R.drawable.icon_feather_meh)
        }
    }

     fun numberForRank(rank: Int, response: List<EventLeaderboardModel>) : Int {
         var count = 0
         for (lb:EventLeaderboardModel in response) {
             if (lb.rank == rank) {
                 count++
             } else if (lb.rank > rank) {
                 break
             }
         }
         return count

     }

    fun emojiSymbolsForInteger(number: Int): String {
        if (number > 500) {
            return "💰💰💰"
        } else if (number > 250) {
            return "💰💰"
        } else {
            return "💰"
        }

    }

    fun cashForNumberOneRankForNumberOfUsers(numberOfUsers: Int):Int {
        if (numberOfUsers == 1) {
            //One user got rank 1 so they get the normal $1,000
            return 1000
        } else if (numberOfUsers == 2) {
            //Two people tied for first so they split the money of 1 and 2
            return 1500
        } else if (numberOfUsers > 2) {
            //3 or more tied for first so they split the prizes for 1, 2, and 3
            return 1750
        }
        return 1000
    }

    fun cashForNumberTwoRankForNumberOfUsers(numberOf1Rank: Int, numberof2Users:Int, num: Int): String {
        val formatter = DecimalFormat("#,###")
        if (numberOf1Rank >= 3) {
            // There was at least a three way tie for first place, so we don't have any cash for 2nd place ranks
            return num.toString()
        } else if (numberOf1Rank == 2) {
            // There was a two way tie for first, so 2nd should get third places money.
            val number = 250/numberof2Users
            return emojiSymbolsForInteger(number) + " $" + formatter.format(number) + "        "
        } else {
            // Only one user is rank 1, so now we figure out money for
            val number = 500/numberof2Users
            return emojiSymbolsForInteger(number) + " $" + formatter.format(number) + "        "
        }
    }

}