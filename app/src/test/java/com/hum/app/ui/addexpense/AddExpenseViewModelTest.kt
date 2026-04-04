package com.hum.app.ui.addexpense

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hum.app.data.ai.CategoryIconAi
import com.hum.app.data.model.Category
import com.hum.app.data.model.CategoryEntity
import com.hum.app.data.model.Family
import com.hum.app.data.model.RecurringType
import com.hum.app.data.model.User
import com.hum.app.data.repository.AuthRepository
import com.hum.app.data.repository.CategoryRepository
import com.hum.app.data.repository.ExpenseRepository
import com.hum.app.data.repository.FamilyRepository
import com.hum.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoryIconAi: CategoryIconAi
    private lateinit var authRepository: AuthRepository
    private lateinit var familyRepository: FamilyRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    private val testCategories = listOf(
        CategoryEntity(id = "FOOD", name = "FOOD", label = "Food", icon = "🍕", sortOrder = 0),
        CategoryEntity(id = "TRANSPORT", name = "TRANSPORT", label = "Transport", icon = "🚗", sortOrder = 1),
        CategoryEntity(id = "OTHER", name = "OTHER", label = "Other", icon = "📦", sortOrder = 2)
    )

    private val testUser = User(
        id = "user123",
        displayName = "Test User",
        email = "test@test.com",
        familyId = "family1"
    )

    private val testFamily = Family(
        id = "family1",
        name = "Test Family",
        memberIds = listOf("user123")
    )

    private lateinit var viewModel: AddExpenseViewModel

    @Before
    fun setup() {
        expenseRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        categoryIconAi = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        familyRepository = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        firebaseUser = mockk(relaxed = true)

        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "user123"
        every { firebaseUser.displayName } returns "Test User"

        coEvery { categoryRepository.seedDefaultCategoriesIfEmpty() } returns Unit
        every { categoryRepository.observeCategories() } returns flowOf(testCategories)

        every { authRepository.observeCurrentUser() } returns flowOf(testUser)
        every { familyRepository.observeFamily("family1") } returns flowOf(testFamily)
        every { familyRepository.observeFamilyMembers(listOf("user123")) } returns flowOf(
            listOf(User(id = "user123", displayName = "Test User"))
        )

        viewModel = createViewModel()
    }

    private fun createViewModel() = AddExpenseViewModel(
        expenseRepository = expenseRepository,
        categoryRepository = categoryRepository,
        categoryIconAi = categoryIconAi,
        authRepository = authRepository,
        familyRepository = familyRepository,
        auth = auth
    )

    // ── Initial state ─────────────────────────────────────────────────────

    @Test
    fun `initial state has categories loaded`() {
        assertEquals(testCategories, viewModel.uiState.value.categories)
    }

    @Test
    fun `initial state selects OTHER as default category`() {
        assertEquals("OTHER", viewModel.uiState.value.selectedCategory?.name)
    }

    @Test
    fun `initial state has current user as paidBy`() {
        assertEquals("user123", viewModel.uiState.value.paidByUserId)
        assertEquals("Test User", viewModel.uiState.value.paidByUserName)
    }

    @Test
    fun `initial state has family members loaded`() {
        assertEquals(1, viewModel.uiState.value.familyMembers.size)
        assertEquals("user123", viewModel.uiState.value.familyMembers[0].id)
    }

    @Test
    fun `initial state has empty amount and title`() {
        assertEquals("", viewModel.uiState.value.amount)
        assertEquals("", viewModel.uiState.value.title)
    }

    @Test
    fun `initial state is not saving and not saved`() {
        assertFalse(viewModel.uiState.value.isSaving)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `initial state has no error`() {
        assertNull(viewModel.uiState.value.error)
    }

    // ── updateAmount ──────────────────────────────────────────────────────

    @Test
    fun `updateAmount sets amount in state`() {
        viewModel.updateAmount("100")
        assertEquals("100", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateAmount accepts decimal values`() {
        viewModel.updateAmount("99.50")
        assertEquals("99.50", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateAmount filters alphabetic characters`() {
        viewModel.updateAmount("10abc0")
        assertEquals("100", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateAmount filters special characters`() {
        viewModel.updateAmount("1@0#0")
        assertEquals("100", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateAmount prevents multiple decimal points`() {
        viewModel.updateAmount("1.2.3")
        assertEquals("1.23", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateAmount allows leading dot`() {
        viewModel.updateAmount(".50")
        assertEquals(".50", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateAmount handles empty string`() {
        viewModel.updateAmount("100")
        viewModel.updateAmount("")
        assertEquals("", viewModel.uiState.value.amount)
    }

    // ── updateTitle ───────────────────────────────────────────────────────

    @Test
    fun `updateTitle sets title in state`() {
        viewModel.updateTitle("Electricity Bill")
        assertEquals("Electricity Bill", viewModel.uiState.value.title)
    }

    // ── updateCategory ────────────────────────────────────────────────────

    @Test
    fun `updateCategory sets selected category`() {
        val food = testCategories[0]
        viewModel.updateCategory(food)
        assertEquals(food, viewModel.uiState.value.selectedCategory)
    }

    // ── toggleRecurring ───────────────────────────────────────────────────

    @Test
    fun `toggleRecurring flips isRecurring from false to true`() {
        assertFalse(viewModel.uiState.value.isRecurring)
        viewModel.toggleRecurring()
        assertTrue(viewModel.uiState.value.isRecurring)
    }

    @Test
    fun `toggleRecurring flips isRecurring from true to false`() {
        viewModel.toggleRecurring()
        assertTrue(viewModel.uiState.value.isRecurring)
        viewModel.toggleRecurring()
        assertFalse(viewModel.uiState.value.isRecurring)
    }

    // ── updateRecurringType ───────────────────────────────────────────────

    @Test
    fun `updateRecurringType changes recurring type`() {
        viewModel.updateRecurringType(RecurringType.YEARLY)
        assertEquals(RecurringType.YEARLY, viewModel.uiState.value.recurringType)
    }

    // ── updateRecurringDay ────────────────────────────────────────────────

    @Test
    fun `updateRecurringDay sets day within bounds`() {
        viewModel.updateRecurringDay(15)
        assertEquals(15, viewModel.uiState.value.recurringDay)
    }

    @Test
    fun `updateRecurringDay clamps to minimum 1`() {
        viewModel.updateRecurringDay(0)
        assertEquals(1, viewModel.uiState.value.recurringDay)
    }

    @Test
    fun `updateRecurringDay clamps to maximum 31`() {
        viewModel.updateRecurringDay(50)
        assertEquals(31, viewModel.uiState.value.recurringDay)
    }

    @Test
    fun `updateRecurringDay clamps negative to 1`() {
        viewModel.updateRecurringDay(-5)
        assertEquals(1, viewModel.uiState.value.recurringDay)
    }

    // ── updateNotes ───────────────────────────────────────────────────────

    @Test
    fun `updateNotes sets notes in state`() {
        viewModel.updateNotes("Monthly payment")
        assertEquals("Monthly payment", viewModel.uiState.value.notes)
    }

    // ── showAddCategoryDialog / hideAddCategoryDialog ─────────────────────

    @Test
    fun `showAddCategoryDialog opens dialog and resets name`() {
        viewModel.showAddCategoryDialog()
        assertTrue(viewModel.uiState.value.showAddCategoryDialog)
        assertEquals("", viewModel.uiState.value.newCategoryName)
        assertFalse(viewModel.uiState.value.isResolvingIcon)
    }

    @Test
    fun `hideAddCategoryDialog closes dialog and clears name`() {
        viewModel.showAddCategoryDialog()
        viewModel.updateNewCategoryName("Test")
        viewModel.hideAddCategoryDialog()
        assertFalse(viewModel.uiState.value.showAddCategoryDialog)
        assertEquals("", viewModel.uiState.value.newCategoryName)
    }

    // ── updateNewCategoryName ─────────────────────────────────────────────

    @Test
    fun `updateNewCategoryName sets name in state`() {
        viewModel.updateNewCategoryName("Pet Care")
        assertEquals("Pet Care", viewModel.uiState.value.newCategoryName)
    }

    // ── addCustomCategory ─────────────────────────────────────────────────

    @Test
    fun `addCustomCategory with blank name sets error`() {
        viewModel.updateNewCategoryName("   ")
        viewModel.addCustomCategory()
        assertEquals("Enter a category name", viewModel.uiState.value.error)
    }

    @Test
    fun `addCustomCategory with existing label selects that category`() {
        viewModel.updateNewCategoryName("Food")
        viewModel.addCustomCategory()
        assertEquals("FOOD", viewModel.uiState.value.selectedCategory?.name)
        assertFalse(viewModel.uiState.value.showAddCategoryDialog)
    }

    @Test
    fun `addCustomCategory with existing name by normalization selects that category`() {
        viewModel.updateNewCategoryName("other")
        viewModel.addCustomCategory()
        assertEquals("OTHER", viewModel.uiState.value.selectedCategory?.name)
    }

    @Test
    fun `addCustomCategory with new name calls AI and repository`() = runTest {
        val newCategory = CategoryEntity(
            id = "PET_CARE", name = "PET_CARE", label = "Pet Care", icon = "🐾", sortOrder = 3
        )
        coEvery { categoryIconAi.suggestIcon("Pet Care") } returns "🐾"
        coEvery {
            categoryRepository.addCategory(name = "Pet Care", label = "Pet Care", icon = "🐾")
        } returns Result.success(newCategory)

        viewModel.showAddCategoryDialog()
        viewModel.updateNewCategoryName("Pet Care")
        viewModel.addCustomCategory()
        advanceUntilIdle()

        coVerify { categoryIconAi.suggestIcon("Pet Care") }
        coVerify { categoryRepository.addCategory(name = "Pet Care", label = "Pet Care", icon = "🐾") }
        assertEquals(newCategory, viewModel.uiState.value.selectedCategory)
        assertFalse(viewModel.uiState.value.showAddCategoryDialog)
        assertFalse(viewModel.uiState.value.isResolvingIcon)
    }

    @Test
    fun `addCustomCategory shows error on repository failure`() = runTest {
        coEvery { categoryIconAi.suggestIcon(any()) } returns "📦"
        coEvery {
            categoryRepository.addCategory(any(), any(), any())
        } returns Result.failure(RuntimeException("Firestore error"))

        viewModel.showAddCategoryDialog()
        viewModel.updateNewCategoryName("New Category")
        viewModel.addCustomCategory()
        advanceUntilIdle()

        assertEquals("Firestore error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isResolvingIcon)
    }

    // ── saveExpense ───────────────────────────────────────────────────────

    @Test
    fun `saveExpense with empty amount shows error`() {
        viewModel.updateTitle("Test Expense")
        viewModel.saveExpense()
        assertEquals("Enter a valid amount", viewModel.uiState.value.error)
    }

    @Test
    fun `saveExpense with zero amount shows error`() {
        viewModel.updateAmount("0")
        viewModel.updateTitle("Test Expense")
        viewModel.saveExpense()
        assertEquals("Enter a valid amount", viewModel.uiState.value.error)
    }

    @Test
    fun `saveExpense with non-numeric amount shows error`() {
        viewModel.updateAmount("abc")
        viewModel.updateTitle("Test Expense")
        viewModel.saveExpense()
        assertEquals("Enter a valid amount", viewModel.uiState.value.error)
    }

    @Test
    fun `saveExpense with blank title shows error`() {
        viewModel.updateAmount("100")
        viewModel.saveExpense()
        assertEquals("Enter a title", viewModel.uiState.value.error)
    }

    @Test
    fun `saveExpense with no category shows error`() = runTest {
        every { categoryRepository.observeCategories() } returns flowOf(emptyList())
        val vm = createViewModel()
        vm.updateAmount("100")
        vm.updateTitle("Test")
        vm.saveExpense()
        advanceUntilIdle()
        assertEquals("Select a category", vm.uiState.value.error)
    }

    @Test
    fun `saveExpense with valid data saves successfully`() = runTest {
        coEvery { expenseRepository.addExpense(any()) } returns Result.success("expense123")
        coEvery { categoryRepository.ensureCategoryExists(any(), any(), any()) } returns Unit

        viewModel.updateAmount("250.50")
        viewModel.updateTitle("Electricity Bill")
        viewModel.updateCategory(testCategories[0])
        viewModel.saveExpense()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isSaving)
        coVerify { categoryRepository.ensureCategoryExists("FOOD", "Food", "🍕") }
        coVerify { expenseRepository.addExpense(any()) }
    }

    @Test
    fun `saveExpense shows error on repository failure`() = runTest {
        coEvery { expenseRepository.addExpense(any()) } returns Result.failure(
            RuntimeException("Network error")
        )
        coEvery { categoryRepository.ensureCategoryExists(any(), any(), any()) } returns Unit

        viewModel.updateAmount("100")
        viewModel.updateTitle("Test Expense")
        viewModel.saveExpense()
        advanceUntilIdle()

        assertEquals("Network error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveExpense with no family shows error`() = runTest {
        every { authRepository.observeCurrentUser() } returns flowOf(
            User(id = "user123", displayName = "Test", familyId = null)
        )
        val vm = createViewModel()
        vm.updateAmount("100")
        vm.updateTitle("Test Expense")
        vm.saveExpense()
        advanceUntilIdle()
        assertEquals("No family found", vm.uiState.value.error)
    }

    // ── resetForm ─────────────────────────────────────────────────────────

    @Test
    fun `resetForm clears amount and title`() {
        viewModel.updateAmount("100")
        viewModel.updateTitle("Test")
        viewModel.resetForm()
        assertEquals("", viewModel.uiState.value.amount)
        assertEquals("", viewModel.uiState.value.title)
    }

    @Test
    fun `resetForm resets selected category to OTHER`() {
        viewModel.updateCategory(testCategories[0])
        viewModel.resetForm()
        assertEquals("OTHER", viewModel.uiState.value.selectedCategory?.name)
    }

    @Test
    fun `resetForm clears dialog state`() {
        viewModel.showAddCategoryDialog()
        viewModel.updateNewCategoryName("Something")
        viewModel.resetForm()
        assertFalse(viewModel.uiState.value.showAddCategoryDialog)
        assertEquals("", viewModel.uiState.value.newCategoryName)
        assertFalse(viewModel.uiState.value.isResolvingIcon)
    }

    @Test
    fun `resetForm clears recurring settings`() {
        viewModel.toggleRecurring()
        viewModel.updateRecurringType(RecurringType.YEARLY)
        viewModel.updateRecurringDay(15)
        viewModel.resetForm()
        assertFalse(viewModel.uiState.value.isRecurring)
        assertEquals(RecurringType.MONTHLY, viewModel.uiState.value.recurringType)
        assertEquals(1, viewModel.uiState.value.recurringDay)
    }

    @Test
    fun `resetForm clears notes`() {
        viewModel.updateNotes("Some notes")
        viewModel.resetForm()
        assertEquals("", viewModel.uiState.value.notes)
    }

    @Test
    fun `resetForm clears error`() {
        viewModel.saveExpense()
        assertNotNull(viewModel.uiState.value.error)
        viewModel.resetForm()
        assertNull(viewModel.uiState.value.error)
    }

    // ── clearError ────────────────────────────────────────────────────────

    @Test
    fun `clearError clears error state`() {
        viewModel.saveExpense()
        assertNotNull(viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    // ── updatePaidBy ──────────────────────────────────────────────────────

    @Test
    fun `updatePaidBy updates paidBy fields`() {
        val member = User(id = "user456", displayName = "Jane Doe")
        viewModel.updatePaidBy(member)
        assertEquals("user456", viewModel.uiState.value.paidByUserId)
        assertEquals("Jane Doe", viewModel.uiState.value.paidByUserName)
    }

    // ── updateDate ────────────────────────────────────────────────────────

    @Test
    fun `updateDate updates date in state`() {
        val newDate = java.util.Date(1000000000000L)
        viewModel.updateDate(newDate)
        assertEquals(newDate, viewModel.uiState.value.date)
    }
}
