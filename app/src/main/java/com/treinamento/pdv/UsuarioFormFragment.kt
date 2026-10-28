package com.treinamento.pdv

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.MercadoriaBody
import com.treinamento.pdv.data.Usuario
import com.treinamento.pdv.data.UsuarioBody
import com.treinamento.pdv.databinding.FragmentUsuarioFormBinding
import kotlinx.coroutines.launch

class UsuarioFormFragment : FragmentBase() {

    private var _b: FragmentUsuarioFormBinding? = null
    private val b get() = _b!!

    private var usuario: Usuario? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentUsuarioFormBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.salvar.setOnClickListener { salvar() }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

    private fun salvar() {
        val dados = lerCampos() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Api.servico().criarUsuario(dados)
                aviso("Usuário salvo.")
                voltar()
            } catch (e: Exception) {
                mostrarErro(e)
            }
        }
    }

    private fun lerCampos(): UsuarioBody? {
        val nome = b.nome.text.toString().trim()
        val email = b.email.text.toString().trim()
        val senha = b.senha.text.toString()
        if (nome.isEmpty()) {
            aviso("Informe o nome.")
            return null
        }
        if (email.isEmpty() || (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            aviso("Informe um email válido.")
            return null
        }
        if (senha.isEmpty()) {
            aviso("Informe a senha.")
            return null
        }
        return UsuarioBody(nome, email, senha)
    }

}
