package com.treinamento.pdv

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellationException

open class FragmentBase : Fragment() {
    fun aviso(texto: String) {
        (requireActivity() as TelaBase).aviso(texto)
    }

    // Janela de erro (usa a mesma da TelaBase)
    fun mostrarErro(erro: Exception) {
        // A tela foi fechada no meio do pedido. Não é um erro de verdade.
        if (erro is CancellationException) {
            throw erro
        }
        (requireActivity() as TelaBase).mostrarErro(erro)
    }

    fun abrir(tela: Fragment) {
        (requireActivity() as MainActivity).abrir(tela)
    }

    fun voltar() {
        parentFragmentManager.popBackStack()
    }
    fun titulo(texto: String) {
        requireActivity().title = texto
    }
}