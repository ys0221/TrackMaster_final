package com.example.trackmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.trackmaster.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start) // activity_start.xml 연결

        val startStationInput = findViewById<EditText>(R.id.startStationInput)
        val endStationInput = findViewById<EditText>(R.id.endStationInput)
        val calculateButton = findViewById<Button>(R.id.calculateButton)

        // CSV 데이터 읽기
        val stationRepository = StationRepository()
        val stationList = stationRepository.readCSV(this)
        val errorUtility = ErrorUtility()

        calculateButton.setOnClickListener {

            startStationInput.error = null
            endStationInput.error = null

            val startStation = startStationInput.text.toString().trim()
            val endStation = endStationInput.text.toString().trim()

            // 조건에 따른 오류 메시지 처리
            when {
                startStation.isBlank() && endStation.isBlank() -> {
                    startStationInput.error = "출발역을 입력하세요."
                    endStationInput.error = "도착역을 입력하세요."
                    return@setOnClickListener
                }
                startStation.isBlank() -> {
                    startStationInput.error = "출발역을 입력하세요."
                    return@setOnClickListener
                }
                endStation.isBlank() -> {
                    endStationInput.error = "도착역을 입력하세요."
                    return@setOnClickListener
                }
                stationList.none { it.출발역 == startStation || it.도착역 == startStation } &&
                        stationList.none { it.출발역 == endStation || it.도착역 == endStation } -> {
                    startStationInput.error = "출발역이 올바르지 않습니다."
                    endStationInput.error = "도착역이 올바르지 않습니다."
                    return@setOnClickListener
                }
                startStation == endStation -> {
                    startStationInput.error = "출발역과 도착역이 같습니다."
                    endStationInput.error = "출발역과 도착역이 같습니다."
                    return@setOnClickListener
                }
                stationList.none { it.출발역 == startStation || it.도착역 == startStation } -> {
                    startStationInput.error = "출발역이 올바르지 않습니다."
                    return@setOnClickListener
                }
                stationList.none { it.출발역 == endStation || it.도착역 == endStation } -> {
                    endStationInput.error = "도착역이 올바르지 않습니다."
                    return@setOnClickListener
                }
            }

            // 유효성 검사 통과 시 결과 화면으로 데이터 전달
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("startStation", startStation)
                putExtra("endStation", endStation)
            }
            startActivity(intent)
        }
        // 텍스트 변경 시 오류 메시지 초기화
        startStationInput.addTextChangedListener {
            startStationInput.error = null
        }

        endStationInput.addTextChangedListener {
            endStationInput.error = null
        }
    }
}