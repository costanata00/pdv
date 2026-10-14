package com.treinamento.pdv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.ItemCarrinho
import com.treinamento.pdv.data.Mercadoria
import com.treinamento.pdv.databinding.FragmentVendaBinding
import com.treinamento.pdv.databinding.ItemVendaBinding
import kotlinx.coroutines.launch

class VendaFragment : FragmentBase() {

    private var _b: FragmentVendaBinding? = null
    private val b get() = _b!!

    private var mercadorias: List<Mercadoria> = ArrayList()
    private var clienteId: Int? = null
    private var itensCarrinho: MutableList<ItemCarrinho> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentVendaBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("Venda")

        b.buscar.setOnClickListener {
            carregar()
        }
    }

    private fun carregar() {
        val parametro = b.busca.text.toString().trim()
        b.progresso.visibility = View.VISIBLE
        b.mensagem.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val todas = Api.servico().mercadorias(parametro)
                mercadorias = todas.filter { m -> m.ativo }
                mostrarLista()
            } catch (e: Exception) {
                mostrarErro(e)
            }
            b.progresso.visibility = View.GONE
        }
    }

    private fun mostrarLista() {
        b.lista.adapter = MercadoriaAdapter()
        if (mercadorias.isEmpty()) {
            b.mensagem.visibility = View.VISIBLE
        } else {
            b.mensagem.visibility = View.GONE
        }
    }

    private fun adicionar(m: Mercadoria) {
        val existente = itensCarrinho.find { it.mercadoria.id == m.id }
        if (existente != null) {
            existente.quantidade++
        } else {
            itensCarrinho.add(ItemCarrinho(m, 1))
        }
        val quantidade = existente?.quantidade ?: 1
        aviso("${m.descricao} adicionado ($quantidade)")
    }

    private inner class MercadoriaAdapter :
        ArrayAdapter<Mercadoria>(requireContext(), 0, mercadorias) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = if (convertView == null) {
                ItemVendaBinding.inflate(layoutInflater, parent, false)
            } else {
                ItemVendaBinding.bind(convertView)
            }

            val m = mercadorias[position]
            item.nomeItem.text = m.descricao
            item.precoItem.text = moeda(m.preco)
            item.estoqueItem.text = "Estoque: ${m.estoque}"
            item.botaoItem.setOnClickListener {
                adicionar(m)
            }
            return item.root
        }
    }

    override fun onResume() {
        super.onResume()
        carregar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
