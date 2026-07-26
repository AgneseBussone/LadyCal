package com.beacat.calendar.ladycal.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.beacat.calendar.ladycal.R
import com.beacat.calendar.ladycal.Utilities
import com.tyczj.extendedcalendarview.ExtendedCalendarView

@Composable
fun MainView(
    calendarView: ExtendedCalendarView,
    onResetToday: () -> Unit,
    onStartPeriod: () -> Unit,
    onAddMed: () -> Unit,
) {
    val viewModel: MainViewModel = hiltViewModel()

    val context = LocalContext.current
    val fabColor = Color(Utilities.getThemeColor(context, R.attr.colorAccent))

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)) {

        AndroidView(
            factory = { calendarView },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
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
            onClick = onStartPeriod,
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

//@Preview
//@Composable
//private fun MainViewPreview() {
//    MaterialTheme {
//        MainView(
//            calendarView = ExtendedCalendarView(),
//            onResetToday = {},
//            onStartPeriod = {},
//            onAddMed = {}
//        )
//    }
//}