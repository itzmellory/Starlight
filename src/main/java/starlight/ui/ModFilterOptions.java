package starlight.ui;
import arc.Core;
import arc.func.Boolc;
import arc.func.Boolf;
import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Floatc;
import arc.func.Floatp;
import arc.func.Prov;
import arc.input.KeyCode;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.maps.filters.FilterOption;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OverlayFloor;
public final class ModFilterOptions {
   private ModFilterOptions() {}
   public static final Boolf<Block> floorsOnly = b -> b instanceof Floor && !(b instanceof OverlayFloor) && !Vars.headless && Core.atlas.isFound(b.fullIcon);
   public static final Boolf<Block> floorsOptional = b -> b == Blocks.air || b instanceof Floor && !(b instanceof OverlayFloor) && !Vars.headless && Core.atlas.isFound(b.fullIcon);
   public static final Boolf<Block> wallsOnly = b -> !b.synthetic() && !(b instanceof Floor) && !Vars.headless && Core.atlas.isFound(b.fullIcon) && b.inEditor;
   public static final Boolf<Block> oresOnly = b -> b instanceof OverlayFloor && !Vars.headless && Core.atlas.isFound(b.fullIcon);
   public static class SliderOption extends FilterOption {
      public final String name;
      public final Floatp getter;
      public final Floatc setter;
      public final float min, max, step;
      public SliderOption(String name, Floatp getter, Floatc setter, float min, float max, float step) { this.name=name; this.getter=getter; this.setter=setter; this.min=min; this.max=max; this.step=step; }
      public void build(Table table) {
         Table label = new Table().marginLeft(11f).marginRight(11f);
         label.touchable = Touchable.disabled;
         label.add("@filter.option." + name).growX().wrap().style(Styles.outlineLabel);
         label.label(() -> Strings.autoFixed(getter.get(), 2)).style(Styles.outlineLabel).right().labelAlign(Align.right).padLeft(6f);
         Slider slider = new Slider(min, max, step, false);
         slider.moved(setter);
         slider.setValue(getter.get());
         if (Vars.updateEditorOnChange) slider.changed(changed); else slider.released(changed);
         table.stack(slider, label).colspan(2).pad(3f).growX().row();
      }
   }
   public static class ToggleOption extends FilterOption {
      public final String name;
      public final Boolp getter;
      public final Boolc setter;
      public ToggleOption(String name, Boolp getter, Boolc setter) { this.name=name; this.getter=getter; this.setter=setter; }
      public void build(Table table) {
         table.row();
         CheckBox check = table.check("@filter.option." + name, setter).growX().padBottom(5f).padTop(5f).center().get();
         check.setChecked(getter.get());
         check.changed(changed);
      }
   }
   public static class BlockOption extends FilterOption {
      public final String name;
      public final Prov<Block> supplier;
      public final Cons<Block> consumer;
      public final Boolf<Block> filter;
      public BlockOption(String name, Prov<Block> supplier, Cons<Block> consumer, Boolf<Block> filter) { this.name=name; this.supplier=supplier; this.consumer=consumer; this.filter=filter; }
      public void build(Table table) {
         Button button = table.button(buttonTable -> buttonTable.image(supplier.get().uiIcon).update(image -> ((TextureRegionDrawable)image.getDrawable()).setRegion(supplier.get()==Blocks.air ? Icon.none.getRegion() : supplier.get().uiIcon)).size(Vars.iconSmall), () -> {
            BaseDialog dialog = new BaseDialog("@filter.option." + name);
            dialog.cont.pane(blockTable -> {
               int index=0;
               for(Block block : Vars.content.blocks()) {
                  if(!filter.get(block)) continue;
                  blockTable.image(block==Blocks.air ? Icon.none.getRegion() : block.uiIcon).size(Vars.iconMed).pad(3f).tooltip(block==Blocks.air ? "@none" : block.localizedName).get().clicked(() -> { consumer.get(block); dialog.hide(); changed.run(); });
                  if(++index%10==0) blockTable.row();
               }
               dialog.setFillParent(index>100);
            }).scrollX(false);
            dialog.addCloseButton(); dialog.show();
         }).pad(4f).margin(12f).get();
         button.clicked(KeyCode.mouseMiddle, () -> { Core.app.setClipboardText(supplier.get().name); Vars.ui.showInfoFade("@copied"); });
         button.clicked(KeyCode.mouseRight, () -> { Block clipboardBlock=Vars.content.block(Core.app.getClipboardText()); if(clipboardBlock!=null && filter.get(clipboardBlock)){ consumer.get(clipboardBlock); changed.run(); } });
         table.add("@filter.option." + name).left().growX().style(Styles.outlineLabel).padLeft(6f).row();
      }
   }
}
