package mt;
import arc.func.Cons;
import arc.scene.style.Drawable;
import arc.scene.ui.TextButton;
import mindustry.ui.Styles;
public class MTStyles {
   public static Drawable titleBarbgNormal;
   public static Drawable titleBarbgSnapped;
   public static Drawable white;
   public static Drawable gray2;
   public static float buttonSize = 32.0F;
   public static TextButton.TextButtonStyle textb;
   public static TextButton.TextButtonStyle textbtoggle;
   public static Cons<TextButton> funcSetTextb;
   public static void load() {
   }
   static {
      textb = Styles.flatt;
      textbtoggle = Styles.flatTogglet;
      funcSetTextb = (c) -> {
         c.getLabel().setAlignment(1);
         c.getLabel().setWrap(false);
         c.margin(6.0F);
      };
   }
}
