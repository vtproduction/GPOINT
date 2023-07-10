package com.jedmahonisgroup.gamepoint.model.gameshow

/**
 * Created by nienle on 28,December,2022
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class PostAnswer(
    val title: String,
    val isCorrectAnswer: Boolean,
    var isPlayerSelected: Boolean,
    val id: Int
){

    constructor(answer: Answer, choice: Boolean)
            : this(answer.title, answer.isCorrectAnswer, choice, answer.id)
}
