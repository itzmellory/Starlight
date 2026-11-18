package mfe.filters;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.world.Block;
public class GridFilter extends MI2UGenerateFilter {
   public float width = 40.0F;
   public float height = 40.0F;
   public float stroke = 3.0F;
   public float offX = 0.0F;
   public float offY = 0.0F;
   public Block floor;
   public Block block;
   public Block target;
   public Block target2;
   public GridFilter() {
      this.floor = Blocks.air;
      this.block = Blocks.air;
      this.target = Blocks.air;
      this.target2 = Blocks.air;
   }
   public FilterOption[] options() {
      return new FilterOption[]{new FilterOptions.SliderFieldOption("width", () -> this.width, (f) -> this.width = f, 1.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("height", () -> this.height, (f) -> this.height = f, 1.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("offsetX", () -> this.offX, (f) -> this.offX = f, -200.0F, 200.0F, 1.0F), new FilterOptions.SliderFieldOption("offsetY", () -> this.offY, (f) -> this.offY = f, -200.0F, 200.0F, 1.0F), new FilterOptions.SliderFieldOption("stroke", () -> this.stroke, (f) -> this.stroke = f, 1.0F, 100.0F, 1.0F), new FilterOptions.BlockOption("targetFloor", () -> this.target, (b) -> this.target = b, FilterOption.floorsOptional), new FilterOptions.BlockOption("floor", () -> this.floor, (b) -> this.floor = b, FilterOption.floorsOptional), new FilterOptions.BlockOption("targetWall", () -> this.target2, (b) -> this.target2 = b, FilterOption.wallsOptional), new FilterOptions.BlockOption("wall", () -> this.block, (b) -> this.block = b, FilterOption.wallsOptional)};
   }
   public void apply(GenerateFilter.GenerateInput in) {
      this.preConsume(in);
      if (regionConsumer != this || regionseq.count((r) -> r.contains((float)in.x, (float)in.y)) > 0) {
         Vec2 v = Tmp.v3.set((float)in.x, (float)in.y);
         if (transConsumer == this) {
            transeq.each((c) -> c.get(v));
         }
         if (Mathf.mod(v.x + this.offX, this.width + this.stroke) < this.stroke || Mathf.mod(v.y + this.offY, this.height + this.stroke) < this.stroke) {
            if (this.floor != Blocks.air && (this.target == Blocks.air || in.floor == this.target)) {
               in.floor = this.floor;
            }
            if (this.block != Blocks.air && in.block == this.target2) {
               in.block = this.block;
            }
         }
      }
   }
}
