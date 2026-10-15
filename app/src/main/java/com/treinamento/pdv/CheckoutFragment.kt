package com.treinamento.pdv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.treinamento.pdv.data.Cliente
import com.treinamento.pdv.data.ItemCarrinho
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.databinding.FragmentCheckoutBinding
import kotlinx.coroutines.launch

class CheckoutFragment : FragmentBase() {

    private var carrinho: List<ItemCarrinho>? = null
    private var cliente: Cliente? = null
    private var _b: FragmentCheckoutBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentCheckoutBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("Check-out")

        val jsonCliente = arguments?.getString("cliente")
        if (jsonCliente != null) {
            cliente = Gson().fromJson(jsonCliente, Cliente::class.java)
        }
        val jsonCarrinho = arguments?.getString("carrinho")
        if (jsonCarrinho != null) {
            carrinho = Gson().fromJson(jsonCarrinho, Array<ItemCarrinho>::class.java).toList()
        }
        mostrarLista()
        val rg = view.findViewById<RadioGroup>(R.id.rgPagamento)
        var pagamento = when (rg.checkedRadioButtonId) {
            R.id.rbDinheiro -> "Dinheiro"
            R.id.rbCredito -> "Cartão"
            R.id.rbPix -> "PIX"
            else -> "Dinheiro"
        }

    }
    fun mostrarLista() {
        val itens = carrinho ?: emptyList()
        val linhas = ArrayList<String>()
        var total = 0.0
        for (item in itens) {
            val m = item.mercadoria
            val subtotal = m.preco * item.quantidade
            total += subtotal
            linhas.add("${m.descricao} x${item.quantidade} = ${moeda(subtotal)}")

        }
        b.lista.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, linhas)
        b.total.text = "Subtotal: ${moeda(total)}"
        b.cliente.text = cliente?.nome ?: "Cliente não selecionado"

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

    private fun templateChamadaApi() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // val resposta = Api.servico().algumaRota()
            } catch (e: Exception) {
                mostrarErro(e)
            }
        }
    }

}