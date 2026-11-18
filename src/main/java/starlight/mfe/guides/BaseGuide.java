package mfe.guides;
import arc.func.Floatc;
import arc.func.Floatp;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import mfe.MapFilterExt;
import mindustry.Vars;
import mindustry.editor.MapView;
import mindustry.ui.Styles;
import mt.ui.MCollapser;
public class BaseGuide {
   public static final Rect drawRect = new Rect();
   public static final Rect tileRect = new Rect();
   public String name = "";
   public Color color;
   public Vec2 off;
   public float rotDegree;
   public boolean enable;
   public boolean axis;
   public transient Table cfgButtons;
   public transient boolean minimized;
   public transient boolean transChanged;
   public BaseGuide() {
      this.color = (new Color(Color.salmon)).a(0.5F);
      this.off = new Vec2();
      this.rotDegree = 0.0F;
      this.enable = true;
      this.axis = false;
      this.cfgButtons = new Table();
      this.minimized = false;
      this.name = "BaseDebug";
      this.cfgButtons.defaults().size(MapFilterExt.buttonSize).pad(2.0F);
   }
   public void draw() {
   }
   public void drawAxis() {
      Draw.color(Color.black, 0.8F);
      Lines.stroke(8.0F);
      Vec2 xpos = Tmp.v1.set(1.5F * getW(), 0.0F).rotate(this.rotDegree);
      Vec2 xneg = Tmp.v2.set(Tmp.v1).rotate90(0).rotate90(0);
      Vec2 o = Tmp.v3.set(iposx() + xt2i(this.off.x + 0.5F), iposy() + yt2i(this.off.y + 0.5F));
      Lines.line(o.x + xneg.x, o.y + xneg.y, o.x + xpos.x, o.y + xpos.y);
      xpos.rotate90(0);
      xneg.rotate90(0);
      Lines.line(o.x + xneg.x, o.y + xneg.y, o.x + xpos.x, o.y + xpos.y);
   }
   public void buildConfigure(Table table) {
      table.clear();
      table.background(Styles.flatOver).defaults().pad(2.0F);
      table.image().fillX().color(Color.white).height(2.0F);
      table.row();
      table.table((title) -> {
         title.background(Styles.black).defaults().pad(2.0F).height(MapFilterExt.buttonSize);
         title.button("\ue804", Styles.flatt, () -> {
            int self = GuideSeqImage.guidesImage.guides.indexOf(this);
            int tgt = Mathf.clamp(self - 1, 0, GuideSeqImage.guidesImage.guides.size - 1);
            GuideSeqImage.guidesImage.guides.swap(self, tgt);
            GuideSeqImage.guidesImage.cfgPop.setNeedsRebuild();
         }).size(MapFilterExt.buttonSize);
         title.button("\ue805", Styles.flatt, () -> {
            int self = GuideSeqImage.guidesImage.guides.indexOf(this);
            int tgt = Mathf.clamp(self + 1, 0, GuideSeqImage.guidesImage.guides.size - 1);
            GuideSeqImage.guidesImage.guides.swap(self, tgt);
            GuideSeqImage.guidesImage.cfgPop.setNeedsRebuild();
         }).size(MapFilterExt.buttonSize);
         title.button((b) -> b.label(() -> (this.minimized ? '\ue824' : '\ue826') + ""), Styles.flatt, () -> this.minimized = !this.minimized).size(MapFilterExt.buttonSize);
         title.button((b) -> b.image().size(MapFilterExt.buttonSize).update((i) -> i.setColor(this.color)), Styles.flati, () -> Vars.ui.picker.show(this.color, true, (c) -> this.color.set(c))).size(MapFilterExt.buttonSize);
         ((TextButton)title.button("\ue815", Styles.flatt, () -> Vars.ui.showConfirm("Delete " + this.name + " ?", () -> {
               this.onRemove();
               GuideSeqImage.guidesImage.guides.remove(this);
               GuideSeqImage.guidesImage.cfgPop.setNeedsRebuild();
            })).size(MapFilterExt.buttonSize).get()).getLabel().setColor(Color.scarlet);
         title.button("\ue816", Styles.flatt, () -> Vars.ui.showTextInput("", "New Name: ", this.name, (str) -> this.name = str)).size(MapFilterExt.buttonSize);
         title.button((b) -> b.label(() -> (this.enable ? "[accent]\ue88e" : "[gray]\ue88f") + this.name).growX().labelAlign(8), Styles.cleart, () -> this.enable = !this.enable).grow().minWidth(50.0F);
      }).growX();
      table.row();
      table.add((new MCollapser((t) -> {
         t.background(Styles.black5);
         t.add(this.cfgButtons).left();
         t.row();
         t.table((tt) -> this.buildTransConfigure(tt)).left();
         t.row();
         ((Table)t.table(this::buildContent).growX().get()).background(Styles.black6);
      }, this.minimized)).setCollapsed(true, () -> this.minimized).setDirection(true, true)).growX();
   }
   public void buildTransConfigure(Table table) {
      table.background(Styles.black3);
      MapFilterExt.addDragableFloatInput.get((Floatc)(x) -> {
         this.off.x = x;
         this.transChanged = true;
      }, (Floatp)() -> this.off.x, (Label)table.add("\ue818dx").get(), (TextField)table.add(new TextField()).size(80.0F, 18.0F).get());
      MapFilterExt.addDragableFloatInput.get((Floatc)(y) -> {
         this.off.y = y;
         this.transChanged = true;
      }, (Floatp)() -> this.off.y, (Label)table.add("\ue818dy").get(), (TextField)table.add(new TextField()).size(80.0F, 18.0F).get());
      MapFilterExt.addDragableFloatInput.get((Floatc)(r) -> {
         this.rotDegree = r;
         this.transChanged = true;
      }, (Floatp)() -> this.rotDegree, (Label)table.add("\ue823rot").get(), (TextField)table.add(new TextField()).size(80.0F, 18.0F).get());
   }
   public void buildContent(Table table) {
   }
   public void onRemove() {
   }
   public Vec2 projt2g(Vec2 p) {
      return p.sub(this.off).rotate(-this.rotDegree);
   }
   public Vec2 projg2t(Vec2 p) {
      return p.rotate(this.rotDegree).add(this.off);
   }
   public static float xi2t(float x) {
      return x * getIW() / getW();
   }
   public static float yi2t(float y) {
      return y * getIH() / getH();
   }
   public static float xt2i(float x) {
      return x / getIW() * getW();
   }
   public static float yt2i(float y) {
      return y / getIH() * getH();
   }
   public static float getW() {
      return GuideSeqImage.guidesImage.getWidth();
   }
   public static float getH() {
      return GuideSeqImage.guidesImage.getHeight();
   }
   public static float getIW() {
      return (float)GuideSeqImage.guidesImage.imageWidth;
   }
   public static float getIH() {
      return (float)GuideSeqImage.guidesImage.imageHeight;
   }
   public static void updViewRect() {
      MapView view = Vars.ui.editor.getView();
      drawRect.set(Math.max(iposx(), view.x), Math.max(iposy(), view.y), Math.min(getW(), Math.min(view.getWidth() - iposx() + view.x, getW() + iposx() - view.x)), Math.min(getH(), Math.min(view.getHeight() - iposy() + view.y, getH() + iposy() - view.y)));
      tileRect.set(Math.max(xi2t(-iposx() + view.x), 0.0F), Math.max(yi2t(-iposy() + view.y), 0.0F), xi2t(drawRect.width), yi2t(drawRect.height));
   }
   public static float iposx() {
      return GuideSeqImage.guidesImage.x;
   }
   public static float iposy() {
      return GuideSeqImage.guidesImage.y;
   }
   public void onResize() {
   }
                                   }
