package com.lichso.app.feature.content

import com.lichso.app.data.remote.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(private val api: LichSoApi) {
    suspend fun getBanners(): Result<List<Banner>> = api.getBanners()
    suspend fun getPopups(): Result<List<Popup>> = api.getPopups()
    suspend fun getTodayContent(): Result<DayContentResponse> = api.getTodayContent()
    suspend fun getEventsByDate(month: Int, day: Int): Result<List<ContentEvent>> = api.getEventsByDate(month, day)
    suspend fun getFamousPeopleByBirthday(month: Int, day: Int): Result<List<FamousPerson>> = api.getFamousPeopleByBirthday(month, day)
    suspend fun getArticles(page: Int = 1, limit: Int = 20, category: String? = null): Result<List<Article>> = api.getArticles(page, limit, category)
    suspend fun getArticle(id: String): Result<Article> = api.getArticle(id)
    suspend fun getFestivalsByLunarDate(month: Int, day: Int): Result<List<Festival>> = api.getFestivalsByLunarDate(month, day)
    suspend fun getTodayQuote(): Result<Quote> = api.getTodayQuote()
}
