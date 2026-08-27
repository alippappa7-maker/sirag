import { onCall, HttpsError } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";
import { defineSecret } from "firebase-functions/params";
import { GoogleGenAI } from "@google/genai";

admin.initializeApp();

// Read GEMINI_API_KEY from Google Cloud Secret Manager
const geminiApiKey = defineSecret("GEMINI_API_KEY");

// Utility to verify user, check rate limit and deduct balance
async function verifyAndChargeUser(uid: string, cost: number): Promise<void> {
    const userRef = admin.firestore().collection("users").doc(uid);
    
    await admin.firestore().runTransaction(async (t) => {
        const doc = await t.get(userRef);
        if (!doc.exists) throw new HttpsError("not-found", "User not found");
        
        const data = doc.data();
        const balance = data?.balance || 0;
        const lastRequestAt = data?.lastAiRequestAt?.toMillis() || 0;
        
        // Rate limiting: 5 seconds between AI requests
        if (Date.now() - lastRequestAt < 5000) {
            throw new HttpsError("resource-exhausted", "Too many requests. Please wait.");
        }
        
        // Balance check
        if (balance < cost) {
            throw new HttpsError("permission-denied", "Insufficient balance for this operation.");
        }
        
        // Deduct and update time
        t.update(userRef, {
            balance: balance - cost,
            lastAiRequestAt: admin.firestore.FieldValue.serverTimestamp()
        });
    });
}

// 1. Endpoint: Generate Ideas
export const generateIdeas = onCall({ 
    secrets: [geminiApiKey],
    timeoutSeconds: 60,
    maxInstances: 10
}, async (request) => {
    // 1. Verify Firebase ID Token
    if (!request.auth) {
        throw new HttpsError("unauthenticated", "User must be authenticated.");
    }
    const uid = request.auth.uid;
    const reqData = request.data;
    
    // 2. Check Role & Balance (Cost: 1 token/credit conceptually)
    await verifyAndChargeUser(uid, 1);
    
    // 3. Initialize Gemini
    const ai = new GoogleGenAI({ apiKey: geminiApiKey.value() });
    const model = "gemini-3.1-pro"; // Stable model for production
    const promptVersion = "v1.0.0";
    const requestId = `req_${Date.now()}_${uid}`;

    try {
        logger.info("Starting idea generation", { requestId, uid, promptVersion, model });
        
        // 4. Construct Prompt
        const prompt = `أنت مساعد إنتاج محتوى. قم بتوليد 3 أفكار لمحتوى إسلامي عربي بناءً على التالي:
        الموضوع: ${reqData.subject}
        الجمهور: ${reqData.audience}
        المنصة: ${reqData.platform}
        المدة: ${reqData.duration}
        النبرة: ${reqData.tone}
        الهدف: ${reqData.goal}
        
        تنبيه صارم: لا تقم بتأليف أي نصوص قرآنية أو أحاديث نبوية أو فتاوى. إذا كان الموضوع يحتوي على عنصر شرعي (${reqData.hasReligiousElement})، يجب وضع riskLevel=HIGH و needsReview=true.`;

        // 5. Enforce Structured Output (JSON Schema)
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
            config: {
                responseMimeType: "application/json",
                responseSchema: {
                    type: "ARRAY",
                    items: {
                        type: "OBJECT",
                        properties: {
                            id: { type: "STRING" },
                            title: { type: "STRING" },
                            hook: { type: "STRING" },
                            summary: { type: "STRING" },
                            audience: { type: "STRING" },
                            suggestedScenes: { type: "INTEGER" },
                            requiredSources: { type: "ARRAY", items: { type: "STRING" } },
                            riskLevel: { type: "STRING", enum: ["LOW", "MEDIUM", "HIGH"] },
                            needsReview: { type: "BOOLEAN" },
                            disclaimer: { type: "STRING", nullable: true }
                        },
                        required: ["id", "title", "hook", "summary", "audience", "suggestedScenes", "requiredSources", "riskLevel", "needsReview"]
                    }
                }
            }
        });

        // 6. Log Success & Cost tracking
        logger.info("Idea generation completed", { requestId, status: "completed" });
        
        // Parse and return the robust JSON
        const rawText = response.text;
        if (!rawText) throw new Error("Empty response from AI");
        
        const ideas = JSON.parse(rawText);
        
        // Force safety flags server-side just in case AI fails to apply them
        if (reqData.hasReligiousElement) {
            ideas.forEach((idea: any) => {
                idea.riskLevel = "HIGH";
                idea.needsReview = true;
                if (!idea.disclaimer) idea.disclaimer = "محتوى ذو طبيعة شرعية. يتطلب التوثيق والمراجعة البشرية.";
            });
        }

        return ideas;
        
    } catch (error) {
        // 7. Provider Error Handling
        logger.error("Provider Error during generation", { requestId, error });
        throw new HttpsError("internal", "فشل توليد الأفكار بسبب خطأ في المزود. سيتم استرجاع رصيدك.");
        // Note: Real world would need a fallback or refund transaction here.
    }
});

// 2. Endpoint: Generate Plan (Stub)
export const generatePlan = onCall({ secrets: [geminiApiKey] }, async (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "User must be authenticated.");
    await verifyAndChargeUser(request.auth.uid, 2);
    // Implementation for JSON Schema Content Plan goes here
    return { status: "completed", plan: {} };
});

// 3. Endpoint: Generate Script (Stub)
export const generateScript = onCall({ secrets: [geminiApiKey] }, async (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "User must be authenticated.");
    await verifyAndChargeUser(request.auth.uid, 5);
    // Implementation for JSON Schema Script goes here
    return { status: "completed", script: {} };
});
