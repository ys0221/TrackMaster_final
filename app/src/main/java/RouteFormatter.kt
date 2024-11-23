import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.graphics.Color

class RouteFormatter(private val lineColors: Map<Int, String>) {

    // 경로를 호선별 색상으로 포맷
    fun formatRouteWithColors(route: List<String>): CharSequence {
        val spannableBuilder = SpannableStringBuilder()
        for ((index, station) in route.withIndex()) {
            val lineNumber = getLineNumber(station) // 호선 번호 추출
            val color = lineColors[lineNumber] ?: "#000000" // 디폴트 색상은 검정
            val start = spannableBuilder.length
            spannableBuilder.append(station)
            val end = spannableBuilder.length
            spannableBuilder.setSpan(
                ForegroundColorSpan(Color.parseColor(color)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (index < route.size - 1) {
                spannableBuilder.append(" -> ")
            }
        }
        return spannableBuilder
    }

    // 역 이름에서 호선 번호 추출
    private fun getLineNumber(station: String): Int {
        return station.substring(0, 1).toInt()
    }

    companion object {
        val defaultLineColors = mapOf(
            1 to "#008000", // 1호선 초록색
            2 to "#000080", // 2호선 남색
            3 to "#8B4513", // 3호선 갈색
            4 to "#FF0000", // 4호선 빨간색
            5 to "#0000FF", // 5호선 파란색
            6 to "#FFD700", // 6호선 노란색
            7 to "#7CFC00", // 7호선 연두색
            8 to "#00CED1", // 8호선 하늘색
            9 to "#800080"  // 9호선 보라색
        )
    }
}



