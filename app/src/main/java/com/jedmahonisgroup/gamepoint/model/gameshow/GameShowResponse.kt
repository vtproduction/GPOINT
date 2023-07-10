package com.jedmahonisgroup.gamepoint.model.gameshow

/**
 * Created by nienle on 28,December,2022
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class GameShowResponse(
    val id: Int?,
    val name: String?,
    val schoolId: Int?,
    val schoolName: String?,
    val startDate: String?,
    val endDate: String?,
    val photo: String?,
    val video: String?,
    val wagerPoolsViewModel: List<Any>?,
    val wagerPools: String?,
    val questionsViewModels: Any?,
    val questions: String?,
    val gameBreakerTitle: String?,
    val gameBreakerFrom: Int?,
    val gameBreakerTo: Int?,
    val gameBreakerValue: Int?,
    val gameShowStatus: String?,
    val isPublishedResult: Boolean?
)
