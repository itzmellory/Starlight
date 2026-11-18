package mfe.filters;
import arc.Core;
import arc.func.Boolc;
import arc.func.Boolf;
import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Floatc;
import arc.func.Floatp;
import arc.func.Prov;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.TextField;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mfe.MapFilterExt;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.maps.filters.FilterOption;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OverlayFloor;
public class FilterOptions {
   public static final Boolf<Block> oresOptional = (b) -> b == Blocks.air || b instanceof OverlayFloor && !Vars.headless && Core.atlas.isFound(b.fullIcon);
   public static class SliderOption extends FilterOption {
      final String name;
      final Floatp getter;
      final Floatc setter;
      final float min;
      final float max;
      final float step;
      boolean display;
      SliderOption(String name, Floatp getter, Floatc setter, float min, float max) {
         this(name, getter, setter, min, max, (max - min) / 200.0F);
      }
      SliderOption(String name, Floatp getter, Floatc setter, float min, float max, float step) {
         this.display = true;
         this.name = name;
         this.getter = getter;
         this.setter = setter;
         this.min = min;
         this.max = max;
         this.step = step;
      }
      public SliderOption display() {
         this.display = true;
         return this;
      }
      public void build(Table table) {
         Element base;
         if (!this.display) {
            Label l = new Label("@filter.option." + this.name);
            l.setWrap(true);
            l.setStyle(Styles.outlineLabel);
            base = l;
         } else {
            Table t = (new Table()).marginLeft(11.0F).marginRight(11.0F);
            base = t;
            t.add("@filter.option." + this.name).growX().wrap().style(Styles.outlineLabel);
            t.label(() -> Strings.autoFixed(this.getter.get(), 2)).style(Styles.outlineLabel).right().labelAlign(16).padLeft(6.0F);
         }
         base.touchable = Touchable.disabled;
         Slider slider = new Slider(this.min, this.max, this.step, false);
         slider.moved(this.setter);
         slider.setValue(this.getter.get());
         if (Vars.updateEditorOnChange) {
            slider.changed(this.changed);
         } else {
            slider.released(this.changed);
         }
         table.stack(new Element[]{slider, base}).colspan(2).pad(3.0F).growX();
      }
   }
   public static class BlockOption extends FilterOption {
      final String name;
      final Prov<Block> supplier;
      final Cons<Block> consumer;
      final Boolf<Block> filter;
      BlockOption(String name, Prov<Block> supplier, Cons<Block> consumer, Boolf<Block> filter) {
         this.name = name;
         this.supplier = supplier;
         this.consumer = consumer;
         this.filter = filter;
      }
      public void build(Table table) {
         table.button((b) -> b.image(((Block)this.supplier.get()).uiIcon).update((i) -> ((TextureRegionDrawable)i.getDrawable()).setRegion(this.supplier.get() == Blocks.air ? Icon.none.getRegion() : ((Block)this.supplier.get()).uiIcon)).size(24.0F), () -> {
            BaseDialog dialog = new BaseDialog("");
            dialog.setFillParent(false);
            int i = 0;
            for(Block block : Vars.content.blocks()) {
               if (this.filter.get(block)) {
                  ((Image)dialog.cont.image(block == Blocks.air ? Icon.none.getRegion() : block.uiIcon).size(32.0F).pad(3.0F).get()).clicked(() -> {
                     this.consumer.get(block);
                     dialog.hide();
                     this.changed.run();
                  });
                  ++i;
                  if (i % 10 == 0) {
                     dialog.cont.row();
                  }
               }
            }
            dialog.closeOnBack();
            dialog.show();
         }).pad(4.0F).margin(12.0F);
         table.add("@filter.option." + this.name);
      }
   }
   public static class ToggleOption extends FilterOption {
      final String name;
      final Boolp getter;
      final Boolc setter;
      ToggleOption(String name, Boolp getter, Boolc setter) {
         this.name = name;
         this.getter = getter;
         this.setter = setter;
      }
      public void build(Table table) {
         table.row();
         CheckBox check = (CheckBox)table.check("@filter.option." + this.name, this.setter).growX().padBottom(5.0F).padTop(5.0F).center().get();
         check.changed(this.changed);
      }
   }
   public static class FieldOption extends FilterOption {
      final String name;
      final Prov<String> getter;
      final Cons<String> setter;
      TextField.TextFieldFilter textF;
      TextField.TextFieldValidator textV;
      FieldOption(String name, Prov<String> getter, Cons<String> setter, TextField.TextFieldFilter textf, TextField.TextFieldValidator textv) {
         this.name = name;
         this.getter = getter;
         this.setter = (str) -> {
            setter.get(str);
            this.changed.run();
         };
         this.textF = textf;
         this.textV = textv;
      }
      public void build(Table table) {
         table.row();
         table.add("@filter.option." + this.name);
         TextField text = (TextField)table.field(String.valueOf(this.getter.get()), this.textF, this.setter).growX().padBottom(5.0F).padTop(5.0F).center().get();
         text.setValidator(this.textV);
      }
   }
   public static class SliderFieldOption extends SliderOption {
      boolean textMode = false;
      SliderFieldOption(String name, Floatp getter, Floatc setter, float min, float max, float step) {
         super(name, getter, setter, min, max, step);
      }
      public void build(Table table) {
         table.clear();
         if (!this.textMode) {
            super.build(table);
         } else {
            table.add("@filter.option." + this.name);
            TextField text = (TextField)table.field(String.valueOf(this.getter.get()), TextFieldFilter.floatsOnly, (s) -> this.setter.get(Strings.parseFloat(s))).growX().padBottom(5.0F).padTop(5.0F).center().get();
            text.setValidator((s) -> Strings.parseFloat(s) >= this.min && Strings.parseFloat(s) <= this.max);
            text.changed(this.changed);
         }
         table.button("\ue869", Styles.flatt, () -> {
            this.textMode = !this.textMode;
            this.build(table);
         }).checked(this.textMode).size(MapFilterExt.buttonSize);
      }
   }
}
