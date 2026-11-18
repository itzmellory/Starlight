package mfe.filters;
import arc.func.Cons;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import arc.util.noise.Simplex;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
public class MI2UGenerateFilter extends GenerateFilter {
   public static Queue<Cons<Vec2>> transeq = new Queue();
   @Nullable
   public static MI2UGenerateFilter transConsumer = null;
   public static Seq<Rect> regionseq = new Seq();
   @Nullable
   public static MI2UGenerateFilter regionConsumer = null;
   public FilterOption[] options() {
      return new FilterOption[0];
   }
   public char icon() {
      return '\uf7e3';
   }
   public void preConsume(GenerateFilter.GenerateInput in) {
      if (in.x == 0 && in.y == 0) {
         if (transConsumer == null && !transeq.isEmpty()) {
            transConsumer = this;
         }
         if (regionConsumer == null && !regionseq.isEmpty()) {
            regionConsumer = this;
         }
      }
   }
   public float noise(GenerateFilter.GenerateInput in, float sclX, float sclY, float offX, float offY, float rotate, float mag, float octaves, float persistence) {
      Vec2 vec = Tmp.v1;
      vec.set((float)in.x, (float)in.y).rotate(rotate);
      return Simplex.noise2d(this.seed, (double)octaves, (double)persistence, (double)1.0F, (double)((vec.x + offX) / sclX), (double)((vec.y + offY) / sclY)) * mag;
   }
   public float noise(float x, float y, float mag, float octaves, float persistence) {
      return Simplex.noise2d(this.seed, (double)octaves, (double)persistence, (double)1.0F, (double)x, (double)y) * mag;
   }
}
