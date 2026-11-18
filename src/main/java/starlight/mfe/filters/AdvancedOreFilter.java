package mfe.filters;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.world.Block;
public class AdvancedOreFilter extends MI2UGenerateFilter {
   public float threshold = 0.5F;
   public float octaves = 3.0F;
   public float falloff = 0.5F;
   public Block ore;
   public Block targetFloor;
   public Block targetOre;
   public AdvancedOreFilter() {
      this.ore = Blocks.oreCopper;
      this.targetFloor = Blocks.air;
      this.targetOre = Blocks.air;
   }
   public FilterOption[] options() {
      return new FilterOption[]{new FilterOptions.SliderFieldOption("threshold", () -> this.threshold, (f) -> this.threshold = f, 0.0F, 1.0F, 0.01F), new FilterOptions.SliderFieldOption("octaves", () -> this.octaves, (f) -> this.octaves = f, 1.0F, 10.0F, 0.1F), new FilterOptions.SliderFieldOption("falloff", () -> this.falloff, (f) -> this.falloff = f, 0.0F, 1.0F, 0.01F), new FilterOptions.BlockOption("ore", () -> this.ore, (b) -> this.ore = b, FilterOption.oresOnly), new FilterOptions.BlockOption("targetFloor", () -> this.targetFloor, (b) -> this.targetFloor = b, FilterOption.floorsOptional), new FilterOptions.BlockOption("targetOre", () -> this.targetOre, (b) -> this.targetOre = b, FilterOptions.oresOptional)};
   }
   public char icon() {
      return '\uf7e3';
   }
   public void apply(GenerateFilter.GenerateInput in) {
      this.preConsume(in);
      if (regionConsumer != this || regionseq.count((r) -> r.contains((float)in.x, (float)in.y)) > 0) {
         Vec2 v = Tmp.v3.set((float)in.x, (float)in.y);
         if (transConsumer == this) {
            transeq.each((c) -> c.get(v));
         }
         float noise = this.noise(v.x, v.y, 1.0F, this.octaves, this.falloff);
         if (noise > this.threshold && in.overlay != Blocks.spawn && in.floor.asFloor().hasSurface() && (this.targetFloor == Blocks.air || in.floor == this.targetFloor) && (this.targetOre == Blocks.air || in.overlay == this.targetOre)) {
            in.overlay = this.ore;
         }
      }
   }
}
