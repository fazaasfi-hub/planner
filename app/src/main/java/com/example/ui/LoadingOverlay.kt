package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LoadingOverlay(isLoading: Boolean, onLoadingFinished: () -> Unit, modifier: Modifier = Modifier) {
    var progress by remember { mutableFloatStateOf(0f) }
    var stage by remember { mutableStateOf(LoadingStage.ICON_APPEAR) }
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    // Animation orchestration
    LaunchedEffect(isLoading) {
        if (isLoading) {
            progress = 0f
            stage = LoadingStage.ICON_APPEAR
            
            delay(800) // Icon appears (scale up)
            stage = LoadingStage.ICON_SHRINK
            
            delay(800) // Icon shrinks
            stage = LoadingStage.NAME_APPEAR
            
            delay(1200) // Name stays
            stage = LoadingStage.LOGO_EXIT
            
            delay(600) // Fade out logo/name
            stage = LoadingStage.PROGRESS_SHOW
            
            // Progress simulation starts after logo exit
            while (progress < 1f) {
                delay(80)
                progress += Random.nextFloat() * 0.04f + 0.01f
            }
            progress = 1f
            delay(500)
            onLoadingFinished()
        }
    }

    val iconAlpha by animateFloatAsState(
        targetValue = if (stage != LoadingStage.LOGO_EXIT && stage != LoadingStage.PROGRESS_SHOW) 1f else 0f,
        animationSpec = tween(600), label = "iconAlpha"
    )
    
    val iconScale by animateFloatAsState(
        targetValue = when (stage) {
            LoadingStage.ICON_APPEAR -> 1.6f
            LoadingStage.ICON_SHRINK, LoadingStage.NAME_APPEAR -> 1.0f
            else -> 0.5f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "iconScale"
    )

    val nameAlpha by animateFloatAsState(
        targetValue = if (stage == LoadingStage.NAME_APPEAR) 1f else 0f,
        animationSpec = tween(600), label = "nameAlpha"
    )

    val progressAlpha by animateFloatAsState(
        targetValue = if (stage == LoadingStage.PROGRESS_SHOW) 1f else 0f,
        animationSpec = tween(800), label = "progressAlpha"
    )

    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .zIndex(9999f),
            contentAlignment = Alignment.Center
        ) {
            // 1. Background Particles
            LoaderParticles(infiniteTransition)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                    // Stage 1-3: Logo and Name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            alpha = iconAlpha
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(120.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_loading_logo),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Planner Pro",
                            style = TextStyle(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF818CF8), Color(0xFF4ADE80))
                                )
                            ),
                            modifier = Modifier.graphicsLayer { alpha = nameAlpha }
                        )
                    }

                    // Stage 5: Progress Show
                    if (stage == LoadingStage.PROGRESS_SHOW) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer { alpha = progressAlpha }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                                // Progress Ring
                                Canvas(modifier = Modifier.fillMaxSize().rotate(-90f)) {
                                    drawCircle(
                                        color = Color(0xFF1E293B),
                                        style = Stroke(width = 5.dp.toPx())
                                    )
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(Color(0xFF818CF8), Color(0xFF4ADE80), Color(0xFF818CF8))
                                        ),
                                        startAngle = 0f,
                                        sweepAngle = progress * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    color = Color(0xFF818CF8),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(40.dp))
                            
                            // Track
                            Box(
                                modifier = Modifier
                                    .width(260.dp)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                            ) {
                                // Fill
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF818CF8), Color(0xFF4ADE80))
                                            )
                                        )
                                )
                            }
                            
                            Text(
                                text = "Mempersiapkan produktivitas Anda...",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoaderParticles(infiniteTransition: InfiniteTransition) {
    val particleCount = 25
    val particles = remember {
        List(particleCount) {
            ParticleData(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                duration = Random.nextInt(3000, 7000)
            )
        }
    }

    val animValues = particles.mapIndexed { index, p ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(p.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "p$index"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEachIndexed { index, p ->
            val v = animValues[index].value
            val alpha = if (v < 0.5f) v * 2f else (1f - v) * 2f
            val yOffset = (v - 0.5f) * 150f

            drawCircle(
                color = Color(0xFF818CF8),
                radius = p.size.dp.toPx(),
                center = Offset(
                    x = p.x * size.width,
                    y = p.y * size.height + yOffset
                ),
                alpha = alpha * 0.25f
            )
        }
    }
}

data class ParticleData(val x: Float, val y: Float, val size: Float, val duration: Int)

enum class LoadingStage {
    ICON_APPEAR,
    ICON_SHRINK,
    NAME_APPEAR,
    LOGO_EXIT,
    PROGRESS_SHOW
}
