package com.jedmahonisgroup.gamepoint.model

/**
 * Created by nienle on 10,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class StateNotifier<T> (
    val state: State,
    val data: T?,
    val error: Throwable?
    ){

    companion object {
        fun<T> loading()  = StateNotifier<T>(State.LOADING, null, null)
        fun<T> success(data: T)  = StateNotifier<T>(State.SUCCESS, data, null)
        fun<T> failed(error: Throwable) = StateNotifier<T>(State.FAILED, null, error)
    }

}


enum class State {
    LOADING, SUCCESS, FAILED
}