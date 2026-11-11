package com.laterbox.ui.theme

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material variants for the Later Box Global Glass Design System.
 */
enum class GlassVariant {
    /**
     * Subtle Glass:
     * Used for search fields, small floating controls, secondary toolbars, minor overlays, filter pill containers.
     * Characteristics: Light translucency (~75-80%), moderate blur (18f), delicate edge highlight, minimal depth (4dp).
     */
    SUBTLE,

    /**
     * Standard Glass:
     * Used for top navigation controls, floating action surfaces, note formatting toolbars, menus, compact panels.
     * Characteristics: Visible translucent material (~80-85%), stronger background blur (24f), subtle internal reflections,
     * fine double borders, soft floating shadow (8-10dp).
     */
    STANDARD,

    /**
     * Elevated Glass:
     * Used for bottom sheets, modal dialogs, larger popup panels, important overlays.
     * Characteristics: Greater visual depth (~88-94%), layered translucent surfaces, stronger separation from background,
     * high content readability, soft floating shadow (16-20dp), blur (32f).
     */
    ELEVATED,

    /**
     * Refractive Glass:
     * Used selectively for bottom navigation, primary floating AI assistant pill, prominent floating controls.
     * Characteristics: Translucent silver-grey material (~75-80%), 3D optical lens normal curvature and Fresnel refraction
     * via AGSL RuntimeShader on API 33+, curved edge highlights, soft depth (14-16dp), blur (28f).
     */
    REFRACTIVE
}

/**
 * Concrete glass style specification for a particular variant in a given theme.
 */
@Immutable
data class GlassStyle(
    val backgroundBrush: Brush,
    val outerBorderBrush: Brush,
    val innerMeniscusBrush: Brush,
    val topGleamBrush: Brush?,
    val shadowElevation: Dp,
    val shadowAmbientColor: Color,
    val shadowSpotColor: Color,
    val blurRadius: Float,
    val defaultCornerRadius: Dp,
    val contentColor: Color,
    val secondaryContentColor: Color
)

/**
 * Centralized Glass Design Tokens & Material System for Later Box.
 */
object GlassTokens {

    // --- Light Theme Glass Palettes ---
    // Silver-Grey Translucent Float Glass Base
    val LightRefractiveTop = Color(0xCCE2E6EB)     // 80% opacity cool platinum
    val LightRefractiveMid = Color(0xB3D5DBE2)     // 70% opacity silver float glass
    val LightRefractiveBottom = Color(0xBFCDD4DC)  // 75% opacity cool mineral glass

    val LightElevatedTop = Color(0xF2FFFFFF)       // 95% opacity frosted white
    val LightElevatedBottom = Color(0xEBF4F6F9)    // 92% opacity crisp neutral

    val LightStandardTop = Color(0xD9F8F9FB)       // 85% opacity silver-white
    val LightStandardBottom = Color(0xCCE8ECF2)    // 80% opacity soft silver

    val LightSubtleTop = Color(0xCCF4F6F8)         // 80% opacity mist silver
    val LightSubtleBottom = Color(0xBFE5E9EE)      // 75% opacity soft neutral

    // Light Theme Highlights & Borders
    val LightOuterRimTop = Color(0xE6FFFFFF)       // 90% bright white apex gleam
    val LightOuterRimMid = Color(0x66CBD2DC)       // 40% platinum light catch
    val LightOuterRimBottom = Color(0x24000000)    // 14% soft grounding edge

    val LightInnerMeniscusTop = Color(0x59FFFFFF)  // 35% secondary specular reflection
    val LightInnerMeniscusMid = Color(0x1AFFFFFF)  // 10% side reflection
    val LightTopGleam = Color(0x99FFFFFF)          // 60% white horizontal gleam

    // Pale Aqua Water Refraction & Caustic Reflections (Light Theme)
    val LightAquaSheen = Color(0x2E38BDF8)         // 18% delicate pale-aqua water gleam
    val LightAquaEdgeCatch = Color(0x4D38BDF8)     // 30% aqua perimeter light catch
    val LightWaterWavePrimary = Color(0x4038BDF8)   // 25% vibrant pale-aqua wave crest
    val LightWaterWaveSecondary = Color(0x280284C7) // 16% soft marine blue fluid body
    val LightWaterCaustic = Color(0x66FFFFFF)       // 40% crystalline sun highlight

    // --- Dark Theme Glass Palettes ---
    // Charcoal-Silver Translucent Crystal Base
    val DarkRefractiveTop = Color(0xCC20242A)      // 80% opacity dark smoke silver
    val DarkRefractiveMid = Color(0xB3181C22)      // 70% opacity charcoal marine crystal
    val DarkRefractiveBottom = Color(0xBF13161A)   // 75% opacity deep obsidian

    val DarkElevatedTop = Color(0xEB23252A)        // 92% opacity dark crystal
    val DarkElevatedBottom = Color(0xE017191C)     // 88% opacity deep slate

    val DarkStandardTop = Color(0xD9222529)        // 85% opacity smoked crystal
    val DarkStandardBottom = Color(0xCC181A1D)     // 80% opacity deep obsidian

    val DarkSubtleTop = Color(0xCC26292E)          // 80% opacity dark quartz
    val DarkSubtleBottom = Color(0xBF1A1C1F)       // 75% opacity charcoal

    // Dark Theme Highlights & Borders
    val DarkOuterRimTop = Color(0x80FFFFFF)        // 50% white specular rim
    val DarkOuterRimMid = Color(0x338A95A5)        // 20% silver edge
    val DarkOuterRimBottom = Color(0x1A000000)     // 10% dark grounding edge

    val DarkInnerMeniscusTop = Color(0x33FFFFFF)   // 20% secondary reflection
    val DarkInnerMeniscusMid = Color(0x10FFFFFF)   // 6% side reflection
    val DarkTopGleam = Color(0x40FFFFFF)           // 25% white gleam

    // Cool Blue-Silver Water Refraction & Caustics (Dark Theme)
    val DarkAquaSheen = Color(0x3338BDF8)          // 20% cool blue-silver aqua gleam
    val DarkAquaEdgeCatch = Color(0x4D0284C7)      // 30% cool aqua light catch
    val DarkWaterWavePrimary = Color(0x4038BDF8)    // 25% luminous aqua wave crest
    val DarkWaterWaveSecondary = Color(0x300369A1)  // 19% deep azure reflection
    val DarkWaterCaustic = Color(0x4DBAE6FD)        // 30% cool aqua light focus

    /**
     * Resolves the appropriate [GlassStyle] for a given [GlassVariant] and theme mode.
     */
    @Composable
    fun styleFor(
        variant: GlassVariant,
        isDark: Boolean = isSystemInDarkTheme(),
        timeSeconds: Float = 0f
    ): GlassStyle {
        return remember(variant, isDark, timeSeconds) {
            getStyle(variant, isDark, timeSeconds)
        }
    }

    /**
     * Non-composable style resolver for tests and factories.
     */
    fun getStyle(variant: GlassVariant, isDark: Boolean, timeSeconds: Float = 0f): GlassStyle {
        return when (variant) {
            GlassVariant.REFRACTIVE -> {
                val fallbackBrush = if (isDark) {
                    Brush.verticalGradient(
                        listOf(
                            DarkRefractiveTop,
                            Color(0xB3181C22), // charcoal marine crystal
                            DarkRefractiveBottom
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            LightRefractiveTop,
                            Color(0xB3D5DFEA), // silver with pale-aqua sheen
                            LightRefractiveBottom
                        )
                    )
                }
                val backgroundBrush = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    LiquidGlassRefractiveShaderBrush(isDark, timeSeconds)
                } else {
                    fallbackBrush
                }
                val outerRimBrush = if (isDark) {
                    Brush.verticalGradient(listOf(DarkOuterRimTop, DarkOuterRimMid, DarkOuterRimBottom))
                } else {
                    Brush.verticalGradient(listOf(LightOuterRimTop, LightOuterRimMid, LightOuterRimBottom))
                }
                val innerMeniscusBrush = Brush.verticalGradient(
                    listOf(
                        if (isDark) DarkInnerMeniscusTop else LightInnerMeniscusTop,
                        if (isDark) DarkInnerMeniscusMid else LightInnerMeniscusMid,
                        Color.Transparent
                    )
                )
                val topGleamBrush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        if (isDark) DarkTopGleam else LightTopGleam,
                        Color.Transparent
                    )
                )

                GlassStyle(
                    backgroundBrush = backgroundBrush,
                    outerBorderBrush = outerRimBrush,
                    innerMeniscusBrush = innerMeniscusBrush,
                    topGleamBrush = topGleamBrush,
                    shadowElevation = if (isDark) 16.dp else 14.dp,
                    shadowAmbientColor = if (isDark) Color(0x60000000) else Color(0x14000000),
                    shadowSpotColor = if (isDark) Color(0x90000000) else Color(0x24000000),
                    blurRadius = 28f,
                    defaultCornerRadius = 32.dp,
                    contentColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF171717),
                    secondaryContentColor = if (isDark) Color(0xFFA0A0A5) else Color(0xFF666666)
                )
            }

            GlassVariant.ELEVATED -> {
                val bgBrush = if (isDark) {
                    Brush.verticalGradient(listOf(DarkElevatedTop, DarkElevatedBottom))
                } else {
                    Brush.verticalGradient(listOf(LightElevatedTop, LightElevatedBottom))
                }
                val outerRimBrush = if (isDark) {
                    Brush.verticalGradient(listOf(Color(0x66FFFFFF), Color(0x268A95A5), Color(0x14000000)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xCCFFFFFF), Color(0x40CBD2DC), Color(0x20000000)))
                }
                val innerMeniscusBrush = Brush.verticalGradient(
                    listOf(
                        if (isDark) Color(0x29FFFFFF) else Color(0x4DFFFFFF),
                        if (isDark) Color(0x0EFFFFFF) else Color(0x1AFFFFFF),
                        Color.Transparent
                    )
                )
                val topGleamBrush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        if (isDark) Color(0x33FFFFFF) else Color(0x80FFFFFF),
                        Color.Transparent
                    )
                )

                GlassStyle(
                    backgroundBrush = bgBrush,
                    outerBorderBrush = outerRimBrush,
                    innerMeniscusBrush = innerMeniscusBrush,
                    topGleamBrush = topGleamBrush,
                    shadowElevation = if (isDark) 20.dp else 16.dp,
                    shadowAmbientColor = if (isDark) Color(0x80000000) else Color(0x1A000000),
                    shadowSpotColor = if (isDark) Color(0xB3000000) else Color(0x2E000000),
                    blurRadius = 32f,
                    defaultCornerRadius = 24.dp,
                    contentColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF171717),
                    secondaryContentColor = if (isDark) Color(0xFFB7B7BB) else Color(0xFF777777)
                )
            }

            GlassVariant.STANDARD -> {
                val bgBrush = if (isDark) {
                    Brush.verticalGradient(listOf(DarkStandardTop, DarkStandardBottom))
                } else {
                    Brush.verticalGradient(listOf(LightStandardTop, LightStandardBottom))
                }
                val outerRimBrush = if (isDark) {
                    Brush.verticalGradient(listOf(Color(0x4DFFFFFF), Color(0x208A95A5), Color(0x10000000)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xB3FFFFFF), Color(0x33CBD2DC), Color(0x18000000)))
                }
                val innerMeniscusBrush = Brush.verticalGradient(
                    listOf(
                        if (isDark) Color(0x20FFFFFF) else Color(0x33FFFFFF),
                        Color.Transparent
                    )
                )

                GlassStyle(
                    backgroundBrush = bgBrush,
                    outerBorderBrush = outerRimBrush,
                    innerMeniscusBrush = innerMeniscusBrush,
                    topGleamBrush = null,
                    shadowElevation = if (isDark) 10.dp else 8.dp,
                    shadowAmbientColor = if (isDark) Color(0x50000000) else Color(0x10000000),
                    shadowSpotColor = if (isDark) Color(0x70000000) else Color(0x1A000000),
                    blurRadius = 24f,
                    defaultCornerRadius = 18.dp,
                    contentColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF171717),
                    secondaryContentColor = if (isDark) Color(0xFFA0A0A5) else Color(0xFF777777)
                )
            }

            GlassVariant.SUBTLE -> {
                val bgBrush = if (isDark) {
                    Brush.verticalGradient(listOf(DarkSubtleTop, DarkSubtleBottom))
                } else {
                    Brush.verticalGradient(listOf(LightSubtleTop, LightSubtleBottom))
                }
                val outerRimBrush = if (isDark) {
                    Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x14FFFFFF), Color(0x0A000000)))
                } else {
                    Brush.verticalGradient(listOf(Color(0x80FFFFFF), Color(0x24CBD2DC), Color(0x10000000)))
                }
                val innerMeniscusBrush = Brush.verticalGradient(
                    listOf(
                        if (isDark) Color(0x14FFFFFF) else Color(0x20FFFFFF),
                        Color.Transparent
                    )
                )

                GlassStyle(
                    backgroundBrush = bgBrush,
                    outerBorderBrush = outerRimBrush,
                    innerMeniscusBrush = innerMeniscusBrush,
                    topGleamBrush = null,
                    shadowElevation = if (isDark) 4.dp else 3.dp,
                    shadowAmbientColor = if (isDark) Color(0x30000000) else Color(0x0C000000),
                    shadowSpotColor = if (isDark) Color(0x50000000) else Color(0x14000000),
                    blurRadius = 18f,
                    defaultCornerRadius = 16.dp,
                    contentColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF171717),
                    secondaryContentColor = if (isDark) Color(0xFFA0A0A5) else Color(0xFF777777)
                )
            }
        }
    }
}

/**
 * AGSL Runtime Shader for physically modeled Liquid Water Refractive Glass on Android 13+ (API 33+).
 * Computes 3D normal curvature, Snell/Fresnel refraction, organic flowing water caustics, and specular meniscus.
 */
private const val AGSL_REFRACTIVE_GLASS = """
    uniform float2 uResolution;
    uniform float uCornerRadius;
    uniform float4 uTopColor;
    uniform float4 uBottomColor;
    uniform float4 uRimHighlight;
    uniform float4 uInnerMeniscus;
    uniform float4 uAquaReflect;
    uniform float uIsDark;
    uniform float uTime;

    float sdRoundedBox(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + float2(r, r);
        return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    }

    half4 main(float2 fragCoord) {
        float2 halfRes = uResolution * 0.5;
        float2 p = fragCoord - halfRes;
        float r = uCornerRadius;
        float2 b = halfRes;

        float d = sdRoundedBox(p, b, r);
        if (d > 0.5) {
            return half4(0.0);
        }

        float distInside = max(0.0, -d);
        float vCoord = clamp(fragCoord.y / max(uResolution.y, 1.0), 0.0, 1.0);

        // Base translucent silver-grey material gradient
        half4 baseGlass = mix(uTopColor, uBottomColor, half(vCoord));

        // 2D normal pointing outwards
        float2 q = abs(p) - (b - float2(r, r));
        float2 n2D = float2(0.0, sign(p.y));
        if (q.x > 0.0 && q.y > 0.0) {
            n2D = normalize(p - sign(p) * (b - float2(r, r)));
        } else if (q.x > 0.0) {
            n2D = float2(sign(p.x), 0.0);
        }

        // Lens curvature depth profile (3D meniscus at rim)
        float rimWidth = max(r * 0.35, 12.0);
        float lensT = clamp(distInside / rimWidth, 0.0, 1.0);
        float nz = sqrt(max(0.0, 1.0 - (1.0 - lensT) * (1.0 - lensT)));

        // Directional overhead ambient light
        float3 lightDir = normalize(float3(-0.3, -0.8, 0.5));
        float3 normal3D = normalize(float3(n2D.x * (1.0 - lensT), n2D.y * (1.0 - lensT), nz));

        // Outer specular rim
        float outerDist = smoothstep(2.0, 0.0, distInside);
        float topGleam = smoothstep(0.4, -0.6, n2D.y);
        float specularOuter = outerDist * (0.3 + 0.7 * topGleam);

        // Secondary inner meniscus reflection (physical glass thickness line at ~2.0px inset)
        float innerDist = smoothstep(0.0, 1.2, distInside - 1.8) * smoothstep(3.2, 1.8, distInside - 1.8);
        float specularInner = innerDist * (0.25 + 0.45 * topGleam);

        // Fresnel reflection at glancing perimeter angles
        float fresnel = pow(1.0 - nz, 3.0) * (uIsDark > 0.5 ? 0.20 : 0.35);

        // Organic flowing liquid water-wave caustics & refractive displacement
        float2 wc = fragCoord * 0.018;
        float w1 = sin(wc.x * 1.8 + wc.y * 1.2 + uTime * 0.65);
        float w2 = cos(wc.x * 1.4 - wc.y * 2.1 - uTime * 0.55);
        float w3 = sin((wc.x + wc.y) * 1.5 + uTime * 0.40);
        float waveSum = (w1 + w2 + w3) * 0.3333;

        // Distinctive water caustics with fine crystalline crests
        float causticCrest = pow(clamp(0.5 + 0.5 * waveSum, 0.0, 1.0), 3.5);
        float causticRipple = smoothstep(0.40, 0.85, 0.5 + 0.5 * sin(wc.x * 3.5 - wc.y * 2.5 + uTime * 0.85 + waveSum * 3.0));
        float fluidCaustic = clamp(causticCrest * 0.75 + causticRipple * 0.45, 0.0, 1.0);
        float causticMask = smoothstep(0.0, 3.5, distInside);

        half4 result = baseGlass;
        result.rgb += half3(specularOuter * uRimHighlight.rgb * uRimHighlight.a);
        result.rgb += half3(specularInner * uInnerMeniscus.rgb * uInnerMeniscus.a);
        result.rgb += half3(fresnel);

        // Visibly present pale-aqua water reflections and sunlight caustic highlights
        float waterOpacity = (fluidCaustic * 0.45 + (1.0 - lensT) * 0.25) * causticMask;
        result.rgb = mix(result.rgb, uAquaReflect.rgb, half(waterOpacity * uAquaReflect.a * 1.8));

        // Add crystalline water reflection crest highlights
        half3 sunCausticLight = uIsDark > 0.5 ? half3(0.55, 0.85, 1.0) : half3(0.95, 0.98, 1.0);
        result.rgb += sunCausticLight * half(causticCrest * 0.35 * causticMask);

        result.a = clamp(result.a + half(specularOuter * 0.25 + specularInner * 0.15 + causticCrest * 0.08), 0.0, 1.0);

        // Edge antialiasing
        float edgeAlpha = smoothstep(0.5, -0.5, d);
        return result * half(edgeAlpha);
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class LiquidGlassRefractiveShaderBrush(
    private val isDark: Boolean,
    private val timeSeconds: Float = 0f
) : ShaderBrush() {
    private var runtimeShader: RuntimeShader? = null
    private var cachedWidth: Float = 0f
    private var cachedHeight: Float = 0f

    override fun createShader(size: Size): android.graphics.Shader {
        val shader = runtimeShader ?: RuntimeShader(AGSL_REFRACTIVE_GLASS).also {
            runtimeShader = it
        }
        if (size.width > 0f && size.height > 0f) {
            cachedWidth = size.width
            cachedHeight = size.height
            val r = size.height * 0.5f
            shader.setFloatUniform("uResolution", size.width, size.height)
            shader.setFloatUniform("uCornerRadius", r)
            shader.setFloatUniform("uIsDark", if (isDark) 1.0f else 0.0f)
            shader.setFloatUniform("uTime", timeSeconds)
            if (isDark) {
                // Charcoal-silver translucent glass with cool blue-silver reflections
                shader.setFloatUniform("uTopColor", 0.125f, 0.141f, 0.165f, 0.80f)
                shader.setFloatUniform("uBottomColor", 0.075f, 0.086f, 0.102f, 0.75f)
                shader.setFloatUniform("uRimHighlight", 1.0f, 1.0f, 1.0f, 0.55f)
                shader.setFloatUniform("uInnerMeniscus", 1.0f, 1.0f, 1.0f, 0.28f)
                shader.setFloatUniform("uAquaReflect", 0.22f, 0.65f, 0.92f, 0.22f)
            } else {
                // Cool platinum silver-grey translucent glass with pale aqua reflections
                shader.setFloatUniform("uTopColor", 0.886f, 0.902f, 0.922f, 0.80f)
                shader.setFloatUniform("uBottomColor", 0.804f, 0.835f, 0.871f, 0.75f)
                shader.setFloatUniform("uRimHighlight", 1.0f, 1.0f, 1.0f, 0.90f)
                shader.setFloatUniform("uInnerMeniscus", 1.0f, 1.0f, 1.0f, 0.40f)
                shader.setFloatUniform("uAquaReflect", 0.38f, 0.72f, 0.94f, 0.20f)
            }
        }
        return shader
    }
}
