package com.treinamento.pdv.data

// ----- Login -----
data class LoginRequest(
    val email: String,
    val senha: String
)
data class Usuario(
    val id: Int,
    val nome: String
)
data class LoginResponse(
    val token: String,
    val usuario: Usuario
)

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
    val cpfCnpj: String?,
    val telefone: String?,
    val email: String?
)

// O que o app ENVIA para cadastrar ou alterar um cliente
data class ClienteBody(
    val nome: String,
    val cpfCnpj: String,
    val telefone: String,
    val email: String
)

// ----- Vendas -----
data class ItemVenda(
    val id: Int,
    val mercadoriaId: Int,
    val quantidade: Int,
    val precoUnitario: Double,
    val valorTotal: Double,
    val mercadoria: Mercadoria?
)

data class Venda(
    val id: Int,
    val valorTotal: Double,
    val data: String,
    val status: String,
    val cliente: Cliente?,
    val usuario: Usuario?,
    val itens: List<ItemVenda>?
)

// O que o app ENVIA para finalizar uma venda
data class ItemVendaBody(
    val mercadoriaId: Int,
    val quantidade: Int
)
data class VendaBody(
    val clienteId: Int?,
    val itens: List<ItemVendaBody>
)

// Um item da venda que ainda está sendo montada na tela
data class ItemCarrinho(
    val mercadoria: Mercadoria,
    var quantidade: Int
)

// Quando algo dá errado, a API responde { "erro": "mensagem" }
data class ErroApi(
    val erro: String?
)