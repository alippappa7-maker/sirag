package com.siraj.app.features.support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.domain.models.support.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    viewModel: SupportViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val ticket = uiState.myTickets.find { it.id == ticketId } ?: uiState.selectedTicket
    val listState = rememberLazyListState()
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    var showCloseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(ticket?.replies?.size) {
        if (ticket != null && ticket.replies.isNotEmpty()) {
            listState.animateScrollToItem(ticket.replies.size)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(ticket?.ticketNumber ?: "تفاصيل التذكرة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        ticket?.let {
                            Text(it.targetTeam.titleAr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (ticket != null && ticket.status != TicketStatus.CLOSED) {
                        TextButton(onClick = { showCloseDialog = true }) {
                            Text("إغلاق التذكرة", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (ticket != null && ticket.status != TicketStatus.CLOSED) {
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.replyInput,
                            onValueChange = { viewModel.setReplyInput(it) },
                            placeholder = { Text("اكتب ردك هنا...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 4,
                            shape = RoundedCornerShape(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.sendReply(ticket.id) },
                            enabled = uiState.replyInput.isNotBlank(),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال الرد", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (ticket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ticket Overview Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ticket.category.titleAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                TicketStatusBadge(status = ticket.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ticket.subject,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ticket.description,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Metadata info
                            if (!ticket.shariaSurahOrHadithRef.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        "المرجع الشرعي: ${ticket.shariaSurahOrHadithRef}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            if (!ticket.billingTransactionId.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        "رقم العملية المالية: ${ticket.billingTransactionId}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            if (ticket.safeLogs != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("سجلات التشخيص الآمنة مرفقة بنجاح", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "تاريخ الإنشاء: ${dateFormat.format(Date(ticket.createdAt))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Rating Card if Resolved or Closed
                if (ticket.status == TicketStatus.RESOLVED || ticket.status == TicketStatus.CLOSED) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (ticket.rating != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (ticket.rating != null) {
                                    Text("تقييمك لخدمة الدعم:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = if (i <= ticket.rating.stars) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    if (!ticket.rating.feedback.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("ملاحظاتك: \"${ticket.rating.feedback}\"", style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    Text("كيف تقيم مستوى استجابة الدعم لهذه التذكرة؟", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        for (i in 1..5) {
                                            IconButton(onClick = { viewModel.setRatingStars(i) }) {
                                                Icon(
                                                    imageVector = if (i <= uiState.ratingStars) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = "$i نجوم",
                                                    tint = Color(0xFFFFB300),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = uiState.ratingFeedback,
                                        onValueChange = { viewModel.setRatingFeedback(it) },
                                        placeholder = { Text("ملاحظات إضافية لتحسين الخدمة (اختياري)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.submitRating(ticket.id) },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("إرسال التقييم")
                                    }
                                }
                            }
                        }
                    }
                }

                // Replies / Conversation Messages
                item {
                    Text(
                        "الردود والمحادثة (${ticket.replies.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(ticket.replies) { reply ->
                    ReplyBubble(reply = reply, dateFormat = dateFormat)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Close ticket confirm dialog
        if (showCloseDialog && ticket != null) {
            AlertDialog(
                onDismissRequest = { showCloseDialog = false },
                title = { Text("إغلاق التذكرة") },
                text = { Text("هل تم حل مشكلتك وترغب في إغلاق هذه التذكرة نهائياً؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.closeTicket(ticket.id)
                            showCloseDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("نعم، أغلق التذكرة")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCloseDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

@Composable
fun ReplyBubble(
    reply: TicketReply,
    dateFormat: SimpleDateFormat
) {
    val isUser = reply.authorRole == ReplyAuthorRole.USER
    val isSystem = reply.authorRole == ReplyAuthorRole.SYSTEM

    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = when {
        isUser -> MaterialTheme.colorScheme.primaryContainer
        isSystem -> MaterialTheme.colorScheme.surfaceVariant
        reply.authorRole == ReplyAuthorRole.SHARIA_REVIEWER -> MaterialTheme.colorScheme.secondaryContainer
        reply.authorRole == ReplyAuthorRole.BILLING_ADMIN -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reply.authorName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (reply.authorRole) {
                            ReplyAuthorRole.SHARIA_REVIEWER -> MaterialTheme.colorScheme.secondary
                            ReplyAuthorRole.BILLING_ADMIN -> MaterialTheme.colorScheme.tertiary
                            ReplyAuthorRole.SUPPORT_AGENT -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = dateFormat.format(Date(reply.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reply.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
