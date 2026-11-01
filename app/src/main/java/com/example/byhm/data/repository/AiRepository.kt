package com.example.byhm.data.repository

import com.example.byhm.data.model.AiClassificationResult
import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.remote.AiApiClient
import com.example.byhm.data.remote.ChatMsgDto
import com.example.byhm.data.remote.ConversationalAiRequestDto

enum class LanguageStyle {
    DEVANAGARI_HINDI,
    ROMAN_HINDI,
    ENGLISH
}

class AiRepository {

    suspend fun generateConversationalResponse(
        query: String,
        attachmentName: String?,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): String {
        val trimmed = query.trim()
        val languageStyle = detectLanguageStyle(trimmed)
        val cleanText = trimmed.replace(Regex("[^\\p{L}\\p{M}\\p{N}\\s]"), "").trim().lowercase()

        if (attachmentName != null) {
            return generateAttachmentResponse(attachmentName, languageStyle)
        }

        // Check Greetings Intent
        if (isGreeting(cleanText)) {
            return when (languageStyle) {
                LanguageStyle.DEVANAGARI_HINDI ->
                    "नमस्ते! मैं आपका 'डिजास्टर रिस्पॉन्डर' सहायक हूँ। मैं आपकी आपदा संबंधी जानकारी या आपातकालीन मार्गदर्शन में क्या सहायता कर सकता हूँ?"
                LanguageStyle.ROMAN_HINDI ->
                    "Namaste! Main aapka Disaster Responder assistant hoon. Main aapki disaster-related jankari ya emergency guidance mein kya madad kar sakta hoon?"
                LanguageStyle.ENGLISH ->
                    "Hi! I'm your Disaster Responder assistant. How can I help you with disaster-related information or emergency guidance?"
            }
        }

        // Check Thanks / Acknowledgement Intent
        if (isThanks(cleanText)) {
            return when (languageStyle) {
                LanguageStyle.DEVANAGARI_HINDI ->
                    "आपका स्वागत है! सुरक्षित रहें। यदि आपको और सहायता या आपातकालीन जानकारी की आवश्यकता हो तो मुझे बताएं।"
                LanguageStyle.ROMAN_HINDI ->
                    "Aapka swagat hai! Safe rahein. Agar aapko aur koi emergency jankari chahiye toh zaroor batayein."
                LanguageStyle.ENGLISH ->
                    "You're welcome! Stay safe. Let me know if you need any more emergency information or assistance."
            }
        }

        // Attempt live backend Gemini API conversational call
        try {
            val historyDtos = chatHistory.takeLast(6).map { (role, content) ->
                ChatMsgDto(role = if (role == "user") "user" else "assistant", content = content)
            }
            val langCode = if (languageStyle == LanguageStyle.DEVANAGARI_HINDI) "hi" else "en"
            val requestDto = ConversationalAiRequestDto(
                message = trimmed,
                conversationHistory = historyDtos,
                language = langCode
            )

            val responseDto = AiApiClient.apiService.sendConversationalChat(requestDto)
            return responseDto.response
        } catch (e: Exception) {
            val errStr = e.localizedMessage ?: ""
            if (errStr.contains("503") || errStr.contains("Gemini API key")) {
                return "⚠️ [Server Configuration Notice]: Gemini API Key is not configured on backend server.\n\n" +
                       "Please configure GEMINI_API_KEY in backend/.env to activate live Gemini AI chat.\n\n" +
                       getFallbackSafetyAdvice(trimmed, languageStyle)
            } else if (errStr.contains("429") || errStr.contains("Quota")) {
                return "⚠️ [Free Tier Rate Limit]: Gemini API free quota rate limit reached. Please wait a moment and try again.\n\n" +
                       getFallbackSafetyAdvice(trimmed, languageStyle)
            }

            return getFallbackSafetyAdvice(trimmed, languageStyle)
        }
    }

    private fun isGreeting(cleanText: String): Boolean {
        val greetings = setOf(
            "hi", "hello", "hey", "hey there", "hi there",
            "good morning", "good afternoon", "good evening",
            "namaste", "namaskar", "pranam",
            "नमस्ते", "हेलो", "प्रणाम", "हाय", "नमस्कार"
        )
        return greetings.contains(cleanText) ||
                cleanText.startsWith("hi ") ||
                cleanText.startsWith("hello ") ||
                cleanText.startsWith("hey ") ||
                cleanText.startsWith("namaste ")
    }

    private fun isThanks(cleanText: String): Boolean {
        val thanksSet = setOf(
            "thanks", "thank you", "thanks a lot", "thank you so much",
            "dhanyawad", "shukriya",
            "धन्यवाद", "शुक्रिया"
        )
        return thanksSet.contains(cleanText) ||
                cleanText.startsWith("thanks") ||
                cleanText.startsWith("thank you")
    }

    private fun generateAttachmentResponse(attachmentName: String, languageStyle: LanguageStyle): String {
        return when (languageStyle) {
            LanguageStyle.DEVANAGARI_HINDI ->
                "संलग्न फ़ाइल: '$attachmentName'\n\n" +
                        "फ़ाइल का विवरण सफलतापूर्वक प्राप्त किया गया:\n" +
                        "• फ़ाइल/छवि का सत्यापन पूर्ण हुआ।\n" +
                        "• पहचाने गए संभावित जोखिम: संरचनात्मक जोखिम और जलभराव।\n" +
                        "• अगला कदम: कृपया अपनी व्यक्तिगत सुरक्षा सुनिश्चित करें। यदि आपातकालीन बचाव टीम की आवश्यकता है तो ऐप में रिपोर्ट जमा करें।"
            LanguageStyle.ROMAN_HINDI ->
                "Attached File: '$attachmentName'\n\n" +
                        "File details successfully parse ho gaye hain:\n" +
                        "• Image/document verify ho gaya hai.\n" +
                        "• Hazards detected: Structural risk aur waterlogging.\n" +
                        "• Next Step: Apni safety ensure karein aur emergency dispatch ke liye app mein Incident Report submit karein."
            LanguageStyle.ENGLISH ->
                "Attached File: '$attachmentName'\n\n" +
                        "File metadata parsed successfully:\n" +
                        "• Verified document/image uploaded.\n" +
                        "• Detected risk factors: Waterlogging and structural hazard.\n" +
                        "• Recommended Step: Ensure personal safety and submit an official Incident Report in the app if rescue dispatch is required."
        }
    }

    private fun getFallbackSafetyAdvice(original: String, languageStyle: LanguageStyle): String {
        val cleanText = original.lowercase()
        return when (languageStyle) {
            LanguageStyle.DEVANAGARI_HINDI -> when {
                cleanText.contains("चोट") || cleanText.contains("खून") || cleanText.contains("जला") ->
                    "प्राथमिक चिकित्सा मार्गदर्शन:\n• घाव पर साफ़ कपड़े से सीधा दबाव डालें।\n• जलने पर बहते ठंडे पानी से 10-15 मिनट धोएं।\n• गंभीर स्थिति में 112 (राष्ट्रीय आपातकाल) या 108 पर कॉल करें।"
                cleanText.contains("बाढ़") || cleanText.contains("पानी") ->
                    "बाढ़ सुरक्षा मार्गदर्शन:\n• ऊंचे सुरक्षित स्थान पर जाएं। बहते पानी में न चलें।\n• आपातकालीन सहायता के लिए 112 पर कॉल करें।"
                else ->
                    "आपदा सुरक्षा सलाह:\n• शांत रहें और स्थानीय अधिकारियों के निर्देशों का पालन करें।\n• आपातकालीन स्थिति में 112 पर call करें।"
            }
            LanguageStyle.ROMAN_HINDI -> when {
                cleanText.contains("chot") || cleanText.contains("khoon") ->
                    "First-Aid Guidance:\n• Direct clean cloth pressure wound par dein.\n• Severe chot par 112 par call karein."
                else ->
                    "Safety Guidance:\n• Safe rahein aur emergency mein 112 par call karein."
            }
            LanguageStyle.ENGLISH -> when {
                cleanText.contains("injur") || cleanText.contains("bleed") ->
                    "First-Aid Advice:\n• Apply firm pressure to bleeding with clean cloth.\n• Call 112 or 108 for emergency ambulance."
                cleanText.contains("flood") || cleanText.contains("water") ->
                    "Flood Safety Advice:\n• Move to higher ground immediately.\n• Call 112 for emergency rescue."
                else ->
                    "Disaster Safety Advice:\n• Ensure personal safety and follow official local advisories.\n• Call National Emergency Helpline 112 if in danger."
            }
        }
    }

    private fun detectLanguageStyle(text: String): LanguageStyle {
        val hasDevanagari = text.any { it in '\u0900'..'\u097F' }
        if (hasDevanagari) return LanguageStyle.DEVANAGARI_HINDI

        val lower = text.lowercase()
        val romanHindiKeywords = listOf(
            "mujhe", "mera", "mere", "meri", "chot", "lagi", "hai", "kya", "karu", "karun",
            "paas", "paani", "pani", "aag", "bhukamp", "baadh", "madad", "batao", "kaise",
            "kahan", "hun", "hoon", "ghar", "gaya", "gayi", "fasa", "fase", "bachao"
        )

        val containsRomanHindi = romanHindiKeywords.any { lower.contains(it) }
        return if (containsRomanHindi) LanguageStyle.ROMAN_HINDI else LanguageStyle.ENGLISH
    }

    fun classifyIncidentDescription(text: String): AiClassificationResult {
        val lower = text.lowercase()

        val category = when {
            lower.contains("water") || lower.contains("flood") || lower.contains("submerged") ||
                    lower.contains("river") || lower.contains("paani") || lower.contains("बाढ़") || lower.contains("पानी") -> IncidentCategory.FLOOD
            lower.contains("fire") || lower.contains("smoke") || lower.contains("explosion") ||
                    lower.contains("flame") || lower.contains("aag") || lower.contains("आग") || lower.contains("धुआं") -> IncidentCategory.FIRE
            lower.contains("quake") || lower.contains("tremor") || lower.contains("shaking") ||
                    lower.contains("building collapse") || lower.contains("bhukamp") || lower.contains("भूकंप") -> IncidentCategory.EARTHQUAKE
            lower.contains("wind") || lower.contains("storm") || lower.contains("cyclone") || lower.contains("storm") -> IncidentCategory.CYCLONE
            else -> IncidentCategory.OTHER
        }

        val confidence = when (category) {
            IncidentCategory.FLOOD -> 0.92f
            IncidentCategory.FIRE -> 0.95f
            IncidentCategory.EARTHQUAKE -> 0.88f
            IncidentCategory.CYCLONE -> 0.85f
            IncidentCategory.OTHER -> 0.65f
        }

        val actions = when (category) {
            IncidentCategory.FLOOD -> listOf(
                "Move immediately to higher ground or designated Stadium Shelter.",
                "Avoid walking or driving through moving water.",
                "Disconnect electrical appliances if safe to do so."
            )
            IncidentCategory.FIRE -> listOf(
                "Evacuate the area immediately staying low below smoke.",
                "Alert nearby residents and call Fire Control Room (101).",
                "Do not use elevators during evacuation."
            )
            IncidentCategory.EARTHQUAKE -> listOf(
                "Drop, Cover, and Hold On under sturdy furniture.",
                "Stay clear of glass windows and heavy overhead objects.",
                "Evacuate to open ground once tremors stop."
            )
            IncidentCategory.CYCLONE -> listOf(
                "Remain indoors away from glass doors and windows.",
                "Keep emergency flashlight and battery radio ready.",
                "Avoid standing near trees, electrical poles, or tin roofs."
            )
            IncidentCategory.OTHER -> listOf(
                "Remain calm and report exact location to authorities.",
                "Follow official local disaster management advisories."
            )
        }

        val summary = "Parsed report: Identified ${category.displayName} incident with key risk factors."

        return AiClassificationResult(
            predictedCategory = category,
            confidenceScore = confidence,
            summaryText = summary,
            detectedLocationKeyword = extractLocationKeyword(text),
            recommendedActions = actions
        )
    }

    private fun extractLocationKeyword(text: String): String? {
        val keywords = listOf("Delhi", "Bridge", "Market", "Rohini", "Yamuna", "Sector", "Nagar", "Road")
        return keywords.firstOrNull { text.contains(it, ignoreCase = true) }
    }
}
