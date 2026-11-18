package mt.io;
import arc.Core;
import arc.func.Boolc;
import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Func;
import arc.func.Intc;
import arc.func.Prov;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.serialization.Json;
import java.util.Objects;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mt.MTStyles;
import mt.ui.UIUtils;
import mt.utils.RefUtils;
public class SettingHandler {
   public static boolean debug;
   public String prefix;
   public Seq<Setting> list = new Seq();
   public SettingHandler(String prefix) {
      this.prefix = prefix;
   }
   public void add(Setting setting) {
      this.list.add(setting);
   }
   public String prefix(String name) {
      return this.prefix + "." + name;
   }
   public void buildList(Table p) {
      p.clearChildren();
      this.list.each((setting) -> {
         Objects.requireNonNull(setting);
         p.table(setting::rebuild).growX().margin(2.0F).row();
      });
   }
   public Setting getSetting(String name) {
      return (Setting)this.list.find((s) -> s.name.equals(this.prefix(name)));
   }
   public boolean getBool(String name) {
      return Core.settings.getBool(this.prefix(name));
   }
   public boolean getBool(String name, boolean def) {
      return Core.settings.getBool(this.prefix(name), def);
   }
   public int getInt(String name) {
      return Core.settings.getInt(this.prefix(name));
   }
   public int getInt(String name, int def) {
      return !debug ? Core.settings.getInt(this.prefix(name), def) : Strings.parseInt(Core.settings.get(this.prefix(name), def).toString());
   }
   public float getFloat(String name) {
      return !debug ? Core.settings.getFloat(this.prefix(name)) : Strings.parseFloat(Core.settings.get(this.prefix(name), "1f").toString());
   }
   public String getStr(String name) {
      return Core.settings.getString(this.prefix(name));
   }
   public Object get(String name, Object def) {
      return Core.settings.get(this.prefix(name), def);
   }
   public <T> T getJson(String name, Class<T> clazz, Prov<T> def) {
      return (T)Core.settings.getJson(this.prefix(name), clazz, def);
   }
   public void putInt(String name, int v) {
      Core.settings.put(this.prefix(name), v);
   }
   public void putString(String name, String v) {
      Core.settings.put(this.prefix(name), v);
   }
   public void putBool(String name, boolean v) {
      Core.settings.put(this.prefix(name), v);
   }
   public void putJson(String name, Object v) {
      Core.settings.putJson(this.prefix(name), v);
   }
   public static void registerJsonClass(Class<?> clazz) {
      Json json = (Json)RefUtils.getValue(Core.settings, (String)"json");
      if (json != null && json.getClass(clazz.getName()) == null) {
         json.addClassTag(clazz.getName(), clazz);
      }
   }
   public static void registerJsonClass(Class<?>... clazzs) {
      for(Class<?> clazz : clazzs) {
         registerJsonClass(clazz);
      }
   }
   public Title title(String pureName) {
      Title s = new Title(pureName);
      this.list.add(s);
      return s;
   }
   public CustomEntry entry(String name, Cons2<CustomEntry, Table> builder) {
      CustomEntry e = new CustomEntry(name);
      e.entryBuilder = builder;
      this.list.add(e);
      return e;
   }
   public CheckSetting checkPref(String name, boolean def) {
      return this.checkPref(name, def, (Boolc)null);
   }
   public CheckSetting checkPref(String name, boolean def, Boolc changed) {
      CheckSetting s = new CheckSetting(name, def, changed);
      this.list.add(s);
      return s;
   }
   public SliderSetting sliderPref(String name, int def, int min, int max, SettingsMenuDialog.StringProcessor s) {
      return this.sliderPref(name, def, min, max, 1, s);
   }
   public SliderSetting sliderPref(String name, int def, int min, int max, int step, SettingsMenuDialog.StringProcessor s) {
      return this.sliderPref(name, def, min, max, step, s, (Intc)null);
   }
   public SliderSetting sliderPref(String name, int def, int min, int max, int step, SettingsMenuDialog.StringProcessor s, Intc changed) {
      SliderSetting res;
      this.list.add(res = new SliderSetting(name, def, min, max, step, s, changed));
      Core.settings.defaults(new Object[]{this.prefix(name), def});
      return res;
   }
   public TextFieldSetting textPref(String name, String def) {
      return this.textPref(name, def, (Cons)null);
   }
   public TextFieldSetting textPref(String name, String def, Cons<String> changed) {
      return this.textPref(name, def, (TextField.TextFieldFilter)null, (TextField.TextFieldValidator)null, changed);
   }
   public TextFieldSetting textPref(String name, String def, TextField.TextFieldFilter filter, TextField.TextFieldValidator validator, Cons<String> changed) {
      return this.textPref(name, def, filter, validator, changed, (Func)null);
   }
   public TextFieldSetting textPref(String name, String def, TextField.TextFieldFilter filter, TextField.TextFieldValidator validator, Cons<String> changed, Func<String, Object> parser) {
      TextFieldSetting s = new TextFieldSetting(name, def, filter, validator, changed, parser);
      this.list.add(s);
      return s;
   }
   public abstract class Setting extends SettingsMenuDialog.SettingsTable.Setting {
      public boolean restart;
      public boolean reloadWorld;
      public boolean performance;
      public Setting() {
         super("");
      }
      public Setting(String name) {
         super("");
         this.name = SettingHandler.this.prefix(name);
         this.title = Core.bundle.get("settings." + this.name, name);
         this.description = Core.bundle.getOrNull("settings." + this.name + ".description");
      }
      public Setting tag(boolean restart, boolean reloadWorld, boolean performance) {
         this.restart = restart;
         this.reloadWorld = reloadWorld;
         this.performance = performance;
         return this;
      }
      public void add(SettingsMenuDialog.SettingsTable table) {
         table.table(this::rebuild).growX().row();
      }
      public void rebuild(Table table) {
         table.table(this::build).growX();
         table.table((t) -> {
            t.touchable = Touchable.enabled;
            t.fill(Tex.button, (i) -> i.image(Icon.info).color(this.description != null ? Color.gray : Color.clear));
            if (this.performance) {
               t.add("[negstat]PF[]");
            }
            t.row();
            if (this.restart) {
               t.add("[orange]RS[]");
            }
            if (this.reloadWorld) {
               t.add("[sky]RW[]");
            }
         }).with((tb) -> UIUtils.tooltip(tb, (Cons<Table>)((tooltip) -> tooltip.table((t) -> {
                  t.setBackground(Tex.buttonTrans);
                  t.label(() -> "[accent]" + this.title + "[]").growX().row();
                  t.label(() -> "[lightgray]" + this.name + "[]").growX().row();
                  t.labelWrap(() -> this.description).padTop(8.0F).padBottom(8.0F).minWidth(300.0F).growX().row();
                  t.table((warning) -> {
                     if (this.restart) {
                        warning.left().label(() -> "[orange]" + Core.bundle.get("settings.tags.restart") + "[]").pad(2.0F).padRight(8.0F);
                     }
                     if (this.reloadWorld) {
                        warning.left().label(() -> "[sky]" + Core.bundle.get("settings.tags.reloadWorld") + "[]").pad(2.0F).padRight(8.0F);
                     }
                     if (this.performance) {
                        warning.left().label(() -> "[negstat]" + Core.bundle.get("settings.tags.performance") + "[]").pad(2.0F).padRight(8.0F);
                     }
                  }).growX().left().bottom();
               }).growX().maxWidth(500.0F)))).size(40.0F).pad(2.0F);
      }
      public abstract void build(Table var1);
   }
   public class Title extends Setting {
      public Title(String name) {
         this.name = name;
         this.title = Core.bundle.get("settings." + this.name, name);
         this.description = Core.bundle.getOrNull("settings." + this.name + ".description");
      }
      public void build(Table table) {
         table.add(this.title).growX().color(Pal.accent).style(Styles.outlineLabel).padTop(12.0F).row();
         table.image().growX().height(2.0F).color(Pal.accent);
      }
   }
   public class CustomEntry extends Setting {
      public Cons2<CustomEntry, Table> entryBuilder;
      public CustomEntry(String name) {
         this.name = name;
         this.title = Core.bundle.get("settings." + this.name, name);
         this.description = Core.bundle.getOrNull("settings." + this.name + ".description");
      }
      public void build(Table table) {
         table.table((t) -> this.entryBuilder.get(this, t)).growX();
      }
   }
   public class CheckSetting extends Setting {
      boolean def;
      Boolc changed;
      public CheckSetting(String name, boolean def, Boolc changed) {
         super(name);
         this.def = def;
         this.changed = changed;
         Core.settings.defaults(new Object[]{this.name, def});
      }
      public void build(Table table) {
         CheckBox box = new CheckBox(this.title);
         box.update(() -> box.setChecked(Core.settings.getBool(this.name)));
         box.changed(() -> {
            Core.settings.put(this.name, box.isChecked());
            if (this.changed != null) {
               this.changed.get(box.isChecked());
            }
         });
         box.left();
         table.add(box).growX().left().padTop(3.0F);
      }
      public TextButton miniButton() {
         return this.miniButton(this.title);
      }
      public TextButton miniButton(String text) {
         TextButton b = new TextButton(text);
         b.setStyle(MTStyles.textbtoggle);
         MTStyles.funcSetTextb.get(b);
         b.clicked(() -> {
            boolean v = Core.settings.getBool(this.name);
            Core.settings.put(this.name, !v);
            if (this.changed != null) {
               this.changed.get(!v);
            }
         });
         UIUtils.tooltip(b, this.title);
         b.update(() -> b.setChecked(Core.settings.getBool(this.name)));
         return b;
      }
   }
   public class SliderSetting extends Setting {
      int def;
      int min;
      int max;
      int step;
      SettingsMenuDialog.StringProcessor sp;
      Intc changed;
      public SliderSetting(String name, int def, int min, int max, int step, SettingsMenuDialog.StringProcessor s, Intc changed) {
         super(name);
         this.def = def;
         this.min = min;
         this.max = max;
         this.step = step;
         this.sp = s;
         this.changed = changed;
         Core.settings.defaults(new Object[]{this.name, def});
      }
      public void build(Table table) {
         Slider slider = new Slider((float)this.min, (float)this.max, (float)this.step, false);
         int i = Core.settings.getInt(this.name, this.def);
         slider.setValue((float)i);
         Label value = new Label(this.sp.get(i), Styles.outlineLabel);
         Table content = new Table();
         content.add(this.title, Styles.outlineLabel).left().growX().wrap();
         content.add(value).padLeft(10.0F).right();
         content.margin(3.0F, 33.0F, 3.0F, 33.0F);
         content.touchable = Touchable.disabled;
         slider.changed(() -> {
            Core.settings.put(this.name, (int)slider.getValue());
            if (this.changed != null) {
               this.changed.get((int)slider.getValue());
            }
            value.setText(this.sp.get((int)slider.getValue()));
         });
         table.stack(new Element[]{slider, content}).growX().padTop(4.0F);
      }
   }
   public class ChooseSetting extends Setting {
      String def;
      String[] values;
      Func<String, String> textf;
      Cons<String> changed;
      public ChooseSetting(String name, String def, String[] values, Func<String, String> buttonTextFunc, Cons<String> changed) {
         super(name);
         this.def = def;
         this.values = values;
         this.textf = buttonTextFunc == null ? (s) -> s : buttonTextFunc;
         this.changed = changed;
      }
      public void build(Table table) {
         if (this.values != null) {
            ButtonGroup<Button> group = new ButtonGroup();
            table.table((t) -> {
               t.defaults().uniform().growX();
               int i = 0;
               for(String item : this.values) {
                  Cell<Button> var10000 = t.button((String)this.textf.get(item), () -> {
                     group.uncheckAll();
                     group.setChecked((String)this.textf.get(item));
                     Core.settings.put(this.name, item);
                     if (this.changed != null) {
                        this.changed.get(item);
                     }
                  }).with(MTStyles.funcSetTextb);
                  group.add(var10000.get());
                  ++i;
                  if (Mathf.mod(i, 4) == 0) {
                     t.row();
                     i = 0;
                  }
               }
            }).growX().left().maxWidth(Math.min((float)Core.graphics.getWidth() / 1.2F, 460.0F));
            group.setChecked((String)this.textf.get(Core.settings.getString(this.name, this.def)));
            table.add(this.title).row();
         }
      }
   }
   public class TextFieldSetting extends Setting {
      String def;
      Cons<String> changed;
      @Nullable
      TextField.TextFieldFilter filter;
      @Nullable
      TextField.TextFieldValidator validator;
      @Nullable
      Func<String, Object> parser;
      public static Func<String, Object> intParser = Strings::parseInt;
      public static Func<String, Object> floatParser = Strings::parseFloat;
      public static Func<String, Object> boolParser = (s) -> s.equals("true");
      public TextFieldSetting(String name, String def, TextField.TextFieldFilter filter, TextField.TextFieldValidator validator, Cons<String> changed, Func<String, Object> parser) {
         super(name);
         this.def = def;
         this.filter = filter;
         this.validator = validator;
         this.changed = changed;
         this.parser = parser;
         Core.settings.defaults(new Object[]{this.name, parser != null ? parser.get(def) : def});
      }
      public void build(Table table) {
         table.add(this.title).growX();
         table.field(String.valueOf(Core.settings.get(this.name, this.def)), Styles.nodeField, (str) -> {
            if (this.validator == null || this.validator.valid(str)) {
               Core.settings.put(this.name, this.parser != null ? this.parser.get(str) : str);
               if (this.changed != null) {
                  this.changed.get(str);
               }
            }
         }).with((tf) -> {
            tf.setValidator(this.validator);
            tf.setFilter(this.filter);
         }).width(150.0F).right().update((tf) -> {
            if (!tf.hasKeyboard()) {
               tf.setText(String.valueOf(Core.settings.get(this.name, this.def)));
            }
         });
      }
   }
}
