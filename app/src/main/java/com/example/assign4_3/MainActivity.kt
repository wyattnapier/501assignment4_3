package com.example.assign4_3

import android.R
import android.os.Bundle
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.assign4_3.ui.theme.Assign4_3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = MainViewModel()
        enableEdgeToEdge()
        setContent {
            Assign4_3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        vm
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(vm: MainViewModel) {
    val tempList by vm.tempEntries.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ManageDataGeneration(vm)
        TempReadingList(tempList)
    }
}

@Composable
fun ManageDataGeneration(vm: MainViewModel) {
    val pauseDataGeneration by vm.autoGenerate.collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth(fraction=0.8f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Auto generate temperature data")
        Switch(checked = pauseDataGeneration, onCheckedChange = {vm.setAutoGenerate(it)})
    }
}

@Composable
fun TempReadingList(tempList: List<TempPoint>) {
    Column { // only 20 entries so don't need lazycolumn
        tempList.forEach { entry: TempPoint ->
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.timestamp  + " - " + entry.temp.toString())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Assign4_3Theme {
        MainScreen(MainViewModel())
    }
}