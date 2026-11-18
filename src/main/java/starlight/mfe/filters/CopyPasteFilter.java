package mfe.filters;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Vec2;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;
import arc.util.Reflect;
import arc.util.Scaling;
import mindustry.content.Blocks;
import mindustry.graphics.Pal;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.world.Block;
import mindustry.world.Tile;
public class CopyPasteFilter extends MI2UGenerateFilter {
   private static TileBuffer[][] buffers;
   private transient int worldWidth = 1;
   private transient int worldHeight = 1;
   public float fromX = 0.0F;
   public float fromY = 0.0F;
   public float width = 32.0F;
   public float height = 32.0F;
   public float toX = 64.0F;
   public float toY = 64.0F;
   public boolean copyBlock = false;
   public boolean copyOverlay = false;
   public FilterOption[] options() {
      return new FilterOption[]{new FilterOptions.SliderFieldOption("fromX", () -> this.fromX, (f) -> this.fromX = f, 0.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("fromY", () -> this.fromY, (f) -> this.fromY = f, 0.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("width", () -> this.width, (f) -> this.width = f, 1.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("height", () -> this.height, (f) -> this.height = f, 1.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("toX", () -> this.toX, (f) -> this.toX = f, 0.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("toY", () -> this.toY, (f) -> this.toY = f, 0.0F, 500.0F, 1.0F), new FilterOptions.ToggleOption("targetWall", () -> this.copyBlock, (f) -> this.copyBlock = f), new FilterOptions.ToggleOption("targetOre", () -> this.copyOverlay, (f) -> this.copyOverlay = f)};
   }
   public void apply(GenerateFilter.GenerateInput in) {
      if (in.x == 0 && in.y == 0) {
         this.copyToBuffers(in);
      }
      if (buffers != null) {
         this.preConsume(in);
         if (regionConsumer != this || regionseq.count((r) -> r.contains((float)in.x, (float)in.y)) > 0) {
            int bufx = (int)((float)in.x - this.toX);
            int bufy = (int)((float)in.y - this.toY);
            if (bufx >= 0 && bufx < buffers.length && bufy >= 0 && bufy < buffers[0].length) {
               TileBuffer buffer = buffers[bufx][bufy];
               if (buffer != null) {
                  in.floor = buffer.floor;
                  if (this.copyBlock) {
                     in.block = buffer.wall;
                  }
                  if (this.copyOverlay) {
                     in.overlay = buffer.overlay;
                  }
               }
            }
         }
      }
   }
   public void draw(Image image) {
      super.draw(image);
      Vec2 vsize = Scaling.fit.apply(image.getDrawable().getMinWidth(), image.getDrawable().getMinHeight(), image.getWidth(), image.getHeight());
      Lines.stroke(Scl.scl(2.0F), Pal.accent);
      Lines.rect(this.fromX / (float)this.worldWidth * vsize.x + image.x + (image.getWidth() - vsize.x) / 2.0F, this.fromY / (float)this.worldHeight * vsize.y + image.y + (image.getHeight() - vsize.y) / 2.0F, this.width / (float)this.worldWidth * vsize.x, this.height / (float)this.worldHeight * vsize.y);
      Lines.stroke(Scl.scl(2.0F), Pal.items);
      Lines.rect(this.toX / (float)this.worldWidth * vsize.x + image.x + (image.getWidth() - vsize.x) / 2.0F, this.toY / (float)this.worldHeight * vsize.y + image.y + (image.getHeight() - vsize.y) / 2.0F, this.width / (float)this.worldWidth * vsize.x, this.height / (float)this.worldHeight * vsize.y);
      Draw.reset();
   }
   public void copyToBuffers(GenerateFilter.GenerateInput in) {
      buffers = new TileBuffer[(int)this.width][(int)this.height];
      this.worldWidth = in.width;
      this.worldHeight = in.height;
      GenerateFilter.GenerateInput.TileProvider provider = (GenerateFilter.GenerateInput.TileProvider)Reflect.get(in, "buffer");
      for(int x = 0; (float)x < this.width; ++x) {
         for(int y = 0; (float)y < this.height; ++y) {
            if (!((float)x + this.fromX > (float)(in.width - 1)) && !((float)y + this.fromY > (float)(in.height - 1))) {
               Tile tile = provider.get(x + (int)this.fromX, y + (int)this.fromY);
               buffers[x][y] = new TileBuffer(tile);
            }
         }
      }
   }
   public static class TileBuffer {
      public Block wall;
      public Block floor;
      public Block overlay;
      public TileBuffer(Tile tile) {
         this.floor = tile.floor();
         this.overlay = tile.overlay();
         this.wall = !tile.block().hasBuilding() ? tile.block() : Blocks.air;
      }
   }
}
