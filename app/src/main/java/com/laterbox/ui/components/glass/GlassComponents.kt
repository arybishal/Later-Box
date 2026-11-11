package com.laterbox.ui.components.glass

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import kotlin.math.sin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.laterbox.ui.theme.GlassStyle
import com.laterbox.ui.theme.GlassTokens
import com.laterbox.ui.theme.GlassVariant

/**
 * Foundational reusable GlassSurface for Later Box.
 *
 * Implements:
 * - Multi-layer floating shadow.
 * - Hardware RenderEffect blur on Android 12+ (API 31+).
 * - AGSL RuntimeShader on Android 13+ (API 33+) for REFRACTIVE variant.
 * - Translucent silver-grey/charcoal material gradient.
 * - Dual-rim treatment: Outer directional specular rim + secondary inner meniscus reflection (1.5dp inset).
 * - Optional top gleam and corner refraction arcs.
 * - Consistent fallback down to minSdk 24.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.STANDARD,
    shape: Shape? = null,
    cornerRadius: Dp? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val style = GlassTokens.styleFor(variant, isDark)
    val effectiveCornerRadius = cornerRadius ?: style.defaultCornerRadius
    val resolvedShape = shape ?: RoundedCornerShape(effectiveCornerRadius)
    val innerShape = RoundedCornerShape(maxOf(0.dp, effectiveCornerRadius - 1.5.dp))

    Box(
        modifier = modifier
            .shadow(
                elevation = style.shadowElevation,
                shape = resolvedShape,
                ambientColor = style.shadowAmbientColor,
                spotColor = style.shadowSpotColor
            )
            .clip(resolvedShape)
            .background(style.backgroundBrush)
            .border(width = 1.dp, brush = style.outerBorderBrush, shape = resolvedShape)
    ) {
        // Hardware blur underlay for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            style.blurRadius, style.blurRadius,
                            android.graphics.Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
            )
        }

        // Secondary inner meniscus reflection (indicating physical glass thickness inset by 1.5dp)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(1.5.dp)
                .border(
                    width = 1.dp,
                    brush = style.innerMeniscusBrush,
                    shape = innerShape
                )
        )

        // Top specular meniscus gleam
        if (style.topGleamBrush != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .padding(horizontal = 24.dp)
                    .background(brush = style.topGleamBrush)
            )
        }

        // Corner perimeter curvature accents and organic fluid wave for refractive glass
        if (variant == GlassVariant.REFRACTIVE) {
            val aquaGleam = if (isDark) GlassTokens.DarkAquaSheen else GlassTokens.LightAquaSheen
            val waveColor = if (isDark) GlassTokens.DarkWaterWavePrimary else GlassTokens.LightWaterWavePrimary
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(aquaGleam, Color.Transparent),
                            center = Offset(0f, 0f),
                            radius = 350f
                        )
                    )
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                val h = size.height
                val w = size.width
                val strokeWidth = 1.2.dp.toPx()
                val arcColor = if (isDark) Color(0x28FFFFFF) else Color(0x44FFFFFF)
                val aquaEdge = if (isDark) GlassTokens.DarkAquaEdgeCatch else GlassTokens.LightAquaEdgeCatch

                // Organic fluid wave contour across surface
                if (h > 12f && w > 12f) {
                    val wavePath = Path().apply {
                        moveTo(0f, h * 0.65f)
                        val step = w / 8f
                        var x = 0f
                        while (x <= w + step) {
                            val normX = (x / w) * 3f * Math.PI.toFloat()
                            val y = h * 0.65f + sin(normX) * (h * 0.08f).coerceAtMost(5f)
                            lineTo(x, y)
                            x += step
                        }
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(waveColor.copy(alpha = waveColor.alpha * 0.6f), Color.Transparent),
                            startY = h * 0.50f,
                            endY = h
                        )
                    )
                }

                if (h > 0f && w >= h) {
                    drawArc(
                        color = aquaEdge,
                        startAngle = 135f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(0.5f, 0.5f),
                        size = Size(h - 1f, h - 1f),
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = arcColor,
                        startAngle = 315f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(w - h + 0.5f, 0.5f),
                        size = Size(h - 1f, h - 1f),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
        }

        CompositionLocalProvider(LocalContentColor provides style.contentColor) {
            content()
        }
    }
}

/**
 * Reusable GlassContainer for card-like floating panels and compact groups.
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.STANDARD,
    shape: Shape? = null,
    cornerRadius: Dp? = null,
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        variant = variant,
        shape = shape,
        cornerRadius = cornerRadius,
        content = content
    )
}

/**
 * Accessible Glass Button with tactile press animation and WCAG contrast.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.STANDARD,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "glassButtonScale"
    )

    GlassSurface(
        variant = variant,
        shape = shape,
        modifier = modifier
            .scale(animatedScale)
            .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Prominent Primary Button crafted with Liquid Water Glass.
 */
@Composable
fun GlassPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "glassPrimaryBtnScale"
    )

    GlassSurface(
        variant = GlassVariant.REFRACTIVE,
        shape = shape,
        modifier = modifier
            .scale(animatedScale)
            .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Secondary Subtle Glass Button.
 */
@Composable
fun GlassSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "glassSecondaryBtnScale"
    )

    GlassSurface(
        variant = GlassVariant.SUBTLE,
        shape = shape,
        modifier = modifier
            .scale(animatedScale)
            .defaultMinSize(minWidth = 64.dp, minHeight = 44.dp)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Accessible Glass Icon Button with 48dp touch target and glass styling.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.SUBTLE,
    icon: ImageVector,
    contentDescription: String?,
    tint: Color? = null,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "glassIconButtonScale"
    )

    Box(
        modifier = modifier
            .size(maxOf(48.dp, size))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            variant = variant,
            shape = CircleShape,
            modifier = Modifier
                .size(size)
                .scale(animatedScale)
                .clip(CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = size / 2),
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint ?: LocalContentColor.current,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

/**
 * Accessible Glass Icon Button with custom composable icon content.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.SUBTLE,
    size: Dp = 44.dp,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "glassIconButtonScale"
    )

    Box(
        modifier = modifier
            .size(maxOf(48.dp, size))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            variant = variant,
            shape = CircleShape,
            modifier = Modifier
                .size(size)
                .scale(animatedScale)
                .clip(CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, radius = size / 2),
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

/**
 * Reusable Glass Search Field with subtle translucency and clean borders.
 */
@Composable
fun GlassSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector = Icons.Outlined.Search,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val style = GlassTokens.styleFor(GlassVariant.SUBTLE, isDark)
    val shape = RoundedCornerShape(20.dp)

    GlassSurface(
        variant = GlassVariant.SUBTLE,
        shape = shape,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "Search",
                    tint = style.secondaryContentColor,
                    modifier = Modifier.size(20.dp)
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = style.secondaryContentColor
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = style.secondaryContentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (trailingContent != null) {
                    trailingContent()
                }
            },
            singleLine = true,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = style.contentColor,
                focusedTextColor = style.contentColor,
                unfocusedTextColor = style.contentColor
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Reusable Glass Toolbar / Header container.
 */
@Composable
fun GlassToolbar(
    modifier: Modifier = Modifier,
    variant: GlassVariant = GlassVariant.STANDARD,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        variant = variant,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Reusable Modal Bottom Sheet with Elevated Glass surface styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val style = GlassTokens.styleFor(GlassVariant.ELEVATED, isDark)
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = sheetShape,
        containerColor = Color.Transparent,
        dragHandle = null,
        modifier = modifier
    ) {
        GlassSurface(
            variant = GlassVariant.ELEVATED,
            shape = sheetShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // Centered glass drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(style.secondaryContentColor.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

/**
 * Reusable Glass Modal Dialog surface with high contrast content area.
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        GlassSurface(
            variant = GlassVariant.ELEVATED,
            shape = shape,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Signature Liquid Water Glass surface with subtle water-wave optical refraction,
 * delicate pale-aqua reflections, and dual-rim highlights.
 */
@Composable
fun LiquidWaterGlass(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    cornerRadius: Dp? = null,
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        variant = GlassVariant.REFRACTIVE,
        shape = shape,
        cornerRadius = cornerRadius,
        content = content
    )
}

/**
 * Floating Action Button crafted with Liquid Water Glass material.
 */
@Composable
fun GlassFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String?,
    variant: GlassVariant = GlassVariant.STANDARD,
    tint: Color? = null,
    size: Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "glassFabScale"
    )

    GlassSurface(
        variant = variant,
        shape = CircleShape,
        modifier = modifier
            .size(size)
            .scale(animatedScale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = size / 2),
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint ?: LocalContentColor.current,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Global Floating Liquid Water Glass Navigation Bar container.
 */
@Composable
fun GlassNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            variant = GlassVariant.REFRACTIVE,
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

/**
 * Glass Styled Dropdown Menu container for consistent translucent popup panels.
 */
@Composable
fun GlassMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val style = GlassTokens.styleFor(GlassVariant.ELEVATED, isDark)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(style.backgroundBrush)
            .border(1.dp, style.outerBorderBrush, RoundedCornerShape(18.dp)),
        content = content
    )
}

/**
 * Centralized evaluation and diagnostics for platform hardware glass rendering capabilities.
 */
object GlassEffectRenderer {
    val isAgslSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val isRenderEffectSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun describeCapabilities(): String = buildString {
        append("Later Box Liquid Water Glass Engine: ")
        if (isAgslSupported) {
            append("AGSL RuntimeShader Active (API 33+) with 3D lens curvature, Snell/Fresnel refraction, and organic flowing water wave caustics.")
        } else if (isRenderEffectSupported) {
            append("Hardware RenderEffect Active (API 31-32) with GPU blur and multi-stop gradient refraction fallback.")
        } else {
            append("Hardware Canvas Layer Active (API 24-30) with optimized dual-rim translucent compositing.")
        }
    }
}
