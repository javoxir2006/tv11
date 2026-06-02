package com.iptv.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.iptv.tv.data.model.Channel
import com.iptv.tv.ui.theme.*

/**
 * Scrollable channel list panel — left side of the layout.
 *
 * Focus is managed externally by [HomeScreen] so that D-pad navigation
 * between the list and the fullscreen button works correctly.
 */
@Composable
fun ChannelList(
    channels: List<Channel>,
    selectedIndex: Int,
    focusRequesters: List<FocusRequester>,
    onChannelClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to keep selected item visible
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(
            index = selectedIndex.coerceIn(0, channels.lastIndex),
            scrollOffset = -80
        )
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(BgDark)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(channels) { index, channel ->
            ChannelItem(
                channel = channel,
                isSelected = index == selectedIndex,
                focusRequester = focusRequesters[index],
                onClick = { onChannelClick(index) }
            )
        }
    }
}

@Composable
private fun ChannelItem(
    channel: Channel,
    isSelected: Boolean,
    focusRequester: FocusRequester,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused || isSelected) GreenAccent else Color.Transparent,
        animationSpec = tween(150),
        label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) BgLight else Color.Transparent,
        animationSpec = tween(150),
        label = "bg"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = bgColor,
            focusedContainerColor = BgLight
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            // Channel number
            Text(
                text = "${channel.id}",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.width(28.dp)
            )

            // Circle logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(BgLight)
            ) {
                Text(
                    text = channel.logo,
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name
            Text(
                text = channel.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
