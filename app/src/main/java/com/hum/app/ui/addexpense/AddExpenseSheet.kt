package com.hum.app.ui.addexpense

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hum.app.data.model.CategoryEntity
import com.hum.app.data.model.RecurringType
import com.hum.app.data.model.User
import com.hum.app.ui.components.CategoryChip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    onDismiss: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val calendar = remember(uiState.date) {
        Calendar.getInstance().apply { time = uiState.date }
    }
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Expense added!", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Calendar.getInstance().apply { timeInMillis = millis }
                        val merged = Calendar.getInstance().apply {
                            time = uiState.date
                            set(Calendar.YEAR, picked.get(Calendar.YEAR))
                            set(Calendar.MONTH, picked.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
                        }
                        viewModel.updateDate(merged.time)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val merged = Calendar.getInstance().apply {
                        time = uiState.date
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                    }
                    viewModel.updateDate(merged.time)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        AddExpenseFormContent(
            uiState = uiState,
            onAmountChange = viewModel::updateAmount,
            onTitleChange = viewModel::updateTitle,
            onCategorySelect = viewModel::updateCategory,
            onShowAddCategory = viewModel::showAddCategoryDialog,
            onHideAddCategory = viewModel::hideAddCategoryDialog,
            onNewCategoryNameChange = viewModel::updateNewCategoryName,
            onAddCustomCategory = viewModel::addCustomCategory,
            onPaidBySelect = viewModel::updatePaidBy,
            onDateClick = { showDatePicker = true },
            onTimeClick = { showTimePicker = true },
            onToggleRecurring = viewModel::toggleRecurring,
            onRecurringTypeChange = viewModel::updateRecurringType,
            onRecurringDayChange = viewModel::updateRecurringDay,
            onNotesChange = viewModel::updateNotes,
            onSave = viewModel::saveExpense,
            dateDisplay = dateFormat.format(uiState.date),
            timeDisplay = timeFormat.format(uiState.date)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AddExpenseFormContent(
    uiState: AddExpenseUiState,
    onAmountChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategorySelect: (CategoryEntity) -> Unit,
    onShowAddCategory: () -> Unit,
    onHideAddCategory: () -> Unit,
    onNewCategoryNameChange: (String) -> Unit,
    onAddCustomCategory: () -> Unit,
    onPaidBySelect: (User) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onToggleRecurring: () -> Unit,
    onRecurringTypeChange: (RecurringType) -> Unit,
    onRecurringDayChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    dateDisplay: String,
    timeDisplay: String,
    modifier: Modifier = Modifier
) {
    if (uiState.showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = onHideAddCategory,
            title = { Text("New Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter a category name. AI will pick the best icon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = uiState.newCategoryName,
                        onValueChange = onNewCategoryNameChange,
                        label = { Text("Category name") },
                        placeholder = { Text("e.g. Groceries, Gym, Pet Care") },
                        singleLine = true,
                        enabled = !uiState.isResolvingIcon,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_category_name_field")
                    )
                    if (uiState.isResolvingIcon) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Finding the perfect icon…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onAddCustomCategory,
                    enabled = uiState.newCategoryName.isNotBlank() && !uiState.isResolvingIcon,
                    modifier = Modifier.testTag("add_category_confirm")
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(
                    onClick = onHideAddCategory,
                    enabled = !uiState.isResolvingIcon,
                    modifier = Modifier.testTag("add_category_cancel")
                ) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add Expense",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            prefix = { Text("₹", fontSize = 18.sp) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("amount_field")
        )

        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            placeholder = { Text("e.g. Electricity Bill") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("title_field")
        )

        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.categories.forEach { cat ->
                CategoryChip(
                    category = cat,
                    selected = uiState.selectedCategory?.name == cat.name,
                    onClick = { onCategorySelect(cat) },
                    modifier = Modifier.testTag("category_chip_${cat.name}")
                )
            }
            FilterChip(
                selected = false,
                onClick = onShowAddCategory,
                label = { Text("+ Add New", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("add_category_chip")
            )
        }

        if (uiState.familyMembers.size > 1) {
            Text(
                text = "Paid by",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.familyMembers.forEach { member ->
                    val isSelected = member.id == uiState.paidByUserId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPaidBySelect(member) },
                        label = {
                            Text(
                                text = member.displayName.split(" ").first(),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("member_chip_${member.id}")
                    )
                }
            }
        }

        Text(
            text = "Date & Time",
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onDateClick)
                    .testTag("date_button"),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateDisplay,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clickable(onClick = onTimeClick)
                    .testTag("time_button"),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = timeDisplay,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recurring Expense",
                style = MaterialTheme.typography.titleMedium
            )
            Switch(
                checked = uiState.isRecurring,
                onCheckedChange = { onToggleRecurring() },
                modifier = Modifier.testTag("recurring_switch")
            )
        }

        if (uiState.isRecurring) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                RecurringType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.recurringType == type,
                        onClick = { onRecurringTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = RecurringType.entries.size
                        )
                    ) {
                        Text(type.label)
                    }
                }
            }

            OutlinedTextField(
                value = uiState.recurringDay.toString(),
                onValueChange = { onRecurringDayChange(it.toIntOrNull() ?: 1) },
                label = { Text("Day of Month") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recurring_day_field")
            )
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChange,
            label = { Text("Notes (optional)") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notes_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_button")
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Save Expense", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
