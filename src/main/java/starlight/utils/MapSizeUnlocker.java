package starlight.utils;
import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.util.Buffers;
import arc.util.Log;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import mindustry.Vars;
import mindustry.world.Tile;
public class MapScreenshotUtility {
   private static boolean capturing;
   public static void capture() {
      if (capturing || Vars.world == null || Vars.world.width() <= 0 || Vars.world.height() <= 0) return;
      capturing = true;
      Core.app.post(() -> {
         FrameBuffer buffer = null;
         Pixmap pixmap = null;
         Pixmap flipped = null;
         boolean wasHudShown = Vars.ui != null && Vars.ui.hudfrag != null && Vars.ui.hudfrag.shown;
         try {
            int width = Vars.world.width() * 8;
            int height = Vars.world.height() * 8;
            if (Vars.ui != null && Vars.ui.hudfrag != null) Vars.ui.hudfrag.shown = false;
            buffer = new FrameBuffer(width, height);
            buffer.begin();
            Core.graphics.clear(0f, 0f, 0f, 1f);
            Draw.reset();
            Draw.proj().setOrtho(0f, 0f, width, height);
            drawMapLayers();
            Draw.flush();
            ByteBuffer pixels = Buffers.newByteBuffer(width * height * 4);
            Core.gl.glReadPixels(0, 0, width, height, 6408, 5121, pixels);
            pixels.position(0);
            pixmap = new Pixmap(pixels, width, height);
            flipped = pixmap.flipY();
            buffer.end();
            addScreenshotMark(flipped);
            Fi folder = Core.settings.getDataDirectory().child("maps-screenshots");
            folder.mkdirs();
            String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            Fi file = folder.child("map_" + stamp + ".png");
            for (int suffix = 1; file.exists(); file = folder.child("map_" + stamp + "_" + suffix++ + ".png")) {}
            PixmapIO.writePng(file, flipped);
            if (Vars.ui != null) Vars.ui.showInfoToast("Map screenshot saved: " + file.name(), 3f);
         } catch (Throwable error) {
            if (buffer != null) {
               try { buffer.end(); } catch (Throwable ignored) {}
            }
            Log.err("Failed to capture map screenshot", error);
            if (Vars.ui != null) Vars.ui.showInfoToast("Failed to save map screenshot.", 3f);
         } finally {
            Draw.color();
            Draw.reset();
            if (Vars.ui != null && Vars.ui.hudfrag != null) Vars.ui.hudfrag.shown = wasHudShown;
            if (flipped != null) flipped.dispose();
            if (pixmap != null) pixmap.dispose();
            if (buffer != null) buffer.dispose();
            capturing = false;
         }
      });
   }
   private static void drawMapLayers() {
      int width = Vars.world.width();
      int height = Vars.world.height();
      Draw.reset();
      Draw.color(Color.white);
      Draw.alpha(1f);
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            Tile tile = Vars.world.tile(x, y);
            if (tile != null) tile.floor().drawBase(tile);
         }
      }
      Draw.flush();
      Draw.reset();
      Draw.color(Color.white);
      Draw.alpha(1f);
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            Tile tile = Vars.world.tile(x, y);
            if (tile != null && tile.overlay() != null) tile.overlay().drawBase(tile);
         }
      }
      Draw.flush();
      Draw.reset();
      Draw.color(Color.white);
      Draw.alpha(1f);
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            Tile tile = Vars.world.tile(x, y);
            if (tile != null && tile.block() != null) tile.block().drawBase(tile);
         }
      }
      Draw.flush();
      Draw.color();
      Draw.reset();
   }
   private static void addScreenshotMark(Pixmap image) {
      int margin = 10;
      int x = margin;
      int y = margin;
      int scale = Math.max(2, Math.min(5, image.getWidth() / 300));
      int badgeWidth = 100 * scale;
      int badgeHeight = 15 * scale;
      image.setColor(new Color(0.04f, 0.05f, 0.07f, 0.88f));
      image.fillRectangle(x, y, badgeWidth, badgeHeight);
      image.setColor(new Color(0.25f, 0.85f, 1f, 1f));
      image.fillRectangle(x, y + badgeHeight - Math.max(2, scale), badgeWidth, Math.max(2, scale));
      drawBitmapText(image, "STARLIGHT MAP", x + 5 * scale, y + 5 * scale, scale);
   }
   private static void drawBitmapText(Pixmap image, String text, int startX, int startY, int scale) {
      String[] glyphs = {\n         "010101111101101",\n         "110101110101110",\n         "011100100100011",\n         "110101101101110",\n         "111100110100111",\n         "111100110100100",\n         "011100101101011",\n         "101101111101101",\n         "111010010010111",\n         "001001001101010",\n         "101101110101101",\n         "100100100100111",\n         "101111111101101",\n         "110111111111101",\n         "010101101101010",\n         "110101110100100",\n         "010101101111011",\n         "110101110101101",\n         "011100010001110",\n         "111010010010010",\n         "101101101101111",\n         "101101101101010",\n         "101101111111101",\n         "101101010101101",\n         "101101010010010",\n         "111001010100111"\n      };\n      String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";\n      int cursor = startX;
      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == ' ') { cursor += 4 * scale; continue; }
         int index = alphabet.indexOf(c);
         if (index < 0 || index >= glyphs.length) { cursor += 4 * scale; continue; }
         String glyph = glyphs[index];
         for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 3; col++) {
               if (glyph.charAt(row * 3 + col) == '1') image.fillRectangle(cursor + col * scale, startY + (4 - row) * scale, scale, scale);
            }
         }
         cursor += 4 * scale;
      }
   }
}
