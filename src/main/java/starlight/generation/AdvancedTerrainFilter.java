package starlight.generation;
import starlight.ui.ModFilterOptions;
import arc.Core;
import arc.math.Mathf;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.world.Block;
public class AdvancedTerrainFilter extends GenerateFilter {
   public Block floor;
   public Block wall;
   public float scale;
   public float octaves;
   public float wallDensity;
   public float tilt;
   public boolean fillEdgeVoids;
   public float edgeThickness;
   public AdvancedTerrainFilter() {
      this.floor = Blocks.stone;
      this.wall = Blocks.stoneWall;
      this.scale = 80.0F;
      this.octaves = 4.0F;
      this.wallDensity = 0.5F;
      this.tilt = 0.0F;
      this.fillEdgeVoids = false;
      this.edgeThickness = 22.0F;
   }
   public String name() {
      return Core.bundle.get("filter.advancedterrain.name", "Advanced Terrain");
   }
   public char icon() {
      return '\uf7d7';
   }
   public FilterOption[] options() {
      return new FilterOption[]{new ModFilterOptions.BlockOption("floor", () -> this.floor, (b) -> this.floor = b, ModFilterOptions.floorsOnly), new ModFilterOptions.BlockOption("wall", () -> this.wall, (b) -> this.wall = b, ModFilterOptions.wallsOnly), new ModFilterOptions.SliderOption("scale", () -> this.scale, (f) -> this.scale = f, 10.0F, 400.0F, 1.0F), new ModFilterOptions.SliderOption("octaves", () -> this.octaves, (f) -> this.octaves = f, 1.0F, 8.0F, 1.0F), new ModFilterOptions.SliderOption("tilt", () -> this.tilt, (f) -> this.tilt = f, 0.0F, 360.0F, 1.0F), new ModFilterOptions.SliderOption("wallDensity", () -> this.wallDensity, (f) -> this.wallDensity = f, 0.0F, 1.0F, 0.05F), new ModFilterOptions.ToggleOption("fillEdgeVoids", () -> this.fillEdgeVoids, (b) -> this.fillEdgeVoids = b), new ModFilterOptions.SliderOption("edgeThickness", () -> this.edgeThickness, (f) -> this.edgeThickness = f, 5.0F, 80.0F, 1.0F)};
   }
   public void apply(GenerateFilter.GenerateInput in) {
      float rad = this.tilt * ((float)Math.PI / 180F);
      float cos = Mathf.cos(rad);
      float sin = Mathf.sin(rad);
      float nx = (float)in.x * cos - (float)in.y * sin;
      float ny = (float)in.x * sin + (float)in.y * cos;
      float noise = Simplex.noise2d(this.seed, (double)((int)this.octaves), (double)0.5F, (double)(1.0F / this.scale), (double)nx, (double)ny);
      if (this.fillEdgeVoids) {
         float dLeft = (float)in.x;
         float dRight = (float)(in.width - 1 - in.x);
         float dBottom = (float)in.y;
         float dTop = (float)(in.height - 1 - in.y);
         float minEdge = Math.min(Math.min(dLeft, dRight), Math.min(dBottom, dTop));
         float jag = Simplex.noise2d(this.seed + 99, (double)3.0F, (double)0.6F, (double)(1.0F / (this.scale * 0.45F)), (double)in.x, (double)in.y) * this.edgeThickness * 0.65F;
         float effectiveDist = minEdge + jag;
         if (effectiveDist < this.edgeThickness) {
            float boost = Mathf.clamp(1.0F - effectiveDist / this.edgeThickness) * 1.35F;
            noise += boost;
         }
      }
      in.floor = this.floor;
      in.block = noise > 1.0F - this.wallDensity ? this.wall : Blocks.air;
   }
}
