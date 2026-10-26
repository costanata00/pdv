package com.treinamento.pdv.data

//Login
data class LoginRequest(
    val email: String,
    val senha: String
)
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val usuario: Usuario,
    )
data class RefreshBody(
    val refreshToken: String
)
data class RefreshResponse(
    val token: String,
    val refreshToken: String
)


//Usuario
data class UsuarioBody(
    val nome: String,
    val email: String,
    val senha: String
)

data class Usuario(
    val id: Int,
    val nome: String
)

//Mercadorias
data class Mercadoria(
    val id: Int,
    val codigo: String,
    val descricao: String,
    val preco: Double,
    val estoque: Int,
    val ativo: Boolean,
    val imagem_url: String?
)

data class MercadoriaBody(
    val codigo: String,
    val descricao: String,
    val preco: Double,
    val estoque: Int,
    val imagem_url: String?
)

//Clientes
data class Cliente(
    val id: Int,
    val nome: String,
    val cpf_cnpj: String?,
    val telefone: String?,
    val email: String?,
    val ativo: Boolean
)

data class ClienteBody(
    val nome: String,
    val cpf_cnpj: String,
    val telefone: String,
    val email: String
)

data class AtivoBody(val ativo: Boolean)

//Vendas
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
    var status: String?,
    val pago: Boolean,
    val forma_pag: String?,
    val cliente: Cliente?,
    val usuario: Usuario?,
    val itens_vendas: List<ItemVenda>?
)

data class ItemCarrinho(val mercadoria: Mercadoria, var quantidade: Int)

data class ItemVendaBody(val mercadoria_id: Int, val quantidade: Int)

data class VendaBody(
    val cliente_id: Int?,
    val forma_pag: String,
    val pago: Boolean,
    val itens: List<ItemVendaBody>
)

data class ErroApi(val error: String?)