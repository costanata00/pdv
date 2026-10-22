package com.treinamento.pdv


import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


fun moeda(valor: Double): String {
    return "R$ " + String.format(Locale.forLanguageTag("pt-BR"), "%,.2f", valor)
}

fun dataHora(textoDaApi: String?): String {
    if (textoDaApi == null) {
        return ""
    }
    try {
        val leitor = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        leitor.timeZone = TimeZone.getTimeZone("GMT+03:00")
        val data = leitor.parse(textoDaApi)
        if (data == null) {
            return textoDaApi
        }
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(data)
    } catch (e: Exception) {
        return textoDaApi
    }
}