package com.treinamento.pdv.data

// ----- Login -----
data class LoginRequest(val email: String, val senha: String)
data class Usuario(val id: Int, val nome: String)
data class LoginResponse(val token: String, val usuario: Usuario)

// ----- Mercadorias -----
data class Mercadoria(
    val id: Int,
    val codigo: String,
    val descricao: String,
    val preco: Double,
    val estoque: Int,
    val ativo: Boolean
)

// O que o app ENVIA para cadastrar ou alterar uma mercadoria
data class MercadoriaBody(
    val codigo: String,
    val descricao: String,
    val preco: Double,
    val estoque: Int
)

// ----- Clientes -----
data class Cliente(
    val id: Int,
    val nome: String,
    val cpf_cnpj: String?,
    val telefone: String?,
    val email: String?,
    val ativo: Boolean
)

// O que o app ENVIA para cadastrar ou alterar um cliente
data class ClienteBody(
    val nome: String,
    val cpf_cnpj: String,
    val telefone: String,
    val email: String
)

// Usado para desativar ou reativar uma mercadoria ou um cliente
data class AtivoBody(val ativo: Boolean)

// ----- Vendas -----
data class ItemVenda(
    val id: Int,
    val mercadoria_id: Int,
    val quantidade: Int,
    val preco_unitario: Double,
    val valor_total: Double,
    val mercadorium: Mercadoria?
)

data class Venda(
    val id: Int,
    val cliente_id: Int?,
    val valor_total: Double,
    val data_venda: String?,
    val status: String?,
    val pago: Boolean,
    val forma_pag: String?,
    val cliente: Cliente?,
    val usuario: Usuario?,
    val itens_vendas: List<ItemVenda>?
)

// O que o app ENVIA para finalizar uma venda
data class ItemVendaBody(val mercadoria_id: Int, val quantidade: Int)

data class VendaBody(
    val cliente_id: Int?,
    val forma_pag: String,
    val pago: Boolean,
    val itens: List<ItemVendaBody>
)

// Um item da venda que ainda está sendo montada na tela
data class ItemCarrinho(val mercadoria: Mercadoria, var quantidade: Int)

data class ErroApi(val error: String?)