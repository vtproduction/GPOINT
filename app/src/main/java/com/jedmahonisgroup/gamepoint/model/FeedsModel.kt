package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.model.feeds.PostIDsModel

data class FeedsModel(
        val page: Int,
        val page_count: Int,
        val page_size: Int,
        val post_count: Int,
        val post_ids: Array<Int>,
        val posts: List<PostsModel>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeedsModel

        if (!post_ids.contentEquals(other.post_ids)) return false

        return true
    }

    override fun hashCode(): Int {
        return post_ids.contentHashCode()
    }
}