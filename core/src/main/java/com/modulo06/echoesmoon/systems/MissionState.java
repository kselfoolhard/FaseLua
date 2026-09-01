package com.modulo06.echoesmoon.systems;


public class MissionState {
    private int etapa = 0;
    private final String[] etapas = {
        "[1/5] Encontre e colete a Caixa de Pecas (Laranja)",
        "[2/5] Leve a peca ate a Base para reparar a estufa e equipar a arma",
        "[3/5] Entre no Portal (Roxo) para viajar ate Marte",
        "[5/5] Elimine todos os aliens remanescentes para vencer!"
    };

    public String getAtual() {
        return etapas[Math.min(etapa, etapas.length - 1)];
    }

    public int getEtapaIndex() { return etapa; }
    public void setEtapaIndex(int index) { this.etapa = index; }

    public void avancarPara(int novaEtapa) {
        if (novaEtapa > etapa && novaEtapa < etapas.length) {
            etapa = novaEtapa;
        }
    }
}
