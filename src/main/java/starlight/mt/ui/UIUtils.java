package mt.ui;
import arc.func.Cons;
import arc.scene.Element;
import arc.scene.ui.Tooltip;
import arc.scene.ui.Tooltip.Tooltips;
import arc.scene.ui.layout.Table;
import mindustry.ui.Styles;
public class UIUtils {
   public static void tooltip(Element e, Cons<Table> builder) {
      Tooltip tool = new Tooltip((tooltip) -> tooltip.table(builder).margin(2.0F), Tooltips.getInstance());
      tool.container.setBackground(Styles.black3);
      tool.allowMobile = true;
      e.addListener(tool);
   }
   public static void tooltip(Element e, String text) {
      tooltip(e, (Cons)((t) -> t.add(text)));
   }
}
