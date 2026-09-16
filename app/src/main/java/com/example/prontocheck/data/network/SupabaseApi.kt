package com.example.prontocheck.data.network


import com.example.prontocheck.data.model.Empleado
import com.example.prontocheck.data.model.Asistencia
import com.example.prontocheck.data.model.Incidencia
import com.example.prontocheck.data.model.LoginRequest
import com.example.prontocheck.data.model.LoginResponse
import com.example.prontocheck.data.model.Producto
import com.example.prontocheck.data.model.PuntoAcceso
import com.example.prontocheck.data.model.ResumenEmpleado
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.prontocheck.data.model.UltimaAsistencia


interface SupabaseApi {

    // 1. AUTENTICACIÓN
    @POST("auth/v1/token?grant_type=password")
    suspend fun loginUsuario(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/v1/signup")
    suspend fun registrarEnAuth(@Body request: LoginRequest): Response<LoginResponse>

    // 2. GESTIÓN DE EMPLEADOS
    @GET("rest/v1/empleados")
    suspend fun getEmpleados(
        @Query("select") select: String = "*",
        @Header("Range") range: String = "0-99"
    ): Response<List<Empleado>>

    @GET("rest/v1/empleados")
    suspend fun getUserByEmail(
        @Query("email") emailFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<Empleado>>

    @GET("rest/v1/empleados")
    suspend fun getEmpleadosRegistro(
        @Query("select") select: String = "*",
        @Query("activo") activo: String = "eq.true",
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<List<Empleado>>

    @POST("rest/v1/empleados")
    suspend fun registrarEmpleado(@Body empleado: Empleado): Response<Unit>

    // IMPORTANTE: Para actualizar, el Body debe ser un Map o el objeto,
    // pero el Query ID debe coincidir con la columna en Supabase
    @PATCH("rest/v1/empleados")
    suspend fun actualizarEmpleado(
        @Query("id") idFilter: String,
        @Body campos: Any
    ): Response<Unit>

    @DELETE("rest/v1/empleados")
    suspend fun eliminarFisicamente(
        @Query("id") idFilter: String
    ): Response<Unit>

    // 3. ASISTENCIA
    @POST("rest/v1/asistencia")
    suspend fun registrarAsistencia(@Body asistencia: Asistencia): Response<Unit>

    @GET("rest/v1/asistencia")
    suspend fun getUltimaAsistencia(
        @Query("empleado_id") empleadoId: String,
        @Query("select") select: String = "tipo,fecha_hora",
        @Query("order") order: String = "fecha_hora.desc",
        @Query("limit") limit: Int = 1
    ): Response<List<UltimaAsistencia>>

    @GET("rest/v1/asistencia")
    suspend fun getAsistenciaPorPeriodo(
        @Query("fecha") inicio: String,
        @Query("fecha") fin: String,
        @Query("select") select: String = "*"
    ): Response<List<Asistencia>>

    // 4. GEOCERCAS / PUNTOS DE ACCESO
    @GET("rest/v1/puntos_acceso")
    suspend fun getPuntosAcceso(
        @Query("select") select: String = "*",
        @Query("activo") activo: String = "eq.true"
    ): Response<List<PuntoAcceso>>

    @POST("rest/v1/puntos_acceso")
    suspend fun registrarPuntoAcceso(
        @Body punto: PuntoAcceso
    ): Response<Unit>

    @DELETE("rest/v1/puntos_acceso")
    suspend fun eliminarPuntoAcceso(
        @Query("id_punto_acceso") idFilter: String // Usamos el nombre de tu columna ID en la DB
    ): Response<Unit>

    // En SupabaseApi.kt

    @GET("rest/v1/asistencia") // Asegúrate de que este sea el nombre de tu tabla en Supabase
    suspend fun getReporte(
        @Query("fecha") inicio: String,           // Recibirá "gte.2024-05-01"
        @Query("fecha") fin: String,              // Recibirá "lte.2024-05-31"
        @Query("residencial") residencial: String? = null, // Recibirá "eq.Altai" o null
        @Query("select") select: String = "*"     // Trae todas las columnas
    ): Response<List<ResumenEmpleado>>

    // --- NUEVAS RUTAS DE INCIDENCIAS ---

    // --- RUTAS DE INCIDENCIAS ---

    @GET("rest/v1/incidencias")
    suspend fun getIncidencias(
        // Si quieres el nombre del empleado, asegúrate que la tabla se llame 'empleados' en minúsculas
        // Si falla (trae []), deja solo "*"
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<Incidencia>>

    @POST("rest/v1/incidencias")
    suspend fun registrarIncidencia(
        @Body incidencia: Incidencia
    ): Response<Unit>

    @PATCH("rest/v1/incidencias")
    suspend fun actualizarEstadoIncidencias(
        @Query("id") idFilter: String,   // Recibirá "eq.1"
        @Body campos: Map<String, String> // Recibirá {"estado": "Justificada"}
    ): Response<Unit>

    @GET("rest/v1/empleados")
    suspend fun getEmpleadoPorCodigo(
        @Query("codigo_empleado") codigoFiltro: String // Debe coincidir con el nombre de la columna en Supabase
    ): retrofit2.Response<List<com.example.prontocheck.data.model.Empleado>>

    // 5. ADMIN AUTH (Requiere Service Role Key si se usa desde la App, cuidado)
    @DELETE("auth/v1/admin/users/{id}")
    suspend fun eliminarUsuarioAuth(@Path("id") userId: String): Response<Unit>

    @GET("empleados")
    suspend fun getEmpleadosPorResidencial(
        @Query("residencial") residencial: String,
        @Query("activo") activo: String = "eq.true",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<List<Empleado>>


    // Dentro de tu interface SupabaseApi
    @GET("rest/v1/inventario")
    suspend fun getInventario(
        @Query("select") select: String = "*"
    ): Response<List<Producto>>

    @PATCH("rest/v1/inventario")
    suspend fun actualizarStock(
        @Query("id") id: String, // Se enviará como "eq.ID_AQUÍ"
        @Body body: Map<String, Int>
    ): Response<Unit>

    @POST("rest/v1/inventario")
    suspend fun insertarProducto(
        @Body producto: Producto
    ): Response<Unit>
}
