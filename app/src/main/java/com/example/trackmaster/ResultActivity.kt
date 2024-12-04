package com.example.trackmaster

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener

class ResultActivity : AppCompatActivity() {

    private lateinit var resultLayout: LinearLayout
    private lateinit var graph: Map<String, MutableList<Edge>>
    private lateinit var startStation: String
    private lateinit var endStation: String

    // 혼잡도 캐시
    private val congestionCache = mutableMapOf<String, Int>()

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
        val nearbyButton = findViewById<Button>(R.id.nearbyButton)
        nearbyButton.setOnClickListener {
            val intent = Intent(this, NearbySearchActivity::class.java)
            startActivity(intent)
        }

        val stationRepository = StationRepository()
        val graphBuilder = GraphBuilder()
        val stationList = stationRepository.readCSV(this)
        graph = graphBuilder.buildGraph(stationList)
        val pathFinder = PathFinder()
        val errorUtility = ErrorUtility()

        // 초기 데이터 설정
        startStation = intent.getStringExtra("startStation").orEmpty()
        endStation = intent.getStringExtra("endStation").orEmpty()

        // 입력 필드 초기화
        startStationInput.setText(startStation)
        endStationInput.setText(endStation)

        // 출발역 또는 도착역 변경 시 캐시 초기화
        startStationInput.addTextChangedListener {
            congestionCache.clear()
        }
        endStationInput.addTextChangedListener {
            congestionCache.clear()
        }

        // 경로 검색 버튼 클릭
        calculateButton.setOnClickListener {

            startStationInput.error = null
            endStationInput.error = null

            startStation = startStationInput.text.toString().trim()
            endStation = endStationInput.text.toString().trim()

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
            // 텍스트 변경 시 오류 메시지 초기화
            startStationInput.addTextChangedListener {
                startStationInput.error = null
            }

            endStationInput.addTextChangedListener {
                endStationInput.error = null
            }

            // **버튼 색상 초기화**
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)

            // 혼잡도 캐시 초기화
            congestionCache.clear()
            errorUtility.clearResults(resultLayout)
        }

        // 최소 환승 버튼 클릭
        minTransferButton.setOnClickListener {
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)
            minTransferButton.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#FFFF00"))
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
        buttons.forEach { button ->
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

    private fun addRouteToLayout(label: String, result: Path) {
        val lineImages = mapOf(
            "1" to R.drawable.transfer1,
            "2" to R.drawable.transfer2,
            "3" to R.drawable.transfer3,
            "4" to R.drawable.transfer4,
            "5" to R.drawable.transfer5,
            "6" to R.drawable.transfer6,
            "7" to R.drawable.transfer7,
            "8" to R.drawable.transfer8,
            "9" to R.drawable.transfer9
        )

        val departureImage = R.drawable.departure
        val arrivalImage = R.drawable.arrival
        val lineColors = mapOf(
            "1" to "#00B050", "2" to "#002060", "3" to "#CB6A28",
            "4" to "#FF0000", "5" to "#7F9ED7", "6" to "#FFC000",
            "7" to "#92D050", "8" to "#00B0F0", "9" to "#7030A0"
        )

        // 주요 경로 정보 출력
        val labelTextView = TextView(this).apply {
            text = """
            $label: ${result.route.first()} -> ${result.route.last()}
            소요 시간: ${result.times.sum()} 초
            환승 횟수: ${result.totalTransfers}
            이동 거리: ${result.distances.sum()} m
            소요 비용: ${result.costs.sum()} 원
        """.trimIndent()
            setTextColor(Color.BLACK)
            textSize = 18f
        }
        resultLayout.removeAllViews()
        resultLayout.addView(labelTextView)

        // 혼잡도 정보 출력
        val congestion = Congestion()
        val congestionInfo = congestion.calculateCongestion(result, congestionCache)
        val congestionTextView = TextView(this).apply {
            text = congestionInfo.joinToString("\n") // 혼잡도 정보를 줄바꿈하여 출력
            setTextColor(Color.DKGRAY)
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }
        resultLayout.addView(congestionTextView)

        // 상세 경로 레이아웃 생성
        val detailLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val keyStations = mutableListOf(result.route.first()) // 출발역
        for (i in 1 until result.route.size - 1) {
            val prevLine = result.route[i - 1].substring(0, 1)
            val currentLine = result.route[i].substring(0, 1)
            if (prevLine != currentLine) {
                keyStations.add(result.route[i]) // 환승역 추가
            }
        }
        keyStations.add(result.route.last()) // 도착역 추가

        for (i in keyStations.indices) {
            // 역 정보 출력
            val stationLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(32, 32, 32, 32) }
            }

            val stationImage = ImageView(this).apply {
                setImageResource(
                    when (i) {
                        0 -> departureImage
                        keyStations.size - 1 -> arrivalImage
                        else -> lineImages[keyStations[i].substring(0, 1)] ?: R.drawable.transfer1
                    }
                )
                layoutParams = LinearLayout.LayoutParams(150, 150).apply { setMargins(16, 0, 16, 0) }
            }

            val stationText = TextView(this).apply {
                text = keyStations[i]
                setTextColor(Color.parseColor(lineColors[keyStations[i].substring(0, 1)] ?: "#000000"))
                textSize = 20f
            }

            stationLayout.addView(stationImage)
            stationLayout.addView(stationText)
            detailLayout.addView(stationLayout)

            // 선과 "자세히 보기" 추가
            if (i < keyStations.size - 1) {
                val lineAndButtonLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(16, 16, 16, 16) }
                }

                val lineView = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        8, 300
                    ).apply { setMargins(100, 0, 100, 0) }
                    setBackgroundColor(Color.parseColor(lineColors[keyStations[i].substring(0, 1)] ?: "#CCCCCC"))
                }

                val buttonText = TextView(this).apply {
                    text = "자세히 보기"
                    setTextColor(Color.BLUE)
                    textSize = 18f
                }

                val stationListLayout = LinearLayout(this@ResultActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    visibility = LinearLayout.GONE
                }

                val intermediateStations = result.route.subList(
                    result.route.indexOf(keyStations[i]) + 1,
                    result.route.indexOf(keyStations[i + 1])
                )

                intermediateStations.forEach { station ->
                    val stationTextView = TextView(this@ResultActivity).apply {
                        text = station
                        setTextColor(Color.GRAY)
                        textSize = 16f
                        setOnClickListener {
                            stationListLayout.visibility = LinearLayout.GONE
                            buttonText.visibility = View.VISIBLE
                        }
                    }
                    stationListLayout.addView(stationTextView)
                }

                buttonText.setOnClickListener {
                    if (stationListLayout.visibility == LinearLayout.GONE) {
                        stationListLayout.visibility = LinearLayout.VISIBLE
                        buttonText.visibility = View.GONE
                    }
                }

                lineAndButtonLayout.addView(lineView)
                lineAndButtonLayout.addView(buttonText)
                detailLayout.addView(lineAndButtonLayout)
                detailLayout.addView(stationListLayout)
            }
        }

        resultLayout.addView(detailLayout)
    }


}