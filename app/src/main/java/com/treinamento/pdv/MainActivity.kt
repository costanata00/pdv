package com.treinamento.pdv

import android.os.Bundle
import androidx.fragment.app.Fragment

class MainActivity : TelaBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //default é tela de menu
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.moldura, MenuFragment())
                .commit()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            atualizarSeta()
        }
        atualizarSeta()
    }

    // Coloca um fragment novo no topo da pilha
    fun abrir(tela: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.moldura, tela)
            .addToBackStack(null)
            .commit()
    }

    private fun atualizarSeta() {
        val temParaOndeVoltar = supportFragmentManager.backStackEntryCount > 0
        supportActionBar?.setDisplayHomeAsUpEnabled(temParaOndeVoltar)
    }

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