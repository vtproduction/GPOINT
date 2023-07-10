package com.jedmahonisgroup.gamepoint.model.gameshow

/**
 * Created by nienle on 28,December,2022
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class Question(
    val title : String,
    val id: Int,
    val isSingleChoice: Boolean,
    val answers: List<Answer>

)
