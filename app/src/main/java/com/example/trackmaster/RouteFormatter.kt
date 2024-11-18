import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan

class RouteFormatter(private val lineColors: Map<Int, String>) {

    fun formatRouteWithColors(route: List<String>): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        for ((index, station) in route.withIndex()) {
            val color = lineColors[getLineNumber(station)] ?: "#000000"
            val start = builder.length
            builder.append(station)
            val end = builder.length
            builder.setSpan(
                ForegroundColorSpan(Color.parseColor(color)),
                start,
                end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (index < route.size - 1) builder.append(" -> ")
        }
        return builder
    }

    fun getLineNumber(station: String): Int {
        return station.substring(0, 1).toInt()
    }

    companion object {
        val defaultLineColors = mapOf(
            1 to "#FF5733",
            2 to "#33FF57",
            3 to "#3357FF",
            4 to "#FFC300",
            5 to "#900C3F",
            6 to "#581845",
            7 to "#DAF7A6",
            8 to "#C70039",
            9 to "#1C2833"
        )
    }
}



