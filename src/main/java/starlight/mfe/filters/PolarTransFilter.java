package mfe.filters;
import arc.func.Cons;
import arc.math.geom.Vec2;
import mindustry.maps.filters.GenerateFilter;
public class PolarTransFilter extends MI2UGenerateFilter {
   public static Vec2 tmp = new Vec2();
   public char icon() {
      return '\ue813';
   }
   public void apply(GenerateFilter.GenerateInput in) {
      if (in.x == 0 && in.y == 0) {
         if (transConsumer != null) {
            transeq.clear();
            transConsumer = null;
         }
         transeq.addLast((Cons)(vec2) -> {
            tmp.set(vec2);
            vec2.trns(tmp.x, tmp.y);
         });
      }
   }
}
