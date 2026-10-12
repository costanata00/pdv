package com.treinamento.pdv

import com.treinamento.pdv.data.Venda
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// 25.9 vira "R$ 25,90"
fun moeda(valor: Double): String {
    return "R$ " + String.format(Locale("pt", "BR"), "%,.2f", valor)
}

// A data chega da API assim: "2026-10-09T23:15:00.000Z". Vira "09/10/2026 20:15".
fun dataHora(textoDaApi: String): String {
    try {
        val leitor = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        leitor.timeZone = TimeZone.getTimeZone("UTC")
        val data = leitor.parse(textoDaApi)
        if (data == null) {
            return textoDaApi
        }
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(data)
    } catch (e: Exception) {
        return textoDaApi
    }
}

// Monta o texto do recibo de uma venda
fun recibo(venda: Venda): String {
    var texto = "PDV TREINAMENTO\n\n"
    texto += "Venda: #${venda.id}\n"
    texto += "Data: ${dataHora(venda.data)}\n\n"

    val itens = venda.itens
    if (itens != null) {
        for (item in itens) {
            val nome = item.mercadoria?.descricao ?: "Mercadoria ${item.mercadoriaId}"
            texto += nome + "\n"
            texto += "${item.quantidade} x ${moeda(item.precoUnitario)}\n\n"
        }
    }

    texto += "--------------------\n"
    texto += "TOTAL: ${moeda(venda.valorTotal)}\n"

    val cliente = venda.cliente
    if (cliente != null) {
        texto += "\nCliente: ${cliente.nome}\n"
    }
    return texto
}
