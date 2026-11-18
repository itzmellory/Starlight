package mfe.filters;
import arc.func.Cons;
import arc.math.geom.Vec2;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
public class RotateTransFilter extends MI2UGenerateFilter {
   public float rotate = 0.0F;
   public FilterOption[] options() {
      return new FilterOption[]{new FilterOptions.SliderFieldOption("rotation", () -> this.rotate, (f) -> this.rotate = f, 0.0F, 360.0F, 1.0F)};
   }
   public char icon() {
      return '\ue813';
   }
   public void apply(GenerateFilter.GenerateInput in) {
      if (in.x == 0 && in.y == 0) {
         if (transConsumer != null) {
            transeq.clear();
            transConsumer = null;
         }
         transeq.addLast((Cons<Vec2>)(vec2) -> vec2.rotate(this.rotate));
      }
   }
}
