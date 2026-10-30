package com.treinamento.pdv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.Venda
import com.treinamento.pdv.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardFragment : FragmentBase() {
    private var filtro = "hoje"
    private var ordem = "valor"
    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!
    private var vendas: List<Venda> = ArrayList();
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.lista.layoutManager = LinearLayoutManager(requireContext())
        titulo("Dashboard")
        carregar()

        b.filtro.visibility = View.VISIBLE
        b.filtro.setOnClickListener {
            if (filtro == "hoje") {
                filtro = "7 dias"
                b.filtro.text = "7 dias"
            } else if (filtro == "7 dias") {
                filtro = "30 dias"
                b.filtro.text = "30 dias"
            } else {
                filtro = "hoje"
                b.filtro.text = "Hoje"
            }
            carregar()
        }

        // ordena a saída de estoque pelo valor total ou pela quantidade vendida
        b.ordem.setOnClickListener {
            if (ordem == "valor") {
                ordem = "quantidade"
                b.ordem.text = "Por quantidade"
            } else {
                ordem = "valor"
                b.ordem.text = "Por valor"
            }
            mostrarLista()
        }
    }

    private fun carregar() {
        b.mensagem.visibility = View.GONE
        b.progresso.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                vendas = Api.servico().vendas()
                val formatter =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
                val hoje = LocalDate.now()
                if (filtro == "hoje") {
                    vendas = vendas.filter {
                        val dataVenda =
                            LocalDate.parse(dataHora(it.data_venda).substring(0, 10), formatter)
                        dataVenda.isEqual(hoje)
                    }
                } else if (filtro == "7 dias") {
                    val limite7d = hoje.minusDays(7)
                    vendas = vendas.filter {
                        val dataVenda =
                            LocalDate.parse(dataHora(it.data_venda).substring(0, 10), formatter)
                        !dataVenda.isBefore(limite7d) && !dataVenda.isAfter(hoje)
                    }
                } else if (filtro == "30 dias") {
                    val limite30d = hoje.minusDays(30)
                    vendas = vendas.filter {
                        val dataVenda =
                            LocalDate.parse(dataHora(it.data_venda).substring(0, 10), formatter)
                        !dataVenda.isBefore(limite30d) && !dataVenda.isAfter(hoje)
                    }
                }
                if (vendas.isEmpty()) {
                    b.mensagem.visibility = View.VISIBLE
                } else {
                    b.mensagem.visibility = View.GONE
                }
                b.progresso.visibility = View.GONE
                mostrarLista()
            } catch (e: Exception) {
                b.progresso.visibility = View.GONE
                mostrarErro(e)
            }
        }
    }

    private fun mostrarLista() {
        val vendidos = vendas.filter { it.status == "finalizada" }
            .flatMap { it.itens_vendas ?: emptyList() }
            .groupBy { it.mercadoria_id }
            .values
            .sortedByDescending { itens ->
                if (ordem == "quantidade") itens.sumOf { it.quantidade }.toDouble()
                else itens.sumOf { it.valor_total }
            }

        val linhas = ArrayList<String>()
        for (itens in vendidos) {
            val descricao = itens.first().mercadorium?.descricao ?: "Sem nome"
            val quantidade = itens.sumOf { it.quantidade }
            val total = itens.sumOf { it.valor_total }
            linhas += "$descricao x$quantidade \n   ${moeda(total)}"
        }

        b.lista.adapter = LinhaAdapter(linhas)

        val finalizadas = vendas.filter { it.status == "finalizada" }
        b.qtdVendas.text = "Vendas Finalizadas: ${vendas.count { it.status == "finalizada" }}"
        b.totalFaturado.text =
            "Valor Total Faturado: ${moeda(finalizadas.sumOf { it.valor_total })}"
        b.vendasCanceladas.text = "Vendas Canceladas: ${vendas.count { it.status == "cancelada" }}"
        b.vendasDinheiro.text = "Dinheiro:\n${
            moeda(finalizadas.filter { it.forma_pag == "Dinheiro" }.sumOf { it.valor_total })
        }"
        b.vendasCartao.text = "Cartão:\n${
            moeda(finalizadas.filter { it.forma_pag == "Cartão de Crédito" || it.forma_pag == "Cartão" }
                .sumOf { it.valor_total })
        }"
        b.vendasPix.text =
            "Pix:\n${moeda(finalizadas.filter { it.forma_pag == "PIX" }.sumOf { it.valor_total })}"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

}