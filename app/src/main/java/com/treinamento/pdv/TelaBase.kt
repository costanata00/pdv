package com.treinamento.pdv

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.treinamento.pdv.data.ErroApi
import com.treinamento.pdv.data.Sessao
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

open class TelaBase : AppCompatActivity() {
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true

    }

    // Mostra uma mensagem curta que some sozinha
    fun aviso(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }

    fun mostrarErro(erro: Exception) {
        // A tela foi fechada no meio do pedido. Não é um erro de verdade.
        if (erro is CancellationException) {
            throw erro
        }

        // A API recusou o crachá (ele vence depois de algumas horas): volta ao login
        if (erro is HttpException && erro.code() == 401 && Sessao.token != null) {
            Sessao.sair()
            aviso("Sua sessão terminou. Entre novamente.")
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Não deu certo")
            .setMessage(textoDoErro(erro))
            .setPositiveButton("OK", null)
            .show()
    }

    private fun textoDoErro(erro: Exception): String {
        //A api respondeu um erro
        if (erro is HttpException) {
            try {
                val corpo = erro.response()?.errorBody()?.string()
                val erroApi = Gson().fromJson(corpo, ErroApi::class.java)
                if (erroApi != null && erroApi.error != null) {
                    return erroApi.error
                }
            } catch (e: Exception) {
                // não conseguiu ler a mensagem: usa as frases abaixo
            }
            if (erro.code() >= 500) {
                return "A API está indisponível no momento. Tente de novo em instantes."
            }
            return "A API respondeu com o erro ${erro.code()}."
        }
        // erro de saida ou entrada da api
        if (erro is IOException) {
            return "Não foi possível conectar à API.\n\nConfira se a API está ligada e se o endereço está certo:\n${Sessao.servidor}"
        }
        if (erro is IllegalArgumentException) {
            return "O endereço da API parece inválido:\n${Sessao.servidor}"
        }
        return "Erro inesperado: ${erro.message}"
    }
}