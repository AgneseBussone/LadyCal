package com.beacat.calendar.ladycal.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beacat.calendar.ladycal.R
import com.beacat.calendar.ladycal.Utilities
import java.time.LocalDate

@Composable
fun MainView() {
    val viewModel: MainViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MainViewContent(
        uiState = state,
        onResetToday = viewModel::resetCalendarToToday,
        onStartPeriod = viewModel::startPeriod,
        onAddMed = viewModel::addMedication
    )
}

@Composable
private fun MainViewContent(
    uiState: MainViewUIState,
    onResetToday: () -> Unit,
    onStartPeriod: (LocalDate?) -> Unit,
    onAddMed: () -> Unit,
) {
    val context = LocalContext.current
    val fabColor = Color(Utilities.getThemeColor(context, R.attr.colorAccent))
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)) {

        CalendarView(
            uiState = uiState,
            onSelectedDateChange = { selectedDate = it }
        )

        FloatingActionButton(
            onClick = onResetToday,
            backgroundColor = fabColor,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 50.dp, bottom = 20.dp)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }

        FloatingActionButton(
            onClick = { onStartPeriod(selectedDate) },
            backgroundColor = fabColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_start),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }

        FloatingActionButton(
            onClick = onAddMed,
            backgroundColor = fabColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 50.dp, bottom = 20.dp)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_med),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MainViewContent(
        uiState = MainViewUIState(),
        onResetToday = {},
        onStartPeriod = {},
        onAddMed = {},
    )
}