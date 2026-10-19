package com.treinamento.pdv

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
import com.treinamento.pdv.data.Mercadoria
import com.treinamento.pdv.databinding.FragmentListaBinding
import kotlinx.coroutines.launch

class MercadoriaListaFragment : FragmentBase() {
    private var filtro = "ativas"
    private var _b: FragmentListaBinding? = null
    private val b get() = _b!!
    private var mercadorias: List<Mercadoria> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentListaBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("Mercadorias")

        b.buscar.setOnClickListener {
            carregar()
        }

        b.novo.setOnClickListener {
            abrir(MercadoriaFormFragment())
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
            val escolhida = mercadorias[posicao]
            val tela = MercadoriaFormFragment()
            tela.arguments = bundleOf("mercadoria" to Gson().toJson(escolhida))
            abrir(tela)
        }

        b.lista.setOnItemLongClickListener { _, _, posicao, _ ->
            val m = mercadorias[posicao]
            val texto = if (m.ativo) "Desativar" else "Ativar"
            val texto2 = if (m.ativo) "Desativado" else "Ativado"
            AlertDialog.Builder(requireContext())
                .setPositiveButton(texto) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            Api.servico().mudarAtivoMercadoria(m.id, AtivoBody(!m.ativo))
                            aviso(texto2)
                            carregar()
                        } catch (e: Exception) {
                            mostrarErro(e)
                        }
                    }
                }
                .setNegativeButton( "Editar") { _, _ ->
                    val tela = MercadoriaFormFragment()
                    tela.arguments = bundleOf("mercadoria" to Gson().toJson(mercadorias[posicao]))
                    abrir(tela)
                }
                .setNeutralButton("Sair", null)
                .create()
                .show()
            true
        }



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
                val todas = Api.servico().mercadorias(parametro)
                mercadorias = if (filtro == "ativas") {
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
        for (m in mercadorias) {
            var nome = m.descricao
            if (!m.ativo){
                nome += " (Inativo)"}
            linhas.add("$nome\nCód. ${m.codigo}  ·  Estoque: ${m.estoque}\n${moeda(m.preco)}")
        }
        b.lista.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, linhas)
        if (mercadorias.isEmpty()){
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