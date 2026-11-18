package mfe.guides;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
public class VanillaGridGuide extends BaseGuide {
   public VanillaGridGuide() {
      this.name = Core.bundle.get("guide.vanilla");
      this.color.set(Color.gray);
   }
   public void draw() {
      float iw = getIW();
      float ih = getIH();
      float xspace = getW() / iw;
      float yspace = getH() / ih;
      float s = 1.0F;
      int minspace = 10;
      int jumpx = (int)(Math.max((float)minspace, xspace) / xspace);
      int jumpy = (int)(Math.max((float)minspace, yspace) / yspace);
      Draw.color(this.color);
      for(int x = 0; (float)x <= iw; x += jumpx) {
         Fill.crect((float)((int)(iposx() + xspace * (float)x - s)), iposy() - s, 2.0F, getH() + (float)((float)x == ih ? 1 : 0));
      }
      for(int y = 0; (float)y <= ih; y += jumpy) {
         Fill.crect(iposx() - s, (float)((int)(iposy() + (float)y * yspace - s)), getW(), 2.0F);
      }
   }
}
