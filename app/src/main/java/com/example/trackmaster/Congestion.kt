package com.example.trackmaster

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random
class Congestion {
    fun calculateCongestion(
        path: Path,
        cache: MutableMap<String, Int>
    ): Map<String, String> {
        val lineCongestions = mutableMapOf<String, MutableList<Int>>()

        path.route.forEachIndexed { index, station ->
            if (index < path.route.size - 1) {
                val currentLine = station.substring(0, 1) // 호선 번호
                val nextStation = path.route[index + 1]
                val segmentKey = "$currentLine-$station-$nextStation"

                // 혼잡도 캐싱: 동일 구간 재사용
                val congestion = cache.getOrPut(segmentKey) {
                    Random.nextInt(100) + 1
                }

                // 각 호선별 혼잡도 추가
                lineCongestions.computeIfAbsent(currentLine) { mutableListOf() }.add(congestion)
            }
        }

        // 마지막 역의 호선 처리
        if (path.route.size > 1) {
            val lastStation = path.route.last()
            val secondLastStation = path.route[path.route.size - 2]
            val lastLine = lastStation.substring(0, 1) // 마지막 역의 호선 번호
            val lastSegmentKey = "$lastLine-$secondLastStation-$lastStation"

            // 마지막 구간 혼잡도 추가
            val lastCongestion = cache.getOrPut(lastSegmentKey) {
                Random.nextInt(100) + 1
            }
            lineCongestions.computeIfAbsent(lastLine) { mutableListOf() }.add(lastCongestion)
        }

        // 혼잡도 상태 반환
        return lineCongestions.entries.associate { entry ->
            val averageCongestion = entry.value.average().toInt()
            val status = when {
                averageCongestion > 70 -> "혼잡"
                averageCongestion in 30..70 -> "보통"
                else -> "쾌적"
            }
            entry.key to status
        }
    }
}
