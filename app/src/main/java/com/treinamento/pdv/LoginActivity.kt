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
    //classe que reutilizavel para bindar.
    private lateinit var binding: ActivityLoginBinding

    // Roda quando a tela abre
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Se já existe um crachá guardado, pula o login
        if (Sessao.token != null) {
            abrirMenu()
            return
        }

        // Liga este arquivo ao desenho activity_login.xml
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.servidor.setText(Sessao.servidor)

        // Quando tocarem no botão, rode a função entrar()
        binding.entrar.setOnClickListener {
            entrar()
        }
    }

    private fun entrar() {
        // 1. Lê o que foi digitado
        val email = binding.email.text.toString().trim()
        val senha = binding.senha.text.toString()

        // 2. Confere se está preenchido
        if (email.isEmpty() || senha.isEmpty()) {
            aviso("Informe o e-mail e a senha.")
            return
        }

        // 3. Guarda o endereço da API digitado
        Sessao.servidor = binding.servidor.text.toString()

        // 4. Mostra a rodinha e trava o botão
        binding.progresso.visibility = View.VISIBLE
        binding.entrar.isEnabled = false

        // 5. Faz o pedido à API, em segundo plano
        lifecycleScope.launch {
            try {
                //           cria a conexão     cria objeto pro body e usa rota login
                val resposta = Api.servico().login(LoginRequest(email, senha))
                // Deu certo: guarda o crachá e abre o menu
                Sessao.token = resposta.token
                Sessao.nomeUsuario = resposta.usuario.nome
                abrirMenu()
            } catch (e: Exception) {
                // Deu errado: mostra o motivo
                mostrarErro(e)
            }
            // 6. Esconde a rodinha e libera o botão
            binding.progresso.visibility = View.GONE
            binding.entrar.isEnabled = true
        }
    }

    private fun abrirMenu() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()   // fecha o login, para o botão voltar não retornar a ele
    }
}