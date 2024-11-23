package com.example.trackmaster1119

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader

class NearbySearchActivity : AppCompatActivity() {

    private lateinit var stationInfoTextView: TextView
    private lateinit var amenitiesRecyclerView: RecyclerView
    private lateinit var adapter: AmenityAdapter
    private val amenitiesList = mutableListOf<Amenity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_search)

        val searchStationInput = findViewById<EditText>(R.id.searchStationInput)
        val searchNearbyButton = findViewById<Button>(R.id.searchNearbyButton)
        stationInfoTextView = findViewById(R.id.stationInfoTextView)
        amenitiesRecyclerView = findViewById(R.id.amenitiesRecyclerView)

        // RecyclerView 설정
        adapter = AmenityAdapter(amenitiesList)
        amenitiesRecyclerView.layoutManager = LinearLayoutManager(this)
        amenitiesRecyclerView.adapter = adapter

        // 초기 상태: RecyclerView 숨김
        amenitiesRecyclerView.visibility = RecyclerView.GONE
        stationInfoTextView.text = "역 번호를 입력하세요."

        // 검색 버튼 클릭 리스너
        searchNearbyButton.setOnClickListener {
            val stationNumber = searchStationInput.text.toString().trim()

            if (stationNumber.isNotEmpty()) {
                // 검색 실행
                searchAmenitiesForStation(stationNumber)
            } else {
                // 검색창이 비어 있을 때 처리
                clearAmenitiesList()
            }
        }
    }

    private fun searchAmenitiesForStation(stationNumber: String) {
        val newAmenitiesList = loadAmenitiesForStation(stationNumber)
        amenitiesList.clear()
        amenitiesList.addAll(newAmenitiesList)
        adapter.notifyDataSetChanged()

        if (newAmenitiesList.isEmpty()) {
            stationInfoTextView.text = "${stationNumber}에 대한 데이터를 찾을 수 없습니다."
            amenitiesRecyclerView.visibility = RecyclerView.GONE
        } else {
            stationInfoTextView.text = "${stationNumber}의 주변 편의시설 목록"
            amenitiesRecyclerView.visibility = RecyclerView.VISIBLE
        }
    }

    private fun clearAmenitiesList() {
        // 목록과 메시지 초기화
        amenitiesList.clear()
        adapter.notifyDataSetChanged()
        stationInfoTextView.text = "역 번호를 입력하세요."
        amenitiesRecyclerView.visibility = RecyclerView.GONE
    }

    private fun loadAmenitiesForStation(stationNumber: String?): List<Amenity> {
        val amenitiesList = mutableListOf<Amenity>()
        if (stationNumber == null) return amenitiesList

        try {
            assets.open("amenities.csv").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readLine() // 첫 줄 (헤더) 건너뛰기
                    reader.forEachLine { line ->
                        val tokens = line.split(",")
                        if (tokens.isNotEmpty() && tokens[0].trim() == stationNumber) {
                            val restroom = tokens[1].trim()
                            val convenienceStore = tokens[2].trim()
                            val restaurant = tokens[3].trim()
                            val distances = listOf(
                                tokens[4].toIntOrNull() ?: Int.MAX_VALUE,
                                tokens[5].toIntOrNull() ?: Int.MAX_VALUE,
                                tokens[6].toIntOrNull() ?: Int.MAX_VALUE
                            )

                            amenitiesList.add(Amenity(restroom, distances[0], "화장실"))
                            amenitiesList.add(Amenity(convenienceStore, distances[1], "편의점"))
                            amenitiesList.add(Amenity(restaurant, distances[2], "식당"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return amenitiesList
    }

    // Amenity 데이터 클래스
    data class Amenity(val name: String, val distance: Int, val type: String)

    // RecyclerView 어댑터
    inner class AmenityAdapter(private val amenities: List<Amenity>) :
        RecyclerView.Adapter<AmenityAdapter.AmenityViewHolder>() {

        inner class AmenityViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            val icon: android.widget.ImageView = itemView.findViewById(R.id.amenityIcon)
            val text: android.widget.TextView = itemView.findViewById(R.id.amenityText)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): AmenityViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.amenity_item, parent, false)
            return AmenityViewHolder(view)
        }

        override fun onBindViewHolder(holder: AmenityViewHolder, position: Int) {
            val amenity = amenities[position]
            holder.text.text = "${amenity.name}\n거리: ${amenity.distance}m"

            val iconRes = when (amenity.type) {
                "화장실" -> R.drawable.ic_restroom
                "편의점" -> R.drawable.ic_convenience_store
                "식당" -> R.drawable.ic_restaurant
                else -> 0
            }
            holder.icon.setImageResource(iconRes)
        }

        override fun getItemCount(): Int = amenities.size
    }
}
