package com.example.ui.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CountdownEvent
import com.example.data.model.UserSettings
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.OutlineMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditEventDialog(
    initialEvent: CountdownEvent?,
    prefilledDate: LocalDate?,
    defaultAccentHex: String,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        targetDate: LocalDate,
        startDate: LocalDate,
        emoji: String,
        accentHex: String,
        isStarred: Boolean
    ) -> Unit
) {
    val initialDate = initialEvent?.targetDate ?: prefilledDate ?: LocalDate.now().plusMonths(1)
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var targetDate by remember { mutableStateOf(initialDate) }
    var selectedEmoji by remember { mutableStateOf(initialEvent?.emoji ?: "✨") }
    var selectedAccent by remember { mutableStateOf(initialEvent?.accentHex ?: defaultAccentHex) }
    var isStarred by remember { mutableStateOf(initialEvent?.isStarred ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val starScale by animateFloatAsState(
        targetValue = if (isStarred) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 520f),
        label = "eventPinScale"
    )

    val presetEmojis = listOf(
        "✨", "🎂", "✈️", "🏖️", "🎓", "🎆", "🎄", "💍",
        "🎯", "🚀", "🏡", "🚗", "🎸", "☀️", "🏃", "💼"
    )

    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220))
        ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .animateContentSize()
                .testTag("add_edit_event_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialEvent == null) "New Countdown" else "Edit Countdown",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_event_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    placeholder = { Text("e.g. Summer Vacation, Birthday, Exam") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(android.graphics.Color.parseColor(selectedAccent)),
                        unfocusedBorderColor = OutlineMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Color(android.graphics.Color.parseColor(selectedAccent)),
                        focusedLabelColor = Color(android.graphics.Color.parseColor(selectedAccent)),
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Date Selector Button
                Text(
                    text = "TARGET DATE",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, OutlineMuted, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("event_date_picker_button"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = targetDate.format(dateFormatter),
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Emoji Selection
                Text(
                    text = "ICON / EMOJI",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetEmojis.forEach { emoji ->
                        val isSelected = selectedEmoji == emoji
                        val emojiFill by animateColorAsState(
                            targetValue = if (isSelected) Color(android.graphics.Color.parseColor(selectedAccent)).copy(alpha = 0.16f) else Color.Transparent,
                            animationSpec = tween(160),
                            label = "emojiOptionFill"
                        )
                        val emojiStroke by animateColorAsState(
                            targetValue = if (isSelected) Color(android.graphics.Color.parseColor(selectedAccent)) else OutlineMuted,
                            animationSpec = tween(160),
                            label = "emojiOptionStroke"
                        )
                        Box(
                            modifier = Modifier
                                .animateContentSize()
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(emojiFill)
                                .border(1.dp, emojiStroke, RoundedCornerShape(11.dp))
                                .clickable { selectedEmoji = emoji }
                                .testTag("emoji_option_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accent Color Selection
                Text(
                    text = "ACCENT COLOR",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserSettings.PRESET_ACCENTS.forEach { (hex, _) ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedAccent.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedAccent = hex }
                                .testTag("accent_color_$hex")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Starred / Pinned Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .clickable { isStarred = !isStarred }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pin to top of list",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Pin to top",
                        tint = if (isStarred) Color(0xFFFFB347) else TextTertiary,
                        modifier = Modifier.graphicsLayer { scaleX = starScale; scaleY = starScale }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                val parsedAccent = Color(android.graphics.Color.parseColor(selectedAccent))
                val saveContentColor = if (parsedAccent.luminance() > 0.46f) DeepCharcoal else Color.White
                Button(
                    onClick = {
                        val finalTitle = title.trim().ifEmpty { "Countdown" }
                        onSave(
                            initialEvent?.id ?: 0L,
                            finalTitle,
                            targetDate,
                            initialEvent?.startDate ?: LocalDate.now(),
                            selectedEmoji,
                            selectedAccent,
                            isStarred
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_event_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = parsedAccent,
                        contentColor = saveContentColor
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, parsedAccent.copy(alpha = 0.72f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (initialEvent == null) "Create Countdown" else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            targetDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
