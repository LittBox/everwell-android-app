package com.example.everwell.ui.organ

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.everwell.ui.home.AgentAvatar
import com.example.everwell.ui.home.AgentAvatarAction

internal data class HeartTourStep(
    val title: String,
    val narration: String,
    val structureId: String? = null,
    val avatarAction: AgentAvatarAction = AgentAvatarAction.IDLE
)

internal object HeartGuidedTour {
    val steps = listOf(
        HeartTourStep(
            title = "先认识整颗心脏",
            narration = "你好，我是小伊！心脏就像身体里的动力泵。接下来，我们沿着血液流动的路线，一起认识它的关键结构吧。",
            avatarAction = AgentAvatarAction.WAVE
        ),
        HeartTourStep(
            title = "第一站：右心房壁",
            narration = "右心房接收从全身回流、含氧量较低的血液，再把血液送入右心室。",
            structureId = "right-atrium"
        ),
        HeartTourStep(
            title = "第二站：右心室腔",
            narration = "这里显示的是右心室内的腔室空间。包围它的心肌壁收缩时，会把血液泵入肺动脉干，送往肺部进行气体交换。",
            structureId = "right-ventricle"
        ),
        HeartTourStep(
            title = "第三站：肺动脉干",
            narration = "肺动脉干把血液分流到左右肺动脉。注意，肺动脉运输的是含氧量较低的血液。",
            structureId = "pulmonary-trunk"
        ),
        HeartTourStep(
            title = "第四站：肺部交换氧气",
            narration = "在肺部，血液释放二氧化碳并获得氧气。随后，富氧血经肺静脉回到左心房。",
            avatarAction = AgentAvatarAction.YES
        ),
        HeartTourStep(
            title = "第五站：左心房壁",
            narration = "左心房接收来自肺部的富氧血，再通过二尖瓣把血液送入左心室。",
            structureId = "left-atrium"
        ),
        HeartTourStep(
            title = "第六站：左心室腔",
            narration = "这里显示的是左心室内的腔室空间。较厚的心室肌壁收缩时，会把富氧血经主动脉瓣泵入主动脉，供应全身。",
            structureId = "left-ventricle"
        ),
        HeartTourStep(
            title = "最后一站：升主动脉",
            narration = "血液从左心室进入主动脉，再分配到全身。这样，我们就走完了一次心脏泵血路线！",
            structureId = "ascending-aorta",
            avatarAction = AgentAvatarAction.THUMBS_UP
        )
    )
}

@Composable
internal fun HeartGuidedTourCard(
    step: HeartTourStep,
    index: Int,
    total: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.97f),
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AgentAvatar(
                    modifier = Modifier.size(58.dp),
                    action = step.avatarAction,
                    actionToken = index + 1,
                    active = true
                )
                Column(
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("小伊 · 心脏导览", color = Color(0xFF6750A4), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(step.title, color = Color(0xFF292333), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text("${index + 1}/$total", color = Color(0xFF786D88), fontSize = 12.sp)
            }
            Text(step.narration, color = Color(0xFF494253), fontSize = 13.sp, lineHeight = 19.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("结束导览", fontSize = 12.sp) }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) { Text(if (index == total - 1) "完成探索 ✓" else "点击继续 →", fontSize = 12.sp) }
            }
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF3EFF9), RoundedCornerShape(20.dp)).padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("也可以直接点击心脏画面，进入下一步", color = Color(0xFF75688A), fontSize = 10.sp)
            }
        }
    }
}
