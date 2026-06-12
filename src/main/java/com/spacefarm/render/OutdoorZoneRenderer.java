package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.spacefarm.world.OutdoorZone;
import com.spacefarm.world.OutdoorConstants;
import com.spacefarm.world.ScavengingLocation;
import com.spacefarm.world.TileCoord;

import java.util.*;

public class OutdoorZoneRenderer {
    private OutdoorZone outdoorZone;
    private TiledMapTileLayer baseLayer;
    private TiledMapTileLayer borderLayer;
    private TiledMap map;
    private Texture borderTileTexture;
    private Texture[] locationTextures;
    private Texture[] droneTextures;
    private Texture wheelTexture;
    private Texture greenTileTexture;
    private SpriteBatch batch;
    private Set<Long> greenedTiles = new HashSet<>();

    public OutdoorZoneRenderer(OutdoorZone outdoorZone, TiledMapTileLayer baseLayer, int tileSize) {
        this(outdoorZone,baseLayer,null,tileSize);
    }

    public OutdoorZoneRenderer(OutdoorZone outdoorZone, TiledMapTileLayer baseLayer, TiledMap map, int tileSize) {
        this.outdoorZone = outdoorZone;
        this.baseLayer = baseLayer;
        this.map = map;
        this.batch = new SpriteBatch();
        createTextures(tileSize);
        if(map != null) {
            borderLayer = new TiledMapTileLayer(baseLayer.getWidth(),baseLayer.getHeight(),
                    baseLayer.getTileWidth(),baseLayer.getTileHeight());
            map.getLayers().add(borderLayer);
            applyBorderTiles();
        }
        applyOutdoorZoneTiles();
    }

    private void createTextures(int tileSize) {
        int borderColor = OutdoorConstants.BORDER_COLOR;
        int r = (borderColor >> 16) & 0xFF;
        int g = (borderColor >> 8) & 0xFF;
        int b = borderColor & 0xFF;
        borderTileTexture = createSolidTexture(tileSize,tileSize,r,g,b,255);
        int locationCount = outdoorZone.getLocations().size();
        locationTextures = new Texture[locationCount];
        droneTextures = new Texture[locationCount];
        int locWidth = OutdoorConstants.OUTDOOR_LOCATION_WIDTH * tileSize;
        int locHeight = OutdoorConstants.OUTDOOR_LOCATION_HEIGHT * tileSize;
        for(int i = 0;i < locationCount;i++) {
            int color = outdoorZone.getLocations().get(i).getColor();
            int lr = (color >> 16) & 0xFF;
            int lg = (color >> 8) & 0xFF;
            int lb = color & 0xFF;
            locationTextures[i] = createSolidTexture(tileSize,tileSize,lr,lg,lb,255);
            droneTextures[i] = createCrystalDroneTexture(locWidth,locHeight);
        }

        // Load wheel texture
        try {
            wheelTexture = new Texture(Gdx.files.internal("sprite/object-map/wheel.png"));
        } catch (Exception e) {
            Gdx.app.error("OutdoorZoneRenderer", "Could not load wheel.png: " + e.getMessage());
            wheelTexture = createSolidTexture(tileSize * 2, tileSize * 2, 139, 115, 85, 255); // Fallback brown
        }

        // Green overlay for tree-phase unlocked locations
        // Same green as BaseZoneRenderer grass tiles
        greenTileTexture = createSolidTexture(tileSize, tileSize, 34, 139, 34, 255);
    }

    private void applyBorderTiles() {
        if(borderLayer == null) return;
        int borderX = outdoorZone.getBorderX();
        int borderY = outdoorZone.getBorderY();
        int borderWidth = outdoorZone.getBorderWidth();
        int borderHeight = outdoorZone.getBorderHeight();
        for(int x = borderX;x < borderX + borderWidth;x++) {
            for(int y = borderY;y < borderY + borderHeight;y++) {
                if(x >= 0 && x < borderLayer.getWidth() && y >= 0 && y < borderLayer.getHeight()) {
                    if(outdoorZone.isInBorder(x,y)) {
                        StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(borderTileTexture));
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tile);
                        borderLayer.setCell(x,y,cell);
                    }
                }
            }
        }
    }

    public void applyGreenTiles(int locationIndex) {
        if (locationIndex < 0 || locationIndex >= outdoorZone.getLocations().size()) return;
        ScavengingLocation loc = outdoorZone.getLocations().get(locationIndex);
        
        // Find all border tiles
        List<TileCoord> borderTiles = new ArrayList<>();
        for (int x = outdoorZone.getBorderX(); x < outdoorZone.getBorderX() + outdoorZone.getBorderWidth(); x++) {
            for (int y = outdoorZone.getBorderY(); y < outdoorZone.getBorderY() + outdoorZone.getBorderHeight(); y++) {
                if (outdoorZone.isInBorder(x, y)) {
                    borderTiles.add(new TileCoord(x, y));
                }
            }
        }
        
        int totalBorderTiles = borderTiles.size();
        int targetToGreen = totalBorderTiles / 5;
        if (locationIndex == 4) targetToGreen = totalBorderTiles - greenedTiles.size(); // Green everything on last phase
        
        // BFS from location center
        Queue<TileCoord> queue = new LinkedList<>();
        TileCoord center = new TileCoord(loc.getTopLeft().x() + loc.getWidth()/2, loc.getTopLeft().y() + loc.getHeight()/2);
        queue.add(center);
        
        int greenedThisTime = 0;
        Set<Long> visited = new HashSet<>();
        
        while (!queue.isEmpty() && greenedThisTime < targetToGreen) {
            TileCoord curr = queue.poll();
            long key = (long)curr.y() * baseLayer.getWidth() + curr.x();
            if (visited.contains(key)) continue;
            visited.add(key);
            
            if (outdoorZone.isInBorder(curr.x(), curr.y()) && !greenedTiles.contains(key)) {
                // Apply green tile to baseLayer (not borderLayer, as per original logic's implication)
                StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(greenTileTexture));
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                baseLayer.setCell(curr.x(), curr.y(), cell);
                
                greenedTiles.add(key);
                greenedThisTime++;
            }
            
            // Add neighbors
            int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
            for (int[] d : dirs) {
                int nx = curr.x() + d[0];
                int ny = curr.y() + d[1];
                if (nx >= 0 && nx < baseLayer.getWidth() && ny >= 0 && ny < baseLayer.getHeight()) {
                    queue.add(new TileCoord(nx, ny));
                }
            }
        }
    }

    private Texture createCrystalDroneTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width,height,Pixmap.Format.RGBA8888);
        pixmap.setColor(0,0,0,0);
        pixmap.fill();
        pixmap.setColor(100/255f,180/255f,255/255f,1f);
        int crystalSize = Math.min(width,height)/4;
        pixmap.fillRectangle((width-crystalSize)/2,(height-crystalSize)/2,crystalSize,crystalSize);
        pixmap.setColor(200/255f,220/255f,255/255f,0.8f);
        pixmap.fillRectangle((width-crystalSize/2)/2,(height-crystalSize/2)/2,crystalSize/2,crystalSize/2);
        pixmap.setColor(150/255f,200/255f,255/255f,0.6f);
        int indicatorSize = crystalSize/6;
        pixmap.fillCircle(width/2-crystalSize,height/2,indicatorSize);
        pixmap.fillCircle(width/2+crystalSize,height/2,indicatorSize);
        pixmap.fillCircle(width/2,height/2-crystalSize,indicatorSize);
        pixmap.fillCircle(width/2,height/2+crystalSize,indicatorSize);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void applyOutdoorZoneTiles() {
        for(ScavengingLocation location : outdoorZone.getLocations()) {
            int startX = location.getTopLeft().x();
            int startY = location.getTopLeft().y();
            int width = location.getWidth();
            int height = location.getHeight();
            int colorIndex = outdoorZone.getLocations().indexOf(location);
            if (colorIndex < 0 || colorIndex >= locationTextures.length) {
                continue;
            }
            Texture locationTexture = locationTextures[colorIndex];
            for(int x = startX;x < startX + width;x++) {
                for(int y = startY;y < startY + height;y++) {
                    if(x >= 0 && x < baseLayer.getWidth() && y >= 0 && y < baseLayer.getHeight()) {
                        StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(locationTexture));
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tile);
                        baseLayer.setCell(x,y,cell);
                    }
                }
            }
        }
    }

    private Texture createSolidTexture(int width, int height, int r, int g, int b, int a) {
        Pixmap pixmap = new Pixmap(width,height,Pixmap.Format.RGBA8888);
        pixmap.setColor(r/255f,g/255f,b/255f,a/255f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void render(OrthographicCamera camera, long scavengeDuration) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int tileSize = baseLayer.getTileWidth();
        int locationCount = Math.min(outdoorZone.getLocations().size(), droneTextures.length);
        for(int i = 0;i < locationCount;i++) {
            ScavengingLocation location = outdoorZone.getLocations().get(i);
            float locStartX = location.getTopLeft().x() * tileSize;
            float locStartY = location.getTopLeft().y() * tileSize;
            float locWidth = location.getWidth() * tileSize;
            float locHeight = location.getHeight() * tileSize;

            if (location.getLocationType() == ScavengingLocation.LocationType.SEED_WHEEL) {
                batch.setColor(1, 1, 1, 1f);
                // Scale wheel sprite to cover approx 6 tiles in size (adjusting by tileSize)
                float targetSize = tileSize * 6f;
                float spriteW = wheelTexture.getWidth();
                float spriteH = wheelTexture.getHeight();
                float aspectRatio = spriteH / spriteW;

                float drawW = targetSize;
                float drawH = targetSize * aspectRatio;

                float drawX = locStartX + (locWidth - drawW) / 2f;
                float drawY = locStartY + (locHeight - drawH) / 2f;
                batch.draw(wheelTexture, drawX, drawY, drawW, drawH);
            } else {
                batch.setColor(1, 1, 1, 0.8f);
                batch.draw(droneTextures[i], locStartX, locStartY, locWidth, locHeight);
            }

            if(location.isScavenging()) {
                renderProgressBar(location,locStartX,locStartY,locWidth,locHeight, scavengeDuration);
            }
            if(location.isInCooldown()) {
                renderCooldownIndicator(location,locStartX,locStartY,locWidth,locHeight);
            }
        }
        batch.end();
    }

    private void renderProgressBar(ScavengingLocation location, float x, float y, float width, float height, long scavengeDuration) {
        float progress = location.getScavengingProgress(scavengeDuration);
        float barHeight = 5;
        Pixmap barPixmap = new Pixmap((int)width,(int)barHeight,Pixmap.Format.RGBA8888);
        barPixmap.setColor(1f,0f,0f,0.5f);
        barPixmap.fill();
        Texture barBg = new Texture(barPixmap);
        barPixmap.dispose();
        batch.draw(barBg,x,y+height-barHeight,width,barHeight);
        Pixmap progPixmap = new Pixmap((int)(width*progress/100f),(int)barHeight,Pixmap.Format.RGBA8888);
        progPixmap.setColor(0f,1f,0f,0.8f);
        progPixmap.fill();
        Texture barProgress = new Texture(progPixmap);
        progPixmap.dispose();
        batch.draw(barProgress,x,y+height-barHeight,width*progress/100f,barHeight);
        barBg.dispose();
        barProgress.dispose();
    }

    private void renderCooldownIndicator(ScavengingLocation location, float x, float y, float width, float height) {
        float cooldownPercent = (float)location.getRemainingCooldownTime()/(float)OutdoorConstants.SCAVENGING_COOLDOWN_MILLIS;
        cooldownPercent = Math.min(1.0f,Math.max(0.0f,cooldownPercent));
        float barHeight = 3;
        Pixmap cooldownPixmap = new Pixmap((int)(width*cooldownPercent),(int)barHeight,Pixmap.Format.RGBA8888);
        cooldownPixmap.setColor(1f,0.7f,0f,0.6f);
        cooldownPixmap.fill();
        Texture cooldownBar = new Texture(cooldownPixmap);
        cooldownPixmap.dispose();
        batch.draw(cooldownBar,x,y+height-barHeight-8,width*cooldownPercent,barHeight);
        cooldownBar.dispose();
    }

    public void dispose() {
        if(borderTileTexture != null) borderTileTexture.dispose();
        if(locationTextures != null) for(Texture t : locationTextures) if(t != null) t.dispose();
        if(droneTextures != null) for(Texture t : droneTextures) if(t != null) t.dispose();
        if(wheelTexture != null) wheelTexture.dispose();
        if(greenTileTexture != null) greenTileTexture.dispose();
        if(batch != null) batch.dispose();
    }
}
