package com.example.trackmaster

import GraphBuilder
import PathFinder
import RouteFormatter
import StationRepository
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var resultLayout: LinearLayout
    private val graphBuilder = GraphBuilder()
    private val stationRepository = StationRepository()
    private val pathFinder = PathFinder()
    private val routeFormatter = RouteFormatter(RouteFormatter.defaultLineColors)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startStationInput = findViewById<EditText>(R.id.startStationInput)
        val endStationInput = findViewById<EditText>(R.id.endStationInput)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        resultLayout = findViewById(R.id.resultLayout)

        val minTransferButton = findViewById<Button>(R.id.minTransferButton)
        val minTimeButton = findViewById<Button>(R.id.minTimeButton)
        val minCostButton = findViewById<Button>(R.id.minCostButton)

        val stationList = stationRepository.readCSV(this)
        val graph = graphBuilder.buildGraph(stationList)

        calculateButton.setOnClickListener {
            val startStation = startStationInput.text.toString()
            val endStation = endStationInput.text.toString()
            clearResults()

            val minTransferResult = pathFinder.findLowestCostPathWithTransfers(graph, startStation, endStation)
            val minCostResult = pathFinder.findLowestCostPath(graph, startStation, endStation)
            val minTimeResult = pathFinder.findLowestTimePath(graph, startStation, endStation)

            if (minTransferResult != null) {
                addRouteToLayout(
                    "최소 환승 경로",
                    minTransferResult.route
                )
            }

            if (minCostResult != null) {
                addRouteToLayout(
                    "최소 비용 경로",
                    minCostResult.route
                )
            }

            if (minTimeResult != null) {
                addRouteToLayout(
                    "최소 시간 경로",
                    minTimeResult.route
                )
            }

        }

        minTransferButton.setOnClickListener {
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)
            minTransferButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFF00"))
            handleSpecificCondition(graph, startStationInput, endStationInput, "최소 환승 경로")
            { g: Map<String, MutableList<Edge>>, s: String, e: String ->
                pathFinder.findLowestCostPathWithTransfers(g, s, e)
            }

        }

        minTimeButton.setOnClickListener {
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)
            minTimeButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFF00"))
            handleSpecificCondition(graph, startStationInput, endStationInput, "최소 시간 경로")
            {  g: Map<String, MutableList<Edge>>, s: String, e: String ->
                pathFinder.findLowestTimePath(g, s, e)
            }
        }

        minCostButton.setOnClickListener {
            resetButtonColors(minTransferButton, minTimeButton, minCostButton)
            minCostButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFF00"))
            handleSpecificCondition(graph, startStationInput, endStationInput, "최소 비용 경로")
            {  g: Map<String, MutableList<Edge>>, s: String, e: String ->
                pathFinder.findLowestCostPath(g, s, e)
            }
        }
    }

    // 버튼 색상 초기화 메서드
    private fun resetButtonColors(vararg buttons: Button) {
        for (button in buttons) {
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
        }
    }

    private fun displayResult(label: String, result: Path) {
        // 경로 요약 텍스트
        val lineTextView = TextView(this)
        lineTextView.text = "$label: ${result.route.joinToString(" -> ")}"
        lineTextView.setTextColor(Color.BLACK)
        lineTextView.textSize = 16f
        resultLayout.addView(lineTextView)

        // 추가 정보 텍스트
        val detailTextView = TextView(this)
        detailTextView.text = """
        환승 횟수: ${result.totalTransfers}
        소요 시간: ${result.times.sum()} 분
        소요 비용: ${result.costs.sum()} 원
        이동 거리: ${result.distances.sum()} m
    """.trimIndent()
        detailTextView.setTextColor(Color.DKGRAY)
        detailTextView.textSize = 14f
        resultLayout.addView(detailTextView)
    }


    private fun clearResults() {
        resultLayout.removeAllViews()
    }

    private fun
            handleSpecificCondition(
        graph: Map<String, MutableList<Edge>>,
        startStationInput: EditText,
        endStationInput: EditText,
        label: String,
        pathFindingFunction: (Map<String, MutableList<Edge>>, String, String) -> Path?
    ) {
        val startStation = startStationInput.text.toString()
        val endStation = endStationInput.text.toString()
        clearResults()

        val result = pathFindingFunction(graph, startStation, endStation)
        if (result != null) displayResult(label, result)
    }

    private fun addRouteToLayout(label: String, route: List<String>) {
        val labelTextView = TextView(this).apply {
            text = "$label: ${route.joinToString(" -> ")}"
            setTextColor(Color.BLACK)
            textSize = 16f
        }

        val detailTextView = TextView(this).apply {
            text = routeFormatter.formatRouteWithColors(route)
            textSize = 14f
            visibility = TextView.GONE
        }

        labelTextView.setOnClickListener {
            detailTextView.visibility =
                if (detailTextView.visibility == TextView.GONE) TextView.VISIBLE else TextView.GONE
        }

        resultLayout.addView(labelTextView)
        resultLayout.addView(detailTextView)
    }

}

