package com.example.feature.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

object ShareManager {

    fun shareViaSystemSheet(context: Context, shareContent: ShareContent) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out ${shareContent.title} on Aura Music!")
            putExtra(
                Intent.EXTRA_TEXT,
                "${shareContent.title} - ${shareContent.subtitle}\n\nListen 100% Free on Aura: ${shareContent.shareUrl}"
            )
        }
        val chooserIntent = Intent.createChooser(shareIntent, "Share ${shareContent.title} via")
        context.startActivity(chooserIntent)
    }

    fun copyLinkToClipboard(context: Context, shareContent: ShareContent): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Aura Music Link", shareContent.shareUrl)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun copyTextCardToClipboard(context: Context, shareContent: ShareContent): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val textCard = """
                🎵 ${shareContent.title}
                👤 ${shareContent.subtitle}
                ✨ ${shareContent.description ?: "Stream 100% Free on Aura Music"}
                🔗 ${shareContent.shareUrl}
            """.trimIndent()
            val clip = ClipData.newPlainText("Aura Music Card", textCard)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun createShareUrl(type: ShareContentType, id: String): String {
        return "https://auramusic.app/share/${type.name.lowercase()}/$id"
    }

    fun createDeepLink(type: ShareContentType, id: String): String {
        return "aura://${type.name.lowercase()}/$id"
    }
}
