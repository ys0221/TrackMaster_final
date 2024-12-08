package com.example.trackmaster1119

import ErrorUtility
import GraphBuilder
import PathFinder
import RouteFormatter
import StationRepository
import android.content.Intent
import android.os.Bundle
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var resultLayout: LinearLayout
    private lateinit var graph: Map<String, MutableList<Edge>>
    private lateinit var startStation: String
    private lateinit var endStation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // activity_main.xml 연결

        val startStationInput = findViewById<EditText>(R.id.startStationInput)
        val endStationInput = findViewById<EditText>(R.id.endStationInput)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        resultLayout = findViewById(R.id.resultLayout)

        val minTransferButton = findViewById<Button>(R.id.minTransferButton)
        val minTimeButton = findViewById<Button>(R.id.minTimeButton)
        val minCostButton = findViewById<Button>(R.id.minCostButton)

        // CSV 데이터 읽기 및 그래프 생성
        val stationRepository = StationRepository()
        val graphBuilder = GraphBuilder()
        val stationList = stationRepository.readCSV(this)
        graph = graphBuilder.buildGraph(stationList)
        val pathFinder = PathFinder()
        val errorUtility = ErrorUtility()

        val nearbyButton = findViewById<Button>(R.id.nearbyButton)
        nearbyButton.setOnClickListener {
            val intent = Intent(this, NearbySearchActivity::class.java)
            startActivity(intent)
        }

        // Intent로 초기 데이터 받기
        startStation = intent.getStringExtra("startStation").orEmpty()
        endStation = intent.getStringExtra("endStation").orEmpty()

        // 초기 입력 필드 값 설정
        startStationInput.setText(startStation)
        endStationInput.setText(endStation)

        // 초기화: 화면을 빈칸으로 설정
        errorUtility.clearResults(resultLayout)

        // 경로 검색 버튼 클릭 이벤트
        calculateButton.setOnClickListener {
            startStation = startStationInput.text.toString().trim()
            endStation = endStationInput.text.toString().trim()

            // **결과 초기화**
            errorUtility.clearResults(resultLayout)
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)

            // 조건에 따른 오류 메시지 처리
            when {
                startStation.isBlank() && endStation.isBlank() -> {
                    errorUtility.displayErrorMessage(this, resultLayout, "출발역과 도착역을 입력하세요.")
                    return@setOnClickListener
                }
                startStation.isBlank() -> {
                    errorUtility.displayErrorMessage(this, resultLayout, "출발역을 입력하세요.")
                    return@setOnClickListener
                }
                endStation.isBlank() -> {
                    errorUtility.displayErrorMessage(this, resultLayout, "도착역을 입력하세요.")
                    return@setOnClickListener
                }
                startStation == endStation -> {
                    errorUtility.displayErrorMessage(this, resultLayout, "출발역과 도착역이 같습니다. 다시 입력해주세요.")
                    return@setOnClickListener
                }
                stationList.none { it.출발역 == startStation || it.도착역 == startStation } &&
                        stationList.none { it.출발역 == endStation || it.도착역 == endStation } -> {
                    errorUtility.displayErrorMessage(this, resultLayout, "출발역과 도착역이 모두 올바르지 않습니다. 다시 입력해주세요.")
                    return@setOnClickListener
                }
                stationList.none { it.출발역 == startStation || it.도착역 == startStation } -> {
                    errorUtility.displayErrorMessage(this, resultLayout, "출발역이 올바르지 않습니다. 다시 입력해주세요.")
                    return@setOnClickListener
                }
                stationList.none { it.출발역 == endStation || it.도착역 == endStation } -> {
                    errorUtility.displayErrorMessage(this, resultLayout, "도착역이 올바르지 않습니다. 다시 입력해주세요.")
                    return@setOnClickListener
                }
            }

            // **빈 화면 상태 유지**: 초기화된 상태로 대기
            errorUtility.clearResults(resultLayout)
        }

        // 최소 환승 버튼 클릭
        minTransferButton.setOnClickListener {
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)
            minTransferButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFF00"))
            handleSpecificCondition("최소 환승 경로") { g, s, e ->
                pathFinder.findLowestCostPathWithTransfers(g, s, e)
            }
        }

        // 최소 시간 버튼 클릭
        minTimeButton.setOnClickListener {
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)
            minTimeButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFF00"))
            handleSpecificCondition("최소 시간 경로") { g, s, e ->
                pathFinder.findLowestTimePath(g, s, e)
            }
        }

        // 최소 비용 버튼 클릭
        minCostButton.setOnClickListener {
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)
            minCostButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFF00"))
            handleSpecificCondition("최소 비용 경로") { g, s, e ->
                pathFinder.findLowestCostPath(g, s, e)
            }
        }
    }

    // 버튼 색상 초기화
    private fun resetButtonColors(vararg buttons: Button) {
        for (button in buttons) {
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
        }
    }

    // 특정 조건에 따라 경로 처리
    private fun handleSpecificCondition(
        label: String,
        pathFindingFunction: (Map<String, MutableList<Edge>>, String, String) -> Path?
    ) {
        ErrorUtility().clearResults(resultLayout)

        val result = pathFindingFunction(graph, startStation, endStation)
        if (result != null) {
            addRouteToLayout(label, result)
        } else {
            ErrorUtility().displayErrorMessage(this, resultLayout, "$label 을 찾을 수 없습니다.")
        }
    }

    // 결과 출력
    private fun addRouteToLayout(label: String, result: Path) {
        val routeFormatter = RouteFormatter(RouteFormatter.defaultLineColors)

        // 기본 경로 정보 출력
        val labelTextView = TextView(this).apply {
            text = """
            $label: ${result.route.first()} -> ${result.route.last()}
            소요 시간: ${result.times.sum()} 분
            환승 횟수: ${result.totalTransfers}
            이동 거리: ${result.distances.sum()} m
            소요 비용: ${result.costs.sum()} 원
        """.trimIndent()
            setTextColor(Color.BLACK)
            textSize = 16f
        }

        // 상세 경로 (호선별 색상 적용) 텍스트뷰
        val detailTextView = TextView(this).apply {
            text = routeFormatter.formatRouteWithColors(result.route)
            textSize = 14f
            visibility = TextView.GONE // 초기에는 숨김 상태
        }

        // 상세보기 토글 버튼
        val toggleDetailButton = TextView(this).apply {
            text = "상세보기"
            setTextColor(Color.BLUE)
            textSize = 14f
            setOnClickListener {
                detailTextView.visibility =
                    if (detailTextView.visibility == TextView.GONE) TextView.VISIBLE else TextView.GONE
                text = if (detailTextView.visibility == TextView.VISIBLE) "접기" else "상세보기"
            }
        }

        // 결과 레이아웃에 추가
        resultLayout.addView(labelTextView)
        resultLayout.addView(toggleDetailButton)
        resultLayout.addView(detailTextView)
    }


}



