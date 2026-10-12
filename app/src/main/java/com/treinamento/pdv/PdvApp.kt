package com.treinamento.pdv
import android.app.Application
import com.treinamento.pdv.data.Sessao

class PdvApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Sessao.iniciar(this)
    }
}
