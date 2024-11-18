package com.example.trackmaster

data class Path(
    val station: String,
    val totalCost: Int,
    val totalTransfers: Int,
    val route: List<String>, // 경로를 나타내는 필드
    val distances: List<Int>,
    val costs: List<Int>,
    val times: List<Int>
)