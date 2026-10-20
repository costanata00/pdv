package com.treinamento.pdv

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

class MainActivity : TelaBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.moldura, MenuFragment())
                .commit()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            atualizarSeta()
        }
        atualizarSeta()

        // Botão/gesto voltar do celular também passa pelo voltar() da tela
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                voltar()
            }
        })
    }
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

    // Pede para a tela atual voltar (assim ela pode perguntar antes, como a VendaFragment)
    private fun voltar() {
        val atual = supportFragmentManager.findFragmentById(R.id.moldura)
        if (supportFragmentManager.backStackEntryCount > 0 && atual is FragmentBase) {
            atual.voltar()
        } else {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        voltar()
        return true
    }
}
