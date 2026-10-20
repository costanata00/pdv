
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
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.data.StatusBody
import com.treinamento.pdv.data.Venda
import com.treinamento.pdv.databinding.FragmentListaBinding
import kotlinx.coroutines.launch

class VendaListaFragment : FragmentBase() {
    private var filtro ="todas"
    private var _b: FragmentListaBinding? = null
    private val b get() = _b!!
    private var vendas: List<Venda> = ArrayList();

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentListaBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("Histórico de Vendas")
        b.filtro.text="Todas"

        b.filtro.visibility = View.VISIBLE
        b.filtro.setOnClickListener {
            when(filtro){
             "todas" -> {
                 filtro = "finalizadas"
                 b.filtro.text = "Finalizadas"
             }"finalizadas" -> {
                    filtro = "canceladas"
                    b.filtro.text = "Canceladas"
             }"canceladas" -> {
                    filtro = "todas"
                    b.filtro.text = "Todas"
                }
            }
            carregar()
        }
        b.buscar.setOnClickListener {
            carregar()
        }

        b.lista.setOnItemLongClickListener { _, _, posicao, _ ->
            val v = vendas[posicao]
            AlertDialog.Builder(requireContext())
                .setPositiveButton("Cancelar Venda") { _, _ ->
                    if (v.status == "cancelada") {
                        aviso("A venda já está cancelada.")
                        return@setPositiveButton
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            Api.servico().cancelarVenda(v.id)
                            aviso("Cancelada")
                            carregar()
                        } catch (e: Exception) {
                            mostrarErro(e)
                        }
                    }
                }.setNegativeButton("Ver Mais"){ _, _ ->
                    val tela = VendaDetalhesFragment()
                    tela.arguments = bundleOf("vendaID" to vendas[posicao].id)
                    abrir(tela)
                }
                .setNeutralButton("Sair", null)
                .create()
                .show()
            true
        }

        b.novo.visibility = View.GONE
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
                val todas = Api.servico().vendas(parametro)
                vendas = if (filtro == "finalizadas") {
                    todas.filter { it.status == "finalizada" }
                } else if (filtro == "canceladas") {
                    todas.filter { it.status == "cancelada" }
                }else {
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
        for (v in vendas) {
            val nome = v.cliente?.nome ?: "Não informado"
            linhas.add("Cliente: ${nome}\n" + "Forma de Pagamento: ${v.forma_pag}\n" + "Status: ${v.status?.uppercase()}\n"
                    + "Valor Total: ${moeda(v.valor_total)} \n" + "Data: ${dataHora(v.data_venda)}\n" + "Usuario: ${v.usuario?.nome}")
        }
        b.lista.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, linhas)
        if (vendas.isEmpty()){
            b.mensagem.visibility = View.VISIBLE
        }else{
            b.mensagem.visibility = View.GONE
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

}