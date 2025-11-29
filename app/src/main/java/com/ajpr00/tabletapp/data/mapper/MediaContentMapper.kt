package com.ajpr00.tabletapp.data.mapper

import androidx.room.TypeConverter
import com.ajpr00.tabletapp.domain.model.FormatType

class Converters {
    @TypeConverter
    fun fromFormatType(value: FormatType): String = value.name

    @TypeConverter
    fun toFormatType(value: String): FormatType = FormatType.valueOf(value)
}