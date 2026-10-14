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
import com.treinamento.pdv.data.AtivoBody
import com.treinamento.pdv.data.Cliente
import com.treinamento.pdv.data.ClienteBody
import com.treinamento.pdv.databinding.FragmentClienteFormBinding
import kotlinx.coroutines.launch

class ClienteFormFragment : FragmentBase() {

    private var _b: FragmentClienteFormBinding? = null
    private val b get() = _b!!

    private var cliente: Cliente? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentClienteFormBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cliente = clienteRecebido()

        val m = cliente
        if (m == null) {
            titulo("Novo cliente")
        } else {
            titulo("Editar cliente")
            preencherCampos(m)
        }

        b.salvar.setOnClickListener { salvar() }
        b.ativar.setOnClickListener { confirmarMudancaAtivo() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
    private fun clienteRecebido(): Cliente? {
        val texto = arguments?.getString("cliente") ?: return null
        return Gson().fromJson(texto, Cliente::class.java)
    }

    private fun preencherCampos(m: Cliente) {
        b.nome.setText(m.nome)
        b.cpfcnpj.setText(m.cpf_cnpj)
        b.telefone.setText(m.telefone)
        b.email.setText(m.email)

        b.ativar.visibility = View.VISIBLE
        b.ativar.text = if (m.ativo) "Desativar" else "Reativar"
    }


    private fun salvar() {
        val dados = lerCampos() ?: return

        b.salvar.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val m = cliente
                if (m == null) {
                    Api.servico().criarCliente(dados)
                } else {
                    Api.servico().alterarCliente(m.id, dados)
                }
                aviso("Cliente salvo.")
                voltar()
            } catch (e: Exception) {
                mostrarErro(e)
            }
            b.salvar.isEnabled = true
        }
    }

    private fun lerCampos(): ClienteBody? {
        val nome = b.nome.text.toString().trim()
        val cpfCnpj = b.cpfcnpj.text.toString().trim()
        val telefone = b.telefone.text.toString().trim()
        val email = b.email.text.toString().trim()

        if (nome.isEmpty()) {
            aviso("Informe o nome.")
            return null
        }
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            aviso("Informe um email válido.")
            return null
        }
        return ClienteBody(nome, cpfCnpj, telefone, email)
    }

    private fun confirmarMudancaAtivo() {
        val m = cliente ?: return

        val pergunta = if (m.ativo) {
            "Desativar este cliente? Ele não poderá mais comprar."
        } else {
            "Reativar este cliente?"
        }

        AlertDialog.Builder(requireContext())
            .setMessage(pergunta)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ -> mudarAtivo(m.id, !m.ativo) }
            .show()
    }
    private fun mudarAtivo(id: Int, ativo: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Api.servico().mudarAtivoCliente(id, AtivoBody(ativo))
                aviso("Cliente atualizado.")
                voltar()
            } catch (e: Exception) {
                mostrarErro(e)
            }
        }
    }
}
