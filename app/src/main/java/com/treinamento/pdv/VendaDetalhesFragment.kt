package com.treinamento.pdv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.databinding.FragmentDetalhesVendaBinding
import kotlinx.coroutines.launch

class VendaDetalhesFragment : FragmentBase() {

    private var _b: FragmentDetalhesVendaBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentDetalhesVendaBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("template")

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

