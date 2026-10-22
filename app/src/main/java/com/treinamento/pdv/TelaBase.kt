package com.treinamento.pdv

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.treinamento.pdv.data.ErroApi
import com.treinamento.pdv.data.Sessao
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

open class TelaBase : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // A partir do Android 15 o app sempre desenha atrás das barras do sistema.
        // Ligamos isso em todas as versões e tratamos os espaços em comBarraDeTitulo().
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    // Toda tela que chamar setContentView ganha a barra de título no topo
    // e fica longe da barra de status, da barra de navegação e do teclado.
    override fun setContentView(layoutResID: Int) {
        setContentView(layoutInflater.inflate(layoutResID, FrameLayout(this), false))
    }

    override fun setContentView(view: View) {
        super.setContentView(comBarraDeTitulo(view))
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        setContentView(view)
    }

    private fun comBarraDeTitulo(conteudo: View): View {
        val valor = TypedValue()
        theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, valor, true)
        val alturaBarra = TypedValue.complexToDimensionPixelSize(valor.data, resources.displayMetrics)

        val barra = MaterialToolbar(this)
        val topo = AppBarLayout(this)
        topo.addView(barra, AppBarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, alturaBarra))

        val raiz = LinearLayout(this)
        raiz.orientation = LinearLayout.VERTICAL
        raiz.addView(topo, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        raiz.addView(conteudo, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // A barra passa a ser a action bar: title e seta de voltar continuam funcionando
        setSupportActionBar(barra)

        ViewCompat.setOnApplyWindowInsetsListener(raiz) { _, insets ->
            val sistema = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())

            // barra de status fica atrás da barra de título (mesma cor)
            topo.setPadding(sistema.left, sistema.top, sistema.right, 0)

            // conteúdo não entra embaixo da barra de navegação nem do teclado
            val params = conteudo.layoutParams as LinearLayout.LayoutParams
            params.setMargins(sistema.left, 0, sistema.right, maxOf(sistema.bottom, teclado.bottom))
            conteudo.layoutParams = params

            WindowInsetsCompat.CONSUMED
        }
        return raiz
    }

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