package tv.iptv.tun.tviptv.ui.customview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconButtonPositive(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(4.dp),
    onClick: () -> Unit,
    iconContentDescription: String? = null,
    fontSize: TextUnit = 14.sp
) {
    IconButton(
        modifier,
        title,
        icon,
        onClick,
        iconContentDescription ?: "${title}_Positive",
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = shape,
        fontSize = fontSize
    )
}

@Composable
fun IconButtonNegative(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector?,
    iconContentDescription: String? = null,
    shape: Shape = RoundedCornerShape(4.dp),
    onClick: () -> Unit,
    fontSize: TextUnit = 14.sp
) {
    IconButton(
        modifier,
        title,
        icon,
        onClick,
        iconContentDescription ?: "${title}_Positive",
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = shape,
        fontSize = fontSize
    )
}

@Composable
fun RoundedButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    onClick: () -> Unit,
    fontSize: TextUnit = 14.sp
) {
    IconButtonPositive(
        modifier = modifier,
        title = title,
        icon = icon,
        iconContentDescription = iconContentDescription,
        onClick = onClick,
        shape = RoundedCornerShape(50),
        fontSize = fontSize
    )
}

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    iconContentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    shape: Shape = RoundedCornerShape(4.dp),
    fontSize: TextUnit = 14.sp
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .padding(vertical = 9.dp)
            .defaultMinSize(minHeight = 48.dp),
        shape = shape,
        colors = ButtonDefaults.elevatedButtonColors()
            .copy(
                containerColor = containerColor,
                contentColor = contentColor,
            )
    ) {
        icon?.let {
            Icon(
                icon, iconContentDescription ?: title,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            title,
            fontSize = fontSize
        )
    }
}