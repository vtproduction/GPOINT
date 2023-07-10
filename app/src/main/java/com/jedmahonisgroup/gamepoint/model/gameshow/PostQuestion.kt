package com.jedmahonisgroup.gamepoint.model.gameshow

/**
 * Created by nienle on 28,December,2022
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class PostQuestion(
    val title : String,
    val id: Int,
    val isSingleChoice: Boolean,
    var answers: List<PostAnswer>
) {
    private constructor(question: Question) : this(question.title, question.id, question.isSingleChoice, arrayListOf())

    companion object {
        fun build (question: Question) : PostQuestion{
            val d = PostQuestion(question)
            val a : MutableList<PostAnswer> = mutableListOf()
            question.answers.forEach {
                a.add(PostAnswer(it, false))
            }
            d.answers = a
            return d
        }
    }
}
