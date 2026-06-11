package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.spacefarm.inventory.Crystal;
import com.spacefarm.session.GameSession;
import com.spacefarm.world.BaseZoneConstants;
import com.spacefarm.world.TileCoord;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.graphics;

public class DroneConsoleOverlay {
    // ── Accent colours
    private static final float AC_R = 0.10f, AC_G = 0.88f, AC_B = 1.00f;
    private static final float GR_R = 0.15f, GR_G = 0.90f, GR_B = 0.40f;
    private static final float WN_R = 0.95f, WN_G = 0.75f, WN_B = 0.10f;

    // ── Panel size ────────────────────────────────────────────────────────────
    private static final float PANEL_W   = 600f;
    private static final float PANEL_H   = 480f;
    private static final float HEADER_H  = 50f;
    private static final float TAB_H     = 38f;
    private static final float SLOT_SZ   = 60f;
    private static final int   CRYSTAL_PRICE = 50;
    // Upgrade row height — kept small so all 9 items fit in one screen
    private static final float ROW_H     = 56f;
    private static final float ROW_GAP   = 4f;

    // Computed in render
    private float PX, PY;               // panel origin (Y-up)
    private float contentTop, contentBot; // scrollable area bounds (Y-up)

    // ── Rendering ─────────────────────────────────────────────────────────────
    private final GameSession   session;
    private final ShapeRenderer sr;
    private final SpriteBatch   batch;
    private final BitmapFont    titleFont;
    private final BitmapFont    bodyFont;
    private final BitmapFont    smallFont;
    private final BitmapFont    hintFont;
    private final OrthographicCamera camera;
    private final GlyphLayout   gl = new GlyphLayout();

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean visible   = false;
    private int     activeTab = 0;

    private enum DS { IDLE, FLYING_AWAY, RETURNING }
    private DS    droneState = DS.IDLE;
    private float animTimer  = 0f;
    private float pendingBal = 0f;
    private static final float BASE_FLIGHT = 5f;

    private int   crystals  = 0;
    private float scrollY   = 0f;
    private float maxScroll = 0f;

    private final Rectangle sellBtn = new Rectangle();

    // ── Upgrades ──────────────────────────────────────────────────────────────
    private static class Upg {
        String id, name, desc;
        float cost;
        int lv, max;
        Upg(String id,String name,String desc,float cost,int max){
            this.id=id;this.name=name;this.desc=desc;this.cost=cost;this.max=max;}
        boolean maxed(){return lv>=max;}
    }
    private final List<Upg> tree = new ArrayList<>();
    private final List<Upg> base = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public DroneConsoleOverlay(GameSession session) {
        this.session = session;
        sr    = new ShapeRenderer();
        batch = new SpriteBatch();
        titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 26);
        bodyFont  = FontUtils.createFont("fonts/ArialBold.ttf", 15);
        smallFont = FontUtils.createFont("fonts/ArialBold.ttf", 13);
        hintFont  = FontUtils.createFont("fonts/ArialBold.ttf", 11);
        camera    = new OrthographicCamera();

        tree.add(new Upg("tree_growth","Розширення коріння","Швидший ріст рослин",200,1));
        tree.add(new Upg("tree_oxygen","Щільність листя","Краща генерація кисню",350,1));
        tree.add(new Upg("tree_water","Ядро зволоження","Повільніше випаровування",500,1));
        tree.add(new Upg("tree_rare","Рідкісний цвіт","Шанс рідкісного насіння +10%",750,1));
        tree.add(new Upg("tree_final","Дерево Життя","Максимальний кисень +50%",1500,1));
        base.add(new Upg("base_inv","Мод рюкзака","Більший інвентар",800,2));
        base.add(new Upg("base_speed","Турбо-двигуни","Швидша доставка дрона",400,3));
        base.add(new Upg("base_scavenge","Дрон-сканер","Швидше дослідження руїн",600,3));
        base.add(new Upg("base_garden","Розширення саду","Додати 1 грядку на базу",200,
                BaseZoneConstants.MAX_GARDEN_BEDS - BaseZoneConstants.STARTING_GARDEN_BEDS));

        recalcScroll();
    }

    private void recalcScroll() {
        float total = 22f
                + tree.size() * (ROW_H + ROW_GAP)
                + 18f + 22f
                + base.size() * (ROW_H + ROW_GAP);
        float viewport = PANEL_H - HEADER_H - TAB_H - 12f;
        maxScroll = Math.max(0f, total - viewport);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void setVisible(boolean v) {
        if (this.visible == v) return;
        this.visible = v;
        if (v) { scrollY = 0; }
        else if (droneState == DS.IDLE) {
            while (crystals > 0) {
                if (session.getInventory().addItem(new Crystal())) crystals--;
                else break;
            }
        }
    }

    public boolean isVisible() { return visible; }

    public int getScavengeUpgradeLevel() { return lv("base_scavenge"); }

    private int lv(String id) {
        for (Upg u:tree) if(u.id.equals(id)) return u.lv;
        for (Upg u:base) if(u.id.equals(id)) return u.lv;
        return 0;
    }

    private float flightDur() { return Math.max(1f, BASE_FLIGHT - lv("base_speed")); }

    public void addCrystal() { if (droneState == DS.IDLE) crystals++; }

    public boolean isOverTradeSlot(float sx, float sy) {
        if (!visible || activeTab != 0 || droneState != DS.IDLE) return false;
        float wy = graphics.getHeight() - sy;
        float slotX = PX + PANEL_W/2f - SLOT_SZ/2f;
        float slotY = PY + PANEL_H*0.43f;
        return sx>=slotX && sx<=slotX+SLOT_SZ && wy>=slotY && wy<=slotY+SLOT_SZ;
    }

    public boolean handleScrolled(float amount) {
        if (!visible || activeTab != 1) return false;
        scrollY = Math.max(0, Math.min(maxScroll, scrollY + amount * 25f));
        return true;
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    public void update(float dt) {
        if (droneState == DS.IDLE) return;
        animTimer += dt;
        float prog = animTimer / flightDur();
        float ww = session.getBaseLayer().getWidth()  * session.getBaseLayer().getTileWidth();
        float wh = session.getBaseLayer().getHeight() * session.getBaseLayer().getTileHeight();
        float sx = session.getBaseZone().getDroneZoneCenter().x() * session.getBaseLayer().getTileWidth();
        float sy = session.getBaseZone().getDroneZoneCenter().y() * session.getBaseLayer().getTileHeight();
        float dx = (ww+1000f)-sx, dy = (wh+1000f)-sy;
        if (droneState == DS.FLYING_AWAY)
            session.getBaseZone().setDroneOffsets(prog*dx, prog*dy);
        else
            session.getBaseZone().setDroneOffsets((1f-prog)*dx, (1f-prog)*dy);
        if (animTimer >= flightDur()) {
            animTimer = 0;
            if (droneState == DS.FLYING_AWAY) {
                droneState = DS.RETURNING;
            } else {
                droneState = DS.IDLE;
                session.getBaseZone().setDroneOffsets(0,0);
                session.getWallet().earn(pendingBal);
                pendingBal = 0;
            }
        }
    }

    // ── Render ─────────────────────────────────────────────────────────────────

    public void render() {
        if (!visible) return;
        int sw = graphics.getWidth(), sh = graphics.getHeight();
        camera.setToOrtho(false, sw, sh);
        camera.update();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Center panel
        PX = (sw - PANEL_W) / 2f;
        PY = (sh - PANEL_H) / 2f;
        contentTop = PY + PANEL_H - HEADER_H - TAB_H;
        contentBot = PY + 8f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Background dim
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f,0f,0f,0.65f); sr.rect(0,0,sw,sh);
        sr.end();

        drawPanel();
        drawHeader(sw);
        drawTabs();
        if (activeTab == 0) drawSellTab();
        else                drawUpgradesTab();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // ── Panel chrome ──────────────────────────────────────────────────────────

    private void drawPanel() {
        float cs = 14f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.02f,0.05f,0.11f,0.97f); sr.rect(PX,PY,PANEL_W,PANEL_H);
        sr.setColor(AC_R,AC_G,AC_B,1f);
        sr.rect(PX, PY+PANEL_H-4f, PANEL_W, 4f);
        sr.rect(PX, PY,             PANEL_W, 4f);
        sr.setColor(AC_R*0.6f,AC_G*0.6f,AC_B*0.6f,1f);
        sr.rect(PX,            PY, 4f, PANEL_H);
        sr.rect(PX+PANEL_W-4f, PY, 4f, PANEL_H);
        sr.setColor(AC_R,AC_G,AC_B,1f);
        sr.rect(PX-2f,          PY+PANEL_H-cs, cs,cs);
        sr.rect(PX+PANEL_W-cs+2f, PY+PANEL_H-cs, cs,cs);
        sr.rect(PX-2f,          PY,             cs,cs);
        sr.rect(PX+PANEL_W-cs+2f, PY,           cs,cs);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(AC_R,AC_G,AC_B,0.65f); sr.rect(PX,PY,PANEL_W,PANEL_H);
        sr.end();
    }

    private void drawHeader(int sw) {
        batch.begin();
        titleFont.setColor(AC_R,AC_G,AC_B,1f);
        String t = "ДРОН-КОНСОЛЬ"; gl.setText(titleFont,t);
        titleFont.draw(batch,t, sw/2f-gl.width/2f, PY+PANEL_H-10f);

        bodyFont.setColor(WN_R,WN_G,WN_B,1f);
        String bal = String.format("$%.0f", session.getWallet().getBalance());
        gl.setText(bodyFont,bal);
        bodyFont.draw(batch,bal, PX+PANEL_W-gl.width-14f, PY+PANEL_H-14f);
        batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(AC_R,AC_G,AC_B,0.4f);
        sr.rect(PX+20f, PY+PANEL_H-HEADER_H, PANEL_W-40f, 2f);
        sr.end();
    }

    private void drawTabs() {
        float tabY = PY+PANEL_H-HEADER_H-TAB_H;
        float tabW = PANEL_W/2f;
        String[] labels = {"ПРОДАЖ","МОДУЛІ"};
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i=0;i<2;i++) {
            float tx=PX+i*tabW;
            sr.setColor(i==activeTab?AC_R*0.13f:0.03f,
                    i==activeTab?AC_G*0.13f:0.04f,
                    i==activeTab?AC_B*0.13f:0.07f,1f);
            sr.rect(tx,tabY,tabW,TAB_H);
        }
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        for (int i=0;i<2;i++) {
            float tx=PX+i*tabW;
            sr.setColor(i==activeTab?AC_R:AC_R*0.3f, i==activeTab?AC_G:AC_G*0.3f,
                    i==activeTab?AC_B:AC_B*0.3f, i==activeTab?1f:0.5f);
            sr.rect(tx,tabY,tabW,TAB_H);
        }
        sr.end();
        batch.begin();
        for (int i=0;i<2;i++) {
            float tx=PX+i*tabW;
            smallFont.setColor(i==activeTab?AC_R:0.45f, i==activeTab?AC_G:0.55f,
                    i==activeTab?AC_B:0.60f,1f);
            gl.setText(smallFont,labels[i]);
            smallFont.draw(batch,labels[i], tx+tabW/2f-gl.width/2f, tabY+(TAB_H+gl.height)/2f);
        }
        batch.end();
    }

    // ── Sell tab ──────────────────────────────────────────────────────────────

    private void drawSellTab() {
        float cx = PX+PANEL_W/2f;
        if (droneState == DS.IDLE) {
            float slotX = cx-SLOT_SZ/2f, slotY = PY+PANEL_H*0.43f;

            batch.begin();
            bodyFont.setColor(0.55f,0.75f,0.80f,1f);
            String hint="Кинь кристали сюди:"; gl.setText(bodyFont,hint);
            bodyFont.draw(batch,hint, cx-gl.width/2f, slotY+SLOT_SZ+26f);
            batch.end();

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0.04f,0.08f,0.14f,1f); sr.rect(slotX,slotY,SLOT_SZ,SLOT_SZ);
            if (crystals>0) { sr.setColor(0.25f,0.65f,0.90f,0.85f); sr.rect(slotX+10f,slotY+10f,SLOT_SZ-20f,SLOT_SZ-20f); }
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(crystals>0?AC_R:0.25f, crystals>0?AC_G:0.35f, crystals>0?AC_B:0.40f, 1f);
            sr.rect(slotX,slotY,SLOT_SZ,SLOT_SZ);
            sr.end();

            batch.begin();
            if (crystals>0) {
                bodyFont.setColor(1f,1f,1f,1f);
                bodyFont.draw(batch,"×"+crystals, slotX+SLOT_SZ-24f, slotY+20f);
                bodyFont.setColor(WN_R,WN_G,WN_B,1f);
                String val="Вартість: $"+(crystals*CRYSTAL_PRICE); gl.setText(bodyFont,val);
                bodyFont.draw(batch,val, cx-gl.width/2f, slotY-12f);
            } else {
                hintFont.setColor(0.30f,0.40f,0.45f,1f);
                String e="Порожньо"; gl.setText(hintFont,e);
                hintFont.draw(batch,e, cx-gl.width/2f, slotY-8f);
            }
            batch.end();

            // sell button
            boolean can=crystals>0;
            float bw=130f,bh=38f, bx=cx-bw/2f, by=PY+PANEL_H*0.16f;
            sellBtn.set(bx,by,bw,bh);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(can?0.05f:0.04f, can?0.22f:0.06f, can?0.27f:0.08f, 0.92f);
            sr.rect(bx,by,bw,bh);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(can?AC_R:0.25f, can?AC_G:0.30f, can?AC_B:0.35f, 0.85f);
            sr.rect(bx,by,bw,bh);
            sr.end();
            batch.begin();
            bodyFont.setColor(can?AC_R:0.30f, can?AC_G:0.35f, can?AC_B:0.40f, 1f);
            String bl="ПРОДАТИ"; gl.setText(bodyFont,bl);
            bodyFont.draw(batch,bl, bx+(bw-gl.width)/2f, by+(bh+gl.height)/2f);
            batch.end();

        } else {
            float midY=PY+PANEL_H*0.55f, cx2=PX+PANEL_W/2f;
            batch.begin();
            bodyFont.setColor(WN_R,WN_G,WN_B,1f);
            String st=droneState==DS.FLYING_AWAY?"Дрон доставляє кристали...":"Дрон повертається...";
            gl.setText(bodyFont,st); bodyFont.draw(batch,st, cx2-gl.width/2f, midY+20f);
            batch.end();

            float bw=PANEL_W*0.72f,bh=10f, bx=cx2-bw/2f, by=midY-10f;
            float prog=animTimer/flightDur(); if(droneState==DS.RETURNING) prog=1f-prog;
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0.05f,0.08f,0.13f,1f); sr.rect(bx,by,bw,bh);
            sr.setColor(WN_R,WN_G,WN_B,1f); sr.rect(bx,by,bw*prog,bh);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(WN_R*0.5f,WN_G*0.5f,WN_B*0.5f,1f); sr.rect(bx,by,bw,bh);
            sr.end();
            float dx=bx+bw*prog,dy=by+bh/2f,ds=10f;
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(WN_R,WN_G,WN_B,1f);
            sr.triangle(dx,dy+ds, dx-ds*0.6f,dy-ds*0.5f, dx+ds*0.6f,dy-ds*0.5f);
            sr.end();
        }
    }

    // ── Upgrades tab ──────────────────────────────────────────────────────────

    private void drawUpgradesTab() {
        // No glScissor — manually skip rows outside the viewport
        float curY = contentTop - 4f + scrollY;

        curY = drawSectionLabel(curY, "── МОДУЛІ ДЕРЕВА ──", GR_R,GR_G,GR_B);
        for (Upg u : tree) {
            drawRow(curY, u);
            curY -= ROW_H + ROW_GAP;
        }
        curY -= 10f;
        curY = drawSectionLabel(curY, "── ПОЛІПШЕННЯ БАЗИ ──", AC_R,AC_G,AC_B);
        for (Upg u : base) {
            drawRow(curY, u);
            curY -= ROW_H + ROW_GAP;
        }

        drawScrollbar();
    }

    private float drawSectionLabel(float y, String text, float r, float g, float b) {
        float rowTop  = y;
        float rowBot  = y - 20f;
        if (rowTop > contentBot && rowBot < contentTop) {
            batch.begin();
            smallFont.setColor(r,g,b,1f);
            smallFont.draw(batch, text, PX+14f, Math.min(y, contentTop-2f));
            batch.end();
        }
        return y - 22f;
    }

    private void drawRow(float rowTop, Upg u) {
        float ry = rowTop - ROW_H;
        // skip if fully outside viewport
        if (rowTop < contentBot || ry > contentTop) return;

        boolean can = !u.maxed() && session.getWallet().getBalance() >= u.cost;
        float rx = PX+8f, rw = PANEL_W-20f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        if (u.maxed()) sr.setColor(0.03f,0.10f,0.05f,0.88f);
        else if (can)  sr.setColor(0.04f,0.08f,0.14f,0.88f);
        else           sr.setColor(0.07f,0.04f,0.04f,0.85f);
        sr.rect(rx, ry, rw, ROW_H);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        if (u.maxed())  sr.setColor(GR_R*0.55f,GR_G*0.55f,GR_B*0.55f,0.6f);
        else if (can)   sr.setColor(AC_R*0.45f,AC_G*0.45f,AC_B*0.45f,0.5f);
        else            sr.setColor(0.35f,0.15f,0.10f,0.45f);
        sr.rect(rx, ry, rw, ROW_H);
        sr.end();

        float tx = rx+10f;
        batch.begin();
        bodyFont.getData().setScale(0.95f);
        if (u.maxed()) bodyFont.setColor(GR_R*0.8f+0.2f,GR_G*0.8f+0.2f,GR_B*0.8f+0.2f,1f);
        else           bodyFont.setColor(can?0.90f:0.50f, can?0.90f:0.40f, can?0.95f:0.50f,1f);
        bodyFont.draw(batch, u.name, tx, ry+ROW_H-8f);
        bodyFont.getData().setScale(1f);

        hintFont.getData().setScale(0.9f);
        hintFont.setColor(0.40f,0.55f,0.60f,1f);
        hintFont.draw(batch, u.desc, tx+3f, ry+ROW_H-24f);
        if (!u.maxed()) {
            hintFont.setColor(can?WN_R:0.55f, can?WN_G*0.7f:0.25f, 0.10f, 1f);
            hintFont.draw(batch, "$"+(int)u.cost, tx+3f, ry+12f);
        }
        hintFont.getData().setScale(1f);
        batch.end();

        // level dots
        if (u.max>1) {
            float dotR=4f, dcY=ry+ROW_H/2f-2f;
            float dotX0 = rx+rw-86f-(u.max*(dotR*2f+4f));
            sr.begin(ShapeRenderer.ShapeType.Filled);
            for (int d=0;d<u.max;d++) {
                float dcx=dotX0+d*(dotR*2f+4f)+dotR;
                sr.setColor(d<u.lv?GR_R:0.15f, d<u.lv?GR_G:0.20f, d<u.lv?GR_B:0.25f,1f);
                for (int s=0;s<10;s++) {
                    double a1=2*Math.PI*s/10, a2=2*Math.PI*(s+1)/10;
                    sr.triangle(dcx,dcY,
                            dcx+dotR*(float)Math.cos(a1),dcY+dotR*(float)Math.sin(a1),
                            dcx+dotR*(float)Math.cos(a2),dcY+dotR*(float)Math.sin(a2));
                }
            }
            sr.end();
        }

        // buy button
        float bw=82f,bh=28f, bx=rx+rw-bw-6f, by=ry+(ROW_H-bh)/2f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        if (u.maxed())  sr.setColor(0.05f,0.14f,0.07f,0.90f);
        else if (can)   sr.setColor(0.05f,0.20f,0.26f,0.92f);
        else            sr.setColor(0.12f,0.07f,0.06f,0.90f);
        sr.rect(bx,by,bw,bh);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        if (u.maxed())  sr.setColor(GR_R,GR_G,GR_B,0.70f);
        else if (can)   sr.setColor(AC_R,AC_G,AC_B,0.80f);
        else            sr.setColor(0.45f,0.20f,0.15f,0.55f);
        sr.rect(bx,by,bw,bh);
        sr.end();
        batch.begin();
        String lbl=u.maxed()?"ПРИДБАНО":"КУПИТИ";
        smallFont.setColor(u.maxed()?GR_R:can?AC_R:0.40f, u.maxed()?GR_G:can?AC_G:0.28f,
                u.maxed()?GR_B:can?AC_B:0.22f,1f);
        gl.setText(smallFont,lbl);
        float sx=Math.min(1f,(bw-6f)/gl.width);
        smallFont.getData().setScale(sx,1f); gl.setText(smallFont,lbl);
        smallFont.draw(batch,lbl, bx+(bw-gl.width)/2f, by+(bh+gl.height)/2f);
        smallFont.getData().setScale(1f,1f);
        batch.end();
    }

    private void drawScrollbar() {
        if (maxScroll <= 0) return;
        float sbX=PX+PANEL_W-10f, sbH=contentTop-contentBot, sbY=contentBot;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.05f,0.08f,0.12f,1f); sr.rect(sbX,sbY,6f,sbH);
        float th=Math.max(16f,(sbH/(maxScroll+sbH))*sbH);
        float ty=sbY+sbH-th-(scrollY/maxScroll)*(sbH-th);
        sr.setColor(AC_R*0.5f,AC_G*0.5f,AC_B*0.5f,1f); sr.rect(sbX,ty,6f,th);
        sr.end();
    }

    // ── Touch ─────────────────────────────────────────────────────────────────

    public boolean handleTouchDown(float screenX, float screenY) {
        if (!visible) return false;
        float wy = graphics.getHeight()-screenY;
        if (screenX<PX||screenX>PX+PANEL_W||wy<PY||wy>PY+PANEL_H) return false;

        float tabY=PY+PANEL_H-HEADER_H-TAB_H;
        if (wy>=tabY && wy<=tabY+TAB_H) {
            activeTab = (screenX<PX+PANEL_W/2f)?0:1; return true;
        }
        if (activeTab==0 && droneState==DS.IDLE) {
            if (sellBtn.contains(screenX,wy) && crystals>0) { sell(); return true; }
        } else if (activeTab==1) {
            clickUpgrades(screenX,wy);
        }
        return true;
    }

    private void clickUpgrades(float wx, float wy) {
        float curY = contentTop - 4f + scrollY - 22f; // after section label
        for (Upg u : tree) {
            if (hitBuy(wx,wy,curY,u)) { buy(u); return; }
            curY -= ROW_H+ROW_GAP;
        }
        curY -= 32f; // section gap+label
        for (Upg u : base) {
            if (hitBuy(wx,wy,curY,u)) { buy(u); return; }
            curY -= ROW_H+ROW_GAP;
        }
    }

    private boolean hitBuy(float wx, float wy, float rowTop, Upg u) {
        float rx=PX+8f, rw=PANEL_W-20f, ry=rowTop-ROW_H;
        float bw=82f,bh=28f, bx=rx+rw-bw-6f, by=ry+(ROW_H-bh)/2f;
        return wx>=bx&&wx<=bx+bw&&wy>=by&&wy<=by+bh;
    }

    private void buy(Upg u) {
        if (u.maxed()||session.getWallet().getBalance()<u.cost) return;
        session.getWallet().spend(u.cost);
        u.lv++;
        if ("base_inv".equals(u.id)) session.getInventory().expandInventory();
        if ("base_garden".equals(u.id)) {
            if (session.getBaseZone().addGardenBed()) {
                List<TileCoord> beds=session.getBaseZone().getGardenBeds();
                session.getBaseZoneRenderer().refreshGardenBedTile(beds.get(beds.size()-1));
            }
        }
    }

    private void sell() {
        pendingBal = crystals * CRYSTAL_PRICE;
        crystals = 0;
        droneState = DS.FLYING_AWAY;
        animTimer = 0;
    }

    public void dispose() {
        sr.dispose(); batch.dispose();
        titleFont.dispose(); bodyFont.dispose(); smallFont.dispose(); hintFont.dispose();
    }
}