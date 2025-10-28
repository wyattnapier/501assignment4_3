package com.example.assign4_3;

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

public class MainViewModel {
    private val _tempEntries = MutableStateFlow<List<TempPoint>>(emptyList())
    val tempEntries = _tempEntries.asStateFlow()

    // TODO: turn into stateflow values instead
    var minTemp: Float? = null
    var maxTemp: Float? = null
    var avgTemp: Float? = null

    private var autoGenerateJob: Job? = null

    fun setAutoGenerate(flag: Boolean) {
        if (flag) {
            startAutoGenerate()
        } else {
            stopAutoGenerate()
        }
    }

    private fun startAutoGenerate() {
        autoGenerateJob?.cancel()
        autoGenerateJob = viewModelScope.launch {
            while (true) {
                delay(2 * 1000L)
                generateDataPoint()
                calculateMetrics()
            }
        }
    }

    private fun stopAutoGenerate() {
        autoGenerateJob?.cancel()
        autoGenerateJob = null
    }

    private fun generateDataPoint() {
        val randomTemp: Float = 65 + Math.random().toFloat() * 20 // random float in [65, 85]
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        addTemp(randomTemp, timestamp)
    }

    private fun addTemp(temp: Float, time: String) {
        val currentEntries = _tempEntries.value
        val newEntry = TempPoint(temp, time)
        val updatedEntries = currentEntries + newEntry

        // add newest entry and pop if size is too long
        val finalEntries = if (updatedEntries.size > 20) {
            updatedEntries.drop(1)
        } else {
            updatedEntries
        }

        _tempEntries.value = finalEntries
    }

    private fun calculateMetrics() {
        val currentEntries = _tempEntries.value
        val currentTemps = currentEntries.map{entry -> entry.temp} // just get temp values
        // TODO: decide if necessary
//        if (currentTemps.size < 20) {
//            Log.d("CalculateMetrics", "List of data is not long enough to calculate accurate metrics")
//            return
//        }
        minTemp = currentTemps.min()
        maxTemp = currentTemps.max()
        avgTemp = currentTemps.average().toFloat()
    }
}
