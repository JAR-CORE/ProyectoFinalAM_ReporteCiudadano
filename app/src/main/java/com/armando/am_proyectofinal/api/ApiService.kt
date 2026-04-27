package com.armando.am_proyectofinal.api

import com.armando.am_proyectofinal.model.ReporteRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("reporte.php")
    suspend fun enviarReporte(
        @Header("Authorization") auth: String = "Bearer a0f4dcad-5903-482f-8982-88ec8bc6156e",
        @Body reporte: ReporteRequest
    ): Response<Any>
}