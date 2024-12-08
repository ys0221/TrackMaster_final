package com.example.trackmaster

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
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
                searchStationInput.error = "역 번호를 입력하세요."
                clearAmenitiesList()
            }
        }

        // 입력 이벤트 처리로 오류 초기화
        searchStationInput.addTextChangedListener {
            searchStationInput.error = null
        }
    }

    private fun searchAmenitiesForStation(stationNumber: String) {
        val newAmenitiesList = loadAmenitiesForStation(stationNumber)
        amenitiesList.clear()
        amenitiesList.addAll(newAmenitiesList)
        adapter.notifyDataSetChanged()

        if (newAmenitiesList.isEmpty()) {
            // EditText의 setError로 오류 메시지 표시
            val searchStationInput = findViewById<EditText>(R.id.searchStationInput)
            searchStationInput.error = "역 이름이 올바르지 않습니다."
            amenitiesRecyclerView.visibility = RecyclerView.GONE
        } else {
            // 오류 메시지 초기화
            val searchStationInput = findViewById<EditText>(R.id.searchStationInput)
            searchStationInput.error = null

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

            // 이름별 아이콘 설정
            val iconRes = when (amenity.type) {
                "화장실" -> R.drawable.ic_restroom
                "편의점" -> when (amenity.name) {
                    "GS25" -> R.drawable.ic_gs25
                    "CU" -> R.drawable.ic_cu
                    "세븐일레븐" -> R.drawable.ic_7eleven
                    "이마트24" -> R.drawable.ic_emart24
                    else -> R.drawable.ic_convenience_store
                }
                "식당" -> when (amenity.name) {
                    "아레나" -> R.drawable.ic_101
                    "김밥천국" -> R.drawable.ic_102
                    "역전할머니맥주" -> R.drawable.ic_103
                    "롤링파스타" -> R.drawable.ic_104
                    "맥도날드" -> R.drawable.ic_105
                    "주다방" -> R.drawable.ic_106
                    "롯데리아" -> R.drawable.ic_107
                    "맘스터치" -> R.drawable.ic_108
                    "한신포차" -> R.drawable.ic_109
                    "쉑쉑버거" -> R.drawable.ic_110
                    "FIVE GUYS" -> R.drawable.ic_111
                    "1943" -> R.drawable.ic_112
                    "에그드랍" -> R.drawable.ic_113
                    "포차천국" -> R.drawable.ic_114
                    "아비꼬" -> R.drawable.ic_115
                    "쿠우쿠우" -> R.drawable.ic_116
                    "경양카츠" -> R.drawable.ic_117
                    "오유미당" -> R.drawable.ic_118
                    "별밤" -> R.drawable.ic_119
                    "BBQ" -> R.drawable.ic_120
                    "노랑통닭" -> R.drawable.ic_121
                    "처갓집양념치킨" -> R.drawable.ic_122
                    "투다리" -> R.drawable.ic_123 // 1호선
                    "굽네치킨" -> R.drawable.ic_201
                    "가마치통닭" -> R.drawable.ic_202
                    "멕시카나치킨" -> R.drawable.ic_203
                    "범맥주" -> R.drawable.ic_204
                    "네네치킨" -> R.drawable.ic_205
                    "자담치킨" -> R.drawable.ic_206
                    "호식이두마리치킨" -> R.drawable.ic_207
                    "홍콩반점" -> R.drawable.ic_208
                    "짬뽕지존" -> R.drawable.ic_209
                    "반올림피자" -> R.drawable.ic_210
                    "피자스쿨" -> R.drawable.ic_211
                    "피자마루" -> R.drawable.ic_212
                    "피자나라치킨공주" -> R.drawable.ic_213
                    "피자헛" -> R.drawable.ic_214
                    "도미노" -> R.drawable.ic_215
                    "백종원의 빽보이피자" -> R.drawable.ic_216
                    "파파존스" -> R.drawable.ic_217 // 2호선
                    "피자알볼로" -> R.drawable.ic_301
                    "미스터피자" -> R.drawable.ic_302
                    "두찜" -> R.drawable.ic_303
                    "한촌설렁탕" -> R.drawable.ic_304
                    "금별맥주" -> R.drawable.ic_305
                    "땅스부대찌개" -> R.drawable.ic_306
                    "짚신매운갈비찜" -> R.drawable.ic_307
                    "양평해장국" -> R.drawable.ic_308
                    "응급실국물떡볶이" -> R.drawable.ic_401 //3호선
                    "배떡" -> R.drawable.ic_402
                    "청년다방" -> R.drawable.ic_403
                    "스텔라떡볶이" -> R.drawable.ic_404
                    "할리스" -> R.drawable.ic_405
                    "신전떡볶이" -> R.drawable.ic_406
                    "와플대학" -> R.drawable.ic_407
                    "셍활맥주" -> R.drawable.ic_408
                    "동대문엽기떡볶이" -> R.drawable.ic_409
                    "서브웨이" -> R.drawable.ic_410
                    "요거프레소" -> R.drawable.ic_411
                    "이디야커피" -> R.drawable.ic_412
                    "스타벅스" -> R.drawable.ic_413
                    "용용선생" -> R.drawable.ic_414
                    "메가커피" -> R.drawable.ic_415
                    "파스쿠찌" -> R.drawable.ic_416
                    "디저트39" -> R.drawable.ic_417 // 4호선
                    "요거트 아이스크릠의 정석" -> R.drawable.ic_501
                    "크라운호프" -> R.drawable.ic_502
                    "뚜레쥬르" -> R.drawable.ic_503
                    "파리바게트" -> R.drawable.ic_504
                    "설빙" -> R.drawable.ic_505
                    "김복남맥주" -> R.drawable.ic_506
                    "달콤왕가탕후루" -> R.drawable.ic_507 // 5호선
                    "배스킨라빈스" -> R.drawable.ic_601
                    "메가박스" -> R.drawable.ic_602
                    "롯데시네마" -> R.drawable.ic_603
                    "뉴욕야시장" -> R.drawable.ic_604
                    "홍루이젠" -> R.drawable.ic_605
                    "poke all day" -> R.drawable.ic_606
                    "노티드" -> R.drawable.ic_607
                    "폴바셋" -> R.drawable.ic_608
                    "샐러디" -> R.drawable.ic_609
                    "봉구비어" -> R.drawable.ic_610
                    "던킨" -> R.drawable.ic_611
                    "더벤티" -> R.drawable.ic_612
                    "크리스피크림도넛" -> R.drawable.ic_613
                    "돈까스클럽" -> R.drawable.ic_614
                    "토끼정" -> R.drawable.ic_615
                    "벡소정" -> R.drawable.ic_616
                    "한솥도시락" -> R.drawable.ic_617
                    "얌샘김밥" -> R.drawable.ic_618
                    "대한곱창" -> R.drawable.ic_619
                    "탐라포차" -> R.drawable.ic_620
                    "준코" -> R.drawable.ic_621 // 6호선
                    // "김밥천국" -> R.drawable.ic_622
                    "스시린" -> R.drawable.ic_701
                    "서울주막" -> R.drawable.ic_702
                    "원할머니보쌈족발" -> R.drawable.ic_703 // 6호선
                    "원조부안집" -> R.drawable.ic_704
                    "이차돌" -> R.drawable.ic_705
                    "명륜진사갈비" -> R.drawable.ic_706
                    "등촌칼국수" -> R.drawable.ic_707 // 7호선
                    "노모어피자" -> R.drawable.ic_801
                    "만만코코로" -> R.drawable.ic_802
                    "족발야시장" -> R.drawable.ic_803
                    "지금 보고싶다" -> R.drawable.ic_804
                    "한신우동" -> R.drawable.ic_805
                    "포메인" -> R.drawable.ic_806 // 8호선
                    "수상한포차" -> R.drawable.ic_901
                    "탐앤탐스" -> R.drawable.ic_902
                    "치치" -> R.drawable.ic_903
                    "엔젤리너스" -> R.drawable.ic_904 // 9호선



                    else -> R.drawable.ic_restaurant
                }
                else -> R.drawable.ic_default // 기본 아이콘
            }

            // 아이콘 설정
            holder.icon.setImageResource(iconRes)
        }


        override fun getItemCount(): Int = amenities.size
    }
}