package com.treinamento.pdv

import android.content.res.Configuration
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
import coil.load
import com.google.gson.Gson
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.Cliente
import com.treinamento.pdv.data.ItemCarrinho
import com.treinamento.pdv.data.Mercadoria
import com.treinamento.pdv.databinding.FragmentVendaBinding
import com.treinamento.pdv.databinding.ItemVendaBinding
import kotlinx.coroutines.launch
import android.widget.LinearLayout
import android.widget.Button
import android.view.Gravity

class VendaFragment : FragmentBase() {

    private var _b: FragmentVendaBinding? = null
    private val b get() = _b!!
    private var carrinhoExpandido = true
    private var mercadorias: List<Mercadoria> = ArrayList()
    private var itensCarrinho: MutableList<ItemCarrinho> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentVendaBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("Venda")
        savedInstanceState?.getString("carrinho")?.let {
            itensCarrinho = Gson().fromJson(it, Array<ItemCarrinho>::class.java).toMutableList()
        }
        b.lista.layoutManager = LinearLayoutManager(requireContext())
        b.listaCarrinho.layoutManager = LinearLayoutManager(requireContext())
        b.prosseguir.text = "Total: ${moeda(calcularTotalCarrinho())}"
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("carrinho", Gson().toJson(itensCarrinho))
    }
    private fun carregar() {
        val parametro = b.busca.text.toString().trim()
        b.progresso.visibility = View.VISIBLE
        b.mensagem.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val todas = Api.servico().mercadorias(parametro)
                mercadorias = todas.filter { m -> m.ativo }
                mercadorias = mercadorias.filter { m -> m.estoque > 0 }
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
        b.carrinhoLabel?.visibility = b.listaCarrinho.visibility
        b.carrinhoLabel?.setOnClickListener {
            carrinhoExpandido = !carrinhoExpandido
            if (carrinhoExpandido) {
                b.lista.visibility = View.VISIBLE
                b.carrinhoLabel?.setText("Expandir Carrinho")
            }
            else {
                b.lista.visibility = View.GONE
                b.carrinhoLabel?.setText("Diminuir Carrinho")
                if (itensCarrinho.isEmpty()) {
                    b.listaCarrinho.visibility = View.GONE
                    b.lista.visibility = View.VISIBLE
                }
            }
        }
        b.divisorCarrinho?.visibility = b.listaCarrinho.visibility
        if (context?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) b.listaCarrinho.visibility = View.VISIBLE

        b.prosseguir.text = "Total: ${moeda(calcularTotalCarrinho())}"
    }

    private fun perguntarAdicionarQuantidade(m: Mercadoria) {
        val layoutPrincipal = LinearLayout(requireContext()).apply {
            gravity = Gravity.CENTER
        }
        val campo = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setText("1")
            selectAll()
            layoutParams = LinearLayout.LayoutParams(
                200,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val btnMais = Button(requireContext()).apply {
            text = "+"
            layoutParams = LinearLayout.LayoutParams(100,100)
        }
        btnMais.setOnClickListener {
            val atual = campo.text.toString().toIntOrNull() ?: 1
            campo.setText((atual + 1).toString())
        }

        val btnMenos = Button(requireContext()).apply {
            text = "-"
            layoutParams = LinearLayout.LayoutParams(100,100)
        }
        btnMenos.setOnClickListener {
            val atual = campo.text.toString().toIntOrNull() ?: 1
            if (atual > 1) {
                campo.setText((atual - 1).toString())
            }
        }
        layoutPrincipal.addView(btnMenos)
        layoutPrincipal.addView(campo)
        layoutPrincipal.addView(btnMais)
        val existente = itensCarrinho.find { it.mercadoria.id == m.id }
        val quantidade = existente?.quantidade ?: 0

        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle(m.descricao)
            .setMessage("Quantidade\nNo carrinho: $quantidade\nQuantos quer adicionar ao carrinho?")
            .setView(layoutPrincipal)
            .setPositiveButton("Adicionar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val quantidadeDigitada = campo.text.toString().toIntOrNull() ?: 1
                if (quantidadeDigitada <= 0) {
                    campo.error = "Informe uma quantidade válida"
                    dialogo.dismiss()
                } else {
                    adicionarCarrinho(m, quantidadeDigitada)
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

        val total = existente?.quantidade ?: quantidade
        aviso("${m.descricao} adicionado ($total)")
        b.prosseguir.text = "Total: ${moeda(calcularTotalCarrinho())}"
        mostrarLista()
    }

    private fun perguntarRemoverQuantidade(m: Mercadoria) {
        val existente = itensCarrinho.find { it.mercadoria.id == m.id }
        val quantidadeNoCarrinho = existente?.quantidade ?: 0
        if (quantidadeNoCarrinho <= 0) return
        val layoutPrincipal = LinearLayout(requireContext()).apply {
            gravity = Gravity.CENTER
        }
        val campo = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setText("1")
            selectAll()
            layoutParams = LinearLayout.LayoutParams(
                200,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val btnMais = Button(requireContext()).apply {
            text = "+"
            layoutParams = LinearLayout.LayoutParams(
                80,
                80
            )
        }
        btnMais.setOnClickListener {
            val atual = campo.text.toString().toIntOrNull() ?: 1
            if (atual < quantidadeNoCarrinho) {
                campo.setText((atual + 1).toString())
            }
        }
        val btnMenos = Button(requireContext()).apply {
            text = "-"
            layoutParams = LinearLayout.LayoutParams(
                (48 * resources.displayMetrics.density).toInt(),
                (48 * resources.displayMetrics.density).toInt()
            )
        }
        btnMenos.setOnClickListener {
            val atual = campo.text.toString().toIntOrNull() ?: 1
            if (atual > 1) {
                campo.setText((atual - 1).toString())
            }
        }
        layoutPrincipal.addView(btnMenos)
        layoutPrincipal.addView(campo)
        layoutPrincipal.addView(btnMais)

        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle(m.descricao)
            .setMessage("Quantidade\nNo carrinho: $quantidadeNoCarrinho\nQuantos quer remover do carrinho?")
            .setView(layoutPrincipal)
            .setPositiveButton("Remover", null)
            .setNegativeButton("Cancelar", null)
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val quantidadeDigitada = campo.text.toString().toIntOrNull()
                if (quantidadeDigitada == null || quantidadeDigitada <= 0) {
                    campo.error = "Informe uma quantidade válida"
                } else if (quantidadeDigitada > quantidadeNoCarrinho) {
                    campo.error = "Você só possui $quantidadeNoCarrinho no carrinho"
                } else {
                    removerCarrinho(m, quantidadeDigitada)
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
            b.prosseguir.text = "Total: ${moeda(calcularTotalCarrinho())}"
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
            item.imagemItem.load(m.imagem_url) {
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
            item.nomeItem.text = m.descricao
            item.nomeItem.setOnLongClickListener {
                Toast.makeText(requireContext(), "${m.descricao}", Toast.LENGTH_SHORT).show()
                true
            }
            item.precoItem.text = moeda(m.preco)
            val noCarrinho = itensCarrinho.find { it.mercadoria.id == m.id }?.quantidade ?: 0
            item.estoqueItem.text = "Saldo: ${m.estoque - noCarrinho}"
            item.root.setOnClickListener {
                perguntarAdicionarQuantidade(m)
            }
            item.nomeItem.setOnClickListener {
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
            item.imagemItem.load(c.mercadoria.imagem_url) {
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
            item.nomeItem.text = c.mercadoria.descricao
            item.precoItem.text = moeda(c.mercadoria.preco * c.quantidade)
            item.estoqueItem.text = "Qtd: ${c.quantidade}"
            item.root.setOnClickListener {
                perguntarRemoverQuantidade(c.mercadoria)
            }

        }
    }
}
