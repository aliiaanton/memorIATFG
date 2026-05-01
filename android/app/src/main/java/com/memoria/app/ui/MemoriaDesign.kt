package com.memoria.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val MemoriaBackground = Color(0xFFFCFDFB)
internal val MemoriaInk = Color(0xFF08272D)
internal val MemoriaMuted = Color(0xFF5F7774)
internal val MemoriaSage = Color(0xFF7EA287)
internal val MemoriaSageDark = Color(0xFF6F947B)
internal val MemoriaSageSoft = Color(0xFFDDE9E2)
internal val MemoriaSagePale = Color(0xFFEFF5F1)
internal val MemoriaCardBorder = Color(0xFFDCE8E1)
internal val MemoriaListSurface = Color(0xFFF4F7F5)
internal val MemoriaWarning = Color(0xFFEF9D93)
internal val MemoriaWarningDark = Color(0xFFCF6B62)
internal val MemoriaWarningSoft = Color(0xFFFDEDEA)
internal val MemoriaWarningPanel = Color(0xFFFFF6F3)

internal val MemoriaPanelShape = RoundedCornerShape(14.dp)
internal val MemoriaInnerShape = RoundedCornerShape(11.dp)
internal val MemoriaButtonShape = RoundedCornerShape(10.dp)

internal enum class MemoriaGlyph {
    Heart,
    Caregiver,
    Home,
    Patients,
    Brain,
    Book,
    Bell,
    Play,
    Pause,
    Check,
    Error,
    Mic,
    Dot,
    Plus,
    Edit,
    Trash,
    Exit,
    ChevronRight
}

internal data class CaregiverNavItem(
    val label: String,
    val icon: MemoriaGlyph
)

@Composable
internal fun MemoriaScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MemoriaBackground,
        content = content
    )
}

@Composable
internal fun BrandMark(
    icon: MemoriaGlyph,
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
    outerColor: Color = MemoriaSagePale,
    innerColor: Color = MemoriaSageSoft,
    iconColor: Color = MemoriaSage
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = outerColor
        ) {}
        Surface(
            modifier = Modifier.size(size * 0.66f),
            shape = CircleShape,
            color = innerColor
        ) {}
        MemoriaLineIcon(
            glyph = icon,
            color = iconColor,
            modifier = Modifier.size(size * 0.34f)
        )
    }
}

@Composable
internal fun ModeChoiceCard(
    title: String,
    subtitle: String,
    icon: MemoriaGlyph,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) MemoriaSage else Color(0xFFB0D0BE)
    val foreground = if (selected) Color.White else MemoriaInk
    val iconBackground = if (selected) Color.White.copy(alpha = 0.12f) else Color(0xFF9FBFAE)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 128.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = background,
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(18.dp),
                color = iconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    MemoriaLineIcon(
                        glyph = icon,
                        color = foreground,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    color = foreground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = foreground.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
internal fun CaregiverTopBar(onExit: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        color = MemoriaSage
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "memorIA Cuidador",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconOnlyButton(
                glyph = MemoriaGlyph.Exit,
                contentColor = Color.White,
                containerColor = Color.Transparent,
                onClick = onExit
            )
        }
    }
}

@Composable
internal fun CaregiverBottomBar(
    items: List<CaregiverNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MemoriaCardBorder.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEachIndexed { index, item ->
                val selected = selectedIndex == index
                val color = if (selected) MemoriaSage else MemoriaMuted
                Surface(
                    modifier = Modifier
                        .widthIn(min = 72.dp)
                        .height(52.dp)
                        .clickable { onSelect(index) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) MemoriaSagePale else Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        MemoriaLineIcon(
                            glyph = item.icon,
                            color = color,
                            modifier = Modifier.size(21.dp)
                        )
                        Text(
                            text = item.label,
                            color = color,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ScreenHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        color = MemoriaInk,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
internal fun MemoriaPanel(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    borderColor: Color = MemoriaCardBorder,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MemoriaPanelShape,
        color = background,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(contentPadding)) {
            content()
        }
    }
}

@Composable
internal fun PillBadge(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = MemoriaSagePale,
    contentColor: Color = MemoriaSageDark
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = background
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = contentColor,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
internal fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: MemoriaGlyph? = null,
    height: Dp = 40.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = MemoriaButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MemoriaSage,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFB9CCBF),
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (icon != null) {
            MemoriaLineIcon(glyph = icon, color = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = MemoriaMuted
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .background(MemoriaListSurface, MemoriaButtonShape)
            .height(40.dp),
        shape = MemoriaButtonShape,
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun IconOnlyButton(
    glyph: MemoriaGlyph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MemoriaSagePale,
    contentColor: Color = MemoriaSage,
    size: Dp = 40.dp
) {
    Surface(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick),
        shape = if (size <= 42.dp) CircleShape else RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            MemoriaLineIcon(
                glyph = glyph,
                color = contentColor,
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}

@Composable
internal fun SoftListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickModifier = if (onClick == null) modifier else modifier.clickable(onClick = onClick)
    Surface(
        modifier = clickModifier.fillMaxWidth(),
        shape = MemoriaInnerShape,
        color = MemoriaListSurface
    ) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
            content()
        }
    }
}

@Composable
internal fun DeletableChip(
    text: String,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MemoriaWarningSoft
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = text, color = MemoriaWarningDark, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                MemoriaLineIcon(
                    glyph = MemoriaGlyph.Trash,
                    color = MemoriaWarningDark,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
internal fun PatientStateArt(
    glyph: MemoriaGlyph,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 136.dp,
    ring: Boolean = false
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (ring) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(3.dp, accent.copy(alpha = 0.18f))
            ) {}
        }
        Surface(
            modifier = Modifier.size(size * 0.68f),
            shape = CircleShape,
            color = accent.copy(alpha = 0.18f)
        ) {}
        Surface(
            modifier = Modifier.size(size * 0.46f),
            shape = CircleShape,
            color = accent.copy(alpha = 0.14f)
        ) {}
        MemoriaLineIcon(
            glyph = glyph,
            color = accent,
            modifier = Modifier.size(size * 0.28f)
        )
    }
}

@Composable
internal fun MemoriaLineIcon(
    glyph: MemoriaGlyph,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = (size.minDimension * 0.095f).coerceAtLeast(2.1f)
        fun point(x: Float, y: Float) = Offset(w * x, h * y)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(
                color = color,
                start = point(x1, y1),
                end = point(x2, y2),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
        fun strokedPath(path: Path) {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        when (glyph) {
            MemoriaGlyph.Heart -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.2f, h * 0.58f, w * 0.12f, h * 0.42f, w * 0.23f, h * 0.25f)
                    cubicTo(w * 0.34f, h * 0.09f, w * 0.48f, h * 0.18f, w * 0.5f, h * 0.31f)
                    cubicTo(w * 0.52f, h * 0.18f, w * 0.66f, h * 0.09f, w * 0.77f, h * 0.25f)
                    cubicTo(w * 0.88f, h * 0.42f, w * 0.8f, h * 0.58f, w * 0.5f, h * 0.82f)
                }
                strokedPath(path)
            }
            MemoriaGlyph.Caregiver -> {
                drawCircle(
                    color = color,
                    radius = w * 0.16f,
                    center = point(0.38f, 0.33f),
                    style = Stroke(width = stroke)
                )
                val shoulders = Path().apply {
                    moveTo(w * 0.17f, h * 0.72f)
                    cubicTo(w * 0.17f, h * 0.55f, w * 0.28f, h * 0.48f, w * 0.42f, h * 0.49f)
                    cubicTo(w * 0.5f, h * 0.5f, w * 0.56f, h * 0.55f, w * 0.58f, h * 0.65f)
                }
                strokedPath(shoulders)
                drawCircle(color = color, radius = w * 0.09f, center = point(0.68f, 0.57f), style = Stroke(width = stroke))
                line(0.68f, 0.42f, 0.68f, 0.72f)
                line(0.53f, 0.57f, 0.83f, 0.57f)
                line(0.58f, 0.47f, 0.78f, 0.67f)
                line(0.78f, 0.47f, 0.58f, 0.67f)
            }
            MemoriaGlyph.Home -> {
                line(0.2f, 0.48f, 0.5f, 0.22f)
                line(0.5f, 0.22f, 0.8f, 0.48f)
                line(0.28f, 0.46f, 0.28f, 0.78f)
                line(0.72f, 0.46f, 0.72f, 0.78f)
                line(0.28f, 0.78f, 0.72f, 0.78f)
                line(0.43f, 0.78f, 0.43f, 0.58f)
                line(0.57f, 0.58f, 0.57f, 0.78f)
            }
            MemoriaGlyph.Patients -> {
                drawCircle(color = color, radius = w * 0.13f, center = point(0.42f, 0.34f), style = Stroke(width = stroke))
                drawCircle(color = color, radius = w * 0.1f, center = point(0.68f, 0.4f), style = Stroke(width = stroke))
                val main = Path().apply {
                    moveTo(w * 0.18f, h * 0.77f)
                    cubicTo(w * 0.2f, h * 0.58f, w * 0.31f, h * 0.53f, w * 0.42f, h * 0.53f)
                    cubicTo(w * 0.55f, h * 0.53f, w * 0.64f, h * 0.62f, w * 0.66f, h * 0.77f)
                }
                val side = Path().apply {
                    moveTo(w * 0.58f, h * 0.68f)
                    cubicTo(w * 0.66f, h * 0.6f, w * 0.8f, h * 0.62f, w * 0.84f, h * 0.77f)
                }
                strokedPath(main)
                strokedPath(side)
            }
            MemoriaGlyph.Brain -> {
                drawCircle(color = color, radius = w * 0.16f, center = point(0.4f, 0.38f), style = Stroke(width = stroke))
                drawCircle(color = color, radius = w * 0.16f, center = point(0.6f, 0.38f), style = Stroke(width = stroke))
                drawCircle(color = color, radius = w * 0.17f, center = point(0.35f, 0.58f), style = Stroke(width = stroke))
                drawCircle(color = color, radius = w * 0.17f, center = point(0.65f, 0.58f), style = Stroke(width = stroke))
                line(0.5f, 0.25f, 0.5f, 0.78f)
            }
            MemoriaGlyph.Book -> {
                line(0.22f, 0.22f, 0.22f, 0.78f)
                line(0.5f, 0.3f, 0.5f, 0.82f)
                line(0.78f, 0.22f, 0.78f, 0.78f)
                val left = Path().apply {
                    moveTo(w * 0.22f, h * 0.22f)
                    cubicTo(w * 0.33f, h * 0.16f, w * 0.43f, h * 0.18f, w * 0.5f, h * 0.3f)
                }
                val right = Path().apply {
                    moveTo(w * 0.78f, h * 0.22f)
                    cubicTo(w * 0.67f, h * 0.16f, w * 0.57f, h * 0.18f, w * 0.5f, h * 0.3f)
                }
                strokedPath(left)
                strokedPath(right)
            }
            MemoriaGlyph.Bell -> {
                val dome = Path().apply {
                    moveTo(w * 0.28f, h * 0.62f)
                    cubicTo(w * 0.3f, h * 0.33f, w * 0.7f, h * 0.33f, w * 0.72f, h * 0.62f)
                    lineTo(w * 0.78f, h * 0.72f)
                    lineTo(w * 0.22f, h * 0.72f)
                    close()
                }
                strokedPath(dome)
                line(0.43f, 0.82f, 0.57f, 0.82f)
            }
            MemoriaGlyph.Play -> {
                val path = Path().apply {
                    moveTo(w * 0.34f, h * 0.24f)
                    lineTo(w * 0.34f, h * 0.76f)
                    lineTo(w * 0.76f, h * 0.5f)
                    close()
                }
                strokedPath(path)
            }
            MemoriaGlyph.Pause -> {
                drawRoundRect(
                    color = color,
                    topLeft = point(0.3f, 0.24f),
                    size = Size(w * 0.14f, h * 0.52f),
                    cornerRadius = CornerRadius(stroke, stroke),
                    style = Stroke(width = stroke)
                )
                drawRoundRect(
                    color = color,
                    topLeft = point(0.56f, 0.24f),
                    size = Size(w * 0.14f, h * 0.52f),
                    cornerRadius = CornerRadius(stroke, stroke),
                    style = Stroke(width = stroke)
                )
            }
            MemoriaGlyph.Check -> {
                line(0.24f, 0.54f, 0.43f, 0.72f)
                line(0.43f, 0.72f, 0.78f, 0.32f)
            }
            MemoriaGlyph.Error -> {
                line(0.28f, 0.28f, 0.72f, 0.72f)
                line(0.72f, 0.28f, 0.28f, 0.72f)
            }
            MemoriaGlyph.Mic -> {
                drawRoundRect(
                    color = color,
                    topLeft = point(0.38f, 0.18f),
                    size = Size(w * 0.24f, h * 0.42f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = Stroke(width = stroke)
                )
                val path = Path().apply {
                    moveTo(w * 0.25f, h * 0.46f)
                    cubicTo(w * 0.25f, h * 0.68f, w * 0.75f, h * 0.68f, w * 0.75f, h * 0.46f)
                }
                strokedPath(path)
                line(0.5f, 0.68f, 0.5f, 0.84f)
                line(0.36f, 0.84f, 0.64f, 0.84f)
            }
            MemoriaGlyph.Dot -> {
                drawCircle(color = color, radius = w * 0.22f, center = point(0.5f, 0.5f))
            }
            MemoriaGlyph.Plus -> {
                line(0.5f, 0.25f, 0.5f, 0.75f)
                line(0.25f, 0.5f, 0.75f, 0.5f)
            }
            MemoriaGlyph.Edit -> {
                line(0.25f, 0.72f, 0.7f, 0.27f)
                line(0.62f, 0.2f, 0.78f, 0.36f)
                line(0.2f, 0.8f, 0.36f, 0.74f)
            }
            MemoriaGlyph.Trash -> {
                line(0.28f, 0.34f, 0.72f, 0.34f)
                line(0.38f, 0.25f, 0.62f, 0.25f)
                line(0.34f, 0.38f, 0.38f, 0.78f)
                line(0.66f, 0.38f, 0.62f, 0.78f)
                line(0.38f, 0.78f, 0.62f, 0.78f)
                line(0.46f, 0.45f, 0.46f, 0.68f)
                line(0.54f, 0.45f, 0.54f, 0.68f)
            }
            MemoriaGlyph.Exit -> {
                line(0.26f, 0.22f, 0.52f, 0.22f)
                line(0.26f, 0.22f, 0.26f, 0.78f)
                line(0.26f, 0.78f, 0.52f, 0.78f)
                line(0.48f, 0.5f, 0.82f, 0.5f)
                line(0.68f, 0.35f, 0.82f, 0.5f)
                line(0.68f, 0.65f, 0.82f, 0.5f)
            }
            MemoriaGlyph.ChevronRight -> {
                line(0.38f, 0.24f, 0.64f, 0.5f)
                line(0.64f, 0.5f, 0.38f, 0.76f)
            }
        }
    }
}
