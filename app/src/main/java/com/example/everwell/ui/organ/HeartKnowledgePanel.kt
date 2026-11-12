package com.example.everwell.ui.organ

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object HeartPalette {
    val Primary = Color(0xFF6750A4)
    val PrimarySoft = Color(0xFFEDE6F7)
    val Background = Color(0xFFF7F7F9)
    val Ink = Color(0xFF272135)
    val Secondary = Color(0xFF716A80)
    val Border = Color(0xFFE9E3F0)
}

@Composable
internal fun HeartKnowledgePanel(
    structure: HeartStructure?,
    onStart: () -> Unit,
    onClose: () -> Unit,
    onOpenSource: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        AnimatedContent(
            targetState = structure,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "Heart knowledge card"
        ) { selected ->
            if (selected == null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(22.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("从一个结构开始", color = HeartPalette.Ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "轻点心脏中的部位，或从结构目录选择。\n每一次选择，都有对应的知识与来源。",
                        color = HeartPalette.Secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 23.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    FilledTonalButton(
                        onClick = onStart,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = HeartPalette.PrimarySoft,
                            contentColor = HeartPalette.Primary
                        )
                    ) { Text("从升主动脉开始") }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }) {
                            Text(selected.category + " · 结构知识", color = HeartPalette.Primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(
                                selected.name, modifier = Modifier.padding(top = 3.dp),
                                color = HeartPalette.Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold,
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                            Text(selected.englishName, color = HeartPalette.Secondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        TextButton(onClick = onClose) { Text("收起", color = HeartPalette.Secondary) }
                    }
                    Column(
                        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(selected.summary, color = HeartPalette.Ink, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                        if (selected.flow.isNotEmpty()) BloodFlowRelationship(selected.flow)
                        selected.modelNote?.let {
                            Text(it, color = HeartPalette.Secondary, fontSize = 11.sp, lineHeight = 17.sp)
                        }
                    }
                    HorizontalDivider(color = HeartPalette.Border)
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .clickable(onClickLabel = "打开知识来源") { onOpenSource(selected.source.url) }
                            .padding(top = 10.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(30.dp).background(HeartPalette.PrimarySoft, CircleShape), contentAlignment = Alignment.Center) {
                            Text("源", color = HeartPalette.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(selected.source.publisher, color = HeartPalette.Primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(selected.source.title, color = HeartPalette.Secondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("查看原文 ↗", color = HeartPalette.Primary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/** A relationship diagram, not a physiological simulation. */
@Composable
private fun BloodFlowRelationship(steps: List<String>) {
    Column {
        Text("血流关系", color = HeartPalette.Secondary, fontSize = 10.sp)
        Row(Modifier.fillMaxWidth().padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            steps.forEachIndexed { index, name ->
                if (index > 0) Text("→", modifier = Modifier.padding(horizontal = 5.dp), color = HeartPalette.Primary, fontSize = 13.sp)
                Box(
                    modifier = Modifier.weight(1f)
                        .background(if (index == 1) HeartPalette.PrimarySoft else HeartPalette.Background, RoundedCornerShape(9.dp))
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name, color = if (index == 1) HeartPalette.Primary else HeartPalette.Secondary,
                        fontSize = 11.sp, fontWeight = if (index == 1) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
