package mfe.filters;
import arc.func.Cons;
import arc.math.geom.Vec2;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
public class TranslateTransFilter extends MI2UGenerateFilter {
   public float offX = 0.0F;
   public float offY = 0.0F;
   public FilterOption[] options() {
      return new FilterOption[]{new FilterOptions.SliderFieldOption("offsetX", () -> this.offX, (f) -> this.offX = f, -100.0F, 100.0F, 1.0F), new FilterOptions.SliderFieldOption("offsetY", () -> this.offY, (f) -> this.offY = f, -100.0F, 100.0F, 1.0F)};
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
         transeq.addLast((Cons<Vec2>)(vec2) -> vec2.add(this.offX, this.offY));
      }
   }
}
