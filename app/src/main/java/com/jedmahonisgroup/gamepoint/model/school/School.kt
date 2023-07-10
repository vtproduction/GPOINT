package com.jedmahonisgroup.gamepoint.model.school

import java.io.Serializable

data class School(
        val id: Int,
        val name: String,
        val short_name: String,
        val primary_color: String,
        val secondary_color: String,
        val dark_primary_color: String,
        val dark_secondary_color: String,
        val checkin_multiplier: Float,
        val created_at: String,
        val updated_at: String,
        val url: String,
        val logo: String
) : Serializable

