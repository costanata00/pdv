package com.treinamento.pdv.data
import android.content.Context
import android.content.SharedPreferences


object
Sessao {
    private lateinit var gaveta: SharedPreferences
    fun iniciar(contexto: Context) {
        gaveta = contexto.getSharedPreferences("sessao", Context.MODE_PRIVATE)
    }


    // o token + o usuario logado
    var token: String?
        get() = gaveta.getString("token", null)
        set(valor) { gaveta.edit().putString("token", valor).apply() }
    var refreshToken: String?
        get() = gaveta.getString("refreshToken", null)
        set(valor) { gaveta.edit().putString("refreshToken", valor).apply() }
    var nomeUsuario: String?
        get() = gaveta.getString("nome", "")
        set(valor) { gaveta.edit().putString("nome", valor).apply() }
    // saldo em dinheiro na gaveta (SharedPreferences nao guarda Double, entao vai como bits num Long)
    var caixa: Double
        get() = Double.fromBits(gaveta.getLong("saldoCaixa", 0.0.toRawBits()))
        set(valor) { gaveta.edit().putLong("saldoCaixa", valor.toRawBits()).apply() }

    var servidor: String
        get() = gaveta.getString("servidor", null) ?: "http://127.0.0.1:3000/"
        set(valor) {
            var endereco = valor.trim()
            if (!endereco.startsWith("http")) { endereco = "http://" +
                    endereco }
            if (!endereco.endsWith("/")) { endereco = endereco + "/" }
            gaveta.edit().putString("servidor", endereco).apply()
        }
    fun sair() {
        gaveta.edit().remove("token").remove("refreshToken").remove("nome").apply()
    }
}