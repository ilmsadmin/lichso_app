package com.lichso.app.feature.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class KnowledgeFeedUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val events: List<ContentEvent> = emptyList(),
    val famousPeople: List<FamousPerson> = emptyList(),
    val categories: List<ArticleCategory> = emptyList(),
    val articles: List<Article> = emptyList(),
    val quote: Quote? = null,
    val selectedCategorySlug: String? = null,
    val selectedCategoryName: String? = null,
    val hasMoreArticles: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class KnowledgeFeedViewModel @Inject constructor(
    private val repository: ContentRepository,
) : ViewModel() {
    private val articlePageSize = 10
    private var nextArticlePage = 1
    private var selectedCategorySlug: String? = null

    private val _uiState = MutableStateFlow(KnowledgeFeedUiState())
    val uiState: StateFlow<KnowledgeFeedUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun refresh() {
        loadContent()
    }

    fun selectCategory(category: ArticleCategory?) {
        val slug = category?.slug
        if (selectedCategorySlug == slug) return

        selectedCategorySlug = slug
        _uiState.update {
            it.copy(
                selectedCategorySlug = slug,
                selectedCategoryName = category?.name,
                articles = emptyList(),
                isLoadingMore = false,
                hasMoreArticles = true,
            )
        }
        loadArticles(reset = true)
    }

    fun loadMoreArticles() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMoreArticles) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            repository.getArticles(page = nextArticlePage, limit = articlePageSize, category = selectedCategorySlug)
                .onSuccess { newArticles ->
                    val merged = (_uiState.value.articles + newArticles).distinctBy { it.id }
                    nextArticlePage += 1
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            articles = merged,
                            hasMoreArticles = newArticles.size >= articlePageSize,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { current ->
                        current.copy(
                            isLoadingMore = false,
                            error = "Không thể tải thêm bài viết. Vui lòng thử lại.",
                        )
                    }
                }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            nextArticlePage = 1
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    error = null,
                    hasMoreArticles = true,
                )
            }
            val today = LocalDate.now()
            val month = today.monthValue
            val day = today.dayOfMonth

            val eventsDeferred = async { repository.getEventsByDate(month, day) }
            val peopleDeferred = async { repository.getFamousPeopleByBirthday(month, day) }
            val categoriesDeferred = async { repository.getCategories() }
            val articlesDeferred = async {
                repository.getArticles(
                    page = nextArticlePage,
                    limit = articlePageSize,
                    category = selectedCategorySlug,
                )
            }
            val quoteDeferred = async { repository.getTodayQuote() }

            val events = eventsDeferred.await().getOrNull() ?: emptyList()
            val people = peopleDeferred.await().getOrNull() ?: emptyList()
            val categories = flattenCategories(categoriesDeferred.await().getOrNull() ?: emptyList())
            val articles = articlesDeferred.await().getOrNull() ?: emptyList()
            val quote = quoteDeferred.await().getOrNull()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    events = events,
                    famousPeople = people,
                    categories = categories,
                    articles = articles,
                    quote = quote,
                    selectedCategorySlug = selectedCategorySlug,
                    selectedCategoryName = categories.firstOrNull { category -> category.slug == selectedCategorySlug }?.name,
                    hasMoreArticles = articles.size >= articlePageSize,
                )
            }
            nextArticlePage = 2
        }
    }

    private fun loadArticles(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                nextArticlePage = 1
                _uiState.update {
                    it.copy(
                        isLoadingMore = true,
                        hasMoreArticles = true,
                        error = null,
                    )
                }
            }

            repository.getArticles(page = nextArticlePage, limit = articlePageSize, category = selectedCategorySlug)
                .onSuccess { newArticles ->
                    nextArticlePage += 1
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            articles = newArticles,
                            hasMoreArticles = newArticles.size >= articlePageSize,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { current ->
                        current.copy(
                            isLoadingMore = false,
                            error = "Không thể tải bài viết. Vui lòng thử lại.",
                        )
                    }
                }
        }
    }

    private fun flattenCategories(categories: List<ArticleCategory>): List<ArticleCategory> {
        return categories.flatMap { category ->
            listOf(category) + flattenCategories(category.children.orEmpty())
        }.distinctBy { it.id }
    }
}
