package com.treinamento.pdv

import android.R
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
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

    fun aviso(texto: String) {
        //Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
        val snack = Snackbar.make(findViewById<View>(R.id.content), texto, Snackbar.LENGTH_SHORT)
//        val params = snack.view.layoutParams as FrameLayout.LayoutParams
//        params.gravity = Gravity.CENTER
//        snack.view.layoutParams = params
        snack.show()
    }

    fun mostrarErro(erro: Exception) {
        if (erro is CancellationException) {
            throw erro
        }

        if (erro is HttpException && erro.code() == 401 && Sessao.token != null) {
            Sessao.sair()
            Toast.makeText(this, "Sua sessão terminou. Entre novamente.", Toast.LENGTH_SHORT).show()

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
        if (erro is HttpException) {
            try {
                val corpo = erro.response()?.errorBody()?.string()
                val erroApi = Gson().fromJson(corpo, ErroApi::class.java)
                if (erroApi != null && erroApi.error != null) {
                    return erroApi.error
                }
            } catch (e: Exception) {
            }
            if (erro.code() >= 500) {
                return "A API está indisponível no momento. Tente de novo em instantes."
            }
            return "A API respondeu com o erro ${erro.code()}."
        }
        if (erro is IOException) {
            return "Não foi possível conectar à API.\n\nConfira se a API está ligada e se o endereço está certo:\n${Sessao.servidor}"
        }
        if (erro is IllegalArgumentException) {
            return "O endereço da API parece inválido:\n${Sessao.servidor}"
        }
        return "Erro inesperado: ${erro.message}"
    }
}