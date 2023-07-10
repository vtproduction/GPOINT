package com.jedmahonisgroup.gamepoint.model.gameshow

/**
 * Created by nienle on 28,December,2022
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class Answer(
    val title: String,
    val isCorrectAnswer: Boolean,
    val isGamePointSelected: Boolean,
    val isPlayerSelected: Boolean?,
    val id: Int
)
