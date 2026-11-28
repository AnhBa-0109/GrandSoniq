package ntu.ntth.grandsoniq.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ntu.ntth.grandsoniq.R

@Composable
internal fun GrandSoniqHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier.widthIn(max = 480.dp).fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).border(1.dp, Color(0xFF343D36), CircleShape))
        Text(
            title,
            Modifier.weight(1f).semantics { heading() },
            color = Color(0xFFF3F5EF),
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.2).sp,
            fontWeight = FontWeight.SemiBold
        )
        Box(Modifier.width(40.dp).height(48.dp), contentAlignment = Alignment.Center) {
            AssetSvgIcon("discover/notifications.svg", 24.dp, stringResource(R.string.header_notifications))
        }
    }
}
