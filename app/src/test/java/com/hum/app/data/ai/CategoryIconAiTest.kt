package com.hum.app.data.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CategoryIconAiTest {

    private lateinit var ai: CategoryIconAi

    @Before
    fun setup() {
        ai = CategoryIconAi()
    }

    // ── matchLocal: basic keyword lookups ─────────────────────────────────

    @Test
    fun `matchLocal returns grocery emoji for grocery keyword`() {
        assertEquals("🛒", ai.matchLocal("Grocery Shopping"))
    }

    @Test
    fun `matchLocal returns food emoji for restaurant keyword`() {
        assertEquals("🍕", ai.matchLocal("Restaurant dinner"))
    }

    @Test
    fun `matchLocal returns transport emoji for cab keyword`() {
        assertEquals("🚗", ai.matchLocal("Uber Cab"))
    }

    @Test
    fun `matchLocal returns milk emoji for dairy keyword`() {
        assertEquals("🥛", ai.matchLocal("Dairy products"))
    }

    @Test
    fun `matchLocal returns tea emoji for coffee keyword`() {
        assertEquals("☕", ai.matchLocal("Morning Coffee"))
    }

    @Test
    fun `matchLocal returns sweet emoji for cake keyword`() {
        assertEquals("🍰", ai.matchLocal("Chocolate Cake"))
    }

    @Test
    fun `matchLocal returns parking emoji for parking keyword`() {
        assertEquals("🅿️", ai.matchLocal("Parking Fee"))
    }

    @Test
    fun `matchLocal returns utility emoji for electric keyword`() {
        assertEquals("💡", ai.matchLocal("Electric Bill"))
    }

    @Test
    fun `matchLocal returns water emoji for water keyword`() {
        assertEquals("💧", ai.matchLocal("Water Tanker"))
    }

    @Test
    fun `matchLocal returns internet emoji for wifi keyword`() {
        assertEquals("🌐", ai.matchLocal("WiFi Bill"))
    }

    @Test
    fun `matchLocal returns phone emoji for recharge keyword`() {
        assertEquals("📱", ai.matchLocal("Jio Recharge"))
    }

    @Test
    fun `matchLocal returns rent emoji for house rent`() {
        assertEquals("🏠", ai.matchLocal("House Rent"))
    }

    @Test
    fun `matchLocal returns medical emoji for hospital`() {
        assertEquals("🏥", ai.matchLocal("Hospital visit"))
    }

    @Test
    fun `matchLocal returns gym emoji for fitness`() {
        assertEquals("🏋️", ai.matchLocal("Gym Workout"))
    }

    @Test
    fun `matchLocal returns salon emoji for haircut`() {
        assertEquals("💇", ai.matchLocal("Salon Haircut"))
    }

    @Test
    fun `matchLocal returns clothing emoji for dress`() {
        assertEquals("👕", ai.matchLocal("New Dress"))
    }

    @Test
    fun `matchLocal returns shoe emoji for footwear`() {
        assertEquals("👟", ai.matchLocal("Running Shoe"))
    }

    @Test
    fun `matchLocal returns jewel emoji for gold`() {
        assertEquals("💎", ai.matchLocal("Gold Necklace"))
    }

    @Test
    fun `matchLocal returns shopping emoji for amazon`() {
        assertEquals("🛍️", ai.matchLocal("Amazon Purchase"))
    }

    @Test
    fun `matchLocal returns gift emoji for birthday present`() {
        assertEquals("🎁", ai.matchLocal("Birthday Gift"))
    }

    @Test
    fun `matchLocal returns movie emoji for cinema`() {
        assertEquals("🎬", ai.matchLocal("Movie Cinema"))
    }

    @Test
    fun `matchLocal returns music emoji for spotify`() {
        assertEquals("🎵", ai.matchLocal("Spotify Premium"))
    }

    @Test
    fun `matchLocal returns gaming emoji for playstation`() {
        assertEquals("🎮", ai.matchLocal("Playstation Game"))
    }

    @Test
    fun `matchLocal returns book emoji for education`() {
        assertEquals("📚", ai.matchLocal("College Education Fee"))
    }

    @Test
    fun `matchLocal returns toy emoji for baby items`() {
        assertEquals("🧸", ai.matchLocal("Baby Diaper"))
    }

    @Test
    fun `matchLocal returns pet emoji for vet`() {
        assertEquals("🐾", ai.matchLocal("Vet Checkup"))
    }

    @Test
    fun `matchLocal returns garden emoji for plant`() {
        assertEquals("🌱", ai.matchLocal("Garden Plant"))
    }

    @Test
    fun `matchLocal returns insurance emoji for premium`() {
        assertEquals("🛡️", ai.matchLocal("LIC Premium"))
    }

    @Test
    fun `matchLocal returns tax emoji for income tax`() {
        assertEquals("📋", ai.matchLocal("Income Tax"))
    }

    @Test
    fun `matchLocal returns invest emoji for mutual fund`() {
        assertEquals("📈", ai.matchLocal("Mutual Fund SIP"))
    }

    @Test
    fun `matchLocal returns donation emoji for charity`() {
        assertEquals("🙏", ai.matchLocal("Temple Donation"))
    }

    @Test
    fun `matchLocal returns travel emoji for vacation`() {
        assertEquals("✈️", ai.matchLocal("Holiday Vacation"))
    }

    @Test
    fun `matchLocal returns laundry emoji for dry clean`() {
        assertEquals("👔", ai.matchLocal("Dry Clean Laundry"))
    }

    @Test
    fun `matchLocal returns repair emoji for plumber`() {
        assertEquals("🔧", ai.matchLocal("Plumber Repair"))
    }

    @Test
    fun `matchLocal returns furniture emoji for sofa`() {
        assertEquals("🪑", ai.matchLocal("New Sofa"))
    }

    @Test
    fun `matchLocal returns electronics emoji for laptop`() {
        assertEquals("💻", ai.matchLocal("New Laptop"))
    }

    @Test
    fun `matchLocal returns subscription emoji for membership`() {
        assertEquals("🔄", ai.matchLocal("Club Membership"))
    }

    @Test
    fun `matchLocal returns smoke emoji for alcohol`() {
        assertEquals("🚬", ai.matchLocal("Beer at Bar"))
    }

    @Test
    fun `matchLocal returns newspaper emoji for magazine`() {
        assertEquals("📰", ai.matchLocal("Daily Newspaper"))
    }

    @Test
    fun `matchLocal returns courier emoji for delivery`() {
        assertEquals("📦", ai.matchLocal("Courier Delivery"))
    }

    @Test
    fun `matchLocal returns wedding emoji for marriage`() {
        assertEquals("💒", ai.matchLocal("Marriage Ceremony"))
    }

    @Test
    fun `matchLocal returns office emoji for cowork`() {
        assertEquals("💼", ai.matchLocal("Cowork Space"))
    }

    @Test
    fun `matchLocal returns cleaning emoji for maid`() {
        assertEquals("🧹", ai.matchLocal("Maid Salary"))
    }

    // ── matchLocal: edge cases ────────────────────────────────────────────

    @Test
    fun `matchLocal returns null for unknown category`() {
        assertNull(ai.matchLocal("xyzzy random unrelated"))
    }

    @Test
    fun `matchLocal returns null for empty string`() {
        assertNull(ai.matchLocal(""))
    }

    @Test
    fun `matchLocal is case insensitive`() {
        assertEquals("🛒", ai.matchLocal("GROCERY"))
        assertEquals("🛒", ai.matchLocal("grocery"))
        assertEquals("🛒", ai.matchLocal("Grocery"))
    }

    @Test
    fun `matchLocal handles partial keyword match within word`() {
        assertEquals("🛒", ai.matchLocal("Groceries"))
    }

    // ── Bug fix verification: longest keyword match wins ──────────────────

    @Test
    fun `matchLocal returns fire emoji for gas cylinder not fuel emoji`() {
        assertEquals("🔥", ai.matchLocal("Gas Cylinder"))
    }

    @Test
    fun `matchLocal returns fire emoji for cooking gas`() {
        assertEquals("🔥", ai.matchLocal("Cooking Gas"))
    }

    @Test
    fun `matchLocal returns fire emoji for lpg refill`() {
        assertEquals("🔥", ai.matchLocal("LPG Refill"))
    }

    @Test
    fun `matchLocal returns fire emoji for pipeline gas`() {
        assertEquals("🔥", ai.matchLocal("Pipeline Gas Bill"))
    }

    @Test
    fun `matchLocal returns fuel emoji for plain gas`() {
        assertEquals("⛽", ai.matchLocal("Gas"))
    }

    @Test
    fun `matchLocal returns fuel emoji for petrol`() {
        assertEquals("⛽", ai.matchLocal("Petrol Fill"))
    }

    @Test
    fun `matchLocal returns fuel emoji for bike ride`() {
        assertEquals("⛽", ai.matchLocal("Bike Ride"))
    }

    // ── suggestIcon: integration with matchLocal ──────────────────────────

    @Test
    fun `suggestIcon returns local match without calling AI`() = runTest {
        val result = ai.suggestIcon("Grocery Store")
        assertEquals("🛒", result)
    }

    @Test
    fun `suggestIcon returns fallback for unknown category when AI unavailable`() = runTest {
        val result = ai.suggestIcon("xyzzy random unknown")
        assertEquals(CategoryIconAi.FALLBACK_ICON, result)
    }

    @Test
    fun `suggestIcon returns local match for food category`() = runTest {
        assertEquals("🍕", ai.suggestIcon("Pizza Lunch"))
    }

    @Test
    fun `suggestIcon returns local match for transport category`() = runTest {
        assertEquals("🚗", ai.suggestIcon("Ola Cab Fare"))
    }

    @Test
    fun `FALLBACK_ICON is package emoji`() {
        assertEquals("📦", CategoryIconAi.FALLBACK_ICON)
    }
}
