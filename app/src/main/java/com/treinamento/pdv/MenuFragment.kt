package com.treinamento.pdv

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.treinamento.pdv.data.Sessao
import com.treinamento.pdv.databinding.FragmentMenuBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MenuFragment : FragmentBase() {
    private var _b: FragmentMenuBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentMenuBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titulo("PDV")
        b.saudacao.text = "Olá, ${Sessao.nomeUsuario}"
        mostrarSaldo()

        b.mercadorias.setOnClickListener { abrir(MercadoriaListaFragment())}
        b.clientes.setOnClickListener    { abrir(ClienteListaFragment())   }
        b.novaVenda.setOnClickListener   { abrir(VendaFragment())          }
        b.historico.setOnClickListener   { abrir(VendaListaFragment())     }
        b.cadastrar.setOnClickListener   { abrir(UsuarioFormFragment())    }
        b.dashboard.setOnClickListener   { abrir(DashboardFragment())      }
        b.sair.setOnClickListener        {
            Sessao.sair()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        b.caixa.setOnClickListener {
            perguntarQuantidade()
        }
    }

    private fun perguntarQuantidade() {
        val layoutPrincipal = LinearLayout(requireContext()).apply {
            gravity = Gravity.CENTER
        }
        val campo = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setText("")
            selectAll()
            layoutParams = LinearLayout.LayoutParams(
                200,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        layoutPrincipal.addView(campo)
        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle("Suprimento e Sangria")
            .setMessage("No caixa: ${moeda(Sessao.caixa)}")
            .setView(layoutPrincipal)
            .setPositiveButton("Adicionar", null)
            .setNegativeButton("Remover", null)
            .setNeutralButton("Sair", null)
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val quantidadeDigitada = campo.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                if (quantidadeDigitada > 0) {
                    Sessao.caixa += quantidadeDigitada
                    dialogo.dismiss()
                    mostrarSaldo()
                    imprimirComprovante("Suprimento", quantidadeDigitada)
                } else {
                    campo.error = "Digite um valor maior que zero"
                }
            }
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                val quantidadeDigitada = campo.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                if (quantidadeDigitada > 0 && quantidadeDigitada <= Sessao.caixa) {
                    Sessao.caixa -= quantidadeDigitada
                    dialogo.dismiss()
                    mostrarSaldo()
                    imprimirComprovante("Sangria", quantidadeDigitada)
                } else {
                    campo.error = "Valor inválido"
                }
            }
        }
        dialogo.show()
    }

    private fun mostrarSaldo() {
        b.saldoCaixa.text = moeda(Sessao.caixa)
    }

    private fun imprimirComprovante(tipo: String, valor: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ok = ImpressoraRede.imprimeComprovante(
                    requireContext(),
                    logo = BitmapFactory.decodeResource(resources, R.drawable.logobmp),
                    tipo = tipo,
                    valor = valor,
                    saldo = Sessao.caixa
                )
                if (!ok) aviso("Falha na impressão")
            } catch (e: Exception) {
                mostrarErro(e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}