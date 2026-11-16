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
public class MapScreenshotUtility {
   private static boolean capturing;
   public static void capture() {
      if (!capturing && Vars.world != null && Vars.world.width() > 0 && Vars.world.height() > 0) {
         capturing = true;
         Core.app.post(() -> {
            FrameBuffer buffer = null;
            Pixmap pixmap = null;
            Pixmap flipped = null;
            float oldX = Core.camera.position.x;
            float oldY = Core.camera.position.y;
            float oldW = Core.camera.width;
            float oldH = Core.camera.height;
            boolean wasHudShown = Vars.ui != null && Vars.ui.hudfrag != null && Vars.ui.hudfrag.shown;
            try {
               int width = Vars.world.width() * 8;
               int height = Vars.world.height() * 8;
               if (Vars.ui != null && Vars.ui.hudfrag != null) {
                  Vars.ui.hudfrag.shown = false;
               }
               buffer = new FrameBuffer(width, height);
               buffer.begin();
               Core.graphics.clear(0.0F, 0.0F, 0.0F, 1.0F);
               if (Vars.state.isEditor() && Vars.editor != null && Vars.editor.renderer != null) {
                  Draw.proj().setOrtho(0.0F, 0.0F, (float)width, (float)height);
                  Draw.reset();
                  Vars.editor.renderer.draw(0.0F, 0.0F, (float)width, (float)height);
                  Draw.flush();
               } else if (Vars.renderer != null) {
                  Core.camera.position.set((float)width / 2.0F, (float)height / 2.0F);
                  Core.camera.width = (float)width;
                  Core.camera.height = (float)height;
                  Core.camera.update();
                  Draw.proj(Core.camera);
                  Vars.renderer.draw();
                  Draw.flush();
               }
               ByteBuffer pixels = Buffers.newByteBuffer(width * height * 4);
               Core.gl.glReadPixels(0, 0, width, height, 6408, 5121, pixels);
               pixels.position(0);
               pixmap = new Pixmap(pixels, width, height);
               flipped = pixmap.flipY();
               buffer.end();
               Fi folder = Core.settings.getDataDirectory().child("maps-screenshots");
               folder.mkdirs();
               String stamp = (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date());
               Fi file = folder.child("map_" + stamp + ".png");
               for(int suffix = 1; file.exists(); file = folder.child("map_" + stamp + "_" + suffix++ + ".png")) {
               }
               PixmapIO.writePng(file, flipped);
               if (Vars.ui != null) {
                  Vars.ui.showInfoToast("Map screenshot saved: " + file.name(), 3.0F);
               }
            } catch (Throwable error) {
               if (buffer != null) {
                  try {
                     buffer.end();
                  } catch (Throwable var19) {
                  }
               }
               Log.err("Failed to capture map screenshot", error);
               if (Vars.ui != null) {
                  Vars.ui.showInfoToast("Failed to save map screenshot.", 3.0F);
               }
            } finally {
               Core.camera.position.set(oldX, oldY);
               Core.camera.width = oldW;
               Core.camera.height = oldH;
               Core.camera.update();
               if (Vars.ui != null && Vars.ui.hudfrag != null) {
                  Vars.ui.hudfrag.shown = wasHudShown;
               }
               if (flipped != null) {
                  flipped.dispose();
               }
               if (pixmap != null) {
                  pixmap.dispose();
               }
               if (buffer != null) {
                  buffer.dispose();
               }
               capturing = false;
            }
         });
      }
   }
}
