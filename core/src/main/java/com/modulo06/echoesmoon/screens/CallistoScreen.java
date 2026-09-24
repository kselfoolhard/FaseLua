package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.BossCalisto;
import com.modulo06.echoesmoon.entities.Enemy;
import com.modulo06.echoesmoon.entities.FoodDrop;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.entities.WorldRock;
import com.modulo06.echoesmoon.systems.BossBalance;
import com.modulo06.echoesmoon.systems.CrosshairUtil;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameHud;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.LoadingOverlay;
import com.modulo06.echoesmoon.systems.RecoverySystems;
import com.modulo06.echoesmoon.systems.RouteSystem;
import com.modulo06.echoesmoon.systems.SegredoSystem;
import com.modulo06.echoesmoon.systems.SoundManager;
import com.modulo06.echoesmoon.systems.UpgradeSystem;
import com.modulo06.echoesmoon.systems.PlayerCombat;
import com.modulo06.echoesmoon.systems.WorldCollision;

/** Calisto: arena 1200x1200 + boss bullet hell + tres formas. */
public class CallistoScreen implements Screen {
    private final RecoverySystems.Drone drone = new RecoverySystems.Drone();
    private static final float WORLD_W = 1200f, WORLD_H = 1200f, VIEW_W = 800f, VIEW_H = 600f;
    private final Game game; private final GameSaveData save;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer(); private final SpriteBatch batch = new SpriteBatch(); private final BitmapFont font = new BitmapFont();
    private final DialogSystem dialog = new DialogSystem(); private final BossCalisto boss;
    private final Rectangle player = new Rectangle(120,120,32,48); private final Rectangle portalAharin = new Rectangle(1080,1040,54,88); private final Rectangle segredoCalisto = new Rectangle(1120,90,40,40);
    private final Array<FoodDrop> comidas = new Array<>(); private final Array<Enemy> minions = new Array<>(); private final Array<WorldRock> pedrasMapa = new Array<>(); private final Array<SlashWave> projectiles = new Array<>(); private final Array<SlashWave> slashes = new Array<>();
    private Texture fundoTex,bossTex,foodTex,iceTex,pedraTex,portraitBoss,alienTex;
    private Animation<TextureRegion> animIdle,animWalk,animSlash; private Animation<TextureRegion> animIdleUnarmed,animWalkUnarmed,animPunch; private int estadoJogador=0; private float slashAnimTimer,timerPasso,stateTime,shootCooldown,hordeTimer,bulletTimer,chargeTimer; private String mensagemHUD=""; private float mensagemTimer; private float fadeAlpha=1f; private boolean fadingOut=false; private Screen nextScreen=null; private boolean mapaAberto=false;

    public CallistoScreen(Game game, GameSaveData save) {
        this.game=game; this.save=save; save.sincronizarInventario();
        drone.ativo = save.droneAtivo; drone.loadSprite(); save.codex.visitar("CALISTO"); GameHud.reset(); camera.setToOrtho(false,VIEW_W,VIEW_H);
        boss=new BossCalisto(BossBalance.hpFor(save,300)); boss.x=820; boss.y=820; loadTextures(); spawnAmbient();  SoundManager.playSound("bossgrowl");
        dialog.start(new String[]{"Ate aqui voce chegou...","CALISTO nao vai cair sem exigir tudo de voce.","Prepare-se: a ultima guardia conhece tres formas de combate."}, portraitBoss!=null?portraitBoss:bossTex);
    }
    private void loadTextures(){
        fundoTex=load("fundo_calisto.png"); bossTex=load("boss_calisto.png"); foodTex=load("food.png"); iceTex=load("ice.png"); pedraTex=load("pedra.png"); portraitBoss=load("portrait_boss.png"); alienTex=load("alien.png");
        Texture idle=load("player_lunar.png"), walk=load("player_lunar_walk.png"), slash=load("player_lunar_slash.png");
        Texture idleUnarmed=load("player_unarmed.png"), walkUnarmed=load("player_unarmed_walk.png"), punch=load("player_unarmed_punch.png");
        if(idle!=null) animIdle=new Animation<>(0.2f,TextureRegion.split(idle,Math.max(1,idle.getWidth()/4),idle.getHeight())[0]);
        if(walk!=null) animWalk=new Animation<>(0.12f,TextureRegion.split(walk,Math.max(1,walk.getWidth()/4),walk.getHeight())[0]);
        if(slash!=null) animSlash=new Animation<>(0.1f,TextureRegion.split(slash,Math.max(1,slash.getWidth()/4),slash.getHeight())[0]);
        if(idleUnarmed!=null) animIdleUnarmed=new Animation<>(0.2f,TextureRegion.split(idleUnarmed,Math.max(1,idleUnarmed.getWidth()/4),idleUnarmed.getHeight())[0]);
        if(walkUnarmed!=null) animWalkUnarmed=new Animation<>(0.12f,TextureRegion.split(walkUnarmed,Math.max(1,walkUnarmed.getWidth()/4),walkUnarmed.getHeight())[0]);
        if(punch!=null){TextureRegion[][] punchFrames=TextureRegion.split(punch,Math.max(1,punch.getWidth()/4),punch.getHeight());animPunch=new Animation<>(0.08f,punchFrames[0][0]);}
    }
    private Texture load(String path){ try{ if(Gdx.files.internal(path).exists()) return new Texture(path); String a="images/"+path; if(Gdx.files.internal(a).exists()) return new Texture(a); }catch(Exception ignored){} return null; }
    private void spawnAmbient(){
        Rectangle br=new Rectangle(boss.x,boss.y,boss.width,boss.height); for(int i=0;i<12;i++){float x=MathUtils.random(80f,WORLD_W-80f),y=MathUtils.random(80f,WORLD_H-80f);Rectangle r=new Rectangle(x,y,28,28);if(!r.overlaps(player)&&!r.overlaps(portalAharin)&&!r.overlaps(br))comidas.add(new FoodDrop(x,y));}
        Array<Rectangle> f=new Array<>(); f.add(player);f.add(portalAharin);f.add(br);f.add(segredoCalisto);for(FoodDrop c:comidas)f.add(c.rect); WorldRock.spawnMany(pedrasMapa,22,WORLD_W,WORLD_H,player,f,110f,404L);
    }
        @Override public void render(float delta){
        update(delta); Gdx.gl.glClearColor(0.01f,0.02f,0.03f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); float hw=VIEW_W/2f,hh=VIEW_H/2f; camera.position.set(MathUtils.clamp(player.x+16,hw,WORLD_W-hw),MathUtils.clamp(player.y+24,hh,WORLD_H-hh),0);camera.update();
        batch.setProjectionMatrix(camera.combined);batch.begin();if(fundoTex!=null)batch.draw(fundoTex,0,0,WORLD_W,WORLD_H);for(WorldRock r:pedrasMapa)if(pedraTex!=null)batch.draw(pedraTex,r.rect.x,r.rect.y,r.rect.width,r.rect.height);for(FoodDrop f:comidas)if(foodTex!=null)batch.draw(foodTex,f.rect.x,f.rect.y,f.rect.width,f.rect.height);for(Enemy e:minions)if(e.ativo&&alienTex!=null)batch.draw(alienTex,e.rect.x,e.rect.y,e.rect.width,e.rect.height);if(bossTex!=null&&!boss.mortoFinal)batch.draw(bossTex,boss.x,boss.y,boss.width,boss.height);
        for(SlashWave p:projectiles)if(p.active&&slashTex()!=null)batch.draw(slashTex(),p.rect.x,p.rect.y,p.rect.width,p.rect.height);for(SlashWave s:slashes)if(s.active&&slashTex()!=null)batch.draw(slashTex(),s.rect.x,s.rect.y,48,48); drone.draw(batch);
        TextureRegion fr=null; boolean armado=save.inventario.temArma;
        if(estadoJogador==2){ if(armado&&animSlash!=null)fr=animSlash.getKeyFrame(stateTime,false); else if(!armado&&animPunch!=null)fr=animPunch.getKeyFrame(stateTime,false); else if(!armado&&animIdleUnarmed!=null)fr=animIdleUnarmed.getKeyFrame(stateTime,true); }
        else if(estadoJogador==1){ if(armado&&animWalk!=null)fr=animWalk.getKeyFrame(stateTime,true); else if(!armado&&animWalkUnarmed!=null)fr=animWalkUnarmed.getKeyFrame(stateTime,true); }
        else { if(armado&&animIdle!=null)fr=animIdle.getKeyFrame(stateTime,true); else if(!armado&&animIdleUnarmed!=null)fr=animIdleUnarmed.getKeyFrame(stateTime,true); }
        if(fr!=null){batch.setColor(chargeTimer>0?new Color(1f,1f,1f,0.55f+0.45f*(float)Math.abs(Math.sin(stateTime*18f))):Color.WHITE);batch.draw(fr,player.x,player.y,player.width,player.height);batch.setColor(Color.WHITE);} batch.end(); shape.setProjectionMatrix(camera.combined);
        drone.drawBeam(shape); PlayerCombat.drawChargeParticles(shape, player, chargeTimer, stateTime);
        shape.setProjectionMatrix(camera.combined);shape.begin(ShapeRenderer.ShapeType.Filled);for(Enemy e:minions)if(e.ativo){shape.setColor(Color.RED);shape.rect(e.rect.x,e.rect.y+e.rect.height+5,e.rect.width,5);shape.setColor(Color.GREEN);shape.rect(e.rect.x,e.rect.y+e.rect.height+5,e.rect.width*Math.max(0,e.hp/(float)e.maxHp),5);}shape.end();
        desenharHUD(); desenharFade(delta); if(save.inventario.aberto)save.inventario.render(batch,shape,font,new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0,0,1280,720),save); CrosshairUtil.desenharMira(shape);
    }
    private Texture slashTexture;
    private Texture slashTex(){ if(slashTexture==null) slashTexture=load("slash_wave.png"); return slashTexture; }
    private void update(float delta){
        if(save.inventario!=null){if(Gdx.input.isKeyJustPressed(Input.Keys.I))save.inventario.aberto=!save.inventario.aberto;if(save.inventario.aberto){if(Gdx.input.isKeyJustPressed(Input.Keys.C))save.inventario.usarComida(save);return;}}
        if(GameHud.handleInput(save))return; if(Gdx.input.isKeyJustPressed(Input.Keys.C)&&save.inventario.drone>0){drone.ativo=!drone.ativo;save.droneAtivo=drone.ativo;save.salvar();} if(Gdx.input.isKeyJustPressed(Input.Keys.M)){mapaAberto=!mapaAberto;return;}if(mapaAberto)return;if(dialog.isOpen()){if(Gdx.input.isKeyJustPressed(Input.Keys.E)||Gdx.input.isKeyJustPressed(Input.Keys.ENTER))dialog.next();return;}
        shootCooldown-=delta;hordeTimer+=delta;bulletTimer+=delta;if(mensagemTimer>0)mensagemTimer-=delta;RecoverySystems.updateOxygen(save,delta,0.65f);
        if(save.vida<=0){RecoverySystems.criarCadaver(save,player.x,player.y);save.fase="CALISTO";save.salvar();game.setScreen(new GameOverScreen(game));return;}
        float dx=0,dy=0;boolean moving=false;if(Gdx.input.isKeyPressed(Input.Keys.A)){dx-=260*delta;moving=true;}if(Gdx.input.isKeyPressed(Input.Keys.D)){dx+=260*delta;moving=true;}if(Gdx.input.isKeyPressed(Input.Keys.W)){dy+=260*delta;moving=true;}if(Gdx.input.isKeyPressed(Input.Keys.S)){dy-=260*delta;moving=true;}WorldCollision.movePlayer(player,dx,dy,pedrasMapa,WORLD_W,WORLD_H);stateTime+=delta;
        if(estadoJogador==2){slashAnimTimer-=delta;if(slashAnimTimer<=0)estadoJogador=0;}else if(moving){estadoJogador=1;timerPasso-=delta;if(timerPasso<=0){SoundManager.playSound(MathUtils.randomBoolean()?"footstep1":"footstep2");timerPasso=.35f;}}else{estadoJogador=0;timerPasso=0;}
        if(save.cadaverAtivo){float d2=Vector2.dst2(player.x,player.y,save.cadaverX,save.cadaverY);if(d2<60*60&&Gdx.input.isKeyJustPressed(Input.Keys.E)){RecoverySystems.recuperarCadaver(save);mensagemHUD="CADAVER RECUPERADO";mensagemTimer=2.5f;}}
        for(int i=comidas.size-1;i>=0;i--)if(player.overlaps(comidas.get(i).rect)){save.inventario.add("COMIDA");comidas.removeIndex(i);mensagemHUD="+1 COMIDA";mensagemTimer=1.5f;SoundManager.playSound("pickup");}
        if (drone.ativo) {
            boolean disparouDrone = drone.assist(delta, player.x, player.y, save, boss.x + boss.width * 0.5f, boss.y + boss.height * 0.5f, !boss.mortoFinal);
            if (disparouDrone && !boss.mortoFinal) { boss.levarDano(Math.max(4f, UpgradeSystem.danoArma(save) * 0.45f), save.inventario); SoundManager.playSound("hit_enemy"); }
        }
        if(!save.inventario.temArma&&Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)&&shootCooldown<=0f){socarCallisto();}
        else if(save.inventario.temArma&&Gdx.input.isButtonPressed(Input.Buttons.LEFT)){chargeTimer=Math.min(1.2f,chargeTimer+delta);}else if(save.inventario.temArma&&chargeTimer>=0.8f){disparar(true);chargeTimer=0;}else if(save.inventario.temArma&&chargeTimer>0){disparar(false);chargeTimer=0;}
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)&&chargeTimer==0){} // Tap normal e disparo carregado sao separados pelo release.
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE))disparar(false);
        for(int i=slashes.size-1;i>=0;i--){SlashWave s=slashes.get(i);s.update(delta);if(!s.active){slashes.removeIndex(i);continue;}if(!boss.mortoFinal&&s.rect.overlaps(new Rectangle(boss.x,boss.y,boss.width,boss.height))){boss.levarDano(UpgradeSystem.danoArma(save)*(s.damageMultiplier),save.inventario);s.active=false;SoundManager.playSound("hit_enemy");}}
        if(bulletTimer>=1f&&!boss.mortoFinal){
            bulletTimer=0f;
            int n=8+boss.forma*4;
            for(int i=0;i<n;i++){float a=i*(360f/n)+stateTime*45f;float tx=boss.x+MathUtils.cosDeg(a)*600f,ty=boss.y+MathUtils.sinDeg(a)*600f;projectiles.add(new SlashWave(boss.x+32,boss.y+32,tx,ty));}
            // rajada curta apontada para o jogador em toda segunda rodada
            if (((int)stateTime)%2==0) { for(int j=-1;j<=1;j++){ float tx=player.x+j*85f, ty=player.y+j*45f; projectiles.add(new SlashWave(boss.x+32,boss.y+32,tx,ty)); } }
        }
        if(boss.forma==2&&hordeTimer>=8f){hordeTimer=0;for(int i=0;i<RouteSystem.enemyCount(save,1);i++)minions.add(new Enemy(boss.x+70+i*38,boss.y+30+(i%2)*24,1));}
        for(int i=projectiles.size-1;i>=0;i--){SlashWave p=projectiles.get(i);p.update(delta);if(p.rect.overlaps(player)){UpgradeSystem.aplicarDano(save,RouteSystem.isAggressive(save)?15:12);p.active=false;}if(!p.active)projectiles.removeIndex(i);}
        for(int i=minions.size-1;i>=0;i--){Enemy e=minions.get(i);if(!e.ativo){minions.removeIndex(i);continue;}e.update(delta,new Vector2(player.x,player.y));if(e.rect.overlaps(player))UpgradeSystem.aplicarDano(save,8f*delta);}
        if(!boss.mortoFinal){float d2=Vector2.dst2(player.x,player.y,boss.x,boss.y);if(d2<80*80)UpgradeSystem.aplicarDano(save,12f*delta);}
        if(boss.mortoFinal&&!save.bossCalistoDerrotado){save.bossCalistoDerrotado=true;save.inventario.add("CHAVE_LUZ");save.fase="CALISTO";save.salvar();mensagemHUD="CHAVE DE LUZ OBTIDA";mensagemTimer=4f;}
        if(Gdx.input.isKeyJustPressed(Input.Keys.E)&&player.overlaps(portalAharin)){if(save.inventario.tem("CHAVE_LUZ")){save.fase="AHARIN";save.salvar();fadingOut=true;nextScreen=new AharinScreen(game,save);}else{mensagemHUD="A CHAVE DE LUZ ESTA BLOQUEADA";mensagemTimer=2f;}}
    }

    private void socarCallisto(){
        Vector3 m=new Vector3(Gdx.input.getX(),Gdx.input.getY(),0);camera.unproject(m);Rectangle hit=PlayerCombat.punchBox(player,m);estadoJogador=2;slashAnimTimer=.28f;stateTime=0;shootCooldown=.28f;SoundManager.playSound("punch");if(!boss.mortoFinal&&hit.overlaps(new Rectangle(boss.x,boss.y,boss.width,boss.height))){boss.levarDano(8f,save.inventario);SoundManager.playSound("hit_enemy");}}
    private void disparar(boolean charged){if(boss.mortoFinal||save.inventario.municao<=0)return;save.inventario.municao--;save.municao=save.inventario.municao;Vector3 m=new Vector3(Gdx.input.getX(),Gdx.input.getY(),0);camera.unproject(m);estadoJogador=2;slashAnimTimer=.3f;stateTime=0;slashes.add(new SlashWave(player.x,player.y,m.x,m.y,charged));SoundManager.playSound(charged ? "charged" : "slash");shootCooldown=.16f;}
    private void desenharHUD(){GameHud.drawBoss(shape,batch,font,save,"CALISTO",mensagemTimer>0?mensagemHUD:"","GUARDIA DE CALISTO",boss.hp,boss.hpMax,boss.forma,3,!boss.mortoFinal);if(!GameHud.isLogAberto())dialog.render(batch,shape,font);}
    private void desenharFade(float delta){if(fadeAlpha>0||fadingOut){if(fadingOut)fadeAlpha=Math.min(1f,fadeAlpha+delta*2f);else fadeAlpha=Math.max(0f,fadeAlpha-delta*2f);com.badlogic.gdx.math.Matrix4 hud=new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0,0,1280,720);shape.setProjectionMatrix(hud);shape.begin(ShapeRenderer.ShapeType.Filled);shape.setColor(0,0,0,fadeAlpha);shape.rect(0,0,1280,720);shape.end();if(!fadingOut&&fadeAlpha>0)LoadingOverlay.draw(shape,stateTime,fadeAlpha);if(fadingOut&&fadeAlpha>=1&&nextScreen!=null)game.setScreen(nextScreen);}}
    @Override public void show(){ Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None); SoundManager.playMusic("boss", true); }
    @Override public void resize(int w,int h){camera.update();}@Override public void pause(){}@Override public void resume(){}@Override public void hide(){SoundManager.stopMusic();GameHud.reset();}@Override public void dispose(){drone.dispose();shape.dispose();batch.dispose();font.dispose();if(fundoTex!=null)fundoTex.dispose();if(bossTex!=null)bossTex.dispose();if(foodTex!=null)foodTex.dispose();if(iceTex!=null)iceTex.dispose();if(pedraTex!=null)pedraTex.dispose();if(portraitBoss!=null)portraitBoss.dispose();if(alienTex!=null)alienTex.dispose();if(slashTexture!=null)slashTexture.dispose();}
}
