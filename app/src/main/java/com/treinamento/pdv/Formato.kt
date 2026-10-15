package com.treinamento.pdv

import com.treinamento.pdv.data.Venda
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


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