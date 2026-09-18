package com.example.ui

data class AiEngineInfo(
    val id: String,
    val name: String,
    val brandTag: String,
    val description: String,
    val officialKeyUrl: String,
    val defaultModel: String,
    val models: List<AiEngineModelInfo>,
    val placeholderKey: String,
    val isCloudAccountAuth: Boolean = false
)

data class AiEngineModelInfo(
    val id: String,
    val displayName: String,
    val description: String,
    val tag: String,
    val speedBadge: String
)

object JarvisAiEnginesRegistry {

    val ENGINES = listOf(
        AiEngineInfo(
            id = "GEMINI_CLOUD",
            name = "Google Gemini (Cloud / Account Integration)",
            brandTag = "RECOMMENDED (NO KEY) ⭐",
            description = "Google Account OAuth ও Firebase AI এর মাধ্যমে সরাসরি কানেকশন। কোনো ম্যানুয়াল API key এর ঝামেলা নেই।",
            officialKeyUrl = "https://aistudio.google.com/app/apikey",
            defaultModel = "gemini-2.5-flash",
            placeholderKey = "Auto OAuth via Google Account",
            isCloudAccountAuth = true,
            models = listOf(
                AiEngineModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", "Google-এর লেটেস্ট ফ্ল্যাগশিপ ফ্রি মডেল (সুপার-ফাস্ট ও স্মার্ট)", "FLAGSHIP 2026 ⭐", "Ultra-Fast"),
                AiEngineModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", "ডিপ লজিক ও অ্যাডভান্সড কোডিং রিজনিং মডেল", "DEEP INTEL 🧠", "Thinking"),
                AiEngineModelInfo("gemini-3.5-flash", "Gemini 3.5 Flash", "নেক্সট-জেন ফ্রন্টিয়ার ফ্ল্যাশ মডেল (অত্যাধুনিক স্পিড ও কোয়ালিটি)", "FRONTIER 🚀", "Next-Gen")
            )
        ),
        AiEngineInfo(
            id = "GEMINI",
            name = "Google Gemini (API Key / OAuth)",
            brandTag = "OFFICIAL GOOGLE ⭐",
            description = "গুগলের অফিশিয়াল জেমিনি এআই। নতুন AQ... ও লেগ্যাসি AIzaSy... এপিআই কী এবং ya29... ওঅথ টোকেন অটো-ডিটেকশন ও অ্যাডাপ্টিভ অথ সাপোর্ট করে।",
            officialKeyUrl = "https://aistudio.google.com/app/apikey",
            defaultModel = "gemini-2.5-flash",
            placeholderKey = "AQ... / AIzaSy... / ya29...",
            models = listOf(
                AiEngineModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", "Google-এর লেটেস্ট ফ্ল্যাগশিপ ফ্রি মডেল (সুপার-ফাস্ট ও স্মার্ট)", "RECOMMENDED ⭐", "Ultra-Fast"),
                AiEngineModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", "ডিপ রিজনিং, জটিল কোডিং ও থিংকিং ফ্রি টায়ার", "REASONING 🧠", "Deep Logic"),
                AiEngineModelInfo("gemini-flash-latest", "Gemini Flash (Latest)", "গুগল ক্লাউডের অটোমেটিক লেটেস্ট প্রোডাকশন ফ্ল্যাশ", "DYNAMIC 🔄", "Auto"),
                AiEngineModelInfo("gemini-3.5-flash", "Gemini 3.5 Flash", "নেক্সট-জেন ফ্রন্টিয়ার ফ্ল্যাশ মডেল", "FRONTIER 🚀", "Next-Gen")
            )
        ),
        AiEngineInfo(
            id = "GROQ",
            name = "Groq Cloud (LPU)",
            brandTag = "WORLD'S FASTEST ⚡",
            description = "বিশ্বের দ্রুততম LPU চিপ স্পিড (৮০০+ টোকেন/সেকেন্ড)। ১০০% ফ্রি এপিআই কী (gsk_...) ও LLaMA 3.3, Gemma 2, DeepSeek R1 মডেল।",
            officialKeyUrl = "https://console.groq.com/keys",
            defaultModel = "llama-3.3-70b-versatile",
            placeholderKey = "gsk_...",
            models = listOf(
                AiEngineModelInfo("llama-3.3-70b-versatile", "LLaMA 3.3 70B Versatile", "মেটার সেরা ওপেন সোর্স মডেল, ক্ষিপ্র গতি ও নির্ভুল বাংলা", "POPULAR 🔥", "800 T/s"),
                AiEngineModelInfo("llama-3.1-8b-instant", "LLaMA 3.1 8B Instant", "ইনস্ট্যান্ট মিলিসেকেন্ড রেসপন্স, ভয়েস অ্যাসিস্ট্যান্টের জন্য সেরা", "ULTRA SPEED ⚡", "1200 T/s"),
                AiEngineModelInfo("deepseek-r1-distill-llama-70b", "DeepSeek R1 Distill 70B", "অ্যাডভান্সড রিজনিং ও ডিপ প্রব্লেম সলভিং আর্কিটেকচার", "REASONING 🧠", "600 T/s"),
                AiEngineModelInfo("gemma2-9b-it", "Google Gemma 2 9B", "গুগলের ওপেন-ওয়েট লাইটওয়েট ইন্টেলিজেন্ট মডেল", "GOOGLE OPEN 🌐", "700 T/s")
            )
        )
    )

    fun getEngineById(id: String): AiEngineInfo {
        return ENGINES.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ENGINES.first()
    }
}
