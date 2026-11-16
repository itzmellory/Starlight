package starlight.generation;
import starlight.ui.ModFilterOptions;
import arc.Core;
import java.lang.reflect.Method;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.world.Tile;
public class AdvancedRotationFilter extends GenerateFilter {
   public boolean rotate4Way = true;
   public boolean flipDirection = false;
   private static transient Method tileMethod;
   public String name() {
      return Core.bundle.get("filter.advancedrotation.name", "Advanced Rotation");
   }
   public char icon() {
      return '\ue86a';
   }
   public FilterOption[] options() {
      return new FilterOption[]{new ModFilterOptions.ToggleOption("rotate4way", () -> this.rotate4Way, (v) -> this.rotate4Way = v), new ModFilterOptions.ToggleOption("flipdirection", () -> this.flipDirection, (v) -> this.flipDirection = v)};
   }
   public boolean isPost() {
      return true;
   }
   public boolean isBuffered() {
      return true;
   }
   public void apply(GenerateFilter.GenerateInput in) {
      int w = in.width;
      int h = in.height;
      int x = in.x;
      int y = in.y;
      int hx = w / 2;
      int hy = h / 2;
      if (this.rotate4Way) {
         boolean top = y >= hy;
         boolean right = x >= hx;
         if (!this.flipDirection) {
            if (top && !right) {
               return;
            }
            int steps = 0;
            int sx;
            int sy;
            if (top && right) {
               sx = w - 1 - y;
               sy = x;
               steps = 3;
            } else if (!top && right) {
               sx = w - 1 - x;
               sy = h - 1 - y;
               steps = 2;
            } else {
               sx = y;
               sy = h - 1 - x;
               steps = 1;
            }
            this.copyRotated(in, sx, sy, steps);
         } else {
            if (!top && !right) {
               return;
            }
            int steps = 0;
            int var15;
            int var16;
            if (top && !right) {
               var15 = h - 1 - y;
               var16 = x;
               steps = 1;
            } else if (top && right) {
               var15 = w - 1 - x;
               var16 = h - 1 - y;
               steps = 2;
            } else {
               var15 = y;
               var16 = w - 1 - x;
               steps = 3;
            }
            this.copyRotated(in, var15, var16, steps);
         }
      } else {
         if (y > hy || y == hy && x < hx) {
            return;
         }
         int sx = w - 1 - x;
         int sy = h - 1 - y;
         this.copyRotated(in, sx, sy, 2);
      }
   }
   private void copyRotated(GenerateFilter.GenerateInput in, int sx, int sy, int steps) {
      sx = Math.max(0, Math.min(in.width - 1, sx));
      sy = Math.max(0, Math.min(in.height - 1, sy));
      Tile src = readTile(in, sx, sy);
      if (src != null) {
         in.floor = src.floor();
         if (!in.block.synthetic() && !src.block().synthetic()) {
            in.block = src.block();
         }
         in.overlay = src.overlay();
         Tile dst = readTile(in, in.x, in.y);
         if (dst != null && src.build != null && dst.build != null) {
            dst.build.rotation = src.build.rotation + steps & 3;
         }
      }
   }
   private static Tile readTile(GenerateFilter.GenerateInput in, int x, int y) {
      try {
         if (tileMethod != null) {
            return (Tile)tileMethod.invoke(in, (float)x, (float)y);
         }
      } catch (Exception var4) {
      }
      return null;
   }
   static {
      try {
         tileMethod = GenerateFilter.GenerateInput.class.getDeclaredMethod("tile", Float.TYPE, Float.TYPE);
         tileMethod.setAccessible(true);
      } catch (Exception var1) {
      }
   }
}
