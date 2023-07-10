package com.jedmahonisgroup.gamepoint.network

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.model.gameshow.*
import com.jedmahonisgroup.gamepoint.model.gameshowLeaderboard.GameShowLeaderboardResponse
import com.jedmahonisgroup.gamepoint.model.leaderboard.LeaderboardModel
import com.jedmahonisgroup.gamepoint.model.picks.OpenPicksModel
import com.jedmahonisgroup.gamepoint.model.picks.Picks
import com.jedmahonisgroup.gamepoint.model.school.School
import com.jedmahonisgroup.gamepoint.ui.MainViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsViewModel
import io.reactivex.Observable
import retrofit2.http.*


/**
 * The interface which provides methods to get result of webservices
 */
interface GamePointApi {

    @POST("/users.json")
    fun createUser(
            @Header("Content-Type") contentType: String,
            @Body user: UserModel
    ): Observable<UserResponseModel>

    @GET("/users/{id}.json")
    fun getUser(@Header("Content-Type") contentType: String,
                @Header("Authorization") authorization: String,
                @Path("id") userId: String
    ): Observable<UserResponse>

    @PATCH("/users/{userId}.json")
    fun uploadUserPicture(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("userId") userId: String,
            @Body userImage: UserImage
    ): Observable<UserResponse>

    @PATCH("/users/{userId}.json")
    fun updateProfile(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("userId") userId: String,
            @Body updateProfile: UpdateProfile
    ): Observable<UserResponse>

    @PATCH("/users/{userId}.json")
    fun updateProfilePP(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("userId") userId: String,
            @Body updateProfile: UpdateProfilePP
    ): Observable<UserResponse>

    @GET("/users/{userId}.json")
    fun refreshUser(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("userId") userId: String
    ): Observable<UserResponse>

    @POST("/devices/add.json")
    fun sendPushTokenToServer(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Body sendPushTokenToServerBody: MainViewModel.sendPushTokenToServerBody
            ): Observable<Any>


    @POST("/login.json")
    fun login(
            @Header("Content-Type") contentType: String,
            @Body loginModel: LoginModel
    ): Observable<UserResponseModel>

    //Events
    @GET("/events.json")
    fun getEvents(
            //@Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String
    ): Observable<List<EventsModel>>

    @POST("/users/{userId}/refresh.json")
    fun refreshToken(
            @Path("userId") userId: String,
            @Body refreshTokenModel: RefreshTokenModel
    ): Observable<UserResponseModel>

    //Leaderboard
    @GET("/users.json")
    fun getLeaderboard(
            @Header("Authorization") authorization: String
    ): Observable<LeaderboardModel>

    @GET("/deals.json")
    fun getDeals(
            @Header("Authorization") authorization: String
    ): Observable<List<DealModel>>

    //Misc api
    @GET("/deal_redeems.json")
    fun getDealRedeems(
            @Header("Authorization") authorization: String
    ): Observable<List<DealRedeemModel>>

    @POST("/deal_redeems.json")
    fun postDealRedeems(
            @Header("Authorization") authorization: String,
            @Body redeem_deal: RedeemDealBody
    ): Observable<DealRedeemModel>

    //Check Ins...returns only the current user's check_ins
    @GET("/check_ins.json")
    fun getCheckins(
            @Header("Authorization") authorization: String
    ): Observable<List<CheckinsResponseModel>>

    /**
     * returns 201 when creating, returns 200 when a check in already exists for the user and event
     *
     */
    @POST("check_ins.json")
    fun postCheckins(
            @Header("Authorization") authorization: String,
            @Body checkin: CheckIn
    ): Observable<CheckinsResponseModel>


    @POST("check_ins.json")
    fun backgroundChecking(
            @Header("Authorization") authorization: String,
            @Body checkin: BCheckIn
    ): Observable<CheckinsResponseModel>

    //Counter Completed
    @POST("/check_ins/{id}/redeem.json")
    fun postCheckOut(
            @Header("Authorization") authorization: String,
            @Path("id") id: String

    ): Observable<CheckinsResponseModel>

    /**
     * open_picks are available to make a selection on.
     * They will show if the user has  not made a selection yet.
     * My_picks show history
     * @param authorization
     */
    @GET("/picks.json?for_trivia=false")
    fun getPicks(
            @Header("Authorization") authorization: String
    ): Observable<PicksModel>

    @GET("/picks.json?for_trivia=true")
    fun getTriviaPicks(
        @Header("Authorization") authorization: String
    ): Observable<PicksModel>
    /**
     * returns only the current user's picks
     *
     */
    @POST("/user_picks.json")
    fun postUserPicks(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Body userPick: Picks
    ): Observable<OpenPicksModel>

    @GET("/logout.json")
    fun logout(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String
    ): Observable<UserResponseModel>

    @POST("/devices/remove.json")
    fun unregisterPushToken(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Body firebase_key: SettingsViewModel.firebase_key_object
    ): Observable<Any>


    @POST("/reset.json")
    fun forgotPass(
            @Header("Content-Type") contentType: String,
            @Body forgotPassword: ForgotPasswordModel

    ): Observable<ForgotPasswordResponse>

    @GET("/posts.json")
    fun getFeeds(
            @Header("Authorization") authorization: String
    ): Observable<FeedsModel>

    @GET("/posts.json?friends=true")
    fun getFriendsFeeds(
            @Header("Authorization") authorization: String
    ): Observable<FeedsModel>

    @GET("/posts.json?user_id=")
    fun getPrivateFeeds(
            @Header("Authorization") authorization: String,
            @Query("user_id") id: String
    ): Observable<FeedsModel>

    @POST("/posts.json")
    fun postUserPost(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Body post: Post
    ): Observable<PostsModel>


    @POST("/posts/{id}/like.json")
    fun postLike(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("id") iDPost: String

    ): Observable<PostsModel>

    @POST("/posts/{id}/unlike.json")
    fun postDislike(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("id") iDPost: String

    ): Observable<PostsModel>
    @POST("/posts/{id}/report.json")
    fun postReport(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("id") iDPost: String

    ): Observable<msgResponseModel>

    @DELETE("/posts/{id}.json ")
    fun deletePost(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("id") postId:String
    ): Observable<msgResponseModel>

    @GET("/posts/{id}/comments.json ")
    fun getComments(
            @Header("Authorization") authorization: String,
            @Path("id") postId: String

    ): Observable<List<CommentsRModel>>

    @POST("/posts/{id}/comments.json")
    fun postComment(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("id") id:String,
            @Body post: PostCommentModel
    ): Observable<PostsModel>

    @DELETE("/posts/{postId}/comments/{id}.json ")
    fun deleteComment(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("postId") postId:String,
            @Path("id") id:String
            ): Observable<PostsModel>


    @GET
    fun getGameShowList(
        @Url url: String,
        @Header("x-session-id") token: String
    ): Observable<GameShowResponse2>

    @GET
    fun getGameShowLeaderBoard(
        @Url url: String,
        @Header("x-session-id") token: String
    ): Observable<GameShowLeaderboardResponse>

    @GET
    fun getGameShowDetail(
        @Url url: String,
        @Header("x-session-id") token: String
    ): Observable<GameShowResponse>

    @POST
    fun joinGameShow(
        @Url url: String,
        @Header("x-session-id") token: String,
        @Query("gameShowId") gameShowId: Int,
        @Query("wager") wager: Int,
    ): Observable<JoinGameShowResponseFromServer>

    @GET
    fun getGameShowResult(
        @Url url: String,
        @Header("x-session-id") token: String,
        @Query("gameShowId") gameShowId: Int,
    ): Observable<GameShowResultResponseFromServer>

    @POST
    fun submitGameAnswers(
        @Url url: String,
        @Header("x-session-id") token: String,
        @Query("joinedId") joinedId: Int,
        @Query("breaker") breaker: Int,
        @Body data: List<PostQuestion>
    ): Observable<SubmitGameShowResponseFromServer>


    @GET("/friend_requests.json?")
    fun getFriends(
            @Header("Authorization") authorization: String
    ): Observable<FriendsModel>

    @POST("/{postid}/comments/{id}/report.json")
    fun commentReport(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("postid") iDPost: String,
            @Path("id") iD: String

    ): Observable<msgResponseModel>

    @POST("/friend_requests.json")
    fun sendRequest(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Body sendId: FriendSentRequestModel

    ): Observable<msgResponseModel>

    @POST("/friend_requests.json")
    fun blockUser(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Body sendId: UserBlockModel

    ): Observable<RequestModel>

    @PATCH("/friend_requests/{iDreq}.json")
    fun Requestanswer(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("iDreq") userId: String,
            @Body approv: FriendADRequestModel
    ): Observable<RequestModel>

    @DELETE("/friend_requests/{id}.json")
    fun deleteFriend(
            @Header("Content-Type") contentType: String,
            @Header("Authorization") authorization: String,
            @Path("id") requestId:String
    ): Observable<msgResponseModel>

    @POST ("/users/{id}/report.json")
    fun ReportUser(
            @Header("Authorization") authorization: String,
            @Path("id") id : String,
            @Body report: reportModel
    ): Observable<msgResponseModel>

    @GET("/pushes.json")
    fun notification(
            @Header("Authorization") authorization: String
    ): Observable<ArrayList<notificationResponseModel>>

    @POST ("/users/search.json")
    fun searchResult(
            @Header("Authorization") authorization: String,
            @Body querysearch: searchModel
    ): Observable<List<User>>


    @GET("/schools.json")
    fun getSchools(
    ): Observable<List<School>>

    @GET("/sponsors.json")
    fun getsponsor(
            @Header("Authorization") authorization: String
    ): Observable<ArrayList<SponsorModel>>


}