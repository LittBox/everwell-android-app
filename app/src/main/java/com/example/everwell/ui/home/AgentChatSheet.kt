package com.example.everwell.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val AgentTopics = listOf("带我认识心脏", "今天想做点什么", "我想聊聊今天")

/** Deliberately local and deterministic; this is a UI preview, not an LLM response. */
internal data class AgentDemoMessage(val fromUser: Boolean, val text: String, val opensHeart: Boolean = false)

private val MessageListSaver = listSaver<List<AgentDemoMessage>, String>(
    save = { messages -> messages.flatMap { listOf(it.fromUser.toString(), it.text, it.opensHeart.toString()) } },
    restore = { values -> values.chunked(3).filter { it.size == 3 }.map { AgentDemoMessage(it[0].toBoolean(), it[1], it[2].toBoolean()) } }
)

private fun demoReply(prompt: String): AgentDemoMessage = when {
    prompt.contains("心脏") || prompt.contains("身体") -> AgentDemoMessage(
        false, "一起从心脏开始吧。你可以旋转模型，点选一个结构，查看它的说明和资料来源。", true
    )
    prompt.contains("你好") || prompt.contains("小伊") -> AgentDemoMessage(
        false, "你好，我是小伊。很高兴见到你！可以先跟我说说今天，也可以一起去探索心脏。", true
    )
    else -> AgentDemoMessage(
        false, "收到。这里先展示我们聊天的样子，回复是预设示例。你可以继续试着输入，或从已经开放的心脏探索开始。", true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgentChatSheet(initialPrompt: String, requestId: Int, onDismiss: () -> Unit, onExploreBody: () -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    var lastRequestId by rememberSaveable { mutableStateOf(-1) }
    var messages by rememberSaveable(stateSaver = MessageListSaver) {
        mutableStateOf(listOf(AgentDemoMessage(false, "嗨，我是小伊。今天想从哪里开始？")))
    }
    val listState = rememberLazyListState()

    LaunchedEffect(requestId) {
        if (requestId != lastRequestId) {
            if (initialPrompt.isNotBlank()) {
                messages = (messages + AgentDemoMessage(true, initialPrompt) + demoReply(initialPrompt)).takeLast(24)
            }
            lastRequestId = requestId
        }
    }

    fun send() {
        val text = draft.trim()
        if (text.isEmpty()) return
        // Bounded preview history avoids growing saved-instance state without limit.
        messages = (messages + AgentDemoMessage(true, text) + demoReply(text)).takeLast(24)
        draft = ""
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AgentBackground,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().fillMaxHeight(0.94f).imePadding()) {
            val compact = maxHeight < 420.dp
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 14.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(AgentPurple, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("小伊", color = AgentInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("本地对话预览 · 预设示例回复", color = AgentMuted, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "关闭对话", tint = AgentMuted) }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "conversation-intro") {
                        Text("认识身体，从一个问题开始。", color = AgentMuted, fontSize = 12.sp)
                    }
                    itemsIndexed(messages) { _, message ->
                        MessageBubble(message, onExploreBody)
                    }
                    if (messages.size <= 2) {
                        item(key = "conversation-topics") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AgentTopics.take(2).forEach { topic ->
                                    Surface(
                                        onClick = {
                                            messages = (messages + AgentDemoMessage(true, topic) + demoReply(topic)).takeLast(24)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFEEE8F7)
                                    ) {
                                        Text(topic, color = AgentPurple, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(500) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("和小伊说点什么…", fontSize = 13.sp) },
                        shape = RoundedCornerShape(20.dp),
                        maxLines = if (compact) 2 else 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgentPurple,
                            unfocusedBorderColor = Color(0xFFE4DBEF),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { send() })
                    )
                    Surface(
                        onClick = ::send,
                        enabled = draft.isNotBlank(),
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = if (draft.isNotBlank()) AgentPurple else Color(0xFFE8E1F0)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.ArrowUpward, "发送消息", tint = if (draft.isNotBlank()) Color.White else Color(0xFFB6A8C9))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: AgentDemoMessage, onExploreBody: () -> Unit) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.fromUser) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!message.fromUser) Text("小伊 · 示例回复", color = AgentMuted, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp))
        Surface(
            modifier = Modifier.widthIn(max = 320.dp).semantics { if (!message.fromUser) liveRegion = LiveRegionMode.Polite },
            color = if (message.fromUser) AgentPurple else Color.White,
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (message.fromUser) 20.dp else 5.dp,
                bottomEnd = if (message.fromUser) 5.dp else 20.dp
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(message.text, color = if (message.fromUser) Color.White else AgentInk, fontSize = 14.sp, lineHeight = 23.sp)
                if (message.opensHeart) {
                    Button(onClick = onExploreBody, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
                        Text("打开 3D 心脏", fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}
