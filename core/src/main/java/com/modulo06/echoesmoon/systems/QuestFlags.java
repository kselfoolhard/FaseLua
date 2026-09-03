package com.modulo06.echoesmoon.systems;

public class QuestFlags {
    public boolean dialogoTita, combateOk, amostraOk, entrouTita;

    public boolean portalLiberado() {
        return dialogoTita && (combateOk || amostraOk);
    }

    public String missaoAtual() {
        if (!dialogoTita) return "MISSAO: Falar com o oficial";
        if (!portalLiberado()) return "MISSAO: Combate ou entregar amostra";
        if (!entrouTita) return "MISSAO: Ativar portal de Tita";
        return "MISSAO: Explorar Tita (Saturno)";
    }

    public String statusPortal() {
        return portalLiberado() ? "PORTAL TITA ONLINE" : "PORTAL TITA BLOQUEADO";
    }
}
