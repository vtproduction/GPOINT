package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.feeds.User
import java.io.Serializable

data class FriendsModel(
        val my_requests : List<RequestModel>,
        val requests : List<RequestModel>,
        val blocked_users : List<RequestModel>,
        val friends : List<User>
)




data class RequestModel(
        val id: Int,
        val user_id: Int,
        val friend_id: Int,
        var approval: String,
        val created_at: String,
        val updated_at: String,
        val user: User,
        val friend: User,
        val url: String

        ) : Serializable




data class FriendSentRequestModel(
        var friend_request: FriendId?

)


data class FriendId(
        val friend_id : Int
)



data class FriendADRequestModel(
        var friend_request: Approvalmodel?

)

data class Approvalmodel(
        val approval : String
)


data class UserBlockModel(
        var friend_request: Blockmodel?

)

data class Blockmodel(
        val friend_id : Int,
        val approval : String
)



