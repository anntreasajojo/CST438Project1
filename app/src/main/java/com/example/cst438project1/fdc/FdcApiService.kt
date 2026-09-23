package com.example.cst438project1.fdc

import retrofit2.http.GET
import retrofit2.http.Query

interface FdcApiService {
    @GET("v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("pageSize") pageSize: Int = 10,
        @Query("api_key") apiKey: String,
    ): FdcSearchResponse
}
