package com.treinamento.pdv

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.LoginRequest
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : TelaBase() {
    //binding serve para ligar o arquivo xml ao codigo
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //checa se tem token
        if (Sessao.token != null) {
            abrirMenu()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.servidor.setText(Sessao.servidor)

        binding.entrar.setOnClickListener {
            entrar()
        }
    }

    private fun entrar() {
        val email = binding.email.text.toString().trim()
        val senha = binding.senha.text.toString()
        if (email.isEmpty() || senha.isEmpty()) {
            aviso("Informe o e-mail e a senha.")
            return
        }
        Sessao.servidor = binding.servidor.text.toString()

        binding.progresso.visibility = View.VISIBLE
        binding.entrar.isEnabled = false

        lifecycleScope.launch {
            try {
                //           cria a conexão-----------cria objeto pro body e usa rota login
                val resposta = Api.servico().login(LoginRequest(email, senha))
                Sessao.token = resposta.token
                Sessao.refreshToken = resposta.refreshToken
                Sessao.nomeUsuario = resposta.usuario.nome
                abrirMenu()
            } catch (e: Exception) {
                mostrarErro(e)
            }
            binding.progresso.visibility = View.GONE
            binding.entrar.isEnabled = true
        }
    }

    private fun abrirMenu() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}