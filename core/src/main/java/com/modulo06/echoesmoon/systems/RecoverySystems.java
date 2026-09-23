package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.LinkedHashSet;
import java.util.Set;

/** Sistemas compactos da recuperação: mantêm estado e lógica fora das telas. */
public final class RecoverySystems {
    private RecoverySystems() {}

    public static final class Quest {
        public final String id, titulo;
        public boolean feita;
        public Quest(String id, String titulo) { this.id = id; this.titulo = titulo; }
    }

    public static final class QuestLog {
        public final Array<Quest> quests = new Array<>();
        public QuestLog() {
            quests.add(new Quest("GELO", "Coletar material de gelo"));
            quests.add(new Quest("ESTUFA", "Reparar a estufa"));
            quests.add(new Quest("BOSS", "Derrotar o boss da fase"));
        }
        public void complete(String id) { for (Quest q : quests) if (q.id.equals(id)) q.feita = true; }
        public boolean isComplete(String id) { for (Quest q : quests) if (q.id.equals(id)) return q.feita; return false; }
    }

    public static final class Drone {
        public final Vector2 position = new Vector2();
        public boolean ativo;
        public void update(float delta, float playerX, float playerY, boolean olhandoEsquerda) {
            Vector2 alvo = new Vector2(playerX + (olhandoEsquerda ? 40 : -40), playerY + 20);
            position.lerp(alvo, Math.min(1f, 3f * delta));
        }
    }

    public static final class Codex {
        public final Set<String> visitados = new LinkedHashSet<>();
        public void visitar(String mundo) { visitados.add(mundo); }
        public boolean aberto(String mundo) { return visitados.contains(mundo); }
        public String texto(String mundo) {
            if (!aberto(mundo)) return "???";
            switch (mundo) {
                case "LUA": return "A base lunar guarda os primeiros sinais da anomalia.";
                case "MARTE": return "As tempestades vermelhas cobrem ruinas de uma antiga colonia.";
                case "TITA": return "O metano de Tita esconde uma rota sob a escuridao.";
                case "CALISTO": return "Fora do cinturao de radiacao, Calisto virou porto logistico.";
                default: return "???";
            }
        }
    }

    public static boolean craft(Inventario inv, String a, String b, String saida) {
        if (!inv.tem(a) || !inv.tem(b)) return false;
        inv.consumir(a); inv.consumir(b); inv.add(saida); return true;
    }
}
