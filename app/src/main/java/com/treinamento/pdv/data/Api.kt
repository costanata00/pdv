package com.treinamento.pdv.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// O CARDÁPIO: cada função é um pedido que o app pode fazer à sua API.
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body dados: LoginRequest): LoginResponse

    // ----- Mercadorias -----
    @GET("mercadorias")
    suspend fun mercadorias(@Query("busca") busca: String): List<Mercadoria>

    @POST("mercadorias")
    suspend fun criarMercadoria(@Body dados: MercadoriaBody): Mercadoria

    @PUT("mercadorias/{id}")
    suspend fun alterarMercadoria(@Path("id") id: Int, @Body dados: MercadoriaBody): Mercadoria

    @PUT("mercadorias/{id}")
    suspend fun mudarAtivoMercadoria(@Path("id") id: Int, @Body dados: AtivoBody): Mercadoria

    // ----- Clientes -----
    @GET("clientes")
    suspend fun clientes(@Query("busca") busca: String): List<Cliente>

    @POST("clientes")
    suspend fun criarCliente(@Body dados: ClienteBody): Cliente

    @PUT("clientes/{id}")
    suspend fun alterarCliente(@Path("id") id: Int, @Body dados: ClienteBody): Cliente

    @PUT("clientes/{id}")
    suspend fun mudarAtivoCliente(@Path("id") id: Int, @Body dados: AtivoBody): Cliente

    // ----- Vendas -----
    @POST("vendas")
    suspend fun criarVenda(@Body dados: VendaBody): Venda

    @GET("vendas")
    suspend fun vendas(): List<Venda>

    @GET("vendas/{id}")
    suspend fun venda(@Path("id") id: Int): Venda
}

object Api {

    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { cadeia ->
            // Antes de cada pedido, mostra o token
            val pedido = cadeia.request().newBuilder()
            val token = Sessao.token
            if (token != null) {
                pedido.header("Authorization", "Bearer " + token)
            }
            cadeia.proceed(pedido.build())
        }
        .build()

    fun servico(): ApiService {
        return Retrofit.Builder()
            .baseUrl(Sessao.servidor)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}