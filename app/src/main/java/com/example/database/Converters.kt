package com.example.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
    
    @TypeConverter
    fun toStringList(list: List<String>): String {
        return Gson().toJson(list)
    }
    
    @TypeConverter
    fun fromFloatMap(value: String): Map<String, Float> {
        return Gson().fromJson(value, object : TypeToken<Map<String, Float>>() {}.type)
    }
    
    @TypeConverter
    fun toFloatMap(map: Map<String, Float>): String {
        return Gson().toJson(map)
    }
    
    @TypeConverter
    fun fromIntList(value: String): List<Int> {
        return Gson().fromJson(value, object : TypeToken<List<Int>>() {}.type)
    }
    
    @TypeConverter
    fun toIntList(list: List<Int>): String {
        return Gson().toJson(list)
    }
}
