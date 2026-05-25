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
    val events: List<ContentEvent> = emptyList(),
    val famousPeople: List<FamousPerson> = emptyList(),
    val articles: List<Article> = emptyList(),
    val quote: Quote? = null,
    val error: String? = null,
)

@HiltViewModel
class KnowledgeFeedViewModel @Inject constructor(
    private val repository: ContentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(KnowledgeFeedUiState())
    val uiState: StateFlow<KnowledgeFeedUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun refresh() {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val today = LocalDate.now()
            val month = today.monthValue
            val day = today.dayOfMonth

            val eventsDeferred = async { repository.getEventsByDate(month, day) }
            val peopleDeferred = async { repository.getFamousPeopleByBirthday(month, day) }
            val articlesDeferred = async { repository.getArticles(limit = 10) }
            val quoteDeferred = async { repository.getTodayQuote() }

            val events = eventsDeferred.await().getOrNull() ?: emptyList()
            val people = peopleDeferred.await().getOrNull() ?: emptyList()
            val articles = articlesDeferred.await().getOrNull() ?: emptyList()
            val quote = quoteDeferred.await().getOrNull()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    events = events,
                    famousPeople = people,
                    articles = articles,
                    quote = quote,
                )
            }
        }
    }
}
