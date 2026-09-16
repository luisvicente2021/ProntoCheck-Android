package com.example.prontocheck.di

import com.example.prontocheck.RelojRepository
import com.example.prontocheck.data.network.RetrofitClient
import com.example.prontocheck.data.repository.EmpleadoRepository
import com.example.prontocheck.data.repository.UserRepository
import com.example.prontocheck.ui.Incidencias.IncidenciasRepository
import com.example.prontocheck.ui.cuadrante.CuadranteRepository
import com.example.prontocheck.ui.gestionUbicaciones.GestionUbicacionesRepository
import com.example.prontocheck.ui.productos.InventarioRepository

/**
 * Contenedor manual de dependencias de la app.
 *
 * Por que existe: centraliza la creacion de repositorios y evita que cada
 * Activity conozca directamente el singleton de Retrofit.
 *
 * Ventaja: el proyecto gana un punto unico para cambiar implementaciones,
 * configurar mocks o migrar despues a Hilt/Koin sin tocar todas las pantallas.
 */
object AppDependencies {
    private val api = RetrofitClient.instance

    val userRepository by lazy { UserRepository(api) }
    val empleadoRepository by lazy { EmpleadoRepository(api) }
    val incidenciasRepository by lazy { IncidenciasRepository(api) }
    val gestionUbicacionesRepository by lazy { GestionUbicacionesRepository(api) }
    val inventarioRepository by lazy { InventarioRepository(api) }
    val relojRepository by lazy { RelojRepository(api) }
    val cuadranteRepository by lazy { CuadranteRepository(empleadoRepository) }
}
