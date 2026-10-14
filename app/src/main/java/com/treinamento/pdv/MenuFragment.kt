package com.treinamento.pdv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.databinding.FragmentMenuBinding

class MenuFragment : FragmentBase() {

    // Ligação com o desenho. Pode ficar vazia (null) quando a tela some.
    private var _b: FragmentMenuBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentMenuBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("PDV")
        b.saudacao.text = "Olá, ${Sessao.nomeUsuario}"

        // Em cada passo indicado, apague as duas barras do início da linha.
         b.mercadorias.setOnClickListener { abrir(MercadoriaListaFragment()) }   // passo 7
         b.clientes.setOnClickListener { abrir(ClienteListaFragment()) }         // passo 8
        // b.novaVenda.setOnClickListener { abrir(VendaFragment()) }           // passo 10
        // b.historico.setOnClickListener { abrir(HistoricoFragment()) }       // passo 11

        b.sair.setOnClickListener {
            Sessao.sair()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}