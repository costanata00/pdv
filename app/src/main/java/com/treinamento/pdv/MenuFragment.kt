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

    // 1. Cria a tela a partir do desenho
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentMenuBinding.inflate(inflater, container, false)
        return b.root
    }

    // 2. A tela está pronta: liga os botões
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("PDV")
        b.saudacao.text = "Olá, ${Sessao.nomeUsuario}"

        // Em cada passo indicado, apague as duas barras do início da linha.
        // b.mercadorias.setOnClickListener { abr"ir(MercadoriasFragment()) }   // passo 7
        // b.clientes.setOnClickListener { abrir(ClientesFragment()) }         // passo 8
        // b.novaVenda.setOnClickListener { abrir(VendaFragment()) }           // passo 10
        // b.historico.setOnClickListener { abrir(HistoricoFragment()) }       // passo 11

        b.sair.setOnClickListener {
            Sessao.sair()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()   // fecha a moldura inteira
        }
    }

    // 3. A tela sumiu: solta a ligação com o desenho
    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}