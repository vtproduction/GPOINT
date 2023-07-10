package com.jedmahonisgroup.gamepoint.ui.comments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.comments.CommentsAdapter
import com.jedmahonisgroup.gamepoint.model.*
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.report_pop_up_alert.view.*


class CommentsFragment : Fragment() , CFListner {


    private lateinit var commentViewModel: CommentsViewModel
    private var TOKEN: String? = null
    private var mUser: UserResponse? = null

    private var binding: ViewDataBinding? = null

     var idPost: String? = null

    private var mAvatarUser : ImageView? = null
    private var mBackBtn : ImageView? = null
    private var mpostCommentButton : TextView? = null
    private var mEditTextComment : EditText? = null


    private var mSwipeRefreshLayout : SwipeRefreshLayout? = null







    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comment, container, false)
        val rootView = binding?.root
        commentViewModel = ViewModelProviders.of(this).get(CommentsViewModel::class.java)


        getToken()

        setUpUi(rootView)


        responseFromServer()
        // Inflate the layout for this fragment
        return rootView



    }

    @SuppressLint("WrongConstant")
    private fun setUpUi(rootView: View?) {



        mSwipeRefreshLayout = rootView?.findViewById(R.id.comment_swipe_refresh_layout)
        mAvatarUser = rootView?.findViewById(R.id.myUserAvatarComment)
        mBackBtn = rootView?.findViewById(R.id.commentBackBtn)
        mpostCommentButton = rootView?.findViewById(R.id.postCommentButton)
        mEditTextComment =  rootView?.findViewById(R.id.myUserCommentBody)



        mSwipeRefreshLayout?.setOnRefreshListener {

            commentViewModel?.getComments(TOKEN!!, idPost!!)

        }

        mEditTextComment?.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                when (actionId) {
                    EditorInfo.IME_ACTION_GO -> {
                        var comment = BodyComment()
                        comment.body =  mEditTextComment?.text.toString()
                        commentViewModel?.postCmmnt(TOKEN!! , idPost!! , PostCommentModel(comment = comment))
                        mEditTextComment?.text?.clear()


                    }
                }
                return false
            }
        })


                mBackBtn?.setOnClickListener {
            getFragmentManager()?.popBackStack("1", 1);
        }

        mpostCommentButton?.setOnClickListener {

            var comment = BodyComment()
            comment.body =  mEditTextComment?.text.toString()
            mEditTextComment?.text?.clear()
            commentViewModel?.postCmmnt(TOKEN!! , idPost!! , PostCommentModel(comment = comment))
            hideKeyboard()


        }




        Glide.with(this).load(mUser!!.avatar.decapitalize()).circleCrop().error(R.drawable.ic_user_account).into(mAvatarUser!!)


        mSwipeRefreshLayout?.isRefreshing = true
        commentViewModel?.getComments(TOKEN!!, idPost!!)



    }

    private fun getToken() {
        var token = requireArguments().getString("token")
        idPost = requireArguments().getString("iDPost")

        TOKEN = token

        val gson = Gson()
        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())



        Log.i("${ContentValues.TAG}, TOKEN: ", token.toString())
        Log.i(ContentValues.TAG, "user  ==========> ${mUser.toString()}")
    }

    private fun responseFromServer() {

        commentViewModel.commentsFromServer.observe(viewLifecycleOwner, Observer { feeds ->
            mSwipeRefreshLayout?.isRefreshing = false

            if (feeds != null) {
                Log.i(TAG, "here are the picks data is: $feeds")

                displayData(feeds)



            } else {
                //picks are null
//                myFeedsRv?.visibility = View.INVISIBLE
//                feedsNoDataView?.visibility = View.VISIBLE

            }


        })

        commentViewModel.serverDataFailed.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching picks from server: $response")
            //mSwipeToRefresh!!.isRefreshing = false
//            myFeedsRv?.visibility = View.INVISIBLE
//            feedsNoDataView?.visibility = View.VISIBLE
        })

//        first, try to get data from the database




        //could not save streak to db


        /**
         * Post deal response
         */

        //posted deal successfully
        commentViewModel.postSuccess.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "post resultsFromServer: $it")

        })

        //failed posting deal
        commentViewModel.postFail.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem posting deal: $it")
            //display alert dialog

        })

    }


    // lets observe server data response


    private fun setupRecyclerview(response: List<CommentsRModel>) {

        var friendrequestListUI = makeFeedsUiList(response)

        commentRV?.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = CommentsAdapter(friendrequestListUI , this@CommentsFragment)
        }




    }


    private fun makeFeedsUiList(RequestResponse: List<CommentsRModel>): ArrayList<CommentsRModel> {
        var requestUi: CommentsRModel


        val requestModelUiList: ArrayList<CommentsRModel> = ArrayList()

        val active: ArrayList<DealModel> = ArrayList()
        for (i in 0 until RequestResponse.size) {
            val d = RequestResponse[i]

            requestUi = CommentsRModel(
                    id = d.id,
                     user_id = d.user_id,
             post_id = d.post_id,
             body = d.body,
             created_at =d.created_at,
             updated_at =d.updated_at,
             reported =d.reported,
             user = d.user


            )
            requestModelUiList.add(requestUi)

        }

        return requestModelUiList
    }




    private fun displayData(comment: List<CommentsRModel>) {




        try {
            if (comment.isEmpty()) {

                //display the empty screen
                setupRecyclerview(comment)


                commentRV?.visibility = View.INVISIBLE
                commentNoData?.visibility = View.VISIBLE
                Log.e(TAG, "Feeds were empty")

            } else if (!comment.isNullOrEmpty()) {
                Log.e(TAG, "commnet were neither null nor empty")

                commentRV?.visibility = View.VISIBLE
                commentNoData?.visibility = View.INVISIBLE
                setupRecyclerview(comment)
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "feeds were null $e")
            commentRV?.visibility = View.INVISIBLE
            commentNoData?.visibility = View.VISIBLE
        }

    }



    override fun reportpopup(postId: String, commentId: String , userid: String) {

        var messageBoxView = LayoutInflater.from(activity).inflate(R.layout.report_pop_up_alert, null)

        //AlertDialogBuilder
        var messageBoxBuilder = AlertDialog.Builder(requireContext()).setView(messageBoxView)


        //setting text values



        //show dialog
        val  messageBoxInstance = messageBoxBuilder.show()


        val lp: WindowManager.LayoutParams = messageBoxInstance.getWindow()!!.getAttributes()
        lp.width = 600
        lp.height = 900

        messageBoxInstance.getWindow()!!.setAttributes(lp)

        messageBoxView.buttonReportPost.visibility = View.GONE
        messageBoxView.buttonReportPostLine.visibility = View.GONE
        messageBoxView.buttonReportUser.visibility = View.GONE
        messageBoxView.buttonReportUserLine.visibility = View.GONE

        messageBoxView.buttonPostDeleteUser.visibility = View.GONE
        messageBoxView.buttonPostDeleteUserLine.visibility = View.GONE

        messageBoxView.buttonBlockUser.visibility = View.GONE
        messageBoxView.buttonBlockUserLine.visibility = View.GONE

        messageBoxView.buttonCommentReportUser.setOnClickListener {

            commentViewModel?.reportComment(TOKEN!!,idPost!!,commentId!!)
            messageBoxInstance.dismiss()



        }

        if (userid == mUser?.id.toString()){


            messageBoxView.buttonCommentReportUser.visibility = View.GONE
            messageBoxView.buttonCommentReportUserLine.visibility = View.GONE


            messageBoxView.buttonCommentDeleteUser.setOnClickListener {

                commentViewModel?.deletComment(TOKEN!!,idPost!!,commentId!!)
                messageBoxInstance.dismiss()


        }
        }else{

            messageBoxView.buttonCommentDeleteUser.visibility = View.GONE
            messageBoxView.buttonCommentDeleteUserLine.visibility = View.GONE


        }


        //set Listener
        messageBoxView.setOnClickListener(){
            //close dialog
            messageBoxInstance.dismiss()
        }

    }




    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


}

interface CFListner {
    fun reportpopup(postId :String , commentId: String, userid: String)



}




