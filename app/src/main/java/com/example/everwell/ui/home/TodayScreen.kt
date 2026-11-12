package com.example.everwell.ui.home

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val AgentPurple = Color(0xFF6750A4)
internal val AgentInk = Color(0xFF292238)
internal val AgentMuted = Color(0xFF81788D)
internal val AgentBackground = Color(0xFFF8F6FC)

/** Home UI only. The conversation is explicitly a local preview, without an AI service. */
@Composable
fun TodayScreen(onExploreBody: () -> Unit) {
    var avatarAction by remember { mutableStateOf(AgentAvatarAction.WAVE) }
    var actionToken by remember { mutableIntStateOf(0) }
    var selectedFeeling by rememberSaveable { mutableStateOf("") }
    var greeting by rememberSaveable { mutableStateOf("很高兴见到你") }
    var chatOpen by rememberSaveable { mutableStateOf(false) }
    var chatSeed by rememberSaveable { mutableStateOf("") }
    var chatRequestId by rememberSaveable { mutableIntStateOf(0) }
    var creditsOpen by rememberSaveable { mutableStateOf(false) }
    val chatStateHolder = rememberSaveableStateHolder()
    val homeListState = rememberLazyListState()
    val heroVisible by remember {
        derivedStateOf { homeListState.layoutInfo.visibleItemsInfo.any { it.key == "hero" } }
    }

    fun wave() {
        avatarAction = AgentAvatarAction.WAVE
        actionToken += 1
        greeting = "嗨，今天也一起探索吧"
    }
    fun openChat(seed: String = "") {
        chatSeed = seed
        chatRequestId += 1
        chatOpen = true
    }

    Box(
        Modifier.fillMaxSize().background(AgentBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(Modifier.widthIn(max = 680.dp).fillMaxSize()) {
            LazyColumn(
                state = homeListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item(key = "header") { HomeHeader(onInfo = { creditsOpen = true }) }
                item(key = "hero") {
                    AgentHero(
                        greeting = greeting,
                        action = avatarAction,
                        actionToken = actionToken,
                        active = heroVisible && !chatOpen && !creditsOpen,
                        onWave = ::wave
                    )
                }
                item(key = "feeling") {
                    FeelingPicker(selectedFeeling) { feeling ->
                        selectedFeeling = feeling
                        greeting = when (feeling) {
                            "精神不错" -> "收到！给今天点个赞"
                            "平平常常" -> "平常的一天，也值得照顾"
                            else -> "收到，我们慢慢来"
                        }
                        avatarAction = if (feeling == "精神不错") {
                            AgentAvatarAction.THUMBS_UP
                        } else {
                            AgentAvatarAction.YES
                        }
                        actionToken += 1
                    }
                }
                item(key = "actions") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeading("从一个小探索开始", "跟着好奇心走")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            HomeActionCard(
                                title = "身体探索",
                                subtitle = "从一颗心脏开始",
                                icon = Icons.Rounded.FavoriteBorder,
                                accent = Color(0xFFBC6684),
                                background = Color(0xFFF4E8EF),
                                modifier = Modifier.weight(1f),
                                onClick = onExploreBody
                            )
                            HomeActionCard(
                                title = "聊聊今天",
                                subtitle = "和小伊打个招呼",
                                icon = Icons.AutoMirrored.Rounded.Chat,
                                accent = AgentPurple,
                                background = Color(0xFFEEE8F8),
                                modifier = Modifier.weight(1f),
                                onClick = { openChat("你好，小伊") }
                            )
                        }
                    }
                }
                item(key = "topics") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeading("也可以这样问", "对话预览")
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AgentTopics.forEach { topic ->
                                Surface(
                                    onClick = { openChat(topic) },
                                    color = Color.White,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEAE5F1))
                                ) {
                                    Text(
                                        text = topic,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                                        color = AgentInk,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
            HomeConversationEntry(onClick = { openChat() })
        }
    }

    if (chatOpen) {
        chatStateHolder.SaveableStateProvider("agent-chat-preview") {
            AgentChatSheet(
                initialPrompt = chatSeed,
                requestId = chatRequestId,
                onDismiss = { chatOpen = false },
                onExploreBody = {
                    chatOpen = false
                    onExploreBody()
                }
            )
        }
    }
    if (creditsOpen) AgentCreditsDialog(onDismiss = { creditsOpen = false })
}

@Composable
private fun HomeHeader(onInfo: () -> Unit) {
    val date = remember { SimpleDateFormat("M月d日  EEEE", Locale.SIMPLIFIED_CHINESE).format(Date()) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(13.dp), color = AgentPurple) {
            Icon(
                Icons.Rounded.AutoAwesome, contentDescription = null,
                tint = Color.White, modifier = Modifier.padding(9.dp).size(20.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("EVERWELL", color = AgentInk, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(date, color = AgentMuted, fontSize = 11.sp)
        }
        IconButton(onClick = onInfo, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Rounded.Info, contentDescription = "关于小伊与模型来源", tint = AgentMuted, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun AgentHero(
    greeting: String,
    action: AgentAvatarAction,
    actionToken: Int,
    active: Boolean,
    onWave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("今天，也照顾一下自己。", color = AgentInk, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 33.sp)
            Text("让小伊陪你，把身体了解得多一点。", color = AgentMuted, fontSize = 13.sp, lineHeight = 20.sp)
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val useStackedHero = maxWidth < 300.dp || LocalDensity.current.fontScale > 1.25f
            if (useStackedHero) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFECE4FA), Color(0xFFFEFCFF))))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("你的健康伙伴", color = AgentPurple, fontSize = 12.sp)
                    Text("你好，我是小伊", color = AgentPurple, fontSize = 25.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
                    AgentAvatar(
                        modifier = Modifier.fillMaxWidth().height(230.dp),
                        action = action, actionToken = actionToken, active = active, onTap = onWave
                    )
                    Surface(onClick = onWave, shape = RoundedCornerShape(16.dp), color = Color.White) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.WavingHand, null, tint = AgentPurple, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("打个招呼", color = AgentPurple, fontSize = 14.sp)
                        }
                    }
                }
            } else Box(
                Modifier
                    .fillMaxWidth()
                    .height(256.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFECE4FA), Color(0xFFF1ECF8), Color(0xFFFEFCFF))))
            ) {
                Box(
                    Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).size(208.dp)
                        .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.92f), Color.White.copy(alpha = 0f))), CircleShape)
                )
                Box(
                    Modifier.align(Alignment.BottomEnd).padding(end = 66.dp, bottom = 26.dp)
                        .size(width = 120.dp, height = 20.dp)
                        .background(Brush.radialGradient(listOf(AgentPurple.copy(alpha = 0.14f), Color.Transparent), radius = 95f), CircleShape)
                )
                AgentAvatar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.8f).height(254.dp),
                    action = action,
                    actionToken = actionToken,
                    active = active,
                    onTap = onWave
                )
                Column(
                    Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(color = Color.White.copy(alpha = 0.88f), shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(5.dp).background(Color(0xFF79A68D), CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text("你的健康伙伴", color = AgentPurple, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Text("你好，\n我是小伊", color = AgentPurple, fontSize = 25.sp, fontWeight = FontWeight.Bold, lineHeight = 33.sp)
                }
                Surface(
                    onClick = onWave,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.WavingHand, contentDescription = null, tint = AgentPurple, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("打个招呼", color = AgentPurple, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        AnimatedContent(targetState = greeting, label = "agent-greeting") { message ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFFAA92CA), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(7.dp))
                Text(message, color = AgentMuted, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun FeelingPicker(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading("你现在感觉怎么样？", "跟我说说")
        Row(
            Modifier.fillMaxWidth().selectableGroup().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("精神不错", "平平常常", "想歇一会").forEach { feeling ->
                val isSelected = selected == feeling
                val color by animateColorAsState(if (isSelected) AgentPurple else Color.White, label = "feeling-color")
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                        .background(color)
                        .selectable(selected = isSelected, onClick = { onSelect(feeling) }, role = Role.RadioButton)
                        .padding(horizontal = 15.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(feeling, color = if (isSelected) Color.White else AgentMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = AgentInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(subtitle, color = AgentMuted, fontSize = 10.sp)
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(23.dp), color = Color.White) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).background(background, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color(0xFFB8AEC5), modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(2.dp))
            Text(title, color = AgentInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = AgentMuted, fontSize = 11.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun HomeConversationEntry(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().background(AgentBackground).padding(horizontal = 24.dp, vertical = 8.dp)) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(23.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5DDEE)),
            shadowElevation = 2.dp
        ) {
            Row(Modifier.fillMaxWidth().padding(start = 17.dp, end = 10.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = AgentPurple, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("有什么想聊的？", color = AgentInk, fontSize = 13.sp)
                    Text("和小伊聊聊 · 对话预览", color = AgentMuted, fontSize = 10.sp)
                }
                Box(Modifier.size(40.dp).background(AgentPurple, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}

@Composable
private fun AgentCreditsDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    fun open(url: String) {
        runCatching { uriHandler.openUri(url) }.onFailure {
            Toast.makeText(context, "暂时无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.AutoAwesome, null, tint = AgentPurple) },
        title = { Text("认识小伊") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("小伊是 Everwell 的健康探索伙伴。本版本提供角色互动、心脏探索入口和本地对话预览。对话尚未接入智能问答服务。")
                Text("角色模型：RobotExpressive\n原作者：Tomás Laulhé（Quaternius）\n表情与格式转换：Don McCurdy\n授权：CC0 1.0", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { open("https://quaternius.com/packs/animatedrobot.html") }) { Text("查看角色来源") }
                TextButton(onClick = { open("https://creativecommons.org/publicdomain/zero/1.0/") }) { Text("查看 CC0 许可") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了") } }
    )
}
