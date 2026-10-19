package com.treinamento.pdv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.databinding.FragmentMenuBinding

class MenuFragment : FragmentBase() {
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

        b.mercadorias.setOnClickListener { abrir(MercadoriaListaFragment()) }
        b.clientes.setOnClickListener { abrir(ClienteListaFragment()) }
        b.novaVenda.setOnClickListener { abrir(VendaFragment()) }
        b.historico.setOnClickListener { abrir(VendaListaFragment()) }

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