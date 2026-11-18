package mfe.filters;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.world.Block;
public class AdvancedNoiseFilter extends MI2UGenerateFilter {
   public float threshold = 0.5F;
   public float octaves = 3.0F;
   public float falloff = 0.5F;
   public Block floor;
   public Block block;
   public Block target;
   public Block target2;
   public AdvancedNoiseFilter() {
      this.floor = Blocks.air;
      this.block = Blocks.air;
      this.target = Blocks.air;
      this.target2 = Blocks.air;
   }
   public FilterOption[] options() {
      return new FilterOption[]{new FilterOptions.SliderFieldOption("threshold", () -> this.threshold, (f) -> this.threshold = f, 0.0F, 1.0F, 0.01F), new FilterOptions.SliderFieldOption("octaves", () -> this.octaves, (f) -> this.octaves = f, 1.0F, 10.0F, 0.1F), new FilterOptions.SliderFieldOption("falloff", () -> this.falloff, (f) -> this.falloff = f, 0.0F, 1.0F, 0.01F), new FilterOptions.BlockOption("targetFloor", () -> this.target, (b) -> this.target = b, FilterOption.floorsOptional), new FilterOptions.BlockOption("floor", () -> this.floor, (b) -> this.floor = b, FilterOption.floorsOptional), new FilterOptions.BlockOption("targetWall", () -> this.target2, (b) -> this.target2 = b, FilterOption.wallsOptional), new FilterOptions.BlockOption("wall", () -> this.block, (b) -> this.block = b, FilterOption.wallsOptional)};
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
         if (noise > this.threshold) {
            if (this.floor != Blocks.air && in.floor == this.target) {
               in.floor = this.floor;
            }
            if (this.block != Blocks.air && in.block == this.target2) {
               in.block = this.block;
            }
         }
      }
   }
}
