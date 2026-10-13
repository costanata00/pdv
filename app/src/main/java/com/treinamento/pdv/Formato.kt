package com.treinamento.pdv

import com.treinamento.pdv.data.Venda
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// 27.9 vira "R$ 27,90"
fun moeda(valor: Double): String {
    return "R$ " + String.format(Locale("pt", "BR"), "%,.2f", valor)
}

// A data chega da API assim: "2026-10-13T12:34:54.004Z". Vira "13/10/2026 09:34".
fun dataHora(textoDaApi: String?): String {
    if (textoDaApi == null) {
        return ""
    }
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
    texto += "Data: ${dataHora(venda.data_venda)}\n\n"

    val itens = venda.itens_vendas
    if (itens != null) {
        for (item in itens) {
            val nome = item.mercadorium?.descricao ?: "Mercadoria ${item.mercadoria_id}"
            texto += nome + "\n"
            texto += "${item.quantidade} x ${moeda(item.preco_unitario)}\n\n"
        }
    }

    texto += "--------------------\n"
    texto += "TOTAL: ${moeda(venda.valor_total)}\n"

    val forma = venda.forma_pag
    if (forma != null) {
        texto += "Pagamento: $forma\n"
    }

    val cliente = venda.cliente
    if (cliente != null) {
        texto += "\nCliente: ${cliente.nome}\n"
    }
    return texto
}