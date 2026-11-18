package mfe.filters;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;
import arc.util.Scaling;
import mindustry.graphics.Pal;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
public class AppliedRegionFilter extends MI2UGenerateFilter {
   private transient int worldWidth = 1;
   private transient int worldHeight = 1;
   public int fromX = 0;
   public int fromY = 0;
   public int width = 32;
   public int height = 32;
   public FilterOption[] options() {
      return new FilterOption[]{new FilterOptions.SliderFieldOption("fromX", () -> (float)this.fromX, (f) -> this.fromX = (int)f, 0.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("fromY", () -> (float)this.fromY, (f) -> this.fromY = (int)f, 0.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("width", () -> (float)this.width, (f) -> this.width = (int)f, 1.0F, 500.0F, 1.0F), new FilterOptions.SliderFieldOption("height", () -> (float)this.height, (f) -> this.height = (int)f, 1.0F, 500.0F, 1.0F)};
   }
   public char icon() {
      return '\ue815';
   }
   public void draw(Image image) {
      super.draw(image);
      Vec2 vsize = Scaling.fit.apply(image.getDrawable().getMinWidth(), image.getDrawable().getMinHeight(), image.getWidth(), image.getHeight());
      Lines.stroke(Scl.scl(2.0F), Pal.remove);
      Lines.rect((float)this.fromX / (float)this.worldWidth * vsize.x + image.x + (image.getWidth() - vsize.x) / 2.0F, (float)this.fromY / (float)this.worldHeight * vsize.y + image.y + (image.getHeight() - vsize.y) / 2.0F, (float)this.width / (float)this.worldWidth * vsize.x, (float)this.height / (float)this.worldHeight * vsize.y);
      Draw.reset();
   }
   public void apply(GenerateFilter.GenerateInput in) {
      if (in.x == 0 && in.y == 0) {
         this.worldWidth = in.width;
         this.worldHeight = in.height;
         if (regionConsumer != null) {
            regionseq.clear();
            regionConsumer = null;
         }
         regionseq.add(new Rect((float)this.fromX, (float)this.fromY, (float)this.width, (float)this.height));
      }
   }
}
