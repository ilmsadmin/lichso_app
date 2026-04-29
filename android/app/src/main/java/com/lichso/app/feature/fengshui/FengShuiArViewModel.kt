package com.lichso.app.feature.fengshui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.remote.FengShuiVisionApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class FengShuiArState(
    val imageUri: Uri? = null,
    val userPrompt: String = "",
    val analysis: String? = null,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class FengShuiArViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val visionApi: FengShuiVisionApi,
) : ViewModel() {

    private val _state = MutableStateFlow(FengShuiArState())
    val state: StateFlow<FengShuiArState> = _state.asStateFlow()

    fun setImageUri(uri: Uri?) = _state.update {
        it.copy(imageUri = uri, analysis = null, errorMessage = null)
    }

    fun setPrompt(text: String) = _state.update { it.copy(userPrompt = text) }

    fun clearError() = _state.update { it.copy(errorMessage = null) }

    fun analyze() {
        val s = _state.value
        val uri = s.imageUri ?: return
        if (s.isAnalyzing) return

        _state.update { it.copy(isAnalyzing = true, errorMessage = null, analysis = null) }
        viewModelScope.launch {
            val base64 = withContext(Dispatchers.IO) { encodeImage(uri) }
            if (base64 == null) {
                _state.update { it.copy(isAnalyzing = false, errorMessage = "Không thể đọc ảnh đã chọn.") }
                return@launch
            }
            val result = visionApi.analyzeRoomPhoto(base64, s.userPrompt)
            result.onSuccess { text ->
                _state.update { it.copy(isAnalyzing = false, analysis = text) }
            }.onFailure { err ->
                _state.update { it.copy(isAnalyzing = false, errorMessage = err.message ?: "Lỗi không xác định") }
            }
        }
    }

    /** Đọc ảnh từ uri, scale max 1024px cạnh dài, encode JPEG quality 80 → base64. */
    private fun encodeImage(uri: Uri): String? = runCatching {
        appContext.contentResolver.openInputStream(uri).use { input ->
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(input, null, opts)
            val maxSide = maxOf(opts.outWidth, opts.outHeight).coerceAtLeast(1)
            val sampleSize = generateSequence(1) { it * 2 }.first { (maxSide / it) <= 1024 }
            opts.inJustDecodeBounds = false
            opts.inSampleSize = sampleSize
            opts
        }.let { opts ->
            appContext.contentResolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input, null, opts)
            }
        }?.let { bitmap ->
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
        }
    }.getOrNull()
}
