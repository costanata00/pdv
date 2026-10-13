package com.treinamento.pdv

import android.os.Bundle
import androidx.fragment.app.Fragment

class MainActivity : TelaBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Na primeira vez que a moldura abre, coloca o menu dentro dela
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.moldura, MenuFragment())
                .commit()
        }

        // Toda vez que a pilha de telas muda, decide se mostra a seta de voltar
        supportFragmentManager.addOnBackStackChangedListener {
            atualizarSeta()
        }
        atualizarSeta()
    }

    // Coloca um fragment novo na moldura. O anterior vai para a pilha.
    fun abrir(tela: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.moldura, tela)
            .addToBackStack(null)
            .commit()
    }

    // A seta só aparece quando existe uma tela para onde voltar
    private fun atualizarSeta() {
        val temParaOndeVoltar = supportFragmentManager.backStackEntryCount > 0
        supportActionBar?.setDisplayHomeAsUpEnabled(temParaOndeVoltar)
    }

    // Tocar na seta = voltar para o fragment anterior
    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return true
    }
}


//Como ler:
//     No código                                Significa
//supportFragmentManager           = O "gerente" que troca os quadros da moldura e cuida da pilha
//beginTransaction() ... .commit() = "Vou fazer uma troca" ... "pode fazer"
//.replace(R.id.moldura, tela)     = Tira o quadro atual da moldura e põe este
//.addToBackStack(null)            = Guarda o quadro anterior na pilha, para o voltar funcionar
//popBackStack()                   = Tira o quadro de cima da pilha (volta uma tela)
//savedInstanceState == null       = É a primeira vez que a tela abre (e não uma reabertura do Android)