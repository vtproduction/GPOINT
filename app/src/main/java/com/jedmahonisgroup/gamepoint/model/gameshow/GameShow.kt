package com.jedmahonisgroup.gamepoint.model.gameshow

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import org.joda.time.LocalDateTime

/**
 * Created by nienle on 28December2022
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class GameShow(data: GameShowResponse) {
    var id: Int
    var name: String
    var description: String
    var schoolId: Int
    var schoolName: String
    var startDate: LocalDateTime
    var endDate: LocalDateTime
    var photo: String
    var video: String
    var wagerPoolsViewModel: List<Any>
    var wagerPools: List<Int>
    var questionsViewModels: Any
    var questions: List<Question>
    var gameBreakerTitle: String
    var gameBreakerFrom: Int
    var gameBreakerTo: Int
    var gameBreakerValue: Int
    var gameShowStatus: Status
    var isPublishedResult: Boolean = false

    enum class Status {
        JOINED {
            override fun toString(): String {
                return "JOINED"
            }
        }, OPEN {
            override fun toString(): String {
                return "OPEN"
            }
        }, CLOSE {
            override fun toString(): String {
                return "CLOSE"
            }
        }, END {
            override fun toString(): String {
                return "END"
            }
        }, UNDEFINED {
            override fun toString(): String {
                return ""
            }
        };

        abstract override fun toString() : String
    }

    init {
        this.id = data.id ?: -1
        this.name = data.name ?: ""
        //this.description = "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like)."
        this.description = ""
        this.schoolId = data.schoolId ?: -1
        this.schoolName = data.schoolName ?: ""
        this.startDate = try{
            StringFormatter.getFormattedTimeStamp2(data.startDate ?: "")
        }catch(t: Throwable){
            LocalDateTime.now()
        }
        this.endDate = try{
            StringFormatter.getFormattedTimeStamp2(data.endDate ?: "")
        }catch(t: Throwable){
            LocalDateTime.now()
        }
        this.photo = data.photo ?: ""
        this.video = data.video ?: ""
        this.wagerPoolsViewModel = data.wagerPoolsViewModel ?: emptyList()
        this.questionsViewModels = data.questionsViewModels ?: Unit
        this.gameBreakerTitle = data.gameBreakerTitle ?: ""
        this.gameBreakerFrom = data.gameBreakerFrom ?: -1
        this.gameBreakerTo = data.gameBreakerTo ?: -1
        this.gameBreakerValue = data.gameBreakerValue ?: -1
        this.gameShowStatus = when (data.gameShowStatus) {
            "JOINED" -> Status.JOINED
            "OPEN" -> Status.OPEN
            "CLOSED" -> Status.CLOSE
            "END"-> Status.END
            else -> Status.UNDEFINED
        }
        this.isPublishedResult = data.isPublishedResult ?: false
        
        val gson = Gson()
        val wagerPools = gson.fromJson<List<Int>>(data.wagerPools ?: "[]", object : TypeToken<List<Int>>(){}.type)
        this.wagerPools = wagerPools

        val questions = gson.fromJson<List<Question>>(data.questions ?: "[]", object : TypeToken<List<Question>>(){}.type)
        this.questions = questions

    }

    override fun toString(): String {
        return "GameShow(id=$id, \n" +
                "name='$name', \n" +
                "description='$description', \nschoolId=$schoolId, \nschoolName='$schoolName', \nstartDate='$startDate', \nendDate='$endDate', \nphoto='$photo', \nvideo='$video', \nwagerPoolsViewModel=$wagerPoolsViewModel, \nwagerPools=$wagerPools, \nquestionsViewModels=$questionsViewModels, \nquestions=$questions, \ngameBreakerTitle='$gameBreakerTitle', \ngameBreakerFrom=$gameBreakerFrom, \ngameBreakerTo=$gameBreakerTo, \ngameBreakerValue=$gameBreakerValue, \nisPublishedResult=$isPublishedResult)"
    }


}
