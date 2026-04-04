package com.hum.app.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryIconAi @Inject constructor() {

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-2.5-flash-lite",
                generationConfig = generationConfig {
                    maxOutputTokens = 10
                    temperature = 0.1f
                }
            )
    }

    suspend fun suggestIcon(categoryName: String): String {
        matchLocal(categoryName)?.let { return it }

        return try {
            val prompt = buildString {
                append("You are an emoji expert. ")
                append("Given the expense category name \"$categoryName\", ")
                append("reply with the single most fitting emoji that visually represents this category. ")
                append("Rules: Reply with ONLY one emoji. No text, no spaces, no punctuation.")
            }
            val response = model.generateContent(prompt)
            val emoji = response.text?.trim().orEmpty()
            if (emoji.isNotEmpty() && emoji.length <= 8) emoji else FALLBACK_ICON
        } catch (e: Exception) {
            Log.w(TAG, "AI unavailable for '$categoryName', using local match", e)
            FALLBACK_ICON
        }
    }

    internal fun matchLocal(name: String): String? {
        val lower = name.lowercase()
        var bestEmoji: String? = null
        var bestLength = 0
        for ((keywords, emoji) in KEYWORD_MAP) {
            for (keyword in keywords) {
                if (keyword in lower && keyword.length > bestLength) {
                    bestEmoji = emoji
                    bestLength = keyword.length
                }
            }
        }
        return bestEmoji
    }

    companion object {
        private const val TAG = "CategoryIconAi"
        const val FALLBACK_ICON = "📦"

        private val KEYWORD_MAP: Map<List<String>, String> = mapOf(
            listOf("grocer", "vegetable", "fruit", "supermarket", "kirana") to "🛒",
            listOf("food", "meal", "lunch", "dinner", "breakfast", "tiffin", "snack", "restaurant", "dine", "eat", "cafe", "canteen", "biryani", "pizza", "burger") to "🍕",
            listOf("milk", "dairy", "curd", "paneer", "cheese") to "🥛",
            listOf("tea", "coffee", "chai", "beverage", "juice", "drink", "smoothie") to "☕",
            listOf("sweet", "dessert", "cake", "ice cream", "chocolate", "mithai") to "🍰",
            listOf("transport", "travel", "cab", "taxi", "uber", "ola", "auto", "rickshaw", "commute", "bus", "metro", "train", "flight", "airfare") to "🚗",
            listOf("ride", "bike", "scooter", "rapido", "fuel", "petrol", "diesel", "gas", "cng") to "⛽",
            listOf("parking", "toll") to "🅿️",
            listOf("electric", "utility", "power", "bijli", "light bill") to "💡",
            listOf("water", "tanker", "borewell") to "💧",
            listOf("gas cylinder", "lpg", "cooking gas", "pipeline gas") to "🔥",
            listOf("internet", "wifi", "broadband", "fiber", "jiofiber") to "🌐",
            listOf("phone", "mobile", "recharge", "airtel", "jio", "vi ", "bsnl", "telecom", "sim") to "📱",
            listOf("rent", "house rent", "flat rent", "pg ", "hostel") to "🏠",
            listOf("emi ", "loan", "mortgage", "installment") to "🏦",
            listOf("maintenance", "society", "apartment") to "🏢",
            listOf("medical", "health", "doctor", "hospital", "clinic", "medicine", "pharma", "tablet", "prescription") to "🏥",
            listOf("gym", "fitness", "workout", "exercise", "yoga", "sports", "swim") to "🏋️",
            listOf("salon", "haircut", "parlour", "parlor", "spa", "beauty", "grooming", "barber") to "💇",
            listOf("cloth", "apparel", "fashion", "dress", "shirt", "jeans", "wear", "garment") to "👕",
            listOf("shoe", "footwear", "sandal", "sneaker", "chappal") to "👟",
            listOf("jewel", "gold", "silver", "ornament") to "💎",
            listOf("shop", "amazon", "flipkart", "online", "purchase", "buy", "ecommerce") to "🛍️",
            listOf("gift", "present", "birthday", "anniversary") to "🎁",
            listOf("movie", "film", "cinema", "theatre", "theater", "netflix", "ott", "streaming", "hotstar", "prime video") to "🎬",
            listOf("music", "concert", "spotify", "gaana") to "🎵",
            listOf("game", "gaming", "playstation", "xbox") to "🎮",
            listOf("book", "stationery", "notebook", "study", "course", "tuition", "class", "coaching", "school", "college", "education", "fee") to "📚",
            listOf("toy", "kid", "child", "baby", "diaper", "nappy") to "🧸",
            listOf("pet", "dog", "cat", "vet", "animal") to "🐾",
            listOf("garden", "plant", "flower", "nursery") to "🌱",
            listOf("insurance", "lic ", "premium", "policy") to "🛡️",
            listOf("tax", "income tax", "gst") to "📋",
            listOf("invest", "mutual fund", "sip ", "stock", "share") to "📈",
            listOf("donation", "charity", "temple", "church", "mosque", "tithe", "zakat") to "🙏",
            listOf("vacation", "holiday", "trip", "tour", "hotel", "resort", "outing", "picnic") to "✈️",
            listOf("laundry", "dry clean", "wash", "iron") to "👔",
            listOf("repair", "plumber", "electrician", "mechanic", "service", "fix", "carpenter") to "🔧",
            listOf("furniture", "sofa", "table", "chair", "bed", "mattress", "cupboard", "almirah") to "🪑",
            listOf("electronic", "laptop", "computer", "tv ", "television", "appliance", "fridge", "ac ", "cooler", "fan") to "💻",
            listOf("subscription", "membership", "renewal") to "🔄",
            listOf("cigarette", "smoke", "tobacco", "alcohol", "beer", "wine", "liquor", "bar") to "🚬",
            listOf("newspaper", "magazine", "paper") to "📰",
            listOf("courier", "delivery", "shipping", "postage") to "📦",
            listOf("wedding", "marriage", "shaadi", "function", "event", "party", "celebration") to "💒",
            listOf("office", "work", "cowork", "stationery") to "💼",
            listOf("cleaning", "maid", "servant", "domestic", "helper", "bai") to "🧹",
        )
    }
}
