import com.example.trackmaster.Edge
import com.example.trackmaster.StationData

class GraphBuilder {

    fun buildGraph(stationList: List<StationData>): Map<String, MutableList<Edge>> {
        val graph = mutableMapOf<String, MutableList<Edge>>()
        for (station in stationList) {
            graph.computeIfAbsent(station.출발역) { mutableListOf() }
                .add(Edge(station.도착역, station.시간, station.거리, station.비용))
            graph.computeIfAbsent(station.도착역) { mutableListOf() }
                .add(Edge(station.출발역, station.시간, station.거리, station.비용))
        }
        return graph
    }
}



