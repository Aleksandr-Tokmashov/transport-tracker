package com.example.transporttracker.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.transporttracker.R
import com.example.transporttracker.ui.analytics.TimeBinCount
import com.example.transporttracker.ui.analytics.TransportShare

@Composable
fun TransportShareChart(shares: List<TransportShare>) {
    if (shares.isEmpty()) return
    ChartCard(title = stringResource(R.string.chart_transport_title)) {
        shares.forEachIndexed { index, share ->
            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
            TransportBarRow(share = share)
        }
    }
}

@Composable
fun TimeBinChart(counts: List<TimeBinCount>) {
    if (counts.all { it.count == 0 }) return
    ChartCard(title = stringResource(R.string.chart_time_title)) {
        counts.forEachIndexed { index, bin ->
            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
            TimeBinRow(bin = bin)
        }
    }
}

@Composable
private fun TransportBarRow(share: TransportShare) {
    val color = share.transportType.color()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = share.transportType.icon(),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = share.transportType.localizedName(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp),
            maxLines = 1
        )
        AnimatedBar(
            fraction = share.fraction,
            color = color,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${share.tripCount}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = "%.0f km".format(share.distanceKm),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TimeBinRow(bin: TimeBinCount) {
    val color = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = bin.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
        AnimatedBar(
            fraction = bin.fraction,
            color = color,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${bin.count}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AnimatedBar(
    fraction: Float,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(fraction) { target = fraction }
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "bar"
    )
    Box(
        modifier = modifier
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.12f))
    ) {
        if (animated > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animated)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
