package com.treinamento.pdv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.treinamento.pdv.data.Api
import com.treinamento.pdv.data.Sessao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

open class FragmentBase : Fragment() {
    fun aviso(texto: String) {
        (requireActivity() as TelaBase).aviso(texto)
    }

    fun mostrarErro(erro: Exception) {
        // A tela foi fechada no meio do pedido. Não é um erro de verdade.
        if (erro is CancellationException) {
            throw erro
        }
        (requireActivity() as TelaBase).mostrarErro(erro)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Sessao.token == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Sessao.token = Api.servico().refresh().token
            }catch (e: Exception){
                mostrarErro(e)
            }
        }
    }

    //puxando o abrir da tela base
    fun abrir(tela: Fragment) {
        (requireActivity() as MainActivity).abrir(tela)
    }

    open fun voltar() {
        parentFragmentManager.popBackStack()
    }
    fun titulo(texto: String) {
        requireActivity().title = texto
    }
}