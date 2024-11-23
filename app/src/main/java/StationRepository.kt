import android.content.Context

import com.example.trackmaster1119.StationData

class StationRepository {

    fun readCSV(context: Context): List<StationData> {
        val stationList = mutableListOf<StationData>()
        try {
            context.assets.open("stations.csv").use { inputStream ->
                inputStream.bufferedReader().useLines { lines ->
                    lines.drop(1).forEach { line ->
                        val tokens = line.split(",")
                        if (tokens.size == 5) {
                            stationList.add(
                                StationData(
                                    출발역 = tokens[0],
                                    도착역 = tokens[1],
                                    시간 = tokens[2].toInt(),
                                    거리 = tokens[3].toInt(),
                                    비용 = tokens[4].toInt()
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stationList
    }
}


