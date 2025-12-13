package com.example.models

data class Tutorial(
    val title: String,
    val subtitle: String,
    val iconResId: Int,
    val videoUrl: String? = null
)
