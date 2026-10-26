package com.treinamento.pdv
import android.content.Context
import android.graphics.Bitmap
import com.treinamento.pdv.data.Venda
import kotlinx.coroutines.*
import rede.smartrede.commons.callback.IPrinterCallback
import rede.smartrede.commons.contract.IConnectorPrinter
import rede.smartrede.commons.printer.PrintAttributes
import rede.smartrede.sdk.api.IRedeSdk
import rede.smartrede.sdk.api.RedeSdk
import kotlin.coroutines.resume
object ImpressoraRede {
    private var sdk: IRedeSdk? = null
    /** O SDK só aceita um newInstance por processo */
    private fun obtemSdk(context: Context): IRedeSdk? {
        sdk?.let { return it }
        return try {
            IRedeSdk.newInstance(context.applicationContext).also { sdk = it }
        } catch (e: IllegalStateException) {
            // Já inicializado por outra parte do app
            runCatching { RedeSdk() }.getOrNull()?.also { sdk = it }
        }
    }
    private fun obtemImpressora(context: Context): IConnectorPrinter? =
        obtemSdk(context)?.getTerminalFunctions()?.getConnectorPrinter()
    /** Envia um comando e espera o callback da impressora (com timeout) */
    private suspend fun executa(
        impressora: IConnectorPrinter,
        comando: (IConnectorPrinter) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        withTimeoutOrNull(30_000L) {
            suspendCancellableCoroutine { cont ->
                try {
                    impressora.setPrinterCallback(object : IPrinterCallback {
                        override fun onCompleted() { if (cont.isActive) cont.resume(true) }
                        override fun onError(erro: String) { if (cont.isActive) cont.resume(false) }
                    })
                    comando(impressora)
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(false)
                }
            }
        } ?: false
    }
    private fun attrs(
        align: Int = PrintAttributes.VALUE_ALIGN_LEFT,
        tamanho: Int = 20,
        fonte: Int = PrintAttributes.VALUE_TYPEFACE_NORMAL,
        peso: Int = 1
    ) = mapOf(
        PrintAttributes.KEY_ALIGN to align,
        PrintAttributes.KEY_TEXTSIZE to tamanho,
        PrintAttributes.KEY_TYPEFACE to fonte,
        PrintAttributes.KEY_WEIGHT to peso
    )
    /** Uma coluna */
    private fun linha(texto: String, a: Map<String, Int> = attrs()) =
        Pair(arrayOf(texto.ifBlank { " " }), listOf(a))   // nunca string vazia!
    /** Duas colunas: rótulo à esquerda, valor à direita */
    private fun linhaDupla(esq: String, dir: String) = Pair(
        arrayOf(esq.ifBlank { " " }, dir.ifBlank { " " }),
        listOf(
            attrs(align = PrintAttributes.VALUE_ALIGN_LEFT, peso = 1),
            attrs(align = PrintAttributes.VALUE_ALIGN_RIGHT, peso = 1)
        )
    )
    suspend fun imprimeCupom(context: Context, logo: Bitmap? = null): Boolean {
        val impressora = obtemImpressora(context) ?: return false
        // 1) Logo (opcional)
        if (logo != null && !executa(impressora) { it.printBitmap(logo) }) return false
        // 2) Texto
        val linhas = listOf(
            linha("MINHA LOJA", attrs(
                align = PrintAttributes.VALUE_ALIGN_CENTER,
                tamanho = 28,
                fonte = PrintAttributes.VALUE_TYPEFACE_BOLD
            )),
            linha(" "),
            linhaDupla("Produto A", "R$ 10,00"),
            linhaDupla("Produto B", "R$ 5,50"),
            linha("--------------------------------"),
            linhaDupla("TOTAL", "R$ 15,50"),
            linha(" "),
            linha("Obrigado!", attrs(align = PrintAttributes.VALUE_ALIGN_CENTER))
        )
        if (!executa(impressora) { it.printArrayTextWithAttributes(linhas) }) return false
        // 3) Avança o papel até o corte
        return executa(impressora) { it.printBlankLines(4) }
    }
    /** Imprime um texto puro (ex.: comprovante devolvido pelo SDK), linha a linha */
    suspend fun imprimeTexto(context: Context, texto: String): Boolean {
        val impressora = obtemImpressora(context) ?: return false
        val linhas = (texto.lines() + List(4) { " " }).map { linha(it) }
        return executa(impressora) { it.printArrayTextWithAttributes(linhas) }
    }

    private suspend fun acorda(impressora: IConnectorPrinter): Boolean {
        var i = 0
        while(i<10) {
            if (executa(impressora) { it.printArrayTextWithAttributes(listOf(linha(" "))) }) return true
            delay(300)
            i++
        }
        return false
    }

    suspend fun imprimeVenda(context: Context, logo: Bitmap? = null, venda: Venda): Boolean {
        val impressora = obtemImpressora(context) ?: return false
        if (!acorda(impressora)) return false

        // 1) Logo (opcional)
        if (logo != null && !executa(impressora) { it.printBitmap(logo) }) return false
        // 2) Texto
        var linhas = listOf(
            linha(
                "PDV Treinamento", attrs(
                    align = PrintAttributes.VALUE_ALIGN_CENTER,
                    tamanho = 28,
                    fonte = PrintAttributes.VALUE_TYPEFACE_BOLD
                )
            ),
        )
        linhas += linha("Venda #"+venda.id, attrs(align = PrintAttributes.VALUE_ALIGN_CENTER))
        linhas += linha(" ")
        linhas += linhaDupla(dataHora(venda.data_venda), "Pagamento: ${venda.forma_pag}")
        linhas += linha("Status: ${venda.status?.uppercase()}")
        linhas += linha(" ")
        linhas += linha("----------------------------------------------------------------")
        for (i in venda.itens_vendas ?: emptyList()) {
            linhas += linhaDupla(
                "${i.mercadorium?.descricao}",
                "${moeda(i.valor_total / i.quantidade)}x${i.quantidade}=${moeda(i.valor_total)}"
            )
        }
        linhas += linha(" ")
        linhas += linha("----------------------------------------------------------------")
        linhas += linhaDupla("TOTAL", moeda(venda.valor_total))
        linhas += linha(" ")
        linhas += linha("Obrigado!", attrs(align = PrintAttributes.VALUE_ALIGN_CENTER))
        linhas += linha(" ")
        linhas += linhaDupla("Cliente:", venda.cliente?.nome ?: "Não informado")

        if (!executa(impressora) { it.printArrayTextWithAttributes(linhas) }) return false

        linhas = emptyList();
        return executa(impressora) { it.printBlankLines(6) }

    }



}

