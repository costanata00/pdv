package com.treinamento.pdv

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class MainActivity : TelaBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.barra))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raiz)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.moldura, MenuFragment())
                .commit()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            atualizarSeta()
        }
        atualizarSeta()

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
        supportActionBar?.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    private fun voltar() {
        val moldura = supportFragmentManager.findFragmentById(R.id.moldura)
        if (supportFragmentManager.backStackEntryCount > 0 && moldura is FragmentBase) {
            moldura.voltar()
        } else {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        voltar()
        return true
    }
}
