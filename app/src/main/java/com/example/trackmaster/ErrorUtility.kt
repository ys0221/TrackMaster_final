package com.example.trackmaster

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView

class ErrorUtility() {
    fun displayErrorMessage(context: Context, resultLayout: LinearLayout, message: String) {
        val errorTextView = TextView(context).apply {
            text = message
            setTextColor(Color.RED)
            textSize = 16f
        }
        resultLayout.addView(errorTextView)
    }

    // 결과 초기화
    fun clearResults(resultLayout: LinearLayout) {
        resultLayout.removeAllViews()
    }
}