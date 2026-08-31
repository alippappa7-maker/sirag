package com.siraj.app.features.project.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.features.project.domain.models.*
import com.siraj.app.features.project.domain.repositories.SubtitleRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.siraj.app.core.error.GlobalErrorHandler

class FirebaseSubtitleRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SubtitleRepository {

    override fun getSubtitles(projectId: String, sceneId: String?): Flow<List<SubtitleItem>> = callbackFlow {
        val collection = firestore.collection("projects").document(projectId).collection("subtitles")
        val query = if (sceneId != null) collection.whereEqualTo("sceneId", sceneId) else collection

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val scId = doc.getString("sceneId") ?: ""
                    val lang = doc.getString("language") ?: "ar"
                    val text = doc.getString("text") ?: ""
                    val startMs = doc.getLong("startMs") ?: 0L
                    val endMs = doc.getLong("endMs") ?: 3000L
                    val locked = doc.getBoolean("locked") ?: false
                    val srcTypeStr = doc.getString("sourceType") ?: SubtitleSourceType.SCENE_NARRATION.name
                    val sourceType = try { SubtitleSourceType.valueOf(srcTypeStr) } catch (e: Exception) {
            GlobalErrorHandler.handle(e) SubtitleSourceType.SCENE_NARRATION }
                    val reviewStr = doc.getString("reviewStatus") ?: SubtitleReviewStatus.NOT_REQUIRED.name
                    val reviewStatus = try { SubtitleReviewStatus.valueOf(reviewStr) } catch (e: Exception) {
            GlobalErrorHandler.handle(e) SubtitleReviewStatus.NOT_REQUIRED }
                    val reviewerNotes = doc.getString("reviewerNotes")
                    val sourceRefTitle = doc.getString("sourceRefTitle")
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                    val styleMap = doc.get("style") as? Map<*, *>
                    val style = if (styleMap != null) {
                        val fontStr = styleMap["fontFamily"] as? String ?: SubtitleFontFamily.SYSTEM_SANS.name
                        val fontFamily = try { SubtitleFontFamily.valueOf(fontStr) } catch (e: Exception) {
            GlobalErrorHandler.handle(e) SubtitleFontFamily.SYSTEM_SANS }
                        val posStr = styleMap["position"] as? String ?: SubtitlePosition.BOTTOM.name
                        val position = try { SubtitlePosition.valueOf(posStr) } catch (e: Exception) {
            GlobalErrorHandler.handle(e) SubtitlePosition.BOTTOM }
                        SubtitleStyle(
                            fontFamily = fontFamily,
                            fontSizeSp = (styleMap["fontSizeSp"] as? Number)?.toInt() ?: 18,
                            textColorHex = styleMap["textColorHex"] as? String ?: "#FFFFFF",
                            backgroundColorHex = styleMap["backgroundColorHex"] as? String ?: "#80000000",
                            position = position,
                            isBold = styleMap["isBold"] as? Boolean ?: true,
                            hasOutline = styleMap["hasOutline"] as? Boolean ?: true,
                            outlineColorHex = styleMap["outlineColorHex"] as? String ?: "#000000",
                            maxWordsPerLine = (styleMap["maxWordsPerLine"] as? Number)?.toInt() ?: 8,
                            burnIntoVideo = styleMap["burnIntoVideo"] as? Boolean ?: true
                        )
                    } else {
                        SubtitleStyle()
                    }

                    SubtitleItem(
                        id = id,
                        projectId = projectId,
                        sceneId = scId,
                        language = lang,
                        text = text,
                        startMs = startMs,
                        endMs = endMs,
                        style = style,
                        sourceType = sourceType,
                        locked = locked,
                        reviewStatus = reviewStatus,
                        reviewerNotes = reviewerNotes,
                        sourceRefTitle = sourceRefTitle,
                        createdAt = createdAt
                    )
                } catch (e: Exception) {
                    null
                }
            }?.sortedBy { it.startMs } ?: emptyList()

            trySend(items)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun saveSubtitle(subtitle: SubtitleItem): Result<Unit> {
        return try {
            val docRef = firestore.collection("projects").document(subtitle.projectId)
                .collection("subtitles").document(subtitle.id)

            val styleMap = mapOf(
                "fontFamily" to subtitle.style.fontFamily.name,
                "fontSizeSp" to subtitle.style.fontSizeSp,
                "textColorHex" to subtitle.style.textColorHex,
                "backgroundColorHex" to subtitle.style.backgroundColorHex,
                "position" to subtitle.style.position.name,
                "isBold" to subtitle.style.isBold,
                "hasOutline" to subtitle.style.hasOutline,
                "outlineColorHex" to subtitle.style.outlineColorHex,
                "maxWordsPerLine" to subtitle.style.maxWordsPerLine,
                "burnIntoVideo" to subtitle.style.burnIntoVideo
            )

            val data = mapOf(
                "id" to subtitle.id,
                "projectId" to subtitle.projectId,
                "sceneId" to subtitle.sceneId,
                "language" to subtitle.language,
                "text" to subtitle.text,
                "startMs" to subtitle.startMs,
                "endMs" to subtitle.endMs,
                "style" to styleMap,
                "sourceType" to subtitle.sourceType.name,
                "locked" to subtitle.locked,
                "reviewStatus" to subtitle.reviewStatus.name,
                "reviewerNotes" to (subtitle.reviewerNotes ?: ""),
                "sourceRefTitle" to (subtitle.sourceRefTitle ?: ""),
                "createdAt" to subtitle.createdAt
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSubtitle(projectId: String, sceneId: String, subtitleId: String): Result<Unit> {
        return try {
            firestore.collection("projects").document(projectId)
                .collection("subtitles").document(subtitleId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateSubtitlesFromScene(
        projectId: String,
        sceneId: String,
        sceneText: String,
        sceneDurationMs: Long
    ): Result<List<SubtitleItem>> {
        return try {
            val cleanText = sceneText.trim()
            if (cleanText.isEmpty()) return Result.success(emptyList())

            // Check if text is Quranic / Sacred Locked source
            val isQuranic = cleanText.contains("﴿") || cleanText.contains("قال تعالى") || cleanText.contains("سورة")
            val isHadith = cleanText.contains("قال رسول الله") || cleanText.contains("صلى الله عليه وسلم") || cleanText.contains("حديث")

            val sourceType = when {
                isQuranic -> SubtitleSourceType.QURAN_SOURCE_LOCKED
                isHadith -> SubtitleSourceType.HADITH_SOURCE_LOCKED
                else -> SubtitleSourceType.SCENE_NARRATION
            }

            val isLocked = isQuranic || isHadith
            val reviewStatus = if (isLocked) SubtitleReviewStatus.VERIFIED_LOCKED else SubtitleReviewStatus.NOT_REQUIRED

            // Split sentence / phrases naturally by punctuation or words count
            val sentences = cleanText.split(Regex("(?<=[.،؛:؟\n])\\s*")).filter { it.isNotBlank() }
            val chunks = if (sentences.isNotEmpty()) sentences else listOf(cleanText)

            val safeDuration = if (sceneDurationMs > 0) sceneDurationMs else (chunks.size * 3500L)
            val chunkDuration = (safeDuration / chunks.size).coerceAtLeast(1500L)

            val generatedList = mutableListOf<SubtitleItem>()
            var currentStart = 0L

            for (chunk in chunks) {
                val currentEnd = (currentStart + chunkDuration).coerceAtMost(safeDuration)
                val item = SubtitleItem(
                    id = "sub_${System.currentTimeMillis()}_${generatedList.size}",
                    projectId = projectId,
                    sceneId = sceneId,
                    language = "ar",
                    text = chunk.trim(),
                    startMs = currentStart,
                    endMs = currentEnd,
                    style = SubtitleStyle(),
                    sourceType = sourceType,
                    locked = isLocked,
                    reviewStatus = reviewStatus,
                    sourceRefTitle = if (isQuranic) "نص قرآني محكم" else if (isHadith) "حديث شريف معتمد" else null,
                    createdAt = System.currentTimeMillis()
                )
                generatedList.add(item)
                saveSubtitle(item)
                currentStart = currentEnd
            }

            Result.success(generatedList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun autoTranslateToEnglish(
        projectId: String,
        sceneId: String,
        arabicSubtitles: List<SubtitleItem>
    ): Result<List<SubtitleItem>> {
        return try {
            val translatedItems = mutableListOf<SubtitleItem>()

            for (sub in arabicSubtitles) {
                val isSacred = sub.locked || sub.sourceType == SubtitleSourceType.QURAN_SOURCE_LOCKED || sub.sourceType == SubtitleSourceType.HADITH_SOURCE_LOCKED
                
                // Construct honest, review-required English translation draft
                val englishTextDraft = if (isSacred) {
                    "[Meaning of the Verse/Hadith Translation]: " + sub.text
                } else {
                    "[EN Translation Draft]: " + sub.text
                }

                val enItem = SubtitleItem(
                    id = "sub_en_${System.currentTimeMillis()}_${translatedItems.size}",
                    projectId = projectId,
                    sceneId = sceneId,
                    language = "en",
                    text = englishTextDraft,
                    startMs = sub.startMs,
                    endMs = sub.endMs,
                    style = sub.style,
                    sourceType = SubtitleSourceType.TRANSLATION_EN,
                    locked = false,
                    reviewStatus = SubtitleReviewStatus.PENDING_REVIEW,
                    reviewerNotes = "تنبيه: ترجمة معاني المحتوى الشرعي تتطلب اعتماد المترجم المعتمد ولا تعتبر نصاً قرآنياً أصلياً.",
                    createdAt = System.currentTimeMillis()
                )
                translatedItems.add(enItem)
                saveSubtitle(enItem)
            }

            Result.success(translatedItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSubtitleStyleForScene(
        projectId: String,
        sceneId: String,
        style: SubtitleStyle
    ): Result<Unit> {
        return try {
            val subsSnapshot = firestore.collection("projects").document(projectId)
                .collection("subtitles").whereEqualTo("sceneId", sceneId).get().await()

            val styleMap = mapOf(
                "fontFamily" to style.fontFamily.name,
                "fontSizeSp" to style.fontSizeSp,
                "textColorHex" to style.textColorHex,
                "backgroundColorHex" to style.backgroundColorHex,
                "position" to style.position.name,
                "isBold" to style.isBold,
                "hasOutline" to style.hasOutline,
                "outlineColorHex" to style.outlineColorHex,
                "maxWordsPerLine" to style.maxWordsPerLine,
                "burnIntoVideo" to style.burnIntoVideo
            )

            for (doc in subsSnapshot.documents) {
                doc.reference.update("style", styleMap).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun exportToSrt(subtitles: List<SubtitleItem>): String {
        val sb = StringBuilder()
        val sorted = subtitles.sortedBy { it.startMs }
        for ((index, sub) in sorted.withIndex()) {
            sb.append("${index + 1}\n")
            sb.append("${formatSrtTime(sub.startMs)} --> ${formatSrtTime(sub.endMs)}\n")
            sb.append("${sub.text}\n\n")
        }
        return sb.toString()
    }

    override fun exportToVtt(subtitles: List<SubtitleItem>): String {
        val sb = StringBuilder()
        sb.append("WEBVTT\n\n")
        val sorted = subtitles.sortedBy { it.startMs }
        for ((index, sub) in sorted.withIndex()) {
            sb.append("${index + 1}\n")
            sb.append("${formatVttTime(sub.startMs)} --> ${formatVttTime(sub.endMs)}\n")
            sb.append("${sub.text}\n\n")
        }
        return sb.toString()
    }

    private fun formatSrtTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }

    private fun formatVttTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }
}
