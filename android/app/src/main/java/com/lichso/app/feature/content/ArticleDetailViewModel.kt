package com.lichso.app.feature.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.remote.Article
import com.lichso.app.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleDetailUiState(
    val isLoading: Boolean = true,
    val article: Article? = null,
    val relatedArticles: List<Article> = emptyList(),
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
            _uiState.update {
                it.copy(
                    isLoading = true,
                    article = null,
                    relatedArticles = emptyList(),
                    error = null,
                )
            }
            repository.getArticle(articleId)
                .onSuccess { article ->
                    _uiState.update {
                        it.copy(isLoading = false, article = article)
                    }
                    loadRelatedArticles(article)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = ErrorMessageUtil.friendlyMessage(exception, "Không thể tải chi tiết bài viết")
                        )
                    }
                }
        }
    }

    private fun loadRelatedArticles(article: Article) {
        viewModelScope.launch {
            val categorySlug = article.category?.slug?.takeIf { it.isNotBlank() }
            val primary = categorySlug?.let {
                repository.getArticles(page = 1, limit = 8, category = it).getOrNull()
            }.orEmpty()

            val fallback = if (primary.count { it.id != article.id } >= 3) {
                emptyList()
            } else {
                repository.getArticles(page = 1, limit = 8).getOrNull().orEmpty()
            }

            val related = (primary + fallback)
                .filter { it.id != article.id }
                .distinctBy { it.id }
                .take(4)

            _uiState.update { it.copy(relatedArticles = related) }
        }
    }
}
