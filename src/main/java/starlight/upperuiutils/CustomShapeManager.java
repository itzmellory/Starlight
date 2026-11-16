package starlight.upperuiutils;
import arc.func.Cons;
import arc.func.Intc2;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.ui.layout.Scl;
import arc.struct.IntSet;
import mindustry.Vars;
import mindustry.editor.MapView;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
public class CustomShapeManager {
   public static BrushShape currentShape;
   public static boolean line45Mode;
   private static final IntSet maskCache;
   private static final Vec2 tmpA;
   private static final Vec2 tmpB;
   public static boolean inside(int dx, int dy, float radius, BrushShape shape) {
      if (radius <= 1.0F) {
         return dx == 0 && dy == 0;
      } else {
         switch (shape) {
            case square:
               return (float)Math.abs(dx) <= radius - 0.5F && (float)Math.abs(dy) <= radius - 0.5F;
            case diamond:
               return (float)(Math.abs(dx) + Math.abs(dy)) <= radius;
            case cross:
               float arm = Math.max(0.0F, (radius - 1.0F) / 3.0F);
               return (float)Math.abs(dx) <= arm && (float)Math.abs(dy) <= radius || (float)Math.abs(dy) <= arm && (float)Math.abs(dx) <= radius;
            default:
               return Mathf.within((float)dx, (float)dy, radius - 0.5F + 1.0E-4F);
         }
      }
   }
   public static void iterateBrush(int cx, int cy, float radius, BrushShape shape, Cons<Tile> cons) {
      int clamped = Math.max(1, (int)Math.ceil((double)radius));
      int w = Vars.editor.width();
      int h = Vars.editor.height();
      for(int rx = -clamped; rx <= clamped; ++rx) {
         for(int ry = -clamped; ry <= clamped; ++ry) {
            if (inside(rx, ry, radius, shape)) {
               int wx = cx + rx;
               int wy = cy + ry;
               if (wx >= 0 && wy >= 0 && wx < w && wy < h) {
                  Tile t = Vars.editor.tile(wx, wy);
                  if (t != null) {
                     cons.get(t);
                  }
               }
            }
         }
      }
   }
   public static Point2 snap45(int x1, int y1, int x2, int y2) {
      int dx = x2 - x1;
      int dy = y2 - y1;
      int adx = Math.abs(dx);
      int ady = Math.abs(dy);
      if ((float)adx > (float)ady * 2.414F) {
         y2 = y1;
      } else if ((float)ady > (float)adx * 2.414F) {
         x2 = x1;
      } else {
         int d = Math.max(adx, ady);
         x2 = x1 + d * (dx >= 0 ? 1 : -1);
         y2 = y1 + d * (dy >= 0 ? 1 : -1);
      }
      return new Point2(x2, y2);
   }
   public static void traceLine(int x1, int y1, int x2, int y2, Intc2 cons) {
      int dx = Math.abs(x2 - x1);
      int sx = x1 < x2 ? 1 : -1;
      int dy = -Math.abs(y2 - y1);
      int sy = y1 < y2 ? 1 : -1;
      int err = dx + dy;
      int x = x1;
      int y = y1;
      while(true) {
         cons.get(x, y);
         if (x == x2 && y == y2) {
            return;
         }
         int e2 = 2 * err;
         if (e2 >= dy) {
            err += dy;
            x += sx;
         }
         if (e2 <= dx) {
            err += dx;
            y += sy;
         }
      }
   }
   public static void drawSteppedOutline(MapView view, int cx, int cy, float radius, BrushShape shape) {
      drawSteppedOutline(view, LandCopyPast.getOverlayElement(), cx, cy, radius, shape);
   }
   public static void drawSteppedOutline(MapView view, Element overlay, int cx, int cy, float radius, BrushShape shape) {
      maskCache.clear();
      int clamped = Math.max(1, (int)Math.ceil((double)radius));
      int w = Vars.editor.width();
      int h = Vars.editor.height();
      for(int rx = -clamped; rx <= clamped; ++rx) {
         for(int ry = -clamped; ry <= clamped; ++ry) {
            if (inside(rx, ry, radius, shape)) {
               int wx = cx + rx;
               int wy = cy + ry;
               if (wx >= 0 && wy >= 0 && wx < w && wy < h) {
                  maskCache.add(Point2.pack(wx, wy));
               }
            }
         }
      }
      drawMaskEdges(view, overlay, maskCache);
   }
   public static void drawLineSteppedOutline(MapView view, int x1, int y1, int x2, int y2, float radius, BrushShape shape) {
      drawLineSteppedOutline(view, LandCopyPast.getOverlayElement(), x1, y1, x2, y2, radius, shape);
   }
   public static void drawLineSteppedOutline(MapView view, Element overlay, int x1, int y1, int x2, int y2, float radius, BrushShape shape) {
      maskCache.clear();
      int clamped = Math.max(1, (int)Math.ceil((double)radius));
      int w = Vars.editor.width();
      int h = Vars.editor.height();
      traceLine(x1, y1, x2, y2, (lx, ly) -> {
         for(int rx = -clamped; rx <= clamped; ++rx) {
            for(int ry = -clamped; ry <= clamped; ++ry) {
               if (inside(rx, ry, radius, shape)) {
                  int wx = lx + rx;
                  int wy = ly + ry;
                  if (wx >= 0 && wy >= 0 && wx < w && wy < h) {
                     maskCache.add(Point2.pack(wx, wy));
                  }
               }
            }
         }
      });
      drawMaskEdges(view, overlay, maskCache);
   }
   public static void drawMaskEdges(MapView view, IntSet mask) {
      drawMaskEdges(view, LandCopyPast.getOverlayElement(), mask);
   }
   public static void drawMaskEdges(MapView view, Element overlay, IntSet mask) {
      try {
         Vec2 o0 = LandCopyPast.tileToOverlay(view, overlay, 0.0F, 0.0F, tmpA);
         Vec2 o1 = LandCopyPast.tileToOverlay(view, overlay, 1.0F, 1.0F, tmpB);
         float x0 = o0.x;
         float y0 = o0.y;
         float tileW = o1.x - x0;
         float tileH = o1.y - y0;
         if (Math.abs(tileW) <= 1.0E-4F || Math.abs(tileH) <= 1.0E-4F) {
            return;
         }
         Draw.color(Pal.accent);
         Lines.stroke(Scl.scl(1.8F));
         mask.each((packed) -> {
            int x = Point2.x(packed);
            int y = Point2.y(packed);
            float px = x0 + (float)x * tileW;
            float py = y0 + (float)y * tileH;
            if (!mask.contains(Point2.pack(x - 1, y))) {
               Lines.line(px, py, px, py + tileH);
            }
            if (!mask.contains(Point2.pack(x + 1, y))) {
               Lines.line(px + tileW, py, px + tileW, py + tileH);
            }
            if (!mask.contains(Point2.pack(x, y - 1))) {
               Lines.line(px, py, px + tileW, py);
            }
            if (!mask.contains(Point2.pack(x, y + 1))) {
               Lines.line(px, py + tileH, px + tileW, py + tileH);
            }
         });
         Draw.reset();
      } catch (Throwable var9) {
      }
   }
   static {
      currentShape = CustomShapeManager.BrushShape.circle;
      line45Mode = false;
      maskCache = new IntSet();
      tmpA = new Vec2();
      tmpB = new Vec2();
   }
   public static enum BrushShape {
      circle("●", "Circle"),
      square("■", "Square"),
      diamond("◆", "Diamond"),
      cross("✚", "Cross");
      public final String symbol;
      public final String title;
      private BrushShape(String s, String t) {
         this.symbol = s;
         this.title = t;
      }
      private static BrushShape[] $values() {
         return new BrushShape[]{circle, square, diamond, cross};
      }
   }
          }
