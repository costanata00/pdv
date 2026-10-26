package com.treinamento.pdv.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body dados: LoginRequest): LoginResponse

    // nao e suspend: o Authenticator do OkHttp roda de forma sincrona
    @POST("auth/refresh")
    fun refresh(@Body dados: RefreshBody): Call<RefreshResponse>

    //Mercadorias
    @GET("mercadorias")
    suspend fun mercadorias(@Query("busca") busca: String): List<Mercadoria>

    @POST("mercadorias")
    suspend fun criarMercadoria(@Body dados: MercadoriaBody): Mercadoria

    @PUT("mercadorias/{id}")
    suspend fun alterarMercadoria(@Path("id") id: Int, @Body dados: MercadoriaBody): Mercadoria

    @PUT("mercadorias/{id}")
    suspend fun mudarAtivoMercadoria(@Path("id") id: Int, @Body dados: AtivoBody): Mercadoria

    @DELETE("mercadorias/noStock")
    suspend fun deletarSemEstoque()

    //Clientes
    @GET("clientes")
    suspend fun clientes(@Query("busca") busca: String): List<Cliente>

    @POST("clientes")
    suspend fun criarCliente(@Body dados: ClienteBody): Cliente

    @PUT("clientes/{id}")
    suspend fun alterarCliente(@Path("id") id: Int, @Body dados: ClienteBody): Cliente

    @PUT("clientes/{id}")
    suspend fun mudarAtivoCliente(@Path("id") id: Int, @Body dados: AtivoBody): Cliente

    //Vendas
    @POST("vendas")
    suspend fun criarVenda(@Body dados: VendaBody): Venda

    @GET("vendas")
    suspend fun vendas(@Query("busca") busca: String): List<Venda>

    @GET("vendas/{id}")
    suspend fun venda(@Path("id") id: Int): Venda

    @PUT("vendas/{id}/cancelar")
    suspend fun cancelarVenda(@Path("id") id: Int)

    //Usuarios
    @POST("usuarios")
    suspend fun criarUsuario(@Body dados: UsuarioBody)

}

object Api {
    private val trava = Any()

    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { cadeia ->
            val pedido = cadeia.request().newBuilder()
            val token = Sessao.token
            if (token != null) {
                pedido.header("Authorization", "Bearer " + token)
            }
            cadeia.proceed(pedido.build())
        }
        // Quando a resposta for 401, renova o token e repete o pedido
        .authenticator { _, resposta -> renovarToken(resposta) }
        .build()

    private fun renovarToken(resposta: Response): Request? {
        val caminho = resposta.request.url.encodedPath
        if (caminho.endsWith("auth/login") || caminho.endsWith("auth/refresh")) return null
        if (resposta.priorResponse != null) return null //

        // so uma thread renova por vez; as outras esperam e usam o token novo
        synchronized(trava) {
            val tokenUsado = resposta.request.header("Authorization")?.removePrefix("Bearer ")
            val atual = Sessao.token
            if (atual != null && atual != tokenUsado) {
                return resposta.request.newBuilder().header("Authorization", "Bearer $atual").build()
            }

            val refresh = Sessao.refreshToken ?: return null
            val nova = try {
                servico().refresh(RefreshBody(refresh)).execute()
            } catch (e: IOException) {
                return null
            }
            val corpo = nova.body()
            if (!nova.isSuccessful || corpo == null) return null

            // nao chama Sessao.sair() aqui: o TelaBase so manda pro login se ainda houver token
            Sessao.token = corpo.token
            Sessao.refreshToken = corpo.refreshToken
            return resposta.request.newBuilder().header("Authorization", "Bearer ${corpo.token}").build()
        }
    }

    fun servico(): ApiService {
        return Retrofit.Builder()
            .baseUrl(Sessao.servidor)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}