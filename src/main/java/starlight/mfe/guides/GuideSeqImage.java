package mfe.guides;
import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import java.util.Objects;
import mfe.MapFilterExt;
import mindustry.Vars;
import mindustry.editor.MapView;
import mindustry.io.JsonIO;
import mindustry.ui.GridImage;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mt.ui.MCollapser;
import mt.ui.MPopup;
import mt.ui.UIUtils;
import mt.utils.RefUtils;
public class GuideSeqImage extends GridImage {
   public static GuideSeqImage guidesImage = new GuideSeqImage(0, 0);
   public Seq<BaseGuide> guides = new Seq(true);
   public GuideListPop cfgPop = new GuideListPop();
   public int imageWidth;
   public int imageHeight;
   public GuideSeqImage(int w, int h) {
      super(w, h);
      this.imageHeight = h;
      this.imageWidth = w;
   }
   public void setImageSize(int w, int h) {
      super.setImageSize(w, h);
      if (this.imageWidth != w || this.imageHeight != h) {
         this.imageHeight = h;
         this.imageWidth = w;
         this.guides.each((g) -> g.onResize());
      }
   }
   public void draw() {
      BaseGuide.updViewRect();
      this.guides.each((g) -> {
         if (g.enable) {
            g.draw();
         }
      });
   }
   public void clearGuides() {
      this.guides.each(BaseGuide::onRemove);
      this.guides.clear();
   }
   public class GuideListPop extends MPopup {
      public BaseDialog selectDialog = new BaseDialog("@guide.addgraph");
      protected ScrollPane paneList;
      public boolean needsRebuild = true;
      public boolean minimize = false;
      public Table list = new Table();
      public GuideListPop() {
         this.buildSelect();
         this.build();
         this.rebuildList();
      }
      public void inject() {
         MapView view = Vars.ui.editor.getView();
         RefUtils.setValue(view, "image", GuideSeqImage.guidesImage);
         view.parent.addChild(this);
         this.setPositionInScreen(0.0F, 0.0F);
         this.shown = true;
         this.rebuildList();
      }
      public void build() {
         this.clear();
         this.table((tit) -> {
            tit.setBackground(Styles.black3);
            tit.margin(2.0F);
            tit.add(" \ue818 MFE Graph").with(this::addDragPopupListener).height(MapFilterExt.buttonSize).growX();
            tit.label(() -> "Mem:" + (int)(Core.app.getJavaHeap() / 1024L / 1024L)).color(Tmp.c1.set(Color.green).a(0.7F)).style(Styles.outlineLabel).width(0.5F).fontScale(0.7F).labelAlign(20).fill();
            tit.button((b) -> b.label(() -> (this.minimize ? '\ue824' : '\ue826') + ""), Styles.cleart, () -> this.minimize = !this.minimize).size(MapFilterExt.buttonSize);
         }).growX();
         this.row();
         this.add((new MCollapser((t) -> {
            t.setBackground(Styles.black3);
            t.table((tit) -> {
               tit.margin(2.0F);
               tit.button("\ue813", Styles.cleart, () -> this.selectDialog.show()).size(MapFilterExt.buttonSize);
               tit.button("\ue874", Styles.cleart, () -> Core.app.setClipboardText(JsonIO.write(GuideSeqImage.this.guides))).size(MapFilterExt.buttonSize).with((tb) -> UIUtils.tooltip(tb, "@guide.exportclipboard"));
               tit.button("\ue852", Styles.cleart, () -> {
                  try {
                     GuideSeqImage.this.clearGuides();
                     GuideSeqImage.this.guides.set((Seq)JsonIO.read(Seq.class, Core.app.getClipboardText()));
                     this.setNeedsRebuild();
                  } catch (Throwable e) {
                     Vars.ui.showException(e);
                     Log.err(e);
                  }
               }).size(MapFilterExt.buttonSize).with((tb) -> UIUtils.tooltip(tb, "@guide.importclipboardclean"));
               tit.button("\ue852", Styles.cleart, () -> {
                  try {
                     GuideSeqImage.this.guides.add((Seq)JsonIO.read(Seq.class, Core.app.getClipboardText()));
                     this.setNeedsRebuild();
                  } catch (Throwable e) {
                     Vars.ui.showException(e);
                     Log.err(e);
                  }
               }).size(MapFilterExt.buttonSize).with((tb) -> tb.add("+").color(Color.green).style(Styles.outlineLabel).width(0.1F).with((l) -> {
                     l.setAlignment(20);
                     l.setFontScale(0.75F);
                  }).fillY()).with((tb) -> UIUtils.tooltip(tb, "@guide.importclipboardtail"));
               tit.button("\ue86f", Styles.cleart, () -> Vars.ui.showConfirm("@guide.cleanconfirm", () -> {
                     GuideSeqImage.this.clearGuides();
                     this.setNeedsRebuild();
                  })).size(MapFilterExt.buttonSize).with((tb) -> tb.getLabel().setColor(Color.scarlet));
               tit.image().color(Tmp.c1.set(Color.gray).a(0.5F)).growY().width(2.0F).pad(0.0F, 8.0F, 0.0F, 8.0F);
               tit.button("\ue811", Styles.cleart, () -> GuideSchematics.schematicsDialog.show()).size(MapFilterExt.buttonSize).with((tb) -> UIUtils.tooltip(tb, "@guide.schematics"));
               tit.image().color(Tmp.c1.set(Color.gray).a(0.5F)).growY().width(2.0F).pad(0.0F, 8.0F, 0.0F, 8.0F);
               tit.button("\uf0f6", Styles.cleart, () -> GuideSeqImage.guidesImage.guides.set((Seq)JsonIO.read(Seq.class, (String)Vars.editor.tags.get("MFE.Graphs")))).size(MapFilterExt.buttonSize).with((tb) -> UIUtils.tooltip(tb, "@guide.maptag.read")).disabled((tb) -> !Vars.editor.tags.containsKey("MFE.Graphs"));
               tit.button("\ue81b", Styles.cleart, () -> Vars.editor.tags.put("MFE.Graphs", JsonIO.write(GuideSeqImage.guidesImage.guides))).size(MapFilterExt.buttonSize).with((tb) -> UIUtils.tooltip(tb, "@guide.maptag.write"));
               tit.button("\ue86f", Styles.cleart, () -> Vars.editor.tags.remove("MFE.Graphs")).size(MapFilterExt.buttonSize).with((tb) -> UIUtils.tooltip(tb, "@guide.maptag.delete")).disabled((tb) -> !Vars.editor.tags.containsKey("MFE.Graphs"));
            }).left();
            t.row();
            t.pane(this.list).with((p) -> {
               this.paneList = p;
               p.setScrollingDisabled(false, false);
               p.exited(() -> {
                  if (p.hasScroll()) {
                     Core.scene.setScrollFocus(Vars.ui.editor.getView());
                  }
               });
            }).growX().left();
         }, this.minimize)).setCollapsed(true, () -> this.minimize).setDirection(true, true)).maxSize(400.0F, 500.0F).growX();
      }
      public void setNeedsRebuild() {
         this.needsRebuild = true;
      }
      public void cancelScroll() {
         if (this.paneList != null) {
            this.paneList.cancel();
         }
      }
      public void act(float delta) {
         if (this.needsRebuild) {
            this.rebuildList();
         }
         this.keepInScreen();
         super.act(delta);
      }
      public void rebuildList() {
         this.list.clear();
         this.list.defaults().pad(2.0F).padBottom(4.0F);
         for(BaseGuide guide : GuideSeqImage.this.guides) {
            Table var10000 = this.list;
            Objects.requireNonNull(guide);
            var10000.table(guide::buildConfigure).growX();
            this.list.row();
         }
         this.needsRebuild = false;
      }
      public void buildSelect() {
         this.selectDialog.addCloseButton();
         this.selectDialog.cont.pane((t) -> {
            t.background(Styles.grayPanel);
            t.defaults().width(500.0F).minHeight(100.0F).top();
            t.button("@guide.vanilla", Styles.flatBordert, () -> {
               GuideSeqImage.this.guides.add(new VanillaGridGuide());
               this.selectDialog.hide();
               this.setNeedsRebuild();
            });
            t.button("@guide.expression", Styles.flatBordert, () -> {
               GuideSeqImage.this.guides.add(new ExpressionGuide());
               this.selectDialog.hide();
               this.setNeedsRebuild();
            });
            t.button("@guide.curve", Styles.flatBordert, () -> {
               GuideSeqImage.this.guides.add(new CurveGuide());
               this.selectDialog.hide();
               this.setNeedsRebuild();
            });
            t.row();
            t.labelWrap("@guide.vanilla.info").fill();
            t.labelWrap("@guide.expression.info").fill();
            t.labelWrap("@guide.curve.info").fill();
         }).growY();
      }
   }
               }
