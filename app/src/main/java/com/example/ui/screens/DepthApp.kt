package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.DepthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepthApp(
    viewModel: DepthViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val selectedSessionId by viewModel.selectedSessionId.collectAsStateWithLifecycle()
    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()
    val messages by viewModel.currentSessionMessages.collectAsStateWithLifecycle()
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val isSummarizing by viewModel.isSummarizing.collectAsStateWithLifecycle()
    val currentSummary by viewModel.currentSummary.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = @Composable {
            ModalDrawerSheet(
                drawerContainerColor = DepthSurface,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.statusBarsPadding())
                
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = DepthPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Your Inquiries",
                                style = MaterialTheme.typography.titleLarge,
                                color = DepthTextPrimary
                            )
                        }
                        Text(
                            text = "A catalog of your self-reflections",
                            style = MaterialTheme.typography.labelSmall,
                            color = DepthTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = DepthSecondary.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                // New inquiry Button
                Button(
                    onClick = {
                        viewModel.createNewSession()
                        scope.launch { drawerState.close() }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DepthPrimary,
                        contentColor = DepthBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("new_inquiry_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(text = "Begin New Reflection", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Sessions List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (sessions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Your inner room is still.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DepthTextSecondary,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(sessions, key = { it.id }) { session ->
                            val isSelected = session.id == selectedSessionId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) DepthPrimary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.selectSession(session.id)
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                                    .testTag("session_item_${session.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = if (isSelected) DepthAccent else DepthTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = session.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                            color = if (isSelected) DepthTextPrimary else DepthTextSecondary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = android.text.format.DateUtils.getRelativeTimeSpanString(session.createdAt).toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DepthTextSecondary.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteSession(session.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete inquiry",
                                        tint = Color.Red.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = DepthBg,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        BreathingTitle(isThinking = isThinking)
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Inquiries history",
                                tint = DepthPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.createNewSession() },
                            modifier = Modifier.testTag("add_session_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Reflection",
                                tint = DepthPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DepthGlowColor.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                if (selectedSession == null) {
                    // Empty/First State: Guide to begin
                    FirstReflectionState(onCreate = { viewModel.createNewSession() })
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        // In-conversation list
                        val listState = rememberLazyListState()
                        
                        LaunchedEffect(messages.size, isThinking) {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                        ) {
                            if (messages.isEmpty()) {
                                item {
                                    QuietIntroCard()
                                }
                            } else {
                                items(messages) { message ->
                                    MessageBubble(message = message)
                                }
                            }

                            if (isThinking) {
                                item {
                                    ThinkingIndicator()
                                }
                            }

                            // If there is an existing summary saved, show a button to open it, 
                            // OR show summary generator if messages >= 6
                            val userMessageCount = messages.count { it.role == "user" }
                            val totalExchanges = messages.size
                            if (totalExchanges >= 6 || selectedSession?.summary != null) {
                                item {
                                    ClaritySummaryPrompt(
                                        hasSummary = selectedSession?.summary != null,
                                        isSummarizing = isSummarizing,
                                        onGenerate = { viewModel.generateClaritySummary() }
                                    )
                                }
                            }
                        }

                        // Message Input Field
                        MessageInputArea(
                            isThinking = isThinking,
                            onSend = { text ->
                                focusManager.clearFocus()
                                viewModel.sendMessage(text)
                            }
                        )
                    }
                }

                // Clarity Summary Modal Overlay
                AnimatedVisibility(
                    visible = currentSummary != null || (selectedSession?.summary != null && currentSummary != null),
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, easing = EaseOutCubic)
                    ),
                    exit = fadeOut(animationSpec = tween(450)) + slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(450, easing = EaseInCubic)
                    )
                ) {
                    val displaySummary = currentSummary ?: selectedSession?.summary ?: ""
                    ClaritySummaryView(
                        summaryText = displaySummary,
                        onClose = { viewModel.clearCurrentSummary() }
                    )
                }
            }
        }
    }
}

@Composable
fun BreathingTitle(isThinking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_orbs")
    
    // Slow, rhythmic duration: 4 seconds for a breath cycle. Speed up when thinking.
    val duration = if (isThinking) 1800 else 4500
    
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    val breathingBlur by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_blur"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Glowing Backdrop Circle
            Canvas(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer(
                        scaleX = breathingScale,
                        scaleY = breathingScale
                    )
                    .blur(breathingBlur.dp)
            ) {
                drawCircle(
                    color = DepthPrimary.copy(alpha = if (isThinking) 0.35f else 0.18f),
                    radius = size.minDimension / 1.5f
                )
            }

            // Foreground Text
            Text(
                text = "Depth",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                ),
                color = DepthPrimary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun FirstReflectionState(onCreate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                tint = DepthPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "Welcome to the Stillness",
                style = MaterialTheme.typography.titleLarge,
                color = DepthTextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "This is a quiet, non-judgmental space to wrestle with your feelings, decisions, or questions. Depth will guide you to find your own answers.",
                style = MaterialTheme.typography.bodyMedium,
                color = DepthTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Button(
                onClick = onCreate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DepthPrimary,
                    contentColor = DepthBg
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("start_inquiry_button")
            ) {
                Text(
                    text = "Begin an Inquiry",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun QuietIntroCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x0AFFFFFF)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(DepthPrimary)
                )
                Text(
                    text = "ENTERING SELF-INQUIRY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = DepthPrimary
                )
            }
            Text(
                text = "Write down whatever is weighing on your mind—a decision, feeling, or life question.\n\nDepth will listen, mirror your thoughts, and pose quiet, powerful questions to help you explore your own depths.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 26.sp,
                    color = DepthTextSecondary
                )
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (isUser) {
            // Elegant user thought styling as requested by Frosted Glass theme
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .testTag("message_bubble_${message.id}")
            ) {
                Text(
                    text = "YOUR CURRENT THOUGHT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 2.5.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = DepthSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "\"${message.content}\"",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        lineHeight = 26.sp,
                        color = DepthTextPrimary.copy(alpha = 0.9f)
                    ),
                    textAlign = TextAlign.End
                )
            }
        } else {
            // Elegant AI Inquiry with thin line decorator as requested in design specs
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .testTag("message_bubble_${message.id}")
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .width(32.dp)
                        .height(1.dp)
                        .background(DepthPrimary.copy(alpha = 0.3f))
                )
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Light,
                        lineHeight = 32.sp,
                        color = DepthPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .testTag("thinking_indicator")
    ) {
        Text(
            text = "DEPTH IS LISTENING",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Medium
            ),
            color = DepthTextSecondary.copy(alpha = 0.5f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(DepthPrimary.copy(alpha = dotAlpha1))
            )
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(DepthPrimary.copy(alpha = dotAlpha2))
            )
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(DepthPrimary.copy(alpha = dotAlpha3))
            )
        }
    }
}

@Composable
fun ClaritySummaryPrompt(
    hasSummary: Boolean,
    isSummarizing: Boolean,
    onGenerate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp)),
        onClick = { if (!isSummarizing) onGenerate() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = DepthPrimary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "Ready for a Clarity Synthesis?",
                style = MaterialTheme.typography.titleMedium,
                color = DepthTextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "You have explored several thoughts. We can distill this dialogue into a reflection of your core feelings, discoveries, and emerging clarity.",
                style = MaterialTheme.typography.bodyMedium,
                color = DepthTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            if (isSummarizing) {
                CircularProgressIndicator(
                    color = DepthPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (hasSummary) "VIEW CLARITY SUMMARY" else "GENERATE CLARITY SUMMARY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = DepthPrimary
                )
            }
        }
    }
}

@Composable
fun MessageInputArea(
    isThinking: Boolean,
    onSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0x0DFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(32.dp))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = {
                            Text(
                                text = "Share what's on your mind...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DepthTextSecondary.copy(alpha = 0.4f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input_field"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = DepthTextPrimary,
                            unfocusedTextColor = DepthTextPrimary,
                            cursorColor = DepthPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (text.isNotBlank() && !isThinking) {
                                    onSend(text)
                                    text = ""
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        singleLine = false,
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (text.isNotBlank() && !isThinking) {
                                onSend(text)
                                text = ""
                                keyboardController?.hide()
                            }
                        },
                        enabled = text.isNotBlank() && !isThinking,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (text.isNotBlank() && !isThinking) DepthPrimary
                                else DepthPrimary.copy(alpha = 0.1f)
                            )
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (text.isNotBlank() && !isThinking) DepthBg else DepthTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "A quiet space for your internal dialogue.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Light
                ),
                color = DepthTextSecondary.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ClaritySummaryView(
    summaryText: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DepthBg.copy(alpha = 0.93f))
            .clickable(enabled = false) {} // block click throughs
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Close header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DepthPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "YOUR CLARITY SYNTHESIS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = DepthPrimary
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x0DFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                        .testTag("close_summary_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close summary",
                        tint = DepthPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Summary Text Display in elegant frosted glass container
            Surface(
                color = Color(0x0DFFFFFF),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp)
                ) {
                    // Scrollable summary body
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            Text(
                                text = "A Mirror for Your Thoughts",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Light
                                ),
                                color = DepthTextPrimary
                            )
                        }
                        item {
                            HorizontalDivider(color = Color(0x1AFFFFFF))
                        }
                        item {
                            Text(
                                text = summaryText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight.Light,
                                    color = DepthTextPrimary.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DepthPrimary,
                            contentColor = DepthBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "RETURN TO QUIET REFLECTION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
