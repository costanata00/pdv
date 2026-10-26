package com.treinamento.pdv

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CancellationException

open class FragmentBase : Fragment() {
    fun aviso(texto: String) {
        (requireActivity() as TelaBase).aviso(texto)
    }

    fun mostrarErro(erro: Exception) {
        if (erro is CancellationException) {
            throw erro
        }
        (requireActivity() as TelaBase).mostrarErro(erro)
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