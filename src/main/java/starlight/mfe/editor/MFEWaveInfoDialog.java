package mfe.editor;
import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.func.Floatf;
import arc.func.Intc;
import arc.func.Intp;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.FrameBuffer;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.ObjectSet;
import arc.struct.OrderedSet;
import arc.struct.Seq;
import arc.util.Buffers;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.ScreenUtils;
import arc.util.Strings;
import arc.util.Structs;
import arc.util.Threads;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.game.SpawnGroup;
import mindustry.game.Waves;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.io.JsonIO;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
public class MFEWaveInfoDialog extends BaseDialog {
   public static MFEWaveInfoDialog mfewave = new MFEWaveInfoDialog();
   Seq<SpawnGroup> groups = new Seq();
   Seq<SpawnGroup> selectedGroups = new Seq();
   WaveCanvas canvas = new WaveCanvas();
   Table config;
   int search = -1;
   boolean batchEditing;
   boolean checkedSpawns;
   boolean showViewSettings;
   boolean showWaveGraph;
   Table viewSettings;
   Table graphViewSettings;
   Table graphViewSettingsColors;
   boolean filterPayloads;
   boolean filterItems;
   boolean filterEffects;
   @Nullable
   UnitType filterType;
   int filterSpawn = -1;
   boolean reverseSort = false;
   boolean detailedBar;
   boolean barFillX;
   public MFEWaveInfoDialog() {
      super("@waves.title");
      this.shown(this::setup);
      this.hidden(() -> {
         if (Vars.state.isEditor() || Vars.state.isMenu()) {
            this.groups.each((g) -> {
               if (g.type.payloadCapacity == 0.0F) {
                  g.payloads = null;
               }
               if (g.type.itemCapacity == 0) {
                  g.items = null;
               }
            });
            Vars.state.rules.spawns = this.groups;
         }
      });
      this.addCloseButton();
      this.buttons.button("@waves.edit", Icon.edit, () -> {
         BaseDialog dialog = new BaseDialog("@waves.edit");
         dialog.addCloseButton();
         dialog.setFillParent(false);
         dialog.cont.table(Tex.button, (t) -> {
            TextButton.TextButtonStyle style = Styles.cleart;
            t.defaults().size(280.0F, 64.0F).pad(2.0F);
            t.button("@waves.copy", Icon.copy, style, () -> {
               Vars.ui.showInfoFade("@waves.copied");
               Core.app.setClipboardText(Vars.maps.writeWaves(this.groups));
               dialog.hide();
            }).disabled((b) -> this.groups == null || this.groups.isEmpty()).marginLeft(12.0F).row();
            t.button("@waves.load", Icon.download, style, () -> {
               try {
                  this.groups = Vars.maps.readWaves(Core.app.getClipboardText());
                  this.buildGroups();
               } catch (Exception e) {
                  e.printStackTrace();
                  Vars.ui.showErrorMessage("@waves.invalid");
               }
               dialog.hide();
            }).disabled(Core.app.getClipboardText() == null || !Core.app.getClipboardText().startsWith("[")).marginLeft(12.0F).row();
            t.button("@clear", Icon.none, style, () -> Vars.ui.showConfirm("@confirm", "@settings.clear.confirm", () -> {
                  this.groups.clear();
                  this.buildGroups();
                  dialog.hide();
               })).marginLeft(12.0F).row();
            t.button("@settings.reset", Icon.refresh, style, () -> Vars.ui.showConfirm("@confirm", "@settings.clear.confirm", () -> {
                  this.groups = (Seq)JsonIO.copy(Vars.waves.get());
                  this.buildGroups();
                  dialog.hide();
               })).marginLeft(12.0F);
         });
         dialog.show();
      }).size(250.0F, 64.0F);
      this.buttons.button(Core.bundle.get("waves.random"), Icon.refresh, () -> {
         this.groups.clear();
         this.groups = Waves.generate(0.1F);
         this.buildGroups();
      }).width(150.0F);
   }
   void setup() {
      this.groups = (Seq)JsonIO.copy(Vars.state.rules.spawns.isEmpty() ? Vars.waves.get() : Vars.state.rules.spawns);
      if (this.groups == null) {
         this.groups = new Seq();
      }
      this.canvas.camera.setZero();
      this.checkedSpawns = false;
      this.batchEditing = false;
      this.selectedGroups.clear();
      this.filterSpawn = -1;
      this.cont.clear();
      this.cont.stack(new Element[]{this.canvas, new Label("@waves.none") {
         {
            this.visible(() -> MFEWaveInfoDialog.this.groups.isEmpty());
            this.touchable = Touchable.disabled;
            this.setWrap(true);
            this.setAlignment(1, 1);
         }
      }, (new Table((shell) -> {
         shell.visible(() -> !this.selectedGroups.isEmpty());
         shell.pane((t) -> {
            this.config = t;
            t.background(Tex.button);
         });
      })).left().bottom(), (new Table((shell) -> {
         shell.visible(() -> this.showViewSettings);
         shell.button("@keybind.screenshot.name", () -> this.canvas.screenshot()).size(120.0F, 40.0F);
      })).right().top(), (new Table((shell) -> {
         shell.visible(() -> this.showViewSettings && !this.showWaveGraph);
         shell.pane((t) -> {
            this.viewSettings = t;
            t.background(Tex.button);
            t.margin(12.0F);
            t.add("@settings.graphics").height(40.0F).grow().row();
            t.table((graph) -> {
               graph.defaults().growX().height(40.0F).pad(4.0F);
               graph.button("Detailed Bar", Styles.togglet, () -> {
                  this.detailedBar = !this.detailedBar;
                  this.buildGroups();
               }).row();
               graph.button("Bar Expand", Styles.togglet, () -> this.barFillX = !this.barFillX).row();
            }).growX().row();
            t.add("@waveinfo.filters").height(40.0F).grow().row();
            t.table((gf) -> {
               gf.defaults().size(40.0F).pad(4.0F);
               gf.button(Icon.effect, Styles.squareTogglei, () -> {
                  this.filterEffects = !this.filterEffects;
                  this.buildGroups();
                  this.canvas.locateWave(((SpawnGroup)this.groups.min((g) -> this.checkFilters(g) ? (float)(g.begin - 999999) : (float)g.begin)).begin);
               });
               gf.button(Icon.box, Styles.squareTogglei, () -> {
                  this.filterItems = !this.filterItems;
                  this.buildGroups();
                  this.canvas.locateWave(((SpawnGroup)this.groups.min((g) -> this.checkFilters(g) ? (float)(g.begin - 999999) : (float)g.begin)).begin);
               });
               gf.button(Icon.itchioSmall, Styles.squareTogglei, () -> {
                  this.filterPayloads = !this.filterPayloads;
                  this.buildGroups();
                  this.canvas.locateWave(((SpawnGroup)this.groups.min((g) -> this.checkFilters(g) ? (float)(g.begin - 999999) : (float)g.begin)).begin);
               });
            }).row();
            t.table((gf) -> {
               gf.defaults().height(40.0F).pad(4.0F);
               gf.button(Icon.units, Styles.squarei, () -> this.showAnyContentsYouWant(this.groups.map((sg) -> sg.type).asSet().toSeq().sort((type) -> (float)type.id), "", (type) -> {
                     this.filterType = type;
                     this.buildGroups();
                     this.canvas.locateWave(((SpawnGroup)this.groups.min((g) -> this.checkFilters(g) ? (float)(g.begin - 999999) : (float)g.begin)).begin);
                  })).width(40.0F).update((b) -> b.getStyle().imageUp = this.filterType != null ? new TextureRegionDrawable(this.filterType.uiIcon) : Icon.units);
               ((TextButton)gf.button("", () -> {
                  if (!this.checkedSpawns) {
                     Vars.spawner.reset();
                     this.checkedSpawns = true;
                  }
                  this.showSpawns((i) -> this.batchEditing((g) -> g.spawn = i), () -> this.filterSpawn);
               }).width(160.0F).get()).getLabel().setText(() -> this.filterSpawn == -1 ? "@waves.spawn.all" : Point2.x(this.filterSpawn) + ", " + Point2.y(this.filterSpawn));
            }).row();
            t.add("@waveinfo.sorters").height(40.0F).grow().row();
            t.table((st) -> {
               st.defaults().pad(4.0F);
               st.table(Tex.button, (f) -> {
                  for(Sort s : MFEWaveInfoDialog.Sort.all) {
                     f.button("@waves.sort." + s, Styles.flatt, () -> {
                        this.sort(s);
                        this.buildGroups();
                        this.canvas.locateWave(((SpawnGroup)this.groups.min((g) -> this.checkFilters(g) ? (float)(g.begin - 999999) : (float)g.begin)).begin);
                     }).size(80.0F, 40.0F);
                  }
               }).row();
               st.check("@waves.sort.reverse", (b) -> {
                  this.reverseSort = b;
                  this.buildGroups();
                  this.canvas.locateWave(((SpawnGroup)this.groups.min((g) -> this.checkFilters(g) ? (float)(g.begin - 999999) : (float)g.begin)).begin);
               }).height(40.0F).checked(this.reverseSort);
            }).row();
         });
      })).right().bottom(), (new Table((shell) -> {
         shell.visible(() -> this.showViewSettings && this.showWaveGraph);
         shell.table((t) -> {
            this.graphViewSettings = t;
            t.background(Tex.button);
            t.margin(16.0F);
            ButtonGroup<Button> group = new ButtonGroup();
            for(Mode m : MFEWaveInfoDialog.Mode.all) {
               t.button("@wavemode." + m.name(), Styles.fullTogglet, () -> this.canvas.mode = m).group(group).height(35.0F).update((b) -> b.setChecked(m == this.canvas.mode)).width(130.0F);
            }
         }).row();
         shell.table((col) -> this.graphViewSettingsColors = col).growX();
      })).right().bottom()}).grow().margin(16.0F);
      this.cont.row();
      this.cont.table((tools) -> {
         tools.defaults().minWidth(120.0F).height(60.0F).left();
         tools.button("@add", () -> {
            this.showAnyContentsYouWant(Vars.content.units().copy().removeAll(UnitType::isHidden), "title", (type) -> this.groups.add(new SpawnGroup(type)), (Runnable)null);
            this.buildGroups();
         });
         tools.check("@waveinfo.batchedit", (b) -> this.batchEditing = b);
         tools.table((s) -> {
            s.image(Icon.zoom).padRight(8.0F);
            ((TextField)s.field("", TextFieldFilter.digitsOnly, (text) -> {
               this.search = Math.max(Strings.parseInt(text, 1) - 1, 0);
               if (this.groups.any()) {
                  this.canvas.locateWave(this.search);
               }
            }).growX().maxTextLength(8).get()).setMessageText("@waves.search");
         });
         tools.add().growX().minWidth(0.0F);
         tools.button("\ue802", () -> {
         }).update((t) -> {
            if (t.getClickListener().isPressed()) {
               this.canvas.scrollX(-20.0F);
            }
         }).width(60.0F);
         tools.button("\ue803", () -> {
         }).update((t) -> {
            if (t.getClickListener().isPressed()) {
               this.canvas.scrollX(20.0F);
            }
         }).width(60.0F);
         tools.button("\ue804", () -> {
         }).update((t) -> {
            if (t.getClickListener().isPressed()) {
               this.canvas.scrollY(20.0F);
            }
         }).width(60.0F);
         tools.button("\ue805", () -> {
         }).update((t) -> {
            if (t.getClickListener().isPressed()) {
               this.canvas.scrollY(-20.0F);
            }
         }).width(60.0F);
         tools.button("-", () -> {
         }).update((t) -> {
            if (t.getClickListener().isPressed()) {
               this.canvas.scaleX(-1);
            }
         }).width(60.0F);
         tools.button("+", () -> {
         }).update((t) -> {
            if (t.getClickListener().isPressed()) {
               this.canvas.scaleX(1);
            }
         }).width(60.0F);
         tools.button(Icon.filter, Styles.clearTogglei, () -> this.showViewSettings = !this.showViewSettings).checked(this.showViewSettings).width(60.0F).padLeft(8.0F);
         tools.button(Icon.chartBar, Styles.clearTogglei, () -> this.showWaveGraph = !this.showWaveGraph);
      }).growX();
      this.buildGroups();
   }
   boolean checkFilters(SpawnGroup group) {
      if (this.filterType != null && group.type != this.filterType) {
         return false;
      } else if (this.filterEffects && group.effect == null) {
         return false;
      } else if (this.filterItems && group.items == null) {
         return false;
      } else if (this.filterPayloads && group.payloads == null) {
         return false;
      } else {
         return this.filterSpawn == -1 || group.spawn == this.filterSpawn;
      }
   }
   void sort(Sort sort) {
      this.groups.sort(Structs.comps(Structs.comparingFloat(sort.sort), Structs.comparingFloat(sort.secondary)));
      if (this.reverseSort) {
         this.groups.reverse();
      }
   }
   void buildGroups() {
      this.canvas.clearChildren();
      if (this.groups != null) {
         for(SpawnGroup group : this.groups) {
            if (group.effect == StatusEffects.none) {
               group.effect = null;
            }
            if (this.checkFilters(group)) {
               this.canvas.addGroup(group);
            }
         }
      }
      this.canvas.addChild(this.canvas.graphDrawer);
      this.buildConfig();
   }
   void buildConfig() {
      if (this.config != null && !this.selectedGroups.isEmpty()) {
         this.canvas.cookStats();
         SpawnGroup group = (SpawnGroup)this.selectedGroups.get(this.selectedGroups.size - 1);
         this.config.clear();
         this.config.button("@waveinfo.deselect", () -> this.selectedGroups.clear()).width(300.0F).row();
         this.config.table((b) -> {
            b.left();
            b.image(group.type.uiIcon).size(32.0F).padRight(3.0F).scaling(Scaling.fit);
            b.add(group.type.localizedName).color(Pal.accent);
            b.add().growX();
            b.button(Icon.copySmall, Styles.emptyi, () -> {
               this.groups.insert(this.groups.indexOf(group) + 1, this.copy(group));
               this.buildGroups();
            }).pad(-6.0F).size(46.0F).tooltip("@editor.copy").disabled((bb) -> this.batchEditing);
            b.button(Icon.unitsSmall, Styles.emptyi, () -> this.showAnyContentsYouWant(Vars.content.units().copy().removeAll(UnitType::isHidden), "", (type) -> {
                  this.batchEditing((g) -> g.type = type);
                  this.buildConfig();
               }, (Runnable)null)).pad(-6.0F).size(46.0F).tooltip("@stat.unittype");
            b.button(Icon.cancel, Styles.emptyi, () -> {
               this.batchEditing((g) -> this.groups.remove(g));
               this.selectedGroups.clear();
               this.buildGroups();
            }).pad(-6.0F).size(46.0F).padRight(-12.0F).tooltip("@waves.remove");
            b.clicked(KeyCode.mouseMiddle, () -> {
               this.groups.insert(this.groups.indexOf(group) + 1, this.copy(group));
               this.buildGroups();
            });
         }).row();
         this.config.table((t) -> {
            t.table((spawns) -> {
               spawns.field("" + (group.begin + 1), TextFieldFilter.digitsOnly, (text) -> {
                  if (Strings.canParsePositiveInt(text)) {
                     this.batchEditing((g) -> g.begin = Strings.parseInt(text) - 1);
                  }
               }).width(100.0F);
               spawns.add("@waves.to").padLeft(4.0F).padRight(4.0F);
               ((TextField)spawns.field(group.end == Integer.MAX_VALUE ? "" : group.end + 1 + "", TextFieldFilter.digitsOnly, (text) -> {
                  if (Strings.canParsePositiveInt(text)) {
                     this.batchEditing((g) -> g.end = Strings.parseInt(text) - 1);
                  } else if (text.isEmpty()) {
                     this.batchEditing((g) -> g.end = Integer.MAX_VALUE);
                  }
               }).width(100.0F).get()).setMessageText("∞");
            }).row();
            t.table((p) -> {
               p.add("@waves.every").padRight(4.0F);
               p.field(group.spacing + "", TextFieldFilter.digitsOnly, (text) -> {
                  if (Strings.canParsePositiveInt(text) && Strings.parseInt(text) > 0) {
                     this.batchEditing((g) -> g.spacing = Strings.parseInt(text));
                  }
               }).width(100.0F);
               p.add("@waves.waves").padLeft(4.0F);
            }).row();
            t.table((a) -> {
               a.field(group.unitAmount + "", TextFieldFilter.digitsOnly, (text) -> {
                  if (Strings.canParsePositiveInt(text)) {
                     int amount = Strings.parseInt(text);
                     if (this.selectedGroups.isEmpty()) group.unitAmount = amount;
                     else this.batchEditing((g) -> g.unitAmount = amount);
                  }
               }).width(80.0F);
               a.add(" + ");
               a.field(Strings.fixed(Math.max(Mathf.zero(group.unitScaling) ? 0.0F : 1.0F / group.unitScaling, 0.0F), 2), TextFieldFilter.floatsOnly, (text) -> {
                  if (Strings.canParsePositiveFloat(text)) {
                     float scaling = 1.0F / Strings.parseFloat(text);
                     if (this.selectedGroups.isEmpty()) group.unitScaling = scaling;
                     else this.batchEditing((g) -> g.unitScaling = scaling);
                  }
               }).width(80.0F);
               a.add("@waves.perspawn").padLeft(4.0F);
            }).row();
            t.table((a) -> {
               a.field(group.max + "", TextFieldFilter.digitsOnly, (text) -> {
                  if (Strings.canParsePositiveInt(text)) {
                     this.batchEditing((g) -> g.max = Strings.parseInt(text));
                  }
               }).width(80.0F);
               a.add("@waves.max").padLeft(5.0F);
            }).row();
            t.table((a) -> {
               a.field((int)group.shields + "", TextFieldFilter.digitsOnly, (text) -> {
                  if (Strings.canParsePositiveInt(text)) {
                     this.batchEditing((g) -> g.shields = (float)Strings.parseInt(text));
                  }
               }).width(80.0F);
               a.add(" + ");
               a.field((int)group.shieldScaling + "", TextFieldFilter.digitsOnly, (text) -> {
                  if (Strings.canParsePositiveInt(text)) {
                     this.batchEditing((g) -> g.shieldScaling = (float)Strings.parseInt(text));
                  }
               }).width(80.0F);
               a.add("@waves.shields").padLeft(4.0F);
            }).row();
            t.table((a) -> {
               a.button((b) -> b.image(group.effect != null ? group.effect.uiIcon : Icon.none.getRegion()).scaling(Scaling.fit).size(32.0F), () -> {
                  this.showEffects();
                  this.buildGroups();
               });
               a.check("@waves.guardian", (b) -> {
                  this.batchEditing((g) -> g.effect = b ? StatusEffects.boss : null);
                  this.buildGroups();
               }).padTop(4.0F).update((b) -> b.setChecked(group.effect == StatusEffects.boss)).padBottom(8.0F);
            }).width(300.0F).row();
            t.table((a) -> {
               a.button((b) -> b.image(group.items == null ? Icon.none.getRegion() : group.items.item.uiIcon).scaling(Scaling.fit).size(32.0F), () -> this.showAnyContentsYouWant(Vars.content.items().copy().removeAll(Item::isHidden), "", (item) -> {
                     this.batchEditing((g) -> {
                        if (g.items == null) {
                           g.items = (new ItemStack()).set(Items.copper, g.type.itemCapacity);
                        }
                        g.items.item = item;
                     });
                     this.buildGroups();
                  }, () -> group.items = null));
               a.field(group.items == null ? "" : String.valueOf(group.items.amount), TextFieldFilter.digitsOnly, (text) -> {
                  if (Strings.canParsePositiveInt(text)) {
                     this.batchEditing((g) -> {
                        if (g.items != null) {
                           g.items.amount = Strings.parseInt(text);
                        }
                     });
                     this.buildGroups();
                  }
               }).width(80.0F).disabled((tf) -> group.items == null);
            }).width(300.0F).row();
            t.table((a) -> {
               a.button((b) -> b.image(Icon.itchioSmall).scaling(Scaling.fit).size(32.0F), () -> this.showPayloads(group)).disabled((b) -> this.batchEditing);
               if (group.payloads != null && !group.payloads.isEmpty()) {
                  a.table((ps) -> {
                     for(int i = Math.min(8, group.payloads.size); i > 0; --i) {
                        ps.image(((UnitType)group.payloads.get(group.payloads.size - i)).uiIcon).size(16.0F).scaling(Scaling.fit);
                     }
                  });
               }
            }).width(300.0F).row();
            t.table((a) -> {
               a.add("@waves.spawn").padRight(8.0F);
               ((TextButton)a.button("", () -> {
                  if (!this.checkedSpawns) {
                     Vars.spawner.reset();
                     this.checkedSpawns = true;
                  }
                  this.showSpawns((i) -> this.batchEditing((g) -> g.spawn = i), () -> group.spawn);
               }).width(160.0F).height(36.0F).get()).getLabel().setText(() -> group.spawn == -1 ? "@waves.spawn.all" : Point2.x(group.spawn) + ", " + Point2.y(group.spawn));
            }).padBottom(8.0F).row();
         }).width(300.0F).row();
      }
   }
   SpawnGroup copy(SpawnGroup group) {
      SpawnGroup copy = group.copy();
      copy.items = group.items == null ? null : group.items.copy();
      copy.payloads = group.payloads == null ? null : group.payloads.copy();
      return copy;
   }
   void batchEditing(Cons<SpawnGroup> run) {
      this.selectedGroups.each(run);
   }
   <T extends UnlockableContent> void showAnyContentsYouWant(Seq<T> seq, String title, Cons<T> setter) {
      this.showAnyContentsYouWant(seq, title, setter, () -> setter.get((T)null));
   }
   <T extends UnlockableContent> void showAnyContentsYouWant(Seq<T> seq, String title, Cons<T> setter, @Nullable Runnable noneSetter) {
      boolean reset = noneSetter != null;
      BaseDialog dialog = new BaseDialog(title);
      dialog.cont.pane((p) -> {
         p.defaults().pad(2.0F).fillX();
         if (reset) {
            p.button((t) -> {
               t.left();
               t.image(Icon.none).size(32.0F).scaling(Scaling.fit).padRight(2.0F);
               t.add("@settings.resetKey");
            }, () -> {
               noneSetter.run();
               dialog.hide();
               this.buildGroups();
            }).margin(12.0F);
         }
         int i = reset ? 1 : 0;
         for(T type : seq) {
            p.button((t) -> {
               t.left();
               t.image(type.uiIcon).size(32.0F).scaling(Scaling.fit).padRight(2.0F);
               t.add(type.localizedName);
            }, () -> {
               setter.get(type);
               dialog.hide();
               this.buildGroups();
            }).margin(12.0F);
            ++i;
            if (i % 3 == 0) {
               p.row();
            }
         }
      }).growX().scrollX(false);
      dialog.addCloseButton();
      dialog.show();
   }
   void showEffects() {
      this.showAnyContentsYouWant(Vars.content.statusEffects().copy().removeAll((effect) -> effect.reactive), "", (effect) -> this.batchEditing((g) -> g.effect = effect));
   }
   void showSpawns(Intc setter, Intp getter) {
      BaseDialog dialog = new BaseDialog("@waves.spawn.select");
      dialog.cont.pane((p) -> {
         p.background(Tex.button).margin(10.0F);
         int i = 0;
         int cols = 4;
         int max = 20;
         if (Vars.spawner.getSpawns().size >= max) {
            p.add("[lightgray](first " + max + ")").colspan(cols).padBottom(4.0F).row();
         }
         for(Tile spawn : Vars.spawner.getSpawns()) {
            p.button(spawn.x + ", " + spawn.y, Styles.flatTogglet, () -> {
               setter.get(spawn.pos());
               dialog.hide();
            }).size(110.0F, 45.0F).checked(getter.get() == spawn.pos());
            ++i;
            if (i % cols == 0) {
               p.row();
            }
            if (i >= 20) {
               break;
            }
         }
         if (Vars.spawner.getSpawns().isEmpty()) {
            p.add("@waves.spawn.none");
         } else {
            p.button("@waves.spawn.all", Styles.flatTogglet, () -> {
               setter.get(-1);
               dialog.hide();
            }).size(110.0F, 45.0F).checked(getter.get() == -1);
         }
      }).grow();
      dialog.setFillParent(false);
      dialog.addCloseButton();
      dialog.show();
   }
   void showPayloads(final SpawnGroup group) {
      BaseDialog dialog = new BaseDialog("") {
         Table pane;
         boolean showConfig;
         {
            this.addCloseButton();
            this.buttons.button("@add", Styles.togglet, () -> this.showConfig = !this.showConfig);
            this.hidden(() -> MFEWaveInfoDialog.this.buildGroups());
            this.cont.stack(new Element[]{new Table((t) -> {
               t.setFillParent(true);
               t.pane((p) -> {
                  this.pane = p;
                  this.build();
               }).grow();
            }), (new Table((t) -> {
               t.setFillParent(true);
               t.table((tt) -> {
                  tt.background(Tex.button);
                  tt.pane((p) -> {
                     p.defaults().pad(2.0F).fillX();
                     int i = 0;
                     for(UnitType type : Vars.content.units().copy().removeAll((u) -> u == UnitTypes.block)) {
                        p.button((b) -> {
                           b.center();
                           b.image(type.uiIcon).size(48.0F).scaling(Scaling.fit).padRight(2.0F);
                           b.row();
                           b.add(type.localizedName);
                        }, () -> {
                           if (group.payloads == null) {
                              group.payloads = new Seq();
                           }
                           group.payloads.add(type);
                           this.build();
                        }).margin(12.0F).size(96.0F);
                        ++i;
                        if (i % 8 == 0) {
                           p.row();
                        }
                     }
                  });
               }).height(300.0F).visible(() -> this.showConfig);
            })).bottom()}).grow();
         }
         void build() {
            this.pane.clear();
            this.pane.defaults().pad(2.0F).fillX();
            if (group.payloads != null && !group.payloads.isEmpty()) {
               for(int i = 0; i < group.payloads.size; ++i) {
                  if (i % 8 == 0) {
                     this.pane.row();
                  }
                  final int index = i;
                  final UnitType payload = (UnitType)group.payloads.get(index);
                  this.pane.button((t) -> {
                     t.center();
                     t.image(payload.uiIcon).size(64.0F).scaling(Scaling.fit);
                     t.row();
                     t.add(payload.localizedName);
                  }, () -> {
                     group.payloads.ordered = true;
                     group.payloads.remove(index);
                     this.build();
                  }).margin(12.0F).size(144.0F);
               }
            } else {
               this.pane.add("@waves.none");
            }
         }
      };
      dialog.show();
   }
   Color color(UnitType type) {
      return Tmp.c1.fromHsv((float)type.id / (float)Vars.content.units().size * 360.0F, 0.7F, 0.8F).a(1.0F);
   }
   public class WaveCanvas extends WidgetGroup {
      float tileH;
      float tileW;
      Vec2 vec = new Vec2();
      Vec2 sv = new Vec2();
      public Vec2 camera = new Vec2();
      int start;
      int end;
      int tail;
      Element graphDrawer;
      Mode mode;
      int[][] values;
      OrderedSet<UnitType> used;
      int max;
      int maxTotal;
      float maxHealth;
      ObjectSet<UnitType> hidden;
      boolean pinching;
      public WaveCanvas() {
         this.mode = MFEWaveInfoDialog.Mode.counts;
         this.used = new OrderedSet();
         this.hidden = new ObjectSet();
         this.setTransform(true);
         this.setCullingArea(new Rect());
         this.tileH = 50.0F;
         this.tileW = 50.0F;
         this.hovered(() -> Core.scene.setScrollFocus(this));
         this.addCaptureListener(new InputListener() {
            float lx;
            float ly;
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
               WaveCanvas.this.pinching = Core.input.isTouched(1);
               if (!WaveCanvas.this.pinching && button != KeyCode.mouseRight) {
                  return false;
               } else {
                  event.stop();
                  Core.scene.cancelTouchFocusExcept(this, WaveCanvas.this);
                  WaveCanvas.this.sv.setZero();
                  WaveCanvas.this.vec.setZero();
                  this.lx = x;
                  this.ly = y;
                  return true;
               }
            }
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
               WaveCanvas.this.sv.sub(x - this.lx, y - this.ly);
               WaveCanvas.this.camera.sub(x - this.lx, y - this.ly);
               this.lx = x;
               this.ly = y;
            }
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
               WaveCanvas.this.vec.set(WaveCanvas.this.sv);
            }
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
               WaveCanvas.this.scrollX(amountY * 100.0F * (1.0F + Math.abs(x / WaveCanvas.this.getWidth() - 0.5F) * 8.0F));
               return super.scrolled(event, x, y, amountX, amountY);
            }
         });
         this.graphDrawer = (new Element() {
            public void draw() {
               super.draw();
               Styles.black8.draw(this.x, this.y, this.width, this.height);
               Lines.stroke(Scl.scl(3.0F));
               GlyphLayout lay = (GlyphLayout)Pools.obtain(GlyphLayout.class, GlyphLayout::new);
               Font font = Fonts.outline;
               lay.setText(font, "1");
               int var10000;
               switch (WaveCanvas.this.mode) {
                  case counts:
                     var10000 = WaveCanvas.this.nextStep((float)WaveCanvas.this.max);
                     break;
                  case health:
                     var10000 = WaveCanvas.this.nextStep((float)((int)WaveCanvas.this.maxHealth));
                     break;
                  case totals:
                     var10000 = WaveCanvas.this.nextStep((float)WaveCanvas.this.maxTotal);
                     break;
                  default:
                     throw new IncompatibleClassChangeError();
               }
               int maxY = var10000;
               float fh = lay.height;
               float offsetX = Scl.scl(lay.width * (float)(maxY + "").length() * 2.0F);
               float offsetY = Scl.scl(24.0F) + fh;
               float graphX = this.x + offsetX;
               float graphY = this.y + offsetY;
               float graphW = this.width - offsetX;
               float graphH = this.height - offsetY;
               if (WaveCanvas.this.mode == MFEWaveInfoDialog.Mode.counts) {
                  for(UnitType type : WaveCanvas.this.used.orderedItems()) {
                     Draw.color(MFEWaveInfoDialog.this.color(type));
                     Draw.alpha(this.parentAlpha);
                     Lines.beginLine();
                     for(int i = 0; i < WaveCanvas.this.values.length; ++i) {
                        int val = WaveCanvas.this.values[i][type.id];
                        float cy = graphY + (float)val * graphH / (float)maxY;
                        Lines.linePoint(WaveCanvas.this.tileW / 2.0F + WaveCanvas.this.tileX(WaveCanvas.this.start + i), cy);
                     }
                     Lines.endLine();
                  }
               } else if (WaveCanvas.this.mode == MFEWaveInfoDialog.Mode.totals) {
                  Lines.beginLine();
                  Draw.color(Pal.accent);
                  for(int i = 0; i < WaveCanvas.this.values.length; ++i) {
                     int sum = 0;
                     for(UnitType type : WaveCanvas.this.used.orderedItems()) {
                        sum += WaveCanvas.this.values[i][type.id];
                     }
                     float cy = graphY + (float)sum * graphH / (float)maxY;
                     Lines.linePoint(WaveCanvas.this.tileW / 2.0F + WaveCanvas.this.tileX(WaveCanvas.this.start + i), cy);
                  }
                  Lines.endLine();
               } else if (WaveCanvas.this.mode == MFEWaveInfoDialog.Mode.health) {
                  Lines.beginLine();
                  Draw.color(Pal.health);
                  for(int i = 0; i < WaveCanvas.this.values.length; ++i) {
                     float sum = 0.0F;
                     for(UnitType type : WaveCanvas.this.used.orderedItems()) {
                        sum += type.health * (float)WaveCanvas.this.values[i][type.id];
                     }
                     float cy = graphY + sum * graphH / (float)maxY;
                     Lines.linePoint(WaveCanvas.this.tileW / 2.0F + WaveCanvas.this.tileX(WaveCanvas.this.start + i), cy);
                  }
                  Lines.endLine();
               }
               float totalMarks = (float)Mathf.clamp(maxY, 1, 10);
               int markSpace = Math.max(1, Mathf.ceil((float)maxY / totalMarks));
               Draw.color(Color.lightGray);
               Draw.alpha(0.1F);
               for(int i = 0; i < maxY; i += markSpace) {
                  float cy = graphY + (float)i * graphH / (float)maxY;
                  Lines.line(graphX, cy, graphX + graphW, cy);
                  lay.setText(font, "" + i);
                  font.draw("" + i, graphX, cy + lay.height / 2.0F, 16);
               }
               font.setColor(Color.white);
               Pools.free(lay);
               Draw.reset();
            }
         }).visible(() -> MFEWaveInfoDialog.this.showWaveGraph);
         this.graphDrawer.setFillParent(true);
      }
      public void act(float delta) {
         this.getCullingArea().set(0.0F, 0.0F, this.getWidth(), this.getHeight());
         this.camera.add(this.vec.x, this.vec.y);
         this.sv.scl(0.5F);
         this.vec.scl(0.9F);
         this.camera.lerpDelta(Math.max(this.camera.x, -100.0F), Mathf.clamp(this.camera.y, -60.0F, this.tileH * (float)(MFEWaveInfoDialog.this.groups.size + 1) - this.getHeight() + 20.0F), 0.1F);
         super.act(delta);
         int lastStart = this.start;
         int lastEnd = this.end;
         this.start = this.getStartWave();
         this.end = this.getEndWave();
         if (lastStart != this.start || lastEnd != this.end) {
            this.cookStats();
         }
      }
      public int getStartWave() {
         return Math.max(Mathf.floor(this.camera.x / this.tileW), 0);
      }
      public int getEndWave() {
         return Math.max(Mathf.ceil((this.camera.x + this.getWidth()) / this.tileW), 0);
      }
      public float tileX(int wave) {
         return Mathf.clamp(-this.camera.x + (float)wave * this.tileW, 0.0F, this.getWidth());
      }
      public void draw() {
         if (this.transform) {
            this.applyTransform(this.computeTransform());
         }
         this.drawBackground();
         if (this.clipBegin(0.0F, Scl.scl(16.0F), this.getWidth(), this.getHeight())) {
            this.drawChildren();
            Draw.flush();
            this.clipEnd();
         }
         if (this.transform) {
            this.resetTransform();
         }
      }
      public void drawBackground() {
         int gap = Mathf.ceil(100.0F / this.tileW);
         Lines.stroke(4.0F);
         Draw.color(Color.gray, 0.35F * this.parentAlpha);
         Font font = Fonts.outline;
         font.setColor(Color.gray);
         float y1 = Math.max(16.0F, -this.camera.y);
         float y2 = Math.min(this.getHeight(), this.getChildren() != null && !this.getChildren().isEmpty() ? ((Element)this.getChildren().max((e) -> e instanceof SpawnGroupBar ? e.getTop() : 0.0F)).getTop() : y1);
         for(int i = this.start / gap * gap; i < this.end; ++i) {
            float cx = -this.camera.x + (float)i * this.tileW;
            if (!(cx < 0.0F)) {
               Lines.dashLine(cx, y1, cx, y2, 60);
               if (Mathf.mod(i, gap) == 0) {
                  font.draw(String.valueOf(i + 1), cx + this.tileW / 2.0F, 16.0F, 1);
               }
               if (MFEWaveInfoDialog.this.search == i) {
                  Fill.rect(Tmp.r1.set(cx, 0.0F, this.tileW, this.getHeight()));
               }
            }
         }
         font.setColor(Color.white);
         if (this.tail < this.end) {
            float tx = this.tileX(this.tail);
            Draw.color(Color.navy, 0.5F * this.parentAlpha);
            Fill.rect(Tmp.r1.set(tx, 0.0F, this.getWidth() - tx, this.getHeight()));
         }
         Draw.reset();
      }
      public void locateWave(int wave) {
         this.camera.x = Mathf.maxZero(this.tileW * (float)wave - this.getWidth() / 2.0F);
      }
      public void scrollX(float amount) {
         Vec2 var10000 = this.camera;
         var10000.x += amount;
      }
      public void scrollY(float amount) {
         Vec2 var10000 = this.camera;
         var10000.y += amount;
      }
      void scaleX(int amount) {
         this.tileW += (float)amount;
      }
      public void cookStats() {
         if (MFEWaveInfoDialog.this.groups.isEmpty()) {
            this.tail = 10;
         } else {
            SpawnGroup g = (SpawnGroup)MFEWaveInfoDialog.this.groups.max((g2) -> g2.end != Integer.MAX_VALUE ? (float)Math.max(g2.end, g2.begin + g2.spacing) : (float)(g2.begin + g2.spacing));
            this.tail = g.end != Integer.MAX_VALUE ? Math.max(g.end, g.begin + g.spacing) : g.begin + g.spacing + 1;
         }
         this.values = new int[this.end - this.start + 1][Vars.content.units().size];
         this.used.clear();
         this.max = this.maxTotal = 1;
         this.maxHealth = 1.0F;
         for(int i = this.start; i <= this.end; ++i) {
            int index = i - this.start;
            float healthsum = 0.0F;
            int sum = 0;
            for(SpawnGroup spawn : MFEWaveInfoDialog.this.groups) {
               int spawned = spawn.getSpawned(i);
               int[] var10000 = this.values[index];
               short var10001 = spawn.type.id;
               var10000[var10001] += spawned;
               if (spawned > 0) {
                  this.used.add(spawn.type);
               }
               this.max = Math.max(this.max, this.values[index][spawn.type.id]);
               healthsum += (float)spawned * spawn.type.health;
               sum += spawned;
            }
            this.maxTotal = Math.max(this.maxTotal, sum);
            this.maxHealth = Math.max(this.maxHealth, healthsum);
         }
         ObjectSet<UnitType> usedCopy = new ObjectSet(this.used);
         Table colors = MFEWaveInfoDialog.this.graphViewSettingsColors;
         colors.clear();
         colors.left();
         colors.button("@waves.units.hide", Styles.flatt, () -> {
            if (this.hidden.size == usedCopy.size) {
               this.hidden.clear();
            } else {
               this.hidden.addAll(usedCopy);
            }
            this.used.clear();
            this.used.addAll(usedCopy);
            ObjectSet.ObjectSetIterator var2 = this.hidden.iterator();
            while(var2.hasNext()) {
               UnitType o = (UnitType)var2.next();
               this.used.remove(o);
            }
         }).update((b) -> b.setText(this.hidden.size == usedCopy.size ? "@waves.units.show" : "@waves.units.hide")).height(32.0F).width(130.0F);
         colors.pane((t) -> {
            t.left();
            for(UnitType type : this.used.toSeq().sort((u) -> (float)u.id)) {
               t.button((b) -> {
                  Color tcolor = MFEWaveInfoDialog.this.color(type).cpy();
                  ((Image)b.image().size(32.0F).update((i) -> i.setColor(b.isChecked() ? Tmp.c1.set(tcolor).mul(0.5F) : tcolor)).get()).act(1.0F);
                  ((Image)b.image(type.uiIcon).size(32.0F).padRight(20.0F).update((i) -> i.setColor(b.isChecked() ? Color.gray : Color.white)).get()).act(1.0F);
                  b.margin(0.0F);
               }, Styles.fullTogglet, () -> {
                  if (!this.hidden.add(type)) {
                     this.hidden.remove(type);
                  }
                  this.used.clear();
                  this.used.addAll(usedCopy);
                  ObjectSet.ObjectSetIterator var3 = this.hidden.iterator();
                  while(var3.hasNext()) {
                     UnitType o = (UnitType)var3.next();
                     this.used.remove(o);
                  }
               }).update((b) -> b.setChecked(this.hidden.contains(type)));
            }
         }).scrollY(false);
         ObjectSet.ObjectSetIterator var11 = this.hidden.iterator();
         while(var11.hasNext()) {
            UnitType type = (UnitType)var11.next();
            this.used.remove(type);
         }
      }
      int nextStep(float value) {
         int order;
         for(order = 1; (float)order < value; order *= 10) {
            if ((float)(order * 2) > value) {
               return order * 2;
            }
            if ((float)(order * 5) > value) {
               return order * 5;
            }
            if ((float)(order * 10) > value) {
               return order * 10;
            }
         }
         return order;
      }
      public void addGroup(SpawnGroup group) {
         this.addChild(new SpawnGroupBar(group, this.children.size));
      }
      public void screenshot() {
         float scale = 0.6F;
         int w = (this.end + 1) * (int)this.tileW;
         int h = (MFEWaveInfoDialog.this.groups.size + 1) * (int)this.tileH;
         int memory = w * h / 1024 / 1024;
         if (Vars.checkScreenshotMemory && memory >= (Vars.mobile ? 120 : 240)) {
            Vars.ui.showInfo("@screenshot.invalid");
         } else {
            int pixw = (int)((float)w * scale);
            int pixh = (int)((float)h * scale);
            FrameBuffer buffer = new FrameBuffer(pixw, pixh);
            float cw = this.width;
            float ch = this.height;
            float cx = this.camera.x;
            float cy = this.camera.y;
            this.setSize((float)w, (float)h);
            this.camera.setZero();
            this.vec.setZero();
            this.start = 0;
            this.act(0.1F);
            buffer.begin();
            Draw.proj().setOrtho(0.0F, 0.0F, (float)w, (float)h);
            Styles.black.draw(0.0F, 0.0F, (float)w, (float)h);
            this.draw();
            Draw.flush();
            byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, pixw, pixh, true);
            buffer.end();
            this.setSize(cw, ch);
            this.camera.set(cx, cy);
            buffer.dispose();
            Threads.thread(() -> {
               for(int i = 0; i < lines.length; i += 4) {
                  lines[i + 3] = -1;
               }
               Pixmap fullPixmap = new Pixmap(pixw, pixh);
               Buffers.copy(lines, 0, fullPixmap.pixels, lines.length);
               Fi file = Vars.screenshotDirectory.child("screenshot-" + Time.millis() + ".png");
               PixmapIO.writePng(file, fullPixmap);
               fullPixmap.dispose();
               Core.app.post(() -> Vars.ui.showInfoFade(Core.bundle.format("screenshot", new Object[]{file.toString()})));
            });
         }
      }
      public class SpawnGroupBar extends Table {
         static float marginTop = 4.0F;
         SpawnGroup group;
         int index;
         boolean pressed;
         public SpawnGroupBar(final SpawnGroup group, int i) {
            this.group = group;
            this.index = i;
            this.margin(2.0F);
            this.background(Tex.whiteui);
            this.touchable = Touchable.enabled;
            this.setColor(MFEWaveInfoDialog.this.color(group.type));
            this.addCaptureListener(new InputListener() {
               float downx;
               float downy;
               public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                  this.downx = x;
                  this.downy = y;
                  SpawnGroupBar.this.pressed = true;
                  return true;
               }
               public void touchDragged(InputEvent event, float x, float y, int pointer) {
                  MFEWaveInfoDialog.this.buildConfig();
                  SpawnGroupBar.this.setTranslation(x - this.downx + SpawnGroupBar.this.translation.x, y - this.downy + SpawnGroupBar.this.translation.y);
                  super.touchDragged(event, x, y, pointer);
               }
               public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                  super.touchUp(event, x, y, pointer, button);
                  if (!WaveCanvas.this.pinching) {
                     int gap = group.end - group.begin;
                     float dx = x - this.downx + SpawnGroupBar.this.translation.x;
                     if (Math.abs(dx) > WaveCanvas.this.tileW) {
                        SpawnGroup var10000 = group;
                        var10000.begin += Mathf.round(dx / WaveCanvas.this.tileW);
                        if (group.end != Integer.MAX_VALUE) {
                           var10000 = group;
                           var10000.end += Mathf.round(dx / WaveCanvas.this.tileW);
                        }
                     }
                     group.begin = Math.max(group.begin, 0);
                     group.end = Math.max(group.end, gap);
                     float dy = y - this.downy + SpawnGroupBar.this.translation.y;
                     if (Math.abs(dy) > WaveCanvas.this.tileH) {
                        int newIndex = Mathf.clamp(SpawnGroupBar.this.index + Mathf.round(dy / WaveCanvas.this.tileH), 0, MFEWaveInfoDialog.this.groups.size - 1);
                        MFEWaveInfoDialog.this.groups.remove(group, true);
                        MFEWaveInfoDialog.this.groups.insert(newIndex, group);
                        MFEWaveInfoDialog.this.buildGroups();
                     }
                  }
                  SpawnGroupBar.this.pressed = false;
                  SpawnGroupBar.this.setTranslation(0.0F, 0.0F);
               }
            });
            this.clicked(() -> {
               if (!MFEWaveInfoDialog.this.batchEditing) {
                  if (!MFEWaveInfoDialog.this.selectedGroups.remove(group, true)) {
                     MFEWaveInfoDialog.this.selectedGroups.clear().add(group);
                  }
               } else if (!MFEWaveInfoDialog.this.selectedGroups.remove(group, true)) {
                  MFEWaveInfoDialog.this.selectedGroups.add(group);
               }
               MFEWaveInfoDialog.this.buildConfig();
            });
            this.label(() -> String.valueOf(group.begin + 1));
            this.table((t) -> {
               Image img = new Image(group.type.uiIcon);
               img.setScaling(Scaling.fit).setAlign(8);
               if (MFEWaveInfoDialog.this.detailedBar) {
                  Table lt = new Table((ls) -> {
                     ls.label(() -> "" + group.unitAmount).growX().row();
                     ls.label(() -> "+" + Strings.autoFixed(1.0F / group.unitScaling, 2)).fontScale(0.75F).growX();
                  });
                  this.stack(new Element[]{img, lt}).height(32.0F).maxWidth(64.0F);
                  img = new Image(Icon.defense);
                  img.setColor(Pal.shield);
                  img.setScaling(Scaling.fit).setAlign(8);
                  lt = new Table((ls) -> {
                     ls.label(() -> "" + UI.formatAmount((long)group.shields)).growX().labelAlign(16).row();
                     ls.label(() -> "+" + Strings.autoFixed(group.shieldScaling, 1)).growX().labelAlign(16).fontScale(0.75F);
                  });
                  this.stack(new Element[]{img, lt}).height(32.0F).maxWidth(64.0F);
               } else {
                  this.add(img);
               }
               if (group.effect != null) {
                  this.image(group.effect.uiIcon).size(32.0F).scaling(Scaling.fit);
               }
               if (group.items != null) {
                  Label label = new Label(String.valueOf(group.items.amount));
                  label.setAlignment(20);
                  label.setFillParent(true);
                  label.setFontScale(0.75F);
                  this.stack(new Element[]{(new Image(group.items.item.uiIcon)).setScaling(Scaling.fit), label}).size(32.0F);
               }
               this.label(() -> (group.payloads != null && !group.payloads.isEmpty() ? "\ue82a" : "") + (group.spawn == -1 ? "" : "\uf8ff"));
            }).grow();
            this.label(() -> String.valueOf(group.unitAmount)).style(Styles.outlineLabel).color(Color.white).padLeft(4.0F).padRight(4.0F);
            this.label(() -> group.end == Integer.MAX_VALUE ? "∞" : String.valueOf(group.end + 1));
         }
         public void act(float delta) {
            super.act(delta);
            if (MFEWaveInfoDialog.this.barFillX) {
               this.setSize(WaveCanvas.this.getWidth(), WaveCanvas.this.tileH);
               this.setPosition(WaveCanvas.this.tileX(WaveCanvas.this.start), -WaveCanvas.this.camera.y + WaveCanvas.this.tileH * (float)this.index + 16.0F);
            } else {
               this.setSize(Mathf.clamp(WaveCanvas.this.tileX(this.group.end + (this.group.end == Integer.MAX_VALUE ? 0 : 1)) - WaveCanvas.this.tileX(this.group.begin), this.getPrefWidth(), WaveCanvas.this.getWidth()), WaveCanvas.this.tileH - marginTop);
               this.setPosition(Math.min(WaveCanvas.this.tileX(this.group.begin), WaveCanvas.this.getWidth() - this.getWidth()), -WaveCanvas.this.camera.y + WaveCanvas.this.tileH * (float)this.index + 16.0F);
            }
         }
         protected void drawBackground(float x, float y) {
            boolean selected = MFEWaveInfoDialog.this.selectedGroups.contains(this.group, true);
            Tmp.c2.set(this.color);
            this.color.set(Color.white).a(selected ? 0.7F : 0.25F);
            super.drawBackground(x, y);
            this.color.set(Tmp.c2);
            Draw.color(selected ? Pal.accent : (this.pressed ? Color.royal : this.color));
            Draw.alpha(0.6F * this.parentAlpha);
            Fill.rect(Tmp.r1.set(WaveCanvas.this.tileX(this.group.begin), y, WaveCanvas.this.tileX(this.group.end) + WaveCanvas.this.tileW - WaveCanvas.this.tileX(this.group.begin), WaveCanvas.this.tileH - marginTop).move(0.0F, -this.translation.y));
            Draw.alpha(this.parentAlpha);
            for(int i = this.group.begin; i <= this.group.end && i <= WaveCanvas.this.end; i += this.group.spacing) {
               if (i >= WaveCanvas.this.start) {
                  Fill.rect(Tmp.r1.set(WaveCanvas.this.tileX(i), y, WaveCanvas.this.tileX(i + 1) - WaveCanvas.this.tileX(i), WaveCanvas.this.tileH - marginTop).move(0.0F, -this.translation.y));
               }
            }
            Draw.reset();
            this.color.a(1.0F);
         }
      }
   }
   public static enum Mode {
      counts,
      totals,
      health;
      static Mode[] all = values();
      private static Mode[] $values() {
         return new Mode[]{counts, totals, health};
      }
   }
   public static enum Sort {
      begin((g) -> (float)g.begin, (g) -> (float)g.type.id),
      end((g) -> (float)g.end, (g) -> (float)g.type.id),
      type((g) -> (float)g.type.id);
      static final Sort[] all = values();
      final Floatf<SpawnGroup> sort;
      final Floatf<SpawnGroup> secondary;
      private Sort(Floatf<SpawnGroup> sort) {
         this(sort, (g) -> (float)g.begin);
      }
      private Sort(Floatf<SpawnGroup> sort, Floatf<SpawnGroup> secondary) {
         this.sort = sort;
         this.secondary = secondary;
      }
      private static Sort[] $values() {
         return new Sort[]{begin, end, type};
      }
   }
}
