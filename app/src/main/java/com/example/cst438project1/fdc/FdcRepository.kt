package com.example.cst438project1.fdc

import com.example.cst438project1.BuildConfig
import com.example.cst438project1.FoodEntry
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FdcRepository(
    // Retrofit builds the HTTP client once here, and the screens just call searchFoods().
    private val api: FdcApiService = Retrofit.Builder()
        .baseUrl("https://api.nal.usda.gov/fdc/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FdcApiService::class.java),
    private val apiKey: String = BuildConfig.FDC_API_KEY
) {
    suspend fun searchFoods(query: String): Result<List<FoodEntry>> {
        if (query.trim().isBlank() || apiKey.isBlank()) {
            return if (apiKey.isBlank()) {
                Result.failure(
                    IllegalStateException(
                        "Set FDC_API_KEY in gradle.properties, local.properties, or as an environment variable."
                    )
                )
            } else {
                Result.success(emptyList())
            }
        }
        return runCatching {
            api.searchFoods(query = query.trim(), apiKey = apiKey)
                .foods
                .map { it.toFoodEntry() }
                .filter { it.name.isNotBlank() }
        }
    }

}
