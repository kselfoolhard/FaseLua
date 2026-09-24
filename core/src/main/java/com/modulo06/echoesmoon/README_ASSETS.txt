ECHOES MOON - assets opcionais/esperados

VIDEO
- video/weirdending.webm  <- encerramento final (WebM VP8/Vorbis recomendado no desktop)

MENU
- menu_background.png
- logo.png
- menu_astronaut.png  <- sprite sheet horizontal de 4 frames
- border.png  <- moldura da HUD/fullscreen 1280x720
- player_unarmed.png <- sprite sem arma, usado antes do primeiro armamento

CALISTO
- fundo_calisto.png  <- fundo 1200x1200
- boss_calisto.png

TITA
- boss_tita.png
- lantern.png (opcional)

PUZZLE/CORPO
- alavanca.png (opcional; fallback usa o jogo normalmente)
- cadaver.png (opcional; fallback sem sprite)

MUSICA
- music/boss.ogg (fallback)
- music/tita.ogg ou music/boss_tita.ogg

VIDEO CODEC
O codigo usa a API oficial do gdx-video por reflexao e espera isBuffered() antes de play(), evitando erro de compilacao quando a dependencia esta apenas no runtime.
Para Desktop, o projeto precisa incluir gdx-video core + backend LWJGL3 e seus nativos.
WebM VP8 + Vorbis e o formato recomendado para desktop pelo projeto gdx-video.


SPRITES SEM ARMA
- player_unarmed.png = idle parado (folha com 4 frames)
- player_unarmed_walk.png = caminhada sem arma (4 frames)
- player_unarmed_punch.png = soco sem arma (4 frames)
- sfx/punch.wav (ou sfx/soco.wav) = efeito do soco
- music/boss_tita.ogg = musica prioritaria do chefe de Tita

Quando inventario.temArma virar true, o jogo troca automaticamente para os sprites/ataques armados.


LOJA
- B abre/fecha a loja durante o gameplay.
- 1 compra upgrade de arma.
- 2 compra upgrade de armadura.
- Abates geram creditos; eles nao liberam upgrades automaticamente.

CHARGED ATTACK
- Segure o mouse e solte para disparar carregado.
- sfx/charged.wav toca no disparo carregado.
- player_unarmed_punch.png usa apenas o primeiro frame.

INTRO
- lua_cutscene.png e nave_cutscene.png formam a aproximacao da nave ate a Lua.

SOM DO ATAQUE CARREGADO: sfx/charged.wav
ARTE DA INTRO: lua_cutscene.png + nave_cutscene.png
LANERNA TITA: F liga/desliga; a direcao acompanha a crosshair.
LOJA: B durante gameplay; 1 arma; 2 armadura; upgrades somente pela loja.
PLAYER SEM ARMA: player_unarmed.png, player_unarmed_walk.png e primeiro frame de player_unarmed_punch.png.

DRONE
- Adicione drone.png na pasta de assets do jogo.
- A tecla C liga/desliga o drone durante as fases de gameplay.
- Se drone.png nao existir, o jogo cria um sprite pixel-art de emergencia em runtime.
- O drone segue o astronauta, recupera O2 gradualmente e dispara automaticamente contra inimigos/bosses.

OXIGENIO
- Quando o O2 chega a 0, a falta de oxigenio remove vida lentamente ate o jogador morrer.
- Itens de O2, zonas seguras e o drone podem recuperar O2.
