import com.example.trackmaster1119.Edge
import com.example.trackmaster1119.Path
import java.util.PriorityQueue

class PathFinder {

    // 공통 메서드: 역에서 호선 번호 추출
    private fun getLineNumber(station: String): Int {
        return station.substring(0, 1).toInt() // 예: "101" -> 1
    }

    // 최소 시간 경로
    fun findLowestTimePath(graph: Map<String, MutableList<Edge>>, start: String, end: String): Path? {
        return findPath(graph, start, end, "time")
    }

    // 최소 비용 경로
    fun findLowestCostPath(graph: Map<String, MutableList<Edge>>, start: String, end: String): Path? {
        return findPath(graph, start, end, "cost")
    }

    // 최소 환승 경로
    fun findLowestCostPathWithTransfers(graph: Map<String, MutableList<Edge>>, start: String, end: String): Path? {
        return findPath(graph, start, end, "transfers")
    }

    // 다익스트라 알고리즘 기반 공통 경로 탐색
    private fun findPath(
        graph: Map<String, MutableList<Edge>>,
        start: String,
        end: String,
        criteria: String
    ): Path? {
        // 거리, 비용, 시간, 환승 횟수 추적
        val distances = mutableMapOf<String, Int>().withDefault { Int.MAX_VALUE }
        val costs = mutableMapOf<String, Int>().withDefault { Int.MAX_VALUE }
        val times = mutableMapOf<String, Int>().withDefault { Int.MAX_VALUE }
        val transfers = mutableMapOf<String, Int>().withDefault { Int.MAX_VALUE }
        val previousNodes = mutableMapOf<String, String?>()

        val routeDistances = mutableMapOf<String, List<Int>>()
        val routeCosts = mutableMapOf<String, List<Int>>()
        val routeTimes = mutableMapOf<String, List<Int>>()

        // 초기값 설정
        distances[start] = 0
        costs[start] = 0
        times[start] = 0
        transfers[start] = 0

        routeDistances[start] = listOf()
        routeCosts[start] = listOf()
        routeTimes[start] = listOf()

        // 우선 순위 큐: 기준에 따라 정렬
        val priorityQueue = PriorityQueue<Triple<String, Int, Int>>(
            when (criteria) {
                "time" -> compareBy<Triple<String, Int, Int>> { times.getValue(it.first) }.thenBy { costs.getValue(it.first) }
                "cost" -> compareBy<Triple<String, Int, Int>> { costs.getValue(it.first) }.thenBy { times.getValue(it.first) }
                "transfers" -> compareBy<Triple<String, Int, Int>> { transfers.getValue(it.first) }.thenBy { times.getValue(it.first) }
                else -> throw IllegalArgumentException("Invalid criteria: $criteria")
            }
        )

        priorityQueue.add(Triple(start, 0, 0))

        // 다익스트라 탐색
        while (priorityQueue.isNotEmpty()) {
            val (currentStation, _, _) = priorityQueue.poll()

            if (currentStation == end) {
                val route = generateRoute(previousNodes, end)
                return Path(
                    station = currentStation,
                    totalCost = routeCosts[end]!!.sum(),
                    totalTransfers = transfers[end]!!,
                    route = route,
                    distances = routeDistances[end] ?: listOf(),
                    costs = routeCosts[end] ?: listOf(),
                    times = routeTimes[end] ?: listOf()
                )
            }

            for (edge in graph[currentStation] ?: emptyList()) {
                val newDistance = distances.getValue(currentStation) + edge.distance
                val newCost = costs.getValue(currentStation) + edge.cost
                val newTime = times.getValue(currentStation) + edge.time
                val newTransfers = if (getLineNumber(currentStation) != getLineNumber(edge.destination)) transfers.getValue(currentStation) + 1 else transfers.getValue(currentStation)

                // 기준에 따른 업데이트
                when (criteria) {
                    "time" -> {
                        if (newTime < times.getValue(edge.destination) ||
                            (newTime == times.getValue(edge.destination) && newCost < costs.getValue(edge.destination))
                        ) {
                            times[edge.destination] = newTime
                            costs[edge.destination] = newCost
                            transfers[edge.destination] = newTransfers
                            previousNodes[edge.destination] = currentStation
                            routeDistances[edge.destination] = routeDistances[currentStation]!! + edge.distance
                            routeCosts[edge.destination] = routeCosts[currentStation]!! + edge.cost
                            routeTimes[edge.destination] = routeTimes[currentStation]!! + edge.time
                            priorityQueue.add(Triple(edge.destination, 0, 0))
                        }
                    }
                    "cost" -> {
                        if (newCost < costs.getValue(edge.destination) ||
                            (newCost == costs.getValue(edge.destination) && newTime < times.getValue(edge.destination))
                        ) {
                            costs[edge.destination] = newCost
                            times[edge.destination] = newTime
                            transfers[edge.destination] = newTransfers
                            previousNodes[edge.destination] = currentStation
                            routeDistances[edge.destination] = routeDistances[currentStation]!! + edge.distance
                            routeCosts[edge.destination] = routeCosts[currentStation]!! + edge.cost
                            routeTimes[edge.destination] = routeTimes[currentStation]!! + edge.time
                            priorityQueue.add(Triple(edge.destination, 0, 0))
                        }
                    }
                    "transfers" -> {
                        if (newTransfers < transfers.getValue(edge.destination) ||
                            (newTransfers == transfers.getValue(edge.destination) && newTime < times.getValue(edge.destination))
                        ) {
                            transfers[edge.destination] = newTransfers
                            times[edge.destination] = newTime
                            costs[edge.destination] = newCost
                            previousNodes[edge.destination] = currentStation
                            routeDistances[edge.destination] = routeDistances[currentStation]!! + edge.distance
                            routeCosts[edge.destination] = routeCosts[currentStation]!! + edge.cost
                            routeTimes[edge.destination] = routeTimes[currentStation]!! + edge.time
                            priorityQueue.add(Triple(edge.destination, 0, 0))
                        }
                    }
                }
            }
        }

        return null // 경로를 찾을 수 없는 경우
    }

    // 경로 생성
    private fun generateRoute(previousNodes: Map<String, String?>, end: String): List<String> {
        val route = mutableListOf<String>()
        var current: String? = end
        while (current != null) {
            route.add(current)
            current = previousNodes[current]
        }
        return route.reversed()
    }
}
