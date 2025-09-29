package com.google.ai.edge.gallery.customtasks.interactivelearning

import com.google.ai.edge.gallery.ui.llmchat.LlmAskAudioScreen
import com.google.ai.edge.gallery.ui.llmchat.LlmAskImageScreen
import com.google.ai.edge.gallery.ui.llmchat.LlmChatScreen
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel

enum class TalkToMeTab { Chat, Image, Audio }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalkToMeDialog(
    modelManagerViewModel: ModelManagerViewModel,
    onDismissRequest: () -> Unit,
    startTab: TalkToMeTab = TalkToMeTab.Chat,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(startTab) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(8.dp)
        ) {
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("TalkToMe") },
                        actions = {
                            IconButton(onClick = onDismissRequest) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    
                    val tabs = listOf(TalkToMeTab.Chat, TalkToMeTab.Image, TalkToMeTab.Audio)
                    TabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
                        tabs.forEach { tab ->
                            Tab(
                                selected = tab == selectedTab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        when (tab) {
                                            TalkToMeTab.Chat -> "Chat"
                                            TalkToMeTab.Image -> "Ask Image"
                                            TalkToMeTab.Audio -> "Ask Audio"
                                        }
                                    )
                                }
                            )
                        }
                    }

                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(targetState = selectedTab, label = "talkToMeTab") { tab ->
                            when (tab) {
                                TalkToMeTab.Chat -> {
                                    // Reuse existing Chat 
                                    LlmChatScreen(
                                        modelManagerViewModel = modelManagerViewModel,
                                        navigateUp = onDismissRequest,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                TalkToMeTab.Image -> {
                                    // Reuse existing Ask Image 
                                    LlmAskImageScreen(
                                        modelManagerViewModel = modelManagerViewModel,
                                        navigateUp = onDismissRequest,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                TalkToMeTab.Audio -> {
                                    // Reuse existing Ask Audio 
                                    LlmAskAudioScreen(
                                        modelManagerViewModel = modelManagerViewModel,
                                        navigateUp = onDismissRequest,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
