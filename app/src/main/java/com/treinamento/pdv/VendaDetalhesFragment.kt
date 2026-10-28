package com.treinamento.pdv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.Venda
import com.treinamento.pdv.databinding.FragmentDetalhesVendaBinding
import kotlinx.coroutines.launch

class VendaDetalhesFragment : FragmentBase() {
    private var venda: Venda? = null
    private var vendaID: Int? = null
    private var _b: FragmentDetalhesVendaBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentDetalhesVendaBinding.inflate(inflater, container, false)
        return b.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("Detalhes da Venda")
        vendaID = arguments?.getInt("vendaID")
        b.lista.layoutManager = LinearLayoutManager(requireContext())

        b.imprimir.setOnClickListener {
            val v = venda ?: return@setOnClickListener
            b.imprimir.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                val ok = ImpressoraRede.imprimeVenda(requireContext(), venda = v)
                if (!ok) Toast.makeText(requireContext(), "Falha na impressão", Toast.LENGTH_SHORT)
                    .show()
                _b?.imprimir?.isEnabled = true
            }
        }


    }


    override fun onResume() {
        super.onResume()
        carregarVenda()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

    private fun carregarVenda() {
        val id = vendaID ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                venda = Api.servico().venda(id)
                mostrarLista()
            } catch (e: Exception) {
                mostrarErro(e)
            }
        }
    }


    private fun mostrarLista() {
        val v = venda ?: return
        val linhas = ArrayList<String>()
        val imagens = ArrayList<String?>()
        val itens = v.itens_vendas ?: emptyList()

        for (i in itens) {
            linhas.add(
                "${i.mercadorium?.descricao} - ${i.quantidade} x ${moeda(i.mercadorium?.preco ?: 0.0)} = ${
                    moeda(
                        i.valor_total
                    )
                }"
            )
            imagens.add(i.mercadorium?.imagem_url)
        }
        b.lista.adapter = LinhaAdapter(linhas, {}, {}, imagens)
        if (itens.isEmpty()) {
            b.mensagem.visibility = View.VISIBLE
        } else {
            b.mensagem.visibility = View.GONE
        }
        b.vendaID.text = "Venda #${v.id}"
        b.cliente.text = "Cliente: ${v.cliente?.nome ?: "Não informado"}"
        b.total.text = "Total: ${moeda(v.valor_total)}"
        b.status.text = "${v.status?.uppercase() ?: "Não informado"}"
        b.formaPag.text = "Pagamento: ${v.forma_pag?.uppercase() ?: "Não informado"}"
        b.usuario.text = "Usuário: ${v.usuario?.nome ?: "Não informado"}"
        b.dataHora.text = "Data: ${dataHora(v.data_venda)}"

    }
//    private fun mostrarLista() {
//        val v = venda ?: return
//        val linhas = ArrayList<String>()
//        val itens = v.itens_vendas ?: emptyList()
//        for (i in itens) {
//            linhas.add("${i.mercadorium?.descricao} - ${i.quantidade} x ${moeda(i.mercadorium!!.preco)} = ${moeda(i.valor_total)}")
//        }
//        b.lista.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, linhas)
//        if (itens.isEmpty()) {
//            b.mensagem.visibility = View.VISIBLE
//        } else {
//            b.mensagem.visibility = View.GONE
//        }
//        b.vendaID.text = "Venda #${v.id}"
//        b.cliente.text = "Cliente: ${v.cliente?.nome ?: "Não informado"}"
//        b.total.text = "Total: ${moeda(v.valor_total)}"
//        b.status.text = "Status: ${v.status?.uppercase() ?: "Não informado"}"
//        b.formaPag.text ="Pagamento: ${v.forma_pag?.uppercase() ?: "Não informado"}"
//        b.usuario.text = "Usuário: ${v.usuario?.nome ?: "Não informado"}"
//        b.dataHora.text = "Data: ${dataHora(v.data_venda)}"
//    }
}





