package com.treinamento.pdv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treinamento.pdv.databinding.ItemLinhaBinding

class LinhaAdapter(
    private val linhas: List<String>,
    private val aoClicar: (Int) -> Unit = {},
    private val aoSegurar: (Int) -> Unit = {}
) : RecyclerView.Adapter<LinhaAdapter.Linha>() {

    class Linha(val b: ItemLinhaBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Linha {
        val b = ItemLinhaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Linha(b)
    }

    override fun getItemCount() = linhas.size

    override fun onBindViewHolder(linha: Linha, posicao: Int) {
        linha.b.texto.text = linhas[posicao]
        linha.b.root.setOnClickListener { aoClicar(linha.bindingAdapterPosition) }
        linha.b.root.setOnLongClickListener {
            aoSegurar(linha.bindingAdapterPosition)
            true
        }
    }
}