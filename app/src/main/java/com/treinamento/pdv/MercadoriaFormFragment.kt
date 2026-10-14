package com.treinamento.pdv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.AtivoBody
import com.treinamento.pdv.data.Mercadoria
import com.treinamento.pdv.data.MercadoriaBody
import com.treinamento.pdv.databinding.FragmentMercadoriaFormBinding
import kotlinx.coroutines.launch

class MercadoriaFormFragment : FragmentBase() {

    private var _b: FragmentMercadoriaFormBinding? = null
    private val b get() = _b!!

    private var mercadoria: Mercadoria? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentMercadoriaFormBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mercadoria = mercadoriaRecebida()

        val m = mercadoria
        if (m == null) {
            titulo("Nova mercadoria")
        } else {
            titulo("Editar mercadoria")
            preencherCampos(m)
        }

        b.salvar.setOnClickListener { salvar() }
        b.ativar.setOnClickListener { confirmarMudancaAtivo() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
    private fun mercadoriaRecebida(): Mercadoria? {
        val texto = arguments?.getString("mercadoria") ?: return null
        return Gson().fromJson(texto, Mercadoria::class.java)
    }

    private fun preencherCampos(m: Mercadoria) {
        b.codigo.setText(m.codigo)
        b.descricao.setText(m.descricao)
        b.preco.setText(m.preco.toString())
        b.estoque.setText(m.estoque.toString())

        b.ativar.visibility = View.VISIBLE
        b.ativar.text = if (m.ativo) "Desativar" else "Reativar"
    }


    private fun salvar() {
        val dados = lerCampos() ?: return

        b.salvar.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val m = mercadoria
                if (m == null) {
                    Api.servico().criarMercadoria(dados)
                } else {
                    Api.servico().alterarMercadoria(m.id, dados)
                }
                aviso("Mercadoria salva.")
                voltar()
            } catch (e: Exception) {
                mostrarErro(e)
            }
            b.salvar.isEnabled = true
        }
    }

    private fun lerCampos(): MercadoriaBody? {
        val codigo = b.codigo.text.toString().trim()
        val descricao = b.descricao.text.toString().trim()
        val preco = b.preco.text.toString().replace(",", ".").toDoubleOrNull()
        val estoque = b.estoque.text.toString().toIntOrNull()

        if (codigo.isEmpty() || descricao.isEmpty()) {
            aviso("Informe o código e a descrição.")
            return null
        }
        if (preco == null || preco < 0) {
            aviso("Informe um preço válido.")
            return null
        }
        if (estoque == null || estoque < 0) {
            aviso("Informe o estoque.")
            return null
        }
        return MercadoriaBody(codigo, descricao, preco, estoque)
    }

    private fun confirmarMudancaAtivo() {
        val m = mercadoria ?: return

        val pergunta = if (m.ativo) {
            "Desativar esta mercadoria? Ela não poderá mais ser vendida."
        } else {
            "Reativar esta mercadoria?"
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
                Api.servico().mudarAtivoMercadoria(id, AtivoBody(ativo))
                aviso("Mercadoria atualizada.")
                voltar()
            } catch (e: Exception) {
                mostrarErro(e)
            }
        }
    }
}
