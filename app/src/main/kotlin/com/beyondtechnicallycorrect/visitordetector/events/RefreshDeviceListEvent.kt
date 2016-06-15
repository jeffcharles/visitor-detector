package com.beyondtechnicallycorrect.visitordetector.events

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout

data class RefreshDeviceListEvent(val context: Context, val swipeRefreshLayout: SwipeRefreshLayout)
