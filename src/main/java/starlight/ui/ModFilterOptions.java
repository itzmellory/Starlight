package starlight.ui;
import arc.Core;
import arc.func.Boolc;
import arc.func.Boolf;
import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Floatc;
import arc.func.Floatp;
import arc.func.Prov;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.maps.filters.FilterOption;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OverlayFloor;
public class ModFilterOptions {
   public static final Boolf<Block> floorsOnly = (b) -> b instanceof Floor && !(b instanceof OverlayFloor) && !headless() && Core.atlas.isFound(b.fullIcon);
   public static final Boolf<Block> floorsOptional = (b) -> b == Blocks.air || b instanceof Floor && !(b instanceof OverlayFloor) && !headless() && Core.atlas.isFound(b.fullIcon);
   public static final Boolf<Block> wallsOnly = (b) -> !b.synthetic() && !(b instanceof Floor) && !headless() && Core.atlas.isFound(b.fullIcon) && b.inEditor;
   public static final Boolf<Block> oresOnly = (b) -> b instanceof OverlayFloor && !headless() && Core.atlas.isFound(b.fullIcon);
   private static boolean headless() {
      return Vars.headless;
   }
   public static class SliderOption extends FilterOption {
      public final String name;
      public final Floatp getter;
      public final Floatc setter;
      public final float min;
      public final float max;
      public final float step;
      public SliderOption(String n, Floatp g, Floatc s, float mn, float mx, float st) {
         this.name = n;
         this.getter = g;
         this.setter = s;
         this.min = mn;
         this.max = mx;
         this.step = st;
      }
      public void build(Table table) {
         table.add("@filter.option." + this.name).growX().left().style(Styles.outlineLabel).padBottom(3.0F);
         Label val = (Label)table.label(() -> Strings.autoFixed(this.getter.get(), 2)).right().style(Styles.outlineLabel).get();
         table.row();
         table.slider(this.min, this.max, this.step, this.getter.get(), (f) -> {
            this.setter.get(f);
            val.setText(Strings.autoFixed(f, 2));
            this.changed.run();
         }).colspan(2).growX().height(34.0F).padBottom(6.0F).row();
      }
   }
   public static class ToggleOption extends FilterOption {
      public final String name;
      public final Boolp getter;
      public final Boolc setter;
      public ToggleOption(String n, Boolp g, Boolc s) {
         this.name = n;
         this.getter = g;
         this.setter = s;
      }
      public void build(Table table) {
         table.check("@filter.option." + this.name, this.getter.get(), (b) -> {
            this.setter.get(b);
            this.changed.run();
         }).colspan(2).growX().left().padTop(6.0F).padBottom(6.0F).row();
      }
   }
   public static class BlockOption extends FilterOption {
      public final String name;
      public final Prov<Block> supplier;
      public final Cons<Block> consumer;
      public final Boolf<Block> filter;
      public BlockOption(String n, Prov<Block> s, Cons<Block> c, Boolf<Block> f) {
         this.name = n;
         this.supplier = s;
         this.consumer = c;
         this.filter = f;
      }
      public void build(Table table) {
         table.button((b) -> b.image(((Block)this.supplier.get()).uiIcon).update((i) -> i.setDrawable(((Block)this.supplier.get()).uiIcon)).size(24.0F), () -> {
            BaseDialog dialog = new BaseDialog("@filter.option." + this.name);
            dialog.cont.pane((t) -> {
               int i = 0;
               for(Block block : Vars.content.blocks()) {
                  if (this.filter.get(block)) {
                     ((Image)t.image(block.uiIcon).size(32.0F).pad(3.0F).tooltip(block == Blocks.air ? "Any Floor" : block.localizedName).get()).clicked(() -> {
                        this.consumer.get(block);
                        dialog.hide();
                        this.changed.run();
                     });
                     ++i;
                     if (i % 10 == 0) {
                        t.row();
                     }
                  }
               }
            });
            dialog.addCloseButton();
            dialog.show();
         }).pad(4.0F).margin(8.0F);
         table.add("@filter.option." + this.name).left().growX().style(Styles.outlineLabel).padLeft(6.0F).row();
      }
   }
}
