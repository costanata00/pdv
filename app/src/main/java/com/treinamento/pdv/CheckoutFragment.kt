package com.treinamento.pdv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.Cliente
import com.treinamento.pdv.data.ItemCarrinho
import com.treinamento.pdv.data.ItemVendaBody
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.data.Venda
import com.treinamento.pdv.data.VendaBody
import com.treinamento.pdv.databinding.FragmentCheckoutBinding
import kotlinx.coroutines.launch
import java.util.Locale

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
        b.novo.setOnClickListener {
            finalizarVenda()
        }

    }
    private fun mostrarLista() {
        val itens = carrinho ?: emptyList()
        val linhas = ArrayList<String>()
        for (item in itens) {
            val m = item.mercadoria
            val subtotal = m.preco * item.quantidade
            linhas.add("${m.descricao} x${item.quantidade} = ${moeda(subtotal)}")

        }
        b.lista.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, linhas)
        b.total.text = "Total: ${moeda(calcularTotal())}"
        b.cliente.text = cliente?.nome ?: "Cliente não selecionado"

    }

    private fun calcularTotal(): Double {
        var total = 0.0
        for (item in carrinho ?: emptyList()) {
            total += item.mercadoria.preco * item.quantidade
        }
        return total
    }

    private fun finalizarVenda(){
        val rg = view?.findViewById<RadioGroup>(R.id.rgPagamento) ?: return
        val pagamento = when (rg.checkedRadioButtonId) {
            R.id.rbDinheiro -> "Dinheiro"
            R.id.rbCredito -> "Cartão de Crédito"
            R.id.rbPix -> "PIX"
            else -> "Dinheiro"
        }
        if (pagamento == "Dinheiro") {
            perguntarValorRecebido()
        } else {
            enviarVenda(pagamento, 0.0)
        }
    }

    private fun perguntarValorRecebido() {
        val total = calcularTotal()
        val campoDinheiro = EditText(requireContext())
        campoDinheiro.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        campoDinheiro.textAlignment = View.TEXT_ALIGNMENT_CENTER
        campoDinheiro.setText(String.format(Locale.US, "%.2f", total))
        campoDinheiro.selectAll()

        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle("Pagamento em dinheiro")
            .setMessage("Total: ${moeda(total)}\nValor recebido:")
            .setView(campoDinheiro)
            .setPositiveButton("Confirmar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val recebido = campoDinheiro.text.toString().replace(",", ".").toDoubleOrNull()
                if (recebido == null || recebido < total) {
                    campoDinheiro.error = "Valor menor que o total"
                } else {
                    dialogo.dismiss()
                    enviarVenda("Dinheiro", recebido - total)
                }
            }
        }
        dialogo.show()
    }

    private fun enviarVenda(pagamento: String, troco: Double) {
        b.progresso.visibility = View.VISIBLE
        b.novo.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val carrinho = carrinho?.map { item ->
                    ItemVendaBody(item.mercadoria.id, item.quantidade)
                } ?: emptyList()
                val vendaBody = VendaBody(cliente?.id, pagamento, true, carrinho)
                val venda = Api.servico().criarVenda(vendaBody)
                b.progresso.visibility = View.GONE
                var mensagem = "Venda Finalizada"
                if (pagamento == "Dinheiro") mensagem += "\nTroco: ${moeda(troco)}"
                AlertDialog.Builder(requireContext())
                    .setMessage("$mensagem\nQuer imprimir nota fiscal?")
                    .setCancelable(false)
                    .setPositiveButton("Quero ") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                val completa = Api.servico().venda(venda.id)
                                val ok = ImpressoraRede.imprimeVenda(requireContext(), venda = completa)
                                if (!ok) aviso("Falha na impressão")
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                                requireActivity().finish()
                            }catch (e: Exception){
                                mostrarErro(e)
                            }
                        }
                    }
                    .setNegativeButton("Não quero", { _, _ ->
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    })
                    .create()
                    .show()
            } catch (e: Exception) {
                b.progresso.visibility = View.GONE
                b.novo.isEnabled = true
                mostrarErro(e)
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }



}