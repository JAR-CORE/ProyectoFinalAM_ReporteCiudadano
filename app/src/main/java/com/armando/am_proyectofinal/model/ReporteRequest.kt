package com.armando.am_proyectofinal.model

data class ReporteRequest(
    val nombre: String,
    val direccion: String,
    val colonia: String,
    val celular: String,
    val correo: String,
    val tipo: String,
    val descripcion: String,
    val imagen: String?
)