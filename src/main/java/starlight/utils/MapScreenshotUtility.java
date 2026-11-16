package starlight.utils;
import arc.Core;
import arc.files.Fi;
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
      if (!capturing && Vars.world != null && Vars.world.width() > 0 && Vars.world.height() > 0) {
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
               drawCompleteMap();
               Draw.flush();
               ByteBuffer pixels = Buffers.newByteBuffer(width * height * 4);
               Core.gl.glReadPixels(0, 0, width, height, 6408, 5121, pixels);
               pixels.position(0);
               pixmap = new Pixmap(pixels, width, height);
               flipped = pixmap.flipY();
               buffer.end();
               Fi folder = Core.settings.getDataDirectory().child("maps-screenshots");
               folder.mkdirs();
               String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
               Fi file = folder.child("map_" + stamp + ".png");
               for (int suffix = 1; file.exists(); file = folder.child("map_" + stamp + "_" + suffix++ + ".png")) {}
               PixmapIO.writePng(file, flipped);
               if (Vars.ui != null) Vars.ui.showInfoToast("Map screenshot saved: " + file.name(), 3f);
            } catch (Throwable error) {
               if (buffer != null) {
                  try {
                     buffer.end();
                  } catch (Throwable ignored) {}
               }
               Log.err("Failed to capture map screenshot", error);
               if (Vars.ui != null) Vars.ui.showInfoToast("Failed to save map screenshot.", 3f);
            } finally {
               Draw.color();
               Draw.reset();
               Draw.proj(Core.camera);
               Core.camera.update();
               if (Vars.ui != null && Vars.ui.hudfrag != null) Vars.ui.hudfrag.shown = wasHudShown;
               if (flipped != null) flipped.dispose();
               if (pixmap != null) pixmap.dispose();
               if (buffer != null) buffer.dispose();
               capturing = false;
            }
         });
      }
   }
   private static void drawCompleteMap() {
      int width = Vars.world.width();
      int height = Vars.world.height();
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            Tile tile = Vars.world.tile(x, y);
            if (tile != null) tile.floor().drawBase(tile);
         }
      }
      Draw.flush();
      Draw.color();
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            Tile tile = Vars.world.tile(x, y);
            if (tile != null) tile.overlay().drawBase(tile);
         }
      }
      Draw.flush();
      Draw.color();
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            Tile tile = Vars.world.tile(x, y);
            if (tile != null) tile.block().drawBase(tile);
         }
      }
      Draw.flush();
      Draw.color();
      Draw.reset();
   }
}
