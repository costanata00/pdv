package com.treinamento.pdv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.AtivoBody
import com.treinamento.pdv.data.Cliente
import com.treinamento.pdv.data.ItemCarrinho
import com.treinamento.pdv.data.Mercadoria
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.databinding.FragmentListaBinding
import kotlinx.coroutines.launch
import kotlin.text.get

class ClienteListaFragment : FragmentBase() {
    private var carrinho: List<ItemCarrinho>? = null
    private var filtro = "ativas";
    private var clientes: List<Cliente> = ArrayList()

    private var _b: FragmentListaBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentListaBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("Clientes")

        b.buscar.setOnClickListener {
            carregar()
        }

        b.novo.setOnClickListener {
            abrir(ClienteFormFragment())
        }

        b.filtro.visibility = View.VISIBLE
        b.filtro.setOnClickListener {
            if (filtro == "todas") {
                filtro = "ativas"
                b.filtro.text = "Ativas"
            }else if (filtro == "ativas"){
                filtro = "inativas"
                b.filtro.text = "Inativas"
            }else{
                filtro = "todas"
                b.filtro.text = "Todas"
            }
            carregar()
        }

        b.lista.setOnItemClickListener { _, _, posicao, _ ->
            val tela = ClienteFormFragment()
            tela.arguments = bundleOf("cliente" to Gson().toJson(clientes[posicao]))
            abrir(tela)
        }

        b.lista.setOnItemLongClickListener { _, _, posicao, _ ->
            val c = clientes[posicao]
            val texto = if (c.ativo) "Desativar" else "Ativar"
            val texto2 = if (c.ativo) "Desativado" else "Ativado"
            AlertDialog.Builder(requireContext())
                .setPositiveButton(texto) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            Api.servico().mudarAtivoCliente(c.id, AtivoBody(!c.ativo))
                            aviso(texto2)
                            carregar()
                        } catch (e: Exception) {
                            mostrarErro(e)
                        }
                    }
                }
                .setNegativeButton( "Editar") { _, _ ->
                    val tela = ClienteFormFragment()
                    tela.arguments = bundleOf("cliente" to Gson().toJson(clientes[posicao]))
                    abrir(tela)
                }
                .setNeutralButton("Sair", null)
                .create()
                .show()
            true
        }

        b.buscar.setOnLongClickListener { view ->

            true
        }


        val json = arguments?.getString("carrinho")
        if (json != null) {
            carrinho = Gson().fromJson(json, Array<ItemCarrinho>::class.java).toList()
            b.filtro.visibility = View.GONE
            val tela = CheckoutFragment()
            b.lista.setOnItemClickListener { _, _, posicao, _ ->
                tela.arguments = bundleOf(
                    "carrinho" to json, "cliente" to Gson().toJson(clientes[posicao])
                )
                abrir(tela)
            }
            b.novo.text = "Prosseguir sem cliente"
            b.novo.setOnClickListener {
                tela.arguments = bundleOf("carrinho" to json)
                abrir(tela)
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

    override fun onResume() {
        super.onResume()
        carregar()
    }

    private fun carregar() {
        val parametro = b.busca.text.toString().trim()
        b.progresso.visibility = View.VISIBLE
        b.mensagem.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val todas = Api.servico().clientes(parametro)
                clientes = if (filtro == "ativas") {
                    todas.filter { it.ativo }
                } else if (filtro == "inativas") {
                    todas.filter { !it.ativo }
                } else {
                    todas
                }
                mostrarLista()
            } catch (e: Exception) {
                mostrarErro(e)
            }
            b.progresso.visibility = View.GONE
        }
    }

    private fun mostrarLista() {
        val linhas = ArrayList<String>()
        for (c in clientes) {
            var nome = c.nome
            var cpf = if (c.cpf_cnpj.isNullOrEmpty()) "Campo Vazio" else c.cpf_cnpj
            var telefone = if (c.telefone.isNullOrEmpty()) "Campo Vazio" else c.telefone
            var email = if (c.email.isNullOrEmpty()) "Campo Vazio" else c.email

            linhas.add("$nome\nCPF/CNPJ. $cpf\nTelefone: $telefone\nEmail: $email")
        }
        b.lista.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, linhas)
        if (clientes.isEmpty()){
            b.mensagem.visibility = View.VISIBLE
        }else{
            b.mensagem.visibility = View.GONE
        }
    }


}





