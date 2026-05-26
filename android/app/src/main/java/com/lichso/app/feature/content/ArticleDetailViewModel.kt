package com.lichso.app.feature.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.remote.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleDetailUiState(
    val isLoading: Boolean = true,
    val article: Article? = null,
    val error: String? = null,
)

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val repository: ContentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getArticle(articleId)
                .onSuccess { article ->
                    _uiState.update {
                        it.copy(isLoading = false, article = article)
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Không thể tải chi tiết bài viết"
                        )
                    }
                }
        }
    }
}
