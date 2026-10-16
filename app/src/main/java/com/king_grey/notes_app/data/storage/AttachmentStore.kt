package com.king_grey.notes_app.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.king_grey.notes_app.data.models.Note
import com.king_grey.notes_app.ui.screens.notes.NoteBlock
import com.king_grey.notes_app.utils.IdGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps a copy of every attachment in the app's own storage, so a note never depends on a
 * photo-picker grant or another app's file. Notes hold FileProvider `content://` URIs that point
 * at these copies; they are removed when the note is deleted for good.
 */
@Singleton
class AttachmentStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val authority = "${context.packageName}.fileprovider"
    private val dir get() = File(context.filesDir, DIR_NAME).apply { mkdirs() }

    /** Copies what [source] points at into app storage. Returns the new URI, or null if it could not be read. */
    suspend fun import(source: Uri, displayName: String?, mimeType: String?): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(dir, "att_${IdGenerator.newId()}.${extensionFor(displayName, mimeType)}")
                val input = context.contentResolver.openInputStream(source) ?: return@runCatching null
                input.use { i -> file.outputStream().use { o -> i.copyTo(o) } }
                FileProvider.getUriForFile(context, authority, file).toString()
            }.getOrNull()
        }

    suspend fun saveBitmap(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(dir, "att_${IdGenerator.newId()}.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
            FileProvider.getUriForFile(context, authority, file).toString()
        }.getOrNull()
    }

    /** Deletes the copies behind [uris]. Anything that is not one of ours is left alone. */
    suspend fun delete(uris: Collection<String>) = withContext(Dispatchers.IO) {
        uris.mapNotNull { fileFor(it) }.forEach { it.delete() }
    }

    /**
     * Removes copies no note refers to any more (a block removed in the editor, a note purged from
     * Recently deleted). Files from the last day are kept: they may belong to a note being written.
     */
    suspend fun pruneExcept(referenced: Set<String>) = withContext(Dispatchers.IO) {
        val keep = referenced.mapNotNull { fileFor(it)?.name }.toSet()
        val cutoff = System.currentTimeMillis() - DAY_MS
        dir.listFiles()?.forEach { f ->
            if (f.name !in keep && f.lastModified() < cutoff) f.delete()
        }
    }

    private fun fileFor(uri: String): File? {
        val parsed = runCatching { Uri.parse(uri) }.getOrNull() ?: return null
        if (parsed.scheme != "content" || parsed.authority != authority) return null
        val name = parsed.pathSegments.lastOrNull() ?: return null
        return File(dir, name).takeIf { it.parentFile == dir }
    }

    private fun extensionFor(displayName: String?, mimeType: String?): String {
        val fromName = displayName?.substringAfterLast('.', "")?.lowercase()?.filter { it.isLetterOrDigit() }
        if (!fromName.isNullOrEmpty() && fromName.length <= 8) return fromName
        val fromMime = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
        return fromMime ?: "bin"
    }

    private companion object {
        const val DIR_NAME = "attachments"
        const val DAY_MS = 24L * 60 * 60 * 1000
    }
}

/** Image and link/file URIs a note refers to. */
fun Note.attachmentUris(): List<String> = blocks.mapNotNull { block ->
    when (block) {
        is NoteBlock.Image -> block.uri
        is NoteBlock.Link -> block.url
        else -> null
    }
}
