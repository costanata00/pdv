package com.treinamento.pdv.data
import android.content.Context
import android.content.SharedPreferences


object Sessao {
    // "Gaveta" do celular onde os valores ficam salvos, mesmo fechando o app
    private lateinit var gaveta: SharedPreferences
    fun iniciar(contexto: Context) {
        gaveta = contexto.getSharedPreferences("sessao", Context.MODE_PRIVATE)
    }


    // O crachá do login. null = ninguém logado.
    var token: String?
        get() = gaveta.getString("token", null)
        set(valor) { gaveta.edit().putString("token", valor).apply() }
    var nomeUsuario: String?
        get() = gaveta.getString("nome", "")
        set(valor) { gaveta.edit().putString("nome", valor).apply() }



    // Endereço da API. O valor inicial serve para o emulador.
    var servidor: String
        get() = gaveta.getString("servidor", null) ?: "http://10.0.2.2:3000/"
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