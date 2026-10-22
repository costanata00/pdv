package com.treinamento.pdv

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.Cliente
import com.treinamento.pdv.data.ItemCarrinho
import com.treinamento.pdv.data.Mercadoria
import com.treinamento.pdv.databinding.FragmentVendaBinding
import com.treinamento.pdv.databinding.ItemVendaBinding
import kotlinx.coroutines.launch

class VendaFragment : FragmentBase() {

    private var _b: FragmentVendaBinding? = null
    private val b get() = _b!!
    private var carrinhoExpandido = false
    private var mercadorias: List<Mercadoria> = ArrayList()
    private var itensCarrinho: MutableList<ItemCarrinho> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentVendaBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("Venda")
        b.lista.layoutManager = LinearLayoutManager(requireContext())
        b.listaCarrinho.layoutManager = LinearLayoutManager(requireContext())

        b.prosseguir.text = "Subtotal: ${moeda(calcularTotalCarrinho())}"


        b.buscar.setOnClickListener {
            carregar()
        }
        b.prosseguir.setOnClickListener {
            if (itensCarrinho.isEmpty()) {
                aviso("Adicione pelo menos um item ao carrinho.")
                return@setOnClickListener
            }
            val tela = ClienteListaFragment()
            tela.arguments = bundleOf("carrinho" to Gson().toJson(itensCarrinho))
            abrir(tela)
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
        if (mercadorias.isEmpty()) b.mensagem.visibility = View.VISIBLE else b.mensagem.visibility = View.GONE

        b.listaCarrinho.adapter = CarrinhoAdapter()
        if (itensCarrinho.isEmpty()) b.listaCarrinho.visibility = View.GONE else b.listaCarrinho.visibility = View.VISIBLE
        b.carrinhoLabel.visibility = b.listaCarrinho.visibility
        b.carrinhoLabel.setOnClickListener {
            carrinhoExpandido = !carrinhoExpandido
            if (carrinhoExpandido) {
                b.lista.visibility = View.VISIBLE
                b.carrinhoLabel.setText("Expandir Carrinho")
            }
            else {
                b.lista.visibility = View.GONE
                b.carrinhoLabel.setText("Diminuir Carrinho")}
        }
        b.divisorCarrinho.visibility = b.listaCarrinho.visibility

        b.prosseguir.text = "Subtotal: ${moeda(calcularTotalCarrinho())}"
    }

    private fun perguntarAdicionarQuantidade(m: Mercadoria) {
        val campo = EditText(requireContext())
        campo.inputType = InputType.TYPE_CLASS_NUMBER
        campo.textAlignment= View.TEXT_ALIGNMENT_CENTER
        campo.setText("1")
        campo.selectAll()
        val existente = itensCarrinho.find { it.mercadoria.id == m.id }
        val quantidade = existente?.quantidade ?: 0


        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle(m.descricao)
            .setMessage("Quantidade\nNo carrinho: $quantidade")
            .setView(campo)
            .setPositiveButton("Adicionar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val quantidade = campo.text.toString().toIntOrNull()
                if (quantidade == null || quantidade <= 0) {
                    campo.error = "Informe uma quantidade válida"
                } else {
                    adicionarCarrinho(m, quantidade)
                    dialogo.dismiss()
                }
            }
        }
        dialogo.show()
    }

    private fun adicionarCarrinho(m: Mercadoria, quantidade: Int){
        val existente = itensCarrinho.find { it.mercadoria.id == m.id }
        if (existente != null) {
            if ((existente.quantidade + quantidade) > m.estoque) {
                aviso("Quantidade indisponível")
                return
            } else {
                existente.quantidade += quantidade
            }
        } else {
            if (quantidade > m.estoque) {
                aviso("Quantidade indisponível")
                return
            } else {
                itensCarrinho.add(ItemCarrinho(m, quantidade))
            }
        }
        val depois = itensCarrinho.find { it.mercadoria.id == m.id }

        val total = existente?.quantidade ?: quantidade
        aviso("${m.descricao} adicionado ($total)")
        b.prosseguir.text = "Subtotal: ${moeda(calcularTotalCarrinho())}"
        mostrarLista()
    }

    private fun perguntarRemoverQuantidade(m: Mercadoria) {
        val campo = EditText(requireContext())
        campo.inputType = InputType.TYPE_CLASS_NUMBER
        campo.setText("1")
        campo.selectAll()
        val existente = itensCarrinho.find { it.mercadoria.id == m.id }
        val quantidade = existente?.quantidade ?: 0


        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle(m.descricao)
            .setMessage("Quantidade\n No carrinho: $quantidade")
            .setView(campo)
            .setPositiveButton("Remover", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val quantidade = campo.text.toString().toIntOrNull()
                if (quantidade == null || quantidade <= 0) {
                    campo.error = "Informe uma quantidade válida"
                } else {
                    removerCarrinho(m, quantidade)
                    dialogo.dismiss()
                }
            }
        }
        dialogo.show()
    }


        private fun removerCarrinho(m: Mercadoria, quantidade: Int) {
            val item = itensCarrinho.find { it.mercadoria.id == m.id }
            item!!.quantidade -= quantidade
            if (item.quantidade <= 0) itensCarrinho.remove(item);
            aviso("${m.descricao} Removido")
            b.prosseguir.text = "Subtotal: ${moeda(calcularTotalCarrinho())}"
            mostrarLista()
        }

        private fun calcularTotalCarrinho(): Double {
        var total = 0.0
        for (item in itensCarrinho) {
            total += item.mercadoria.preco * item.quantidade
        }
        return total
    }

    override fun onResume() {
        super.onResume()
        carregar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

    override fun voltar() {
        if (itensCarrinho.isEmpty()) {
            super.voltar()
            return
        }
        AlertDialog.Builder(requireContext())
            .setMessage("Tem certeza que quer voltar?\nOs itens que você tiver adicionado serão perdidos.")
            .setPositiveButton("Sim") { _, _ ->
                super.voltar()
            }
            .setNegativeButton("Não", null)
            .create()
            .show()
    }

    private class ItemHolder(val b: ItemVendaBinding) : RecyclerView.ViewHolder(b.root)

    private inner class MercadoriaAdapter : RecyclerView.Adapter<ItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(ItemVendaBinding.inflate(layoutInflater, parent, false))
        }

        override fun getItemCount() = mercadorias.size

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = holder.b
            val m = mercadorias[position]
            item.nomeItem.text = m.descricao
            item.nomeItem.setOnLongClickListener {
                Toast.makeText(requireContext(), "${m.descricao}", Toast.LENGTH_SHORT).show()
                true
            }
            item.precoItem.text = moeda(m.preco)
            // Estoque disponível = estoque do servidor menos o que já está no carrinho
            val noCarrinho = itensCarrinho.find { it.mercadoria.id == m.id }?.quantidade ?: 0
            item.estoqueItem.text = "Estoque: ${m.estoque - noCarrinho}"
            item.botaoItem.setOnClickListener {
                perguntarAdicionarQuantidade(m)
            }
        }
    }

    private inner class CarrinhoAdapter : RecyclerView.Adapter<ItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(ItemVendaBinding.inflate(layoutInflater, parent, false))
        }

        override fun getItemCount() = itensCarrinho.size

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = holder.b
            val c = itensCarrinho[position]
            item.nomeItem.setOnLongClickListener {
                Toast.makeText(requireContext(), "${c.mercadoria.descricao}", Toast.LENGTH_SHORT).show()
                true
            }
            item.nomeItem.text = c.mercadoria.descricao
            item.precoItem.text = moeda(c.mercadoria.preco * c.quantidade)
            item.estoqueItem.text = "Qtd: ${c.quantidade}"
            item.botaoItem.text="-"
            item.botaoItem.setOnClickListener {
                perguntarRemoverQuantidade(c.mercadoria)
            }
        }
    }
}
