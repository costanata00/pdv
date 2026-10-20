package com.treinamento.pdv.data
import android.content.Context
import android.content.SharedPreferences


object Sessao {
    private lateinit var gaveta: SharedPreferences
    fun iniciar(contexto: Context) {
        gaveta = contexto.getSharedPreferences("sessao", Context.MODE_PRIVATE)
    }


    // o token + o usuario logado
    var token: String?
        get() = gaveta.getString("token", null)
        set(valor) { gaveta.edit().putString("token", valor).apply() }
    var nomeUsuario: String?
        get() = gaveta.getString("nome", "")
        set(valor) { gaveta.edit().putString("nome", valor).apply() }

    //o servidor salvo no login
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
        gaveta.edit().remove("token").remove("nome").apply()
    }
}