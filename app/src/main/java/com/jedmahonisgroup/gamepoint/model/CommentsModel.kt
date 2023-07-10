package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.feeds.User


data class CommentsRModel(
        var id: Int,
        var  user_id: Int,
        var post_id: Int,
        var body : String,
        var created_at : String,
        var updated_at: String,
        var reported: String,
        var user: User
)

data class PostCommentModel( var comment : BodyComment )


class BodyComment {

   var body : String? = null

}


