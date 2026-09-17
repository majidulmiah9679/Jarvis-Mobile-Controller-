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
                AiEngineModelInfo("gemini-2.0-flash", "Gemini 2.0 Flash", "হাই স্পিড জেনারেশন, রিয়েল-টাইম বাংলা ও ইংরেজি রেসপন্স", "HIGH SPEED ⚡", "Fast")
            )
        ),
        AiEngineInfo(
            id = "GEMINI",
            name = "Google Gemini (API Key)",
            brandTag = "OFFICIAL GOOGLE ⭐",
            description = "গুগলের অফিশিয়াল জেমিনি এআই। নতুন AQ... ও লেগ্যাসি AIzaSy... উভয় এপিআই কী সাপোর্ট করে।",
            officialKeyUrl = "https://aistudio.google.com/app/apikey",
            defaultModel = "gemini-2.5-flash",
            placeholderKey = "AQ... or AIzaSy...",
            models = listOf(
                AiEngineModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", "Google-এর লেটেস্ট ফ্ল্যাগশিপ ফ্রি মডেল (সুপার-ফাস্ট ও স্মার্ট)", "RECOMMENDED ⭐", "Ultra-Fast"),
                AiEngineModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", "ডিপ রিজনিং, জটিল কোডিং ও থিংকিং ফ্রি টায়ার", "REASONING 🧠", "Deep Logic"),
                AiEngineModelInfo("gemini-2.0-flash", "Gemini 2.0 Flash", "হাই স্পিড জেনারেশন, রিয়েল-টাইম বাংলা ও ইংরেজি রেসপন্স", "HIGH SPEED ⚡", "Fast"),
                AiEngineModelInfo("gemini-flash-latest", "Gemini Flash (Latest)", "গুগল ক্লাউডের অটোমেটিক লেটেস্ট প্রোডাকশন ফ্ল্যাশ", "DYNAMIC 🔄", "Auto"),
                AiEngineModelInfo("gemini-pro-latest", "Gemini Pro (Latest)", "গুগল ক্লাউডের অটোমেটিক লেটেস্ট প্রোডাকশন প্রো", "PRO INTEL 💎", "Max")
            )
        ),
        AiEngineInfo(
            id = "OPENAI",
            name = "OpenAI ChatGPT (Latest)",
            brandTag = "GPT-4o & o1 ⭐",
            description = "বিশ্ববিখ্যাত OpenAI ChatGPT ফ্ল্যাগশিপ মডেল। GPT-4o, GPT-4o-mini ও o1 reasoning সাপোর্ট।",
            officialKeyUrl = "https://platform.openai.com/api-keys",
            defaultModel = "gpt-4o-mini",
            placeholderKey = "sk-proj-...",
            models = listOf(
                AiEngineModelInfo("gpt-4o-mini", "GPT-4o Mini", "ক্ষিপ্র গতি ও সাশ্রয়ী ফ্ল্যাগশিপ মাল্টিমোডাল মডেল", "FAST & SMART ⚡", "Instant"),
                AiEngineModelInfo("gpt-4o", "GPT-4o Omni", "OpenAI-এর পূর্ণাঙ্গ ফ্ল্যাগশিপ ইন্টেলিজেন্স ও নিখুঁত বাংলা", "FLAGSHIP 💎", "Balanced"),
                AiEngineModelInfo("o1-preview", "OpenAI o1 Reasoning", "উন্নত স্টেপ-বাই-স্টেপ থিংকিং ও জটিল সমস্যা সমাধান", "O1 THINKING 🧠", "Max Logic")
            )
        ),
        AiEngineInfo(
            id = "ANTHROPIC",
            name = "Anthropic Claude",
            brandTag = "CLAUDE 3.5 🧠",
            description = "বিশ্বের অন্যতম সেরা কোডিং ও লিটারেচার এআই। ক্লদ ৩.৫ সনেট ও হাইকু মডেল সাপোর্ট।",
            officialKeyUrl = "https://console.anthropic.com/settings/keys",
            defaultModel = "claude-3-5-sonnet-20241022",
            placeholderKey = "sk-ant-...",
            models = listOf(
                AiEngineModelInfo("claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet", "অসাধারণ বুদ্ধিমত্তা, কোডিং ও প্রাকৃতিক কথোপকথন", "TOP RATED ⭐", "Smart"),
                AiEngineModelInfo("claude-3-haiku-20240307", "Claude 3 Haiku", "ক্ষিপ্র গতির লাইটওয়েট রেসপন্স ও ইনস্ট্যান্ট ইন্টারঅ্যাকশন", "FAST SPEED ⚡", "Fast")
            )
        ),
        AiEngineInfo(
            id = "GROQ",
            name = "Groq Cloud (LPU)",
            brandTag = "WORLD'S FASTEST ⚡",
            description = "বিশ্বের দ্রুততম LPU চিপ স্পিড (৮০০+ টোকেন/সেকেন্ড)। ১০০% ফ্রি এপিআই কী ও LLaMA 3.3, Gemma 2, Mistral মডেল।",
            officialKeyUrl = "https://console.groq.com/keys",
            defaultModel = "llama-3.3-70b-versatile",
            placeholderKey = "gsk_...",
            models = listOf(
                AiEngineModelInfo("llama-3.3-70b-versatile", "LLaMA 3.3 70B Versatile", "মেটার সেরা ওপেন সোর্স মডেল, ক্ষিপ্র গতি ও নির্ভুল বাংলা", "POPULAR 🔥", "800 T/s"),
                AiEngineModelInfo("llama-3.1-8b-instant", "LLaMA 3.1 8B Instant", "ইনস্ট্যান্ট মিলিসেকেন্ড রেসপন্স, ভয়েস অ্যাসিস্ট্যান্টের জন্য সেরা", "ULTRA SPEED ⚡", "1200 T/s"),
                AiEngineModelInfo("gemma2-9b-it", "Google Gemma 2 9B", "গুগলের ওপেন-ওয়েট লাইটওয়েট ইন্টেলিজেন্ট মডেল", "GOOGLE OPEN 🌐", "700 T/s"),
                AiEngineModelInfo("mixtral-8x7b-32768", "Mixtral 8x7B (MoE)", "মিক্সচার অব এক্সপার্টস আর্কিটেকচার ও ৩২k কনটেক্সট", "MOE POWER 🧠", "600 T/s")
            )
        ),
        AiEngineInfo(
            id = "OPENROUTER",
            name = "OpenRouter",
            brandTag = "100+ FREE MODELS 🌐",
            description = "একটি মাত্র ফ্রি এপিআই কী দিয়ে ১০০+ ফ্রি এআই মডেল (ChatGPT, Claude, DeepSeek, Llama, Qwen, Gemini) চালানো যায়।",
            officialKeyUrl = "https://openrouter.ai/keys",
            defaultModel = "meta-llama/llama-3.3-70b-instruct:free",
            placeholderKey = "sk-or-v1-...",
            models = listOf(
                AiEngineModelInfo("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B (Free)", "মেটার পাওয়ারফুল ৭০ বিলিয়ন প্যারামিটার মডেল সম্পূর্ণ ফ্রি", "TOP FREE ⭐", "Free Tier"),
                AiEngineModelInfo("google/gemini-2.0-flash-exp:free", "Gemini 2.0 Flash (Free)", "ওপেনরাউটারের মাধ্যমে সরাসরি গুগলের লেটেস্ট ফ্ল্যাশ", "GOOGLE FREE 🟢", "Fast"),
                AiEngineModelInfo("deepseek/deepseek-chat:free", "DeepSeek V3 (Free)", "বিশ্বখ্যাত ডিপসিক ভি৩ রিজনিং মডেল ওপেনরাউটারে ফ্রি", "SMART FREE 💡", "High Intel"),
                AiEngineModelInfo("qwen/qwen-2.5-72b-instruct:free", "Qwen 2.5 72B (Free)", "আলিবাবার উন্নত কোডিং ও বহুভাষিক ফ্ল্যাগশিপ ফ্রি মডেল", "CODING 💻", "Pro")
            )
        ),
        AiEngineInfo(
            id = "DEEPSEEK",
            name = "DeepSeek AI",
            brandTag = "REASONING & CODE 🧠",
            description = "চীন ও গ্লোবাল সেনসেশন ডিপসিক - দুর্দান্ত লজিক, ম্যাথ ও বাংলা ভাষা বোঝার ক্ষমতা। ফ্রি ট্রায়াল ব্যালেন্স সহ এপিআই।",
            officialKeyUrl = "https://platform.deepseek.com/api_keys",
            defaultModel = "deepseek-chat",
            placeholderKey = "sk-...",
            models = listOf(
                AiEngineModelInfo("deepseek-chat", "DeepSeek Chat (V3)", "ডিপসিকের নতুন ভি৩ মডেল - অত্যন্ত নির্ভুল, বাংলা ও ইংরেজি উভয়তেই সাবলীল", "FLAGSHIP ⭐", "Fast"),
                AiEngineModelInfo("deepseek-reasoner", "DeepSeek Reasoner (R1)", "অ্যাডভান্সড চেইন-অব-থট ও ডিপ লজিক্যাল রিজনিং মডেল", "R1 THINKING 🧠", "Deep Logic")
            )
        ),
        AiEngineInfo(
            id = "HUGGINGFACE",
            name = "Hugging Face Inference",
            brandTag = "OPEN SOURCE HUB 🤗",
            description = "হাগিংফেস সার্ভারলেস ইনফ্যারেন্স এপিআই - সম্পূর্ণ ফ্রি ইউজার টোকেন দিয়ে হাজারো ওপেন সোর্স মডেলের অ্যাক্সেস।",
            officialKeyUrl = "https://huggingface.co/settings/tokens",
            defaultModel = "mistralai/Mistral-7B-Instruct-v0.3",
            placeholderKey = "hf_...",
            models = listOf(
                AiEngineModelInfo("mistralai/Mistral-7B-Instruct-v0.3", "Mistral 7B Instruct v0.3", "মিস্ট্রাল এআই-এর জনপ্রিয় হালকা ও ক্ষিপ্র মডেল", "LIGHT & FAST ⚡", "Serverless"),
                AiEngineModelInfo("meta-llama/Meta-Llama-3-8B-Instruct", "Meta Llama 3 8B", "হাগিংফেসে মেটার ওপেন সোর্স মডেলের ফ্রি হোস্ট", "OPEN WEIGHT 🌐", "Reliable")
            )
        )
    )

    fun getEngineById(id: String): AiEngineInfo {
        return ENGINES.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ENGINES.first()
    }
}
