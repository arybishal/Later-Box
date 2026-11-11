package com.laterbox.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.laterbox.data.local.entity.ReminderEntity
import com.laterbox.data.reminder.EventRecurrenceCalculator
import com.laterbox.ui.components.EmptyStateView
import com.laterbox.ui.theme.LaterBoxThemeTokens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun EventsScreen(
    viewModel: EventsViewModel = viewModel(),
    onOpenItem: (Long) -> Unit = {}
) {
    val extended = LaterBoxThemeTokens.extended
    val reminders by viewModel.filteredReminders.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    val birthdaysCount = remember(allReminders) { allReminders.count { it.eventType == "BIRTHDAY" } }
    val anniversariesCount = remember(allReminders) { allReminders.count { it.eventType == "ANNIVERSARY" } }
    val remindersCount = remember(allReminders) { allReminders.count { it.eventType == "REMINDER" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extended.background)
    ) {
        // Screen Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CALENDAR & REMINDERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.tertiaryText,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Events",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = extended.primaryBlack
                    )
                    Text(
                        text = "${allReminders.size} scheduled memories",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }

                // + New Event Action
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(extended.surfaceSecondary)
                        .border(1.dp, extended.border, CircleShape)
                        .clickable { showCreateDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Event",
                        tint = extended.primaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    EventFilterChip(
                        label = "All (${allReminders.size})",
                        isSelected = selectedFilter == "ALL",
                        onClick = { viewModel.setFilter("ALL") }
                    )
                }
                item {
                    EventFilterChip(
                        label = "Birthdays 🎂 ($birthdaysCount)",
                        isSelected = selectedFilter == "BIRTHDAY",
                        onClick = { viewModel.setFilter("BIRTHDAY") }
                    )
                }
                item {
                    EventFilterChip(
                        label = "Anniversaries 💍 ($anniversariesCount)",
                        isSelected = selectedFilter == "ANNIVERSARY",
                        onClick = { viewModel.setFilter("ANNIVERSARY") }
                    )
                }
                item {
                    EventFilterChip(
                        label = "Reminders ⏰ ($remindersCount)",
                        isSelected = selectedFilter == "REMINDER",
                        onClick = { viewModel.setFilter("REMINDER") }
                    )
                }
            }
        }

        // Event List or Empty State
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedFilter != "ALL") {
                    val filterName = when (selectedFilter) {
                        "BIRTHDAY" -> "Birthday"
                        "ANNIVERSARY" -> "Anniversary"
                        "MEETING" -> "Meeting"
                        "REMINDER" -> "Reminder"
                        else -> selectedFilter.lowercase().replaceFirstChar { it.uppercase() }
                    }
                    EmptyStateView(
                        icon = Icons.Default.Event,
                        title = "No $filterName Events",
                        description = "You don't have any $filterName events scheduled.",
                        actionLabel = "Show All Events",
                        onAction = { viewModel.setFilter("ALL") }
                    )
                } else {
                    EmptyStateView(
                        icon = Icons.Default.Event,
                        title = "No Upcoming Events",
                        description = "Save birthdays, anniversaries, meetings, or recurring reminders to keep them organized.",
                        actionLabel = "+ Create Reminder",
                        onAction = { showCreateDialog = true }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    EventCard(
                        reminder = reminder,
                        onMarkDone = { viewModel.markCompleted(reminder) },
                        onSnooze = { viewModel.snoozeReminder(reminder) },
                        onDelete = { viewModel.deleteEvent(reminder.id) },
                        onClick = { onOpenItem(reminder.contentId) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, dueAt, eventType, personName, recurrence, notifyBefore, note, origDate, relation, secondPerson, offsetsJson ->
                viewModel.createEvent(
                    title = title,
                    dueAt = dueAt,
                    eventType = eventType,
                    personName = personName,
                    recurrenceRule = recurrence,
                    notifyBeforeMinutes = notifyBefore,
                    note = note,
                    originalDate = origDate,
                    relationship = relation,
                    secondPersonName = secondPerson,
                    reminderOffsetsDaysJson = offsetsJson
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun EventFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val background = if (isSelected) extended.buttonPrimary else extended.surfaceSecondary
    val textColor = if (isSelected) extended.buttonPrimaryText else extended.primaryText
    val border = if (isSelected) extended.buttonPrimary else extended.border

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun EventCard(
    reminder: ReminderEntity,
    onMarkDone: () -> Unit,
    onSnooze: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val now = System.currentTimeMillis()

    val isAnnual = reminder.eventType == "BIRTHDAY" || reminder.eventType == "ANNIVERSARY"
    val recurrenceResult = if (isAnnual && reminder.originalDate != null) {
        EventRecurrenceCalculator.calculateNextOccurrence(reminder.originalDate)
    } else null

    val dueAt = reminder.snoozedUntil ?: (recurrenceResult?.nextOccurrenceEpochMs ?: reminder.dueAt)
    val diffMillis = dueAt - now
    val diffDays = recurrenceResult?.daysRemaining ?: TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

    val countdownLabel = when {
        reminder.snoozedUntil != null -> "Snoozed"
        recurrenceResult != null -> recurrenceResult.formattedStatus
        diffMillis < 0 -> "Overdue"
        diffDays == 0 -> "Today"
        diffDays == 1 -> "Tomorrow"
        diffDays < 7 -> "In $diffDays days"
        diffDays < 30 -> "In ${diffDays / 7} weeks"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dueAt))
    }

    val eventIcon = when (reminder.eventType) {
        "BIRTHDAY" -> Icons.Default.Cake
        "ANNIVERSARY" -> Icons.Default.Favorite
        "EVENT" -> Icons.Default.Event
        else -> Icons.Default.Notifications
    }

    val subtitleText = when (reminder.eventType) {
        "BIRTHDAY" -> {
            val ageStr = recurrenceResult?.turningAgeOrYears?.let { "Turning $it" }
            val relStr = reminder.relationship?.ifBlank { null }
            listOfNotNull(reminder.personName, relStr, ageStr).joinToString(" · ")
        }
        "ANNIVERSARY" -> {
            val yearsStr = recurrenceResult?.turningAgeOrYears?.let { "$it Years" }
            val couple = when {
                !reminder.personName.isNullOrBlank() && !reminder.secondPersonName.isNullOrBlank() ->
                    "${reminder.personName} & ${reminder.secondPersonName}"
                !reminder.personName.isNullOrBlank() -> reminder.personName
                else -> null
            }
            listOfNotNull(couple, yearsStr).joinToString(" · ")
        }
        else -> reminder.personName ?: reminder.note ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, extended.border)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(extended.surfaceSecondary)
                            .border(1.dp, extended.border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = eventIcon,
                            contentDescription = null,
                            tint = extended.primaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = reminder.eventTitle ?: "Reminder",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = extended.primaryBlack
                        )
                        if (subtitleText.isNotBlank()) {
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = extended.textSecondary
                            )
                        } else if (!reminder.note.isNullOrBlank()) {
                            Text(
                                text = reminder.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Countdown Pill
                val isToday = diffDays == 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isToday) extended.interactiveAccent.copy(alpha = 0.15f) else extended.surfaceSecondary
                        )
                        .border(1.dp, extended.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = countdownLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) extended.interactiveAccent else extended.primaryText
                    )
                }
            }

            // Original Date / Context info if available
            if (reminder.originalDate != null && reminder.originalDate > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val origDateLabel = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(reminder.originalDate))
                val contextPrefix = if (reminder.eventType == "BIRTHDAY") "Born: " else "Since: "
                Text(
                    text = "$contextPrefix$origDateLabel",
                    fontSize = 11.sp,
                    color = extended.tertiaryText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer with recurrence rule & quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!reminder.recurrenceRule.isNullOrBlank() && reminder.recurrenceRule != "NONE") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = extended.tertiaryText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Repeats ${reminder.recurrenceRule.lowercase().replaceFirstChar { it.uppercase() }}",
                            fontSize = 11.sp,
                            color = extended.tertiaryText
                        )
                    }
                } else {
                    Text(
                        text = SimpleDateFormat("h:mm a · MMM d", Locale.getDefault()).format(Date(dueAt)),
                        fontSize = 11.sp,
                        color = extended.tertiaryText
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(extended.surfaceSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = "Snooze 1h",
                            tint = extended.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onMarkDone,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(extended.surfaceSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark Done",
                            tint = extended.primaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(extended.surfaceSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = extended.warmError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        dueAt: Long,
        eventType: String,
        personName: String?,
        recurrence: String?,
        notifyBefore: Int,
        note: String?,
        originalDate: Long?,
        relationship: String?,
        secondPersonName: String?,
        offsetsJson: String?
    ) -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    var eventType by remember { mutableStateOf("BIRTHDAY") }

    // Common fields
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("YEARLY") }
    var notifyBefore by remember { mutableStateOf(1440) }

    // Birthday specific fields
    var personName by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }

    // Anniversary specific fields
    var secondPersonName by remember { mutableStateOf("") }

    // Approaching reminder offsets (days before)
    var selectedOffsets by remember { mutableStateOf(setOf(7, 1, 0)) }

    var showDatePicker by remember { mutableStateOf(false) }

    // Selected date epoch
    var selectedEpoch by remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        mutableStateOf(cal.timeInMillis)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedEpoch)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = it
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                        }
                        selectedEpoch = cal.timeInMillis
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = extended.primaryText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = extended.secondaryText)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = extended.cardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, extended.border),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (eventType) {
                            "BIRTHDAY" -> "Add Birthday"
                            "ANNIVERSARY" -> "Add Anniversary"
                            "EVENT" -> "New Event"
                            else -> "New Reminder"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = extended.primaryBlack
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(extended.surfaceSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = extended.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Event Type Selector Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "BIRTHDAY" to "🎂 Birthday",
                        "ANNIVERSARY" to "💍 Anniv.",
                        "REMINDER" to "⏰ Reminder",
                        "EVENT" to "📅 Event"
                    ).forEach { (type, label) ->
                        val isSelected = eventType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) extended.buttonPrimary else extended.surfaceSecondary)
                                .border(1.dp, if (isSelected) extended.buttonPrimary else extended.border, RoundedCornerShape(10.dp))
                                .clickable {
                                    eventType = type
                                    if (type == "BIRTHDAY" || type == "ANNIVERSARY") {
                                        recurrence = "YEARLY"
                                        selectedOffsets = setOf(30, 7, 1, 0)
                                    } else {
                                        recurrence = "NONE"
                                        selectedOffsets = setOf(0)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) extended.buttonPrimaryText else extended.primaryText
                            )
                        }
                    }
                }

                when (eventType) {
                    "BIRTHDAY" -> {
                        // Person Name
                        OutlinedTextField(
                            value = personName,
                            onValueChange = {
                                personName = it
                                if (title.isBlank() || title.endsWith("'s Birthday")) {
                                    title = if (it.isNotBlank()) "$it's Birthday" else ""
                                }
                            },
                            label = { Text("Person's Name", color = extended.secondaryText) },
                            placeholder = { Text("e.g. Alex", color = extended.tertiaryText) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = extended.surfaceSecondary,
                                unfocusedContainerColor = extended.surfaceSecondary,
                                focusedBorderColor = extended.interactiveAccent,
                                unfocusedBorderColor = extended.border
                            )
                        )

                        // Quick Relationship Chips
                        Column {
                            Text("Relationship (optional)", fontSize = 11.sp, color = extended.secondaryText)
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Family", "Friend", "Partner", "Parent", "Child", "Sibling", "Colleague").forEach { rel ->
                                    val isRelSelected = relationship == rel
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isRelSelected) extended.buttonPrimary else extended.surfaceSecondary)
                                            .border(1.dp, if (isRelSelected) extended.buttonPrimary else extended.border, RoundedCornerShape(8.dp))
                                            .clickable { relationship = if (isRelSelected) "" else rel }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = rel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isRelSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isRelSelected) extended.buttonPrimaryText else extended.primaryText
                                        )
                                    }
                                }
                            }
                        }

                        // Birth Date Picker
                        val calcResult = remember(selectedEpoch) {
                            EventRecurrenceCalculator.calculateNextOccurrence(selectedEpoch)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(extended.surfaceSecondary)
                                .border(1.dp, extended.border, RoundedCornerShape(14.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Real Birth Date (Includes Year)", fontSize = 11.sp, color = extended.secondaryText)
                                Text(
                                    text = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(selectedEpoch)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.primaryBlack
                                )
                                if (calcResult.turningAgeOrYears != null) {
                                    Text(
                                        text = "Will be turning ${calcResult.turningAgeOrYears} (${calcResult.formattedStatus})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = extended.interactiveAccent
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Pick Birth Date",
                                tint = extended.primaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    "ANNIVERSARY" -> {
                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Anniversary Title", color = extended.secondaryText) },
                            placeholder = { Text("e.g. Wedding Anniversary", color = extended.tertiaryText) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = extended.surfaceSecondary,
                                unfocusedContainerColor = extended.surfaceSecondary,
                                focusedBorderColor = extended.interactiveAccent,
                                unfocusedBorderColor = extended.border
                            )
                        )

                        // Couple Names
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = personName,
                                onValueChange = { personName = it },
                                label = { Text("Partner 1", color = extended.secondaryText) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = extended.surfaceSecondary,
                                    unfocusedContainerColor = extended.surfaceSecondary,
                                    focusedBorderColor = extended.interactiveAccent,
                                    unfocusedBorderColor = extended.border
                                )
                            )
                            OutlinedTextField(
                                value = secondPersonName,
                                onValueChange = { secondPersonName = it },
                                label = { Text("Partner 2", color = extended.secondaryText) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = extended.surfaceSecondary,
                                    unfocusedContainerColor = extended.surfaceSecondary,
                                    focusedBorderColor = extended.interactiveAccent,
                                    unfocusedBorderColor = extended.border
                                )
                            )
                        }

                        // Anniversary Original Date Picker
                        val calcResult = remember(selectedEpoch) {
                            EventRecurrenceCalculator.calculateNextOccurrence(selectedEpoch)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(extended.surfaceSecondary)
                                .border(1.dp, extended.border, RoundedCornerShape(14.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Original Anniversary Date", fontSize = 11.sp, color = extended.secondaryText)
                                Text(
                                    text = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(selectedEpoch)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.primaryBlack
                                )
                                if (calcResult.turningAgeOrYears != null) {
                                    Text(
                                        text = "Celebrating ${calcResult.turningAgeOrYears} years (${calcResult.formattedStatus})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = extended.interactiveAccent
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Pick Anniversary Date",
                                tint = extended.primaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    else -> {
                        // General Reminder / Event
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title", color = extended.secondaryText) },
                            placeholder = { Text("e.g. Flight to Tokyo", color = extended.tertiaryText) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = extended.surfaceSecondary,
                                unfocusedContainerColor = extended.surfaceSecondary,
                                focusedBorderColor = extended.interactiveAccent,
                                unfocusedBorderColor = extended.border
                            )
                        )

                        // Date Picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(extended.surfaceSecondary)
                                .border(1.dp, extended.border, RoundedCornerShape(14.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Date", fontSize = 11.sp, color = extended.secondaryText)
                                Text(
                                    text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedEpoch)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.primaryBlack
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Pick Date",
                                tint = extended.primaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Recurrence Selector
                        Column {
                            Text("Recurrence", fontSize = 11.sp, color = extended.secondaryText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "NONE" to "One-time",
                                    "YEARLY" to "Yearly",
                                    "MONTHLY" to "Monthly",
                                    "WEEKLY" to "Weekly"
                                ).forEach { (rec, label) ->
                                    val isSelected = recurrence == rec
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) extended.surfaceSecondary else Color.Transparent)
                                            .border(1.dp, if (isSelected) extended.primaryBlack else extended.border, RoundedCornerShape(10.dp))
                                            .clickable { recurrence = rec }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = extended.primaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Approaching Notifications Schedule (Checklist / Chips)
                Column {
                    Text(
                        text = "Approaching Notifications",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.secondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val offsetOptions = if (eventType == "BIRTHDAY" || eventType == "ANNIVERSARY") {
                            listOf(
                                30 to "30d before",
                                7 to "7d before",
                                3 to "3d before",
                                1 to "1d before",
                                0 to "On the day"
                            )
                        } else {
                            listOf(
                                7 to "7d before",
                                1 to "1d before",
                                0 to "On time"
                            )
                        }

                        offsetOptions.forEach { (days, label) ->
                            val isChecked = selectedOffsets.contains(days)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChecked) extended.buttonPrimary else extended.surfaceSecondary)
                                    .border(1.dp, if (isChecked) extended.buttonPrimary else extended.border, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedOffsets = if (isChecked) {
                                            if (selectedOffsets.size > 1) selectedOffsets - days else selectedOffsets
                                        } else {
                                            selectedOffsets + days
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = (if (isChecked) "✓ " else "") + label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChecked) extended.buttonPrimaryText else extended.primaryText
                                )
                            }
                        }
                    }
                }

                // Optional Notes / Gift Ideas
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = {
                        Text(
                            when (eventType) {
                                "BIRTHDAY" -> "Gift ideas, preferences, or notes (optional)"
                                "ANNIVERSARY" -> "Plans, reservation details, or ideas (optional)"
                                else -> "Additional notes (optional)"
                            },
                            color = extended.tertiaryText
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = extended.surfaceSecondary,
                        unfocusedContainerColor = extended.surfaceSecondary,
                        focusedBorderColor = extended.interactiveAccent,
                        unfocusedBorderColor = extended.border
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Cancel & Save
                val effectiveTitle = when (eventType) {
                    "BIRTHDAY" -> if (title.isNotBlank()) title else if (personName.isNotBlank()) "$personName's Birthday" else ""
                    else -> title
                }
                val canSave = effectiveTitle.isNotBlank()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, extended.border)
                    ) {
                        Text("Cancel", color = extended.secondaryText)
                    }

                    Button(
                        onClick = {
                            if (canSave) {
                                val isAnnual = eventType == "BIRTHDAY" || eventType == "ANNIVERSARY"
                                val finalRecurrence = if (isAnnual) "YEARLY" else recurrence
                                val offsetsString = selectedOffsets.sortedDescending().joinToString(",")
                                val targetDueAt = if (isAnnual) {
                                    EventRecurrenceCalculator.calculateNextOccurrence(selectedEpoch).nextOccurrenceEpochMs
                                } else {
                                    selectedEpoch
                                }

                                onConfirm(
                                    effectiveTitle.trim(),
                                    targetDueAt,
                                    eventType,
                                    personName.trim().ifBlank { null },
                                    finalRecurrence,
                                    notifyBefore,
                                    note.trim().ifBlank { null },
                                    if (isAnnual) selectedEpoch else null,
                                    relationship.trim().ifBlank { null },
                                    secondPersonName.trim().ifBlank { null },
                                    offsetsString
                                )
                            }
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extended.buttonPrimary,
                            contentColor = extended.buttonPrimaryText
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (eventType) {
                                "BIRTHDAY" -> "Save Birthday"
                                "ANNIVERSARY" -> "Save Anniversary"
                                else -> "Save Event"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
