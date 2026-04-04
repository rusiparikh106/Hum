package com.hum.app.ui.addexpense

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.hum.app.data.model.CategoryEntity
import com.hum.app.data.model.RecurringType
import com.hum.app.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date

class AddExpenseFormContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCategories = listOf(
        CategoryEntity(id = "FOOD", name = "FOOD", label = "Food", icon = "🍕", sortOrder = 0),
        CategoryEntity(id = "TRANSPORT", name = "TRANSPORT", label = "Transport", icon = "🚗", sortOrder = 1),
        CategoryEntity(id = "OTHER", name = "OTHER", label = "Other", icon = "📦", sortOrder = 2)
    )

    private val testMembers = listOf(
        User(id = "user1", displayName = "Alice Smith"),
        User(id = "user2", displayName = "Bob Jones")
    )

    private fun defaultState() = AddExpenseUiState(
        categories = testCategories,
        selectedCategory = testCategories[2],
        paidByUserId = "user1",
        paidByUserName = "Alice Smith",
        familyMembers = testMembers,
        date = Date()
    )

    private fun setContent(
        state: AddExpenseUiState = defaultState(),
        onAmountChange: (String) -> Unit = {},
        onTitleChange: (String) -> Unit = {},
        onCategorySelect: (CategoryEntity) -> Unit = {},
        onShowAddCategory: () -> Unit = {},
        onHideAddCategory: () -> Unit = {},
        onNewCategoryNameChange: (String) -> Unit = {},
        onAddCustomCategory: () -> Unit = {},
        onPaidBySelect: (User) -> Unit = {},
        onDateClick: () -> Unit = {},
        onTimeClick: () -> Unit = {},
        onToggleRecurring: () -> Unit = {},
        onRecurringTypeChange: (RecurringType) -> Unit = {},
        onRecurringDayChange: (Int) -> Unit = {},
        onNotesChange: (String) -> Unit = {},
        onSave: () -> Unit = {},
        dateDisplay: String = "Thu, 03 Apr 2026",
        timeDisplay: String = "10:00 AM"
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                AddExpenseFormContent(
                    uiState = state,
                    onAmountChange = onAmountChange,
                    onTitleChange = onTitleChange,
                    onCategorySelect = onCategorySelect,
                    onShowAddCategory = onShowAddCategory,
                    onHideAddCategory = onHideAddCategory,
                    onNewCategoryNameChange = onNewCategoryNameChange,
                    onAddCustomCategory = onAddCustomCategory,
                    onPaidBySelect = onPaidBySelect,
                    onDateClick = onDateClick,
                    onTimeClick = onTimeClick,
                    onToggleRecurring = onToggleRecurring,
                    onRecurringTypeChange = onRecurringTypeChange,
                    onRecurringDayChange = onRecurringDayChange,
                    onNotesChange = onNotesChange,
                    onSave = onSave,
                    dateDisplay = dateDisplay,
                    timeDisplay = timeDisplay
                )
            }
        }
    }

    // ── Form field visibility ─────────────────────────────────────────────

    @Test
    fun formDisplaysTitle() {
        setContent()
        composeTestRule.onNodeWithText("Add Expense").assertIsDisplayed()
    }

    @Test
    fun formDisplaysAmountField() {
        setContent()
        composeTestRule.onNodeWithTag("amount_field").assertIsDisplayed()
    }

    @Test
    fun formDisplaysTitleField() {
        setContent()
        composeTestRule.onNodeWithTag("title_field").assertIsDisplayed()
    }

    @Test
    fun formDisplaysCategoryLabel() {
        setContent()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
    }

    @Test
    fun formDisplaysCategoryChips() {
        setContent()
        composeTestRule.onNodeWithTag("category_chip_FOOD").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_TRANSPORT").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_OTHER").assertIsDisplayed()
    }

    @Test
    fun formDisplaysAddNewCategoryChip() {
        setContent()
        composeTestRule.onNodeWithTag("add_category_chip").assertIsDisplayed()
    }

    @Test
    fun formDisplaysSaveButton() {
        setContent()
        composeTestRule.onNodeWithTag("save_button").assertIsDisplayed()
    }

    @Test
    fun formDisplaysNotesField() {
        setContent()
        composeTestRule.onNodeWithTag("notes_field").assertIsDisplayed()
    }

    @Test
    fun formDisplaysDateAndTimeButtons() {
        setContent()
        composeTestRule.onNodeWithTag("date_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("time_button").assertIsDisplayed()
    }

    @Test
    fun formDisplaysRecurringSwitch() {
        setContent()
        composeTestRule.onNodeWithTag("recurring_switch").assertIsDisplayed()
    }

    @Test
    fun formDisplaysDateAndTimeValues() {
        setContent(dateDisplay = "Thu, 03 Apr 2026", timeDisplay = "10:00 AM")
        composeTestRule.onNodeWithText("Thu, 03 Apr 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("10:00 AM").assertIsDisplayed()
    }

    // ── Family member chips ───────────────────────────────────────────────

    @Test
    fun formDisplaysPaidByLabelWithMultipleMembers() {
        setContent()
        composeTestRule.onNodeWithText("Paid by").assertIsDisplayed()
    }

    @Test
    fun formDisplaysFamilyMemberChips() {
        setContent()
        composeTestRule.onNodeWithTag("member_chip_user1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("member_chip_user2").assertIsDisplayed()
    }

    @Test
    fun formHidesPaidBySectionWithSingleMember() {
        val state = defaultState().copy(
            familyMembers = listOf(User(id = "user1", displayName = "Alice"))
        )
        setContent(state = state)
        composeTestRule.onNodeWithText("Paid by").assertDoesNotExist()
    }

    // ── Callback interactions ─────────────────────────────────────────────

    @Test
    fun clickingCategoryChipTriggersCallback() {
        var selectedName = ""
        setContent(onCategorySelect = { selectedName = it.name })
        composeTestRule.onNodeWithTag("category_chip_FOOD").performClick()
        assertEquals("FOOD", selectedName)
    }

    @Test
    fun clickingAddNewCategoryChipTriggersCallback() {
        var clicked = false
        setContent(onShowAddCategory = { clicked = true })
        composeTestRule.onNodeWithTag("add_category_chip").performClick()
        assertTrue(clicked)
    }

    @Test
    fun clickingSaveButtonTriggersCallback() {
        var saved = false
        setContent(onSave = { saved = true })
        composeTestRule.onNodeWithTag("save_button").performClick()
        assertTrue(saved)
    }

    @Test
    fun clickingDateButtonTriggersCallback() {
        var clicked = false
        setContent(onDateClick = { clicked = true })
        composeTestRule.onNodeWithTag("date_button").performClick()
        assertTrue(clicked)
    }

    @Test
    fun clickingTimeButtonTriggersCallback() {
        var clicked = false
        setContent(onTimeClick = { clicked = true })
        composeTestRule.onNodeWithTag("time_button").performClick()
        assertTrue(clicked)
    }

    @Test
    fun clickingMemberChipTriggersCallback() {
        var selectedId = ""
        setContent(onPaidBySelect = { selectedId = it.id })
        composeTestRule.onNodeWithTag("member_chip_user2").performClick()
        assertEquals("user2", selectedId)
    }

    @Test
    fun clickingRecurringSwitchTriggersCallback() {
        var toggled = false
        setContent(onToggleRecurring = { toggled = true })
        composeTestRule.onNodeWithTag("recurring_switch").performClick()
        assertTrue(toggled)
    }

    // ── Save button state ─────────────────────────────────────────────────

    @Test
    fun saveButtonIsEnabledWhenNotSaving() {
        setContent(state = defaultState().copy(isSaving = false))
        composeTestRule.onNodeWithTag("save_button").assertIsEnabled()
    }

    @Test
    fun saveButtonIsDisabledWhenSaving() {
        setContent(state = defaultState().copy(isSaving = true))
        composeTestRule.onNodeWithTag("save_button").assertIsNotEnabled()
    }

    @Test
    fun saveButtonShowsProgressWhenSaving() {
        setContent(state = defaultState().copy(isSaving = true))
        composeTestRule.onNodeWithText("Save Expense").assertDoesNotExist()
    }

    @Test
    fun saveButtonShowsTextWhenNotSaving() {
        setContent(state = defaultState().copy(isSaving = false))
        composeTestRule.onNodeWithText("Save Expense").assertIsDisplayed()
    }

    // ── Add category dialog ───────────────────────────────────────────────

    @Test
    fun addCategoryDialogShowsWhenStateIsTrue() {
        setContent(state = defaultState().copy(showAddCategoryDialog = true))
        composeTestRule.onNodeWithText("New Category").assertIsDisplayed()
        composeTestRule.onNodeWithTag("new_category_name_field").assertIsDisplayed()
    }

    @Test
    fun addCategoryDialogHiddenByDefault() {
        setContent()
        composeTestRule.onNodeWithText("New Category").assertDoesNotExist()
    }

    @Test
    fun addCategoryConfirmDisabledWhenNameEmpty() {
        setContent(state = defaultState().copy(
            showAddCategoryDialog = true,
            newCategoryName = ""
        ))
        composeTestRule.onNodeWithTag("add_category_confirm").assertIsNotEnabled()
    }

    @Test
    fun addCategoryConfirmEnabledWhenNameProvided() {
        setContent(state = defaultState().copy(
            showAddCategoryDialog = true,
            newCategoryName = "Pet Care"
        ))
        composeTestRule.onNodeWithTag("add_category_confirm").assertIsEnabled()
    }

    @Test
    fun addCategoryConfirmDisabledWhileResolvingIcon() {
        setContent(state = defaultState().copy(
            showAddCategoryDialog = true,
            newCategoryName = "Pet Care",
            isResolvingIcon = true
        ))
        composeTestRule.onNodeWithTag("add_category_confirm").assertIsNotEnabled()
    }

    @Test
    fun addCategoryCancelDisabledWhileResolvingIcon() {
        setContent(state = defaultState().copy(
            showAddCategoryDialog = true,
            isResolvingIcon = true
        ))
        composeTestRule.onNodeWithTag("add_category_cancel").assertIsNotEnabled()
    }

    @Test
    fun addCategoryShowsProgressWhileResolvingIcon() {
        setContent(state = defaultState().copy(
            showAddCategoryDialog = true,
            isResolvingIcon = true
        ))
        composeTestRule.onNodeWithText("Finding the perfect icon…").assertIsDisplayed()
    }

    @Test
    fun addCategoryConfirmTriggersCallback() {
        var confirmed = false
        setContent(
            state = defaultState().copy(
                showAddCategoryDialog = true,
                newCategoryName = "Test"
            ),
            onAddCustomCategory = { confirmed = true }
        )
        composeTestRule.onNodeWithTag("add_category_confirm").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun addCategoryCancelTriggersCallback() {
        var cancelled = false
        setContent(
            state = defaultState().copy(showAddCategoryDialog = true),
            onHideAddCategory = { cancelled = true }
        )
        composeTestRule.onNodeWithTag("add_category_cancel").performClick()
        assertTrue(cancelled)
    }

    // ── Recurring expense section ─────────────────────────────────────────

    @Test
    fun recurringFieldsHiddenWhenNotRecurring() {
        setContent(state = defaultState().copy(isRecurring = false))
        composeTestRule.onNodeWithTag("recurring_day_field").assertDoesNotExist()
    }

    @Test
    fun recurringFieldsVisibleWhenRecurring() {
        setContent(state = defaultState().copy(isRecurring = true))
        composeTestRule.onNodeWithTag("recurring_day_field").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quarterly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yearly").assertIsDisplayed()
    }

    // ── Amount field pre-filled state ─────────────────────────────────────

    @Test
    fun amountFieldShowsCurrentValue() {
        setContent(state = defaultState().copy(amount = "250"))
        composeTestRule.onNodeWithTag("amount_field").assertTextContains("250")
    }

    @Test
    fun titleFieldShowsCurrentValue() {
        setContent(state = defaultState().copy(title = "Test Title"))
        composeTestRule.onNodeWithTag("title_field").assertTextContains("Test Title")
    }

    @Test
    fun notesFieldShowsCurrentValue() {
        setContent(state = defaultState().copy(notes = "Some notes"))
        composeTestRule.onNodeWithTag("notes_field").assertTextContains("Some notes")
    }
}
