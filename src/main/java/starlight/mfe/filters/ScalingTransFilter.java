package mfe.filters;
import arc.func.Cons;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
public class ScalingTransFilter extends MI2UGenerateFilter {
   public float sclX = 1.0F;
   public float sclY = 1.0F;
   public FilterOption[] options() {
      return new FilterOption[]{new FilterOptions.SliderFieldOption("scaleX", () -> this.sclX, (f) -> this.sclX = f, 0.5F, 20.0F, 0.1F), new FilterOptions.SliderFieldOption("scaleY", () -> this.sclY, (f) -> this.sclY = f, 0.5F, 20.0F, 0.1F)};
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
         transeq.addLast((Cons)(vec2) -> vec2.scl(1.0F / this.sclX, 1.0F / this.sclY));
      }
   }
}
