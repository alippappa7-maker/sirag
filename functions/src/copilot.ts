/**
 * سراج — المساعد الإسلامي الذكي (Cloud Function)
 * 
 * يربط المساعد بـ Gemini API مع مصادر إسلامية موثّقة:
 * - Quran.com API v4 (القرآن + التفسير + البحث)
 * - UmmahAPI (الحديث النبوي + البحث)
 * 
 * المعمارية:
 * 1. يستقبل السؤال
 * 2. يبحث في المصادر الإسلامية عن آيات/أحاديث/تفسير ذات صلة
 * 3. يرسل للنموذج Gemini مع تعليمات صارمة: "أجب فقط من المصادر المرفقة"
 * 4. يرجع JSON منظم: answer + sources + confidence + followUpQuestions
 */

import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import { logger } from "firebase-functions";

// Gemini API
import { GoogleGenerativeAI } from "@google/generative-ai";

// ─── أنواع ───

interface CopilotRequest {
  question: string;
  language?: string;
  includeQuran?: boolean;
  includeHadith?: boolean;
  includeTafsir?: boolean;
}

interface IslamicSource {
  type: "quran" | "hadith" | "tafsir" | "dua" | "fiqh";
  title: string;
  reference: string;
  excerpt: string;
  url?: string;
}

interface CopilotResponse {
  answer: string;
  sources: IslamicSource[];
  confidence: number;
  followUpQuestions: string[];
  disclaimer: string;
}

// ─── إعدادات Gemini ───

const GEMINI_API_KEY = defineSecret("GEMINI_API_KEY");
const GEMINI_MODEL = "gemini-2.0-flash";

const SYSTEM_PROMPT = `أنت مساعد إسلامي معرفي موثّق. مهمتك:

1. أجب فقط من المصادر المرفقة في السياق. لا تخترع آيات أو أحاديث.
2. إذا لم تكفِ المصادر للإجابة، قل: "لا أملك مصادر كافية للإجابة على هذا السؤال."
3. اذكر المصدر مع كل معلومة (الآية، الحديث، التفسير).
4. لا تقدم فتاوى. للأسئلة الفقهية الحساسة، وجّه المستخدم لأهل العلم.
5. أجب بالعربية الفصحى المبسّطة.
6. كن موجزاً وواضحاً.

صيغة الرد JSON:
{
  "answer": "الإجابة مع ذكر المصادر inline",
  "sources": [
    { "type": "quran|hadith|tafsir|dua|fiqh", "title": "...", "reference": "...", "excerpt": "..." }
  ],
  "confidence": 0.0-1.0,
  "followUpQuestions": ["سؤال 1", "سؤال 2", "سؤال 3"]
}`;

// ─── Quran.com API ───

const QURAN_API_BASE = "https://api.quran.com/api/v4";

interface QuranVerse {
  verse_key: string;
  text_uthmani: string;
  translations?: { text: string; resource_name: string }[];
}

interface QuranSearchResult {
  verse_key: string;
  text: string;
  highlighted: string;
  translations: { text: string; resource_name: string }[];
}

async function searchQuran(query: string, language: string): Promise<IslamicSource[]> {
  const sources: IslamicSource[] = [];
  try {
    // ابحث في القرآن
    const searchUrl = language === "ar"
      ? `${QURAN_API_BASE}/search?q=${encodeURIComponent(query)}&language=ar&size=5`
      : `${QURAN_API_BASE}/search?q=${encodeURIComponent(query)}&size=5`;
    
    const searchResponse = await fetch(searchUrl);
    if (!searchResponse.ok) return sources;
    
    const searchData = await searchResponse.json() as { results?: QuranSearchResult[] };
    if (!searchData.results) return sources;
    
    for (const result of searchData.results.slice(0, 5)) {
      const [surah, ayah] = result.verse_key.split(":");
      const surahNames: Record<string, string> = {
        "1": "الفاتحة", "2": "البقرة", "3": "آل عمران", "4": "النساء",
        "5": "المائدة", "6": "الأنعام", "7": "الأعراف", "8": "الأنفال",
        "9": "التوبة", "10": "يونس", "11": "هود", "12": "يوسف",
        "13": "الرعد", "14": "إبراهيم", "15": "الحجر", "16": "النحل",
        "17": "الإسراء", "18": "الكهف", "19": "مريم", "20": "طه",
        "21": "الأنبياء", "22": "الحج", "23": "المؤمنون", "24": "النور",
        "25": "الفرقان", "26": "الشعراء", "27": "النمل", "28": "القصص",
        "29": "العنكبوت", "30": "الروم", "31": "لقمان", "32": "السجدة",
        "33": "الأحزاب", "34": "سبأ", "35": "فاطر", "36": "يس",
        "37": "الصافات", "38": "ص", "39": "الزمر", "40": "غافر",
        "41": "فصلت", "42": "الشورى", "43": "الزخرف", "44": "الدخان",
        "45": "الجاثية", "46": "الأحقاف", "47": "محمد", "48": "الفتح",
        "49": "الحجرات", "50": "ق", "51": "الذاريات", "52": "الطور",
        "53": "النجم", "54": "القمر", "55": "الرحمن", "56": "الواقعة",
        "57": "الحديد", "58": "المجادلة", "59": "الحشر", "60": "الممتحنة",
        "61": "الصف", "62": "الجمعة", "63": "المنافقون", "64": "التغابن",
        "65": "الطلاق", "66": "التحريم", "67": "الملك", "68": "القلم",
        "69": "الحاقة", "70": "المعارج", "71": "نوح", "72": "الجن",
        "73": "المزمل", "74": "المدثر", "75": "القيامة", "76": "الإنسان",
        "77": "المرسلات", "78": "النبأ", "79": "النازعات", "80": "عبس",
        "81": "التكوير", "82": "الانفطار", "83": "المطففين", "84": "الانشقاق",
        "85": "البروج", "86": "الطارق", "87": "الأعلى", "88": "الغاشية",
        "89": "الفجر", "90": "البلد", "91": "الشمس", "92": "الليل",
        "93": "الضحى", "94": "الشرح", "95": "التين", "96": "العلق",
        "97": "القدر", "98": "البينة", "99": "الزلزلة", "100": "العاديات",
        "101": "القارعة", "102": "التكاثر", "103": "العصر", "104": "الهمزة",
        "105": "الفيل", "106": "قريش", "107": "الماعون", "108": "الكوثر",
        "109": "الكافرون", "110": "النصر", "111": "المسد", "112": "الإخلاص",
        "113": "الفلق", "114": "الناس",
      };
      
      const surahName = surahNames[surah] || `سورة ${surah}`;
      sources.push({
        type: "quran",
        title: surahName,
        reference: result.verse_key,
        excerpt: result.text,
        url: `https://quran.com/${result.verse_key}`,
      });
    }
  } catch (e) {
    logger.error("Quran search error:", e);
  }
  return sources;
}

// ─── UmmahAPI (Hadith) ───

const UMMAHAPI_BASE = "https://ummahapi.com/api";

interface HadithResult {
  collection: string;
  book: string;
  number: number;
  arabicText: string;
  englishText?: string;
  grade?: string;
}

async function searchHadith(query: string, language: string): Promise<IslamicSource[]> {
  const sources: IslamicSource[] = [];
  try {
    const searchUrl = `${UMMAHAPI_BASE}/hadith/search?q=${encodeURIComponent(query)}&limit=5`;
    const response = await fetch(searchUrl);
    if (!response.ok) return sources;
    
    const data = await response.json() as { results?: HadithResult[] };
    if (!data.results) return sources;
    
    for (const hadith of data.results.slice(0, 5)) {
      sources.push({
        type: "hadith",
        title: hadith.collection,
        reference: `${hadith.book} ${hadith.number}`,
        excerpt: language === "ar" ? hadith.arabicText : (hadith.englishText || hadith.arabicText),
        url: `https://sunnah.com/${hadith.collection}:${hadith.number}`,
      });
    }
  } catch (e) {
    logger.error("Hadith search error:", e);
  }
  return sources;
}

// ─── Tafsir from Quran.com ───

async function getTafsir(verseKey: string): Promise<IslamicSource[]> {
  const sources: IslamicSource[] = [];
  try {
    const [surah, ayah] = verseKey.split(":");
    const tafsirUrl = `${QURAN_API_BASE}/tafsirs/169/by_ayah/${surah}:${ayah}?locale=ar`;
    const response = await fetch(tafsirUrl);
    if (!response.ok) return sources;
    
    const data = await response.json() as { tafsir?: { text: string; resource_name: string } };
    if (data.tafsir) {
      sources.push({
        type: "tafsir",
        title: data.tafsir.resource_name || "تفسير ابن كثير",
        reference: verseKey,
        excerpt: data.tafsir.text.substring(0, 500),
        url: `https://quran.com/${verseKey}/tafsirs`,
      });
    }
  } catch (e) {
    logger.error("Tafsir fetch error:", e);
  }
  return sources;
}

// ─── Cloud Function ───

export const copilotAsk = onRequest(
  {
    secrets: [GEMINI_API_KEY],
    timeoutSeconds: 60,
    maxInstances: 10,
  },
  async (req, res) => {
    // CORS
    res.set("Access-Control-Allow-Origin", "*");
    res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
    res.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    
    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }
    
    if (req.method !== "POST") {
      res.status(405).json({ error: "Method not allowed" });
      return;
    }
    
    try {
      const { question, language = "ar", includeQuran = true, includeHadith = true, includeTafsir = true } = req.body as CopilotRequest;
      
      if (!question || question.trim().length < 2) {
        res.status(400).json({ error: "Question is required" });
        return;
      }
      
      logger.info(`Copilot question: "${question}" (lang: ${language})`);
      
      // 1. ابحث في المصادر الإسلامية
      const allSources: IslamicSource[] = [];
      
      if (includeQuran) {
        const quranSources = await searchQuran(question, language);
        allSources.push(...quranSources);
      }
      
      if (includeHadith) {
        const hadithSources = await searchHadith(question, language);
        allSources.push(...hadithSources);
      }
      
      // 2. اجلب التفسير لأول آية موجودة
      if (includeTafsir && allSources.some(s => s.type === "quran")) {
        const firstQuran = allSources.find(s => s.type === "quran");
        if (firstQuran) {
          const tafsirSources = await getTafsir(firstQuran.reference);
          allSources.push(...tafsirSources);
        }
      }
      
      // 3. جهّز السياق للنموذج
      const sourcesContext = allSources.length > 0
        ? allSources.map((s, i) => 
            `[${i + 1}] النوع: ${s.type} | المصدر: ${s.title} | المرجع: ${s.reference}\nالنص: ${s.excerpt}`
          ).join("\n\n")
        : "لا توجد مصادر متاحة.";
      
      // 4. استدعِ Gemini
      const genAI = new GoogleGenerativeAI(GEMINI_API_KEY.value());
      const model = genAI.getGenerativeModel({
        model: GEMINI_MODEL,
        systemInstruction: SYSTEM_PROMPT,
        generationConfig: {
          temperature: 0.3,
          topP: 0.9,
          topK: 40,
          responseMimeType: "application/json",
        },
      });
      
      const fullPrompt = `سؤال المستخدم: ${question}

المصادر الإسلامية الموثّقة المتاحة:
${sourcesContext}

أجب من هذه المصادر فقط. أرجع JSON بالصيغة المطلوبة.`;
      
      const result = await model.generateContent(fullPrompt);
      const responseText = result.response.text();
      
      // 5. حاول تحليل JSON
      let copilotResponse: CopilotResponse;
      try {
        copilotResponse = JSON.parse(responseText) as CopilotResponse;
        // ادمج المصادر التي جمعناها
        copilotResponse.sources = allSources.length > 0 ? allSources : copilotResponse.sources;
        copilotResponse.disclaimer = "هذا رد معرفي موثّق وليس فتوى. للاستفسارات الفقهية يُرجى الرجوع لأهل العلم.";
      } catch {
        // إذا فشل تحليل JSON، استخدم النص مباشرة
        copilotResponse = {
          answer: responseText || "عذراً، لم أتمكن من معالجة الرد.",
          sources: allSources,
          confidence: 0.7,
          followUpQuestions: [],
          disclaimer: "هذا رد معرفي موثّق وليس فتوى. للاستفسارات الفقهية يُرجى الرجوع لأهل العلم.",
        };
      }
      
      res.status(200).json(copilotResponse);
      
    } catch (error) {
      logger.error("Copilot error:", error);
      res.status(500).json({
        error: "Internal server error",
        message: error instanceof Error ? error.message : "Unknown error",
      });
    }
  }
);
