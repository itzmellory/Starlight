package mfe.guides;
import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.InputEvent;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.I18NBundle;
import arc.util.Interval;
import arc.util.Strings;
import arc.util.Tmp;
import mfe.MapFilterExt;
import mfe.math.Expression;
import mfe.math.ExpressionHandler;
import mindustry.Vars;
import mindustry.editor.MapEditor;
import mindustry.ui.Styles;
import mindustry.world.Tile;
public class ExpressionGuide extends BaseGuide implements ExpressionHandler {
   public Expression exp = new Expression("_MAIN");
   public Expression strokeexp = new Expression("_STROKE");
   protected transient boolean graphChanged = true;
   protected transient float drawStep = 0.5F;
   protected transient Interval timer = new Interval();
   protected transient Pixmap image;
   protected transient Texture texture;
   protected transient TextureRegion region;
   public boolean detailGraph = false;
   public boolean centerStroke = true;
   public boolean polar = false;
   protected static final IntSeq tilesSort = new IntSeq();
   protected Seq<Expression> exps = new Seq();
   public transient Seq<String> used = new Seq();
   public transient int valid;
   protected transient Expression varx = new Expression("x");
   protected transient Expression vary = new Expression("y");
   protected transient Table expsTable;
   protected transient Vec2 pcur = new Vec2();
   protected transient Vec2 pstk = new Vec2();
   protected transient Vec2 ptile = new Vec2();
   protected transient Vec2 pcps = new Vec2();
   protected transient Rect line = new Rect();
   public ExpressionGuide() {
      this.name = Core.bundle.get("guide.expression");
      this.expsTable = new Table();
      this.cfgButtons.button("\ue818", MapFilterExt.titleTogglet, () -> this.axis = !this.axis).checked(this.axis).name("axis");
      this.cfgButtons.button(this.detailGraph ? MapFilterExt.uiStep025 : MapFilterExt.uiStep05, Styles.flati, MapFilterExt.buttonSize, () -> {
      }).with((b) -> b.clicked(() -> {
            this.detailGraph = !this.detailGraph;
            this.drawStep = this.detailGraph ? 0.25F : 0.5F;
            b.getStyle().imageUp = this.detailGraph ? MapFilterExt.uiStep025 : MapFilterExt.uiStep05;
            this.graphChanged = true;
         })).name("detailStep");
      this.cfgButtons.button(this.centerStroke ? MapFilterExt.uiStrokeCenter : MapFilterExt.uiStrokeAdd, Styles.flati, MapFilterExt.buttonSize, () -> {
      }).with((b) -> b.clicked(() -> {
            this.centerStroke = !this.centerStroke;
            b.getStyle().imageUp = this.centerStroke ? MapFilterExt.uiStrokeCenter : MapFilterExt.uiStrokeAdd;
            this.graphChanged = true;
         })).name("centerStroke");
      this.cfgButtons.image().growY().width(2.0F);
      this.cfgButtons.button(this.polar ? MapFilterExt.uiCoordsysPolar : MapFilterExt.uiCoordsysRect, Styles.flati, MapFilterExt.buttonSize, () -> {
      }).with((b) -> b.clicked(() -> {
            this.polar = !this.polar;
            b.getStyle().imageUp = this.polar ? MapFilterExt.uiCoordsysPolar : MapFilterExt.uiCoordsysRect;
            this.graphChanged = true;
         })).name("polar");
      this.cfgButtons.image().growY().width(2.0F);
      this.cfgButtons.button("\ue84c", Styles.flatt, () -> Vars.ui.showConfirm("Fill with " + Vars.editor.drawBlock.localizedName + " ?", this::fill)).name("fill");
   }
   public void drawAxis() {
      if (this.polar) {
         Draw.color(Color.black, 0.8F);
         Lines.stroke(8.0F);
         Vec2 xpos = Tmp.v1.set(1.5F * getW(), 0.0F).rotate(this.rotDegree);
         Vec2 xneg = Tmp.v2.set(Tmp.v1).rotate90(0).rotate90(0);
         Vec2 o = Tmp.v3.set(iposx() + xt2i(this.off.x + 0.5F), iposy() + yt2i(this.off.y + 0.5F));
         Lines.line(o.x + xneg.x, o.y + xneg.y, o.x + xpos.x, o.y + xpos.y);
         xpos.rotate90(0).limit(yt2i(10.0F));
         xneg.rotate90(0).limit(yt2i(10.0F));
         Lines.line(o.x + xneg.x, o.y + xneg.y, o.x + xpos.x, o.y + xpos.y);
         for(int i = 1; (float)i < getIH() / 50.0F; ++i) {
            Lines.circle(o.x, o.y, xt2i((float)i * 50.0F));
         }
      } else {
         super.drawAxis();
      }
   }
   public void draw() {
      if (this.axis) {
         this.drawAxis();
      }
      this.updGraph();
      Draw.color(this.color);
      Draw.rect(this.region, iposx() + getW() / 2.0F + xt2i(0.5F - this.drawStep / 2.0F), iposy() + getH() / 2.0F + yt2i(0.5F - this.drawStep / 2.0F), getW(), getH());
   }
   public void fill() {
      tilesSort.clear();
      this.consTiles((p) -> tilesSort.add(Point2.pack((int)p.x, (int)p.y)), 1.0F);
      if (Vars.editor.getClass() == MapEditor.class) {
         tilesSort.each((pos) -> Vars.editor.drawBlocks(Point2.x(pos), Point2.y(pos), false, Vars.editor.drawBlock.isOverlay(), (t) -> true));
      } else {
         Vars.ui.showCustomConfirm("", Core.bundle.format("warning.fill.novanillaeditor", new Object[]{Vars.editor.getClass().getName()}), "@yes", "@no", () -> tilesSort.each((pos) -> Vars.editor.drawBlocks(Point2.x(pos), Point2.y(pos), false, Vars.editor.drawBlock.isOverlay(), (t) -> true)), () -> tilesSort.each((pos) -> {
               Tile tile = Vars.editor.tile(Point2.x(pos), Point2.y(pos));
               if (Vars.editor.drawBlock.isOverlay()) {
                  tile.setOverlay(Vars.editor.drawBlock.asFloor());
               } else if (Vars.editor.drawBlock.isFloor()) {
                  tile.setFloor(Vars.editor.drawBlock.asFloor());
               } else if (!tile.block().isMultiblock() || Vars.editor.drawBlock.isMultiblock()) {
                  tile.setBlock(Vars.editor.drawBlock, Vars.editor.drawTeam, Vars.editor.rotation);
               }
            }));
      }
      Vars.editor.flushOp();
   }
   public void consTiles(Cons<Vec2> cons, float step) {
      if (this.valid == 1) {
         for(float x = 0.0F; x < getIW(); x += step) {
            for(float y = 0.0F; y < getIH(); y += step) {
               this.projt2g(this.ptile.set(x, y));
               if (this.polar) {
                  this.varx.parse(Mathf.mod(this.ptile.angleRad() + ((float)Math.PI * 2F), ((float)Math.PI * 2F)));
                  this.pcur.set(this.exp.get(), 0.0F).rotateRad(this.varx.get());
                  this.pstk.set(this.strokeexp.get(), 0.0F);
                  if (Mathf.zero(this.pstk.x)) {
                     continue;
                  }
                  this.pstk.rotateRad(this.varx.get());
                  if (this.centerStroke) {
                     this.pcur.mulAdd(this.pstk, -0.5F);
                  }
                  this.pstk.add(this.pcur);
               } else {
                  this.varx.parse(this.ptile.x);
                  this.pcur.set(this.ptile.x, this.exp.get());
                  this.pstk.set(this.ptile.x, this.strokeexp.get());
                  if (Mathf.zero(this.pstk.y)) {
                     continue;
                  }
                  if (this.centerStroke) {
                     this.pcur.sub(0.0F, this.pstk.y / 2.0F);
                  }
                  Vec2 var10000 = this.pstk;
                  var10000.y += this.pcur.y;
               }
               float agl = this.pcps.set(this.pstk).sub(this.pcur).angle();
               this.pcps.rotate(-(agl - 90.0F));
               Tmp.v1.set(this.ptile).sub(this.pcur).rotate(-(agl - 90.0F));
               if (this.line.set(-step / 2.0F, -step / 2.0F, step, this.pcps.len() + step).contains(Tmp.v1)) {
                  cons.get(this.ptile.set(x, y));
               }
            }
         }
      }
   }
   public void buildTransConfigure(Table table) {
      super.buildTransConfigure(table);
      table.row();
      table.collapser((t) -> t.label(() -> {
            StringBuilder var10000 = (new StringBuilder()).append('⚠');
            I18NBundle var10001 = Core.bundle;
            String var10002;
            switch (this.valid) {
               case -1:
                  var10002 = "validstats.circularrefer";
                  break;
               case 0:
                  var10002 = "validstats.invalidexp";
                  break;
               case 1:
                  var10002 = "validstats.valid";
                  break;
               default:
                  var10002 = "validstats.unknown";
            }
            return var10000.append(var10001.get(var10002)).toString();
         }).color(Color.scarlet), () -> this.valid != 1).colspan(4);
   }
   public void buildContent(Table table) {
      table.table((tline) -> {
         tline.add("f(x)=");
         if (this.exp.expression.length() == 0) {
            this.exp.parse("x", this);
         }
         tline.field(this.exp.expression, (s) -> {
            this.exp.parse(s, this);
            this.onExpressionUpdate(this.expsTable);
         }).update((f) -> f.color.set(this.exp.valid ? Color.white : Color.scarlet)).growX();
         tline.row();
         tline.add();
         tline.label(() -> this.exp.expression).height(0.1F).color(Color.clear).padLeft(50.0F).maxWidth(200.0F);
      }).left();
      table.row();
      table.table((tfill) -> {
         tfill.add("s(x)=");
         if (this.strokeexp.expression.length() == 0) {
            this.strokeexp.parse("4", this);
         }
         tfill.field(this.strokeexp.expression, (s) -> {
            this.strokeexp.parse(s, this);
            this.onExpressionUpdate(this.expsTable);
         }).update((f) -> f.color.set(this.strokeexp.valid ? Color.white : Color.scarlet)).growX();
         tfill.row();
         tfill.add();
         tfill.label(() -> this.strokeexp.expression).height(0.1F).color(Color.clear).padLeft(50.0F).maxWidth(200.0F);
      }).left();
      table.row();
      this.onExpressionUpdate(this.expsTable);
      table.add(this.expsTable).fill();
   }
   public void onExpressionUpdate(Table t) {
      this.valid = this.reParse();
      this.graphChanged = true;
      this.exps.removeAll((e) -> this.exp.valid && this.strokeexp.valid && !this.exp.expNames.contains(e.name) && !this.strokeexp.expNames.contains(e.name) && !this.exps.contains((e2) -> e2.expNames.contains(e.name)));
      t.getChildren().each((e) -> {
         if (e.name == null || !this.exps.contains((exp) -> exp.name.equals(e.name))) {
            e.remove();
         }
      });
      this.exps.each((exp) -> {
         if (t.find(exp.name) == null) {
            t.table((tt) -> this.addDragableExpressionInput(exp, (Label)tt.add(exp.name + "=").right().get(), (TextField)tt.add(new TextField()).growX().get())).name(exp.name).growX();
            t.row();
         }
      });
   }
   public void updGraph() {
      if (this.graphChanged || this.transChanged) {
         if (this.timer.get(10.0F)) {
            this.graphChanged = false;
            this.transChanged = false;
            if (this.image == null) {
               this.image = new Pixmap((int)(getIW() / this.drawStep), (int)(getIH() / this.drawStep));
            }
            if (this.image.width != (int)(getIW() / this.drawStep) || this.image.height != (int)(getIH() / this.drawStep)) {
               this.image.dispose();
               this.image = new Pixmap((int)(getIW() / this.drawStep), (int)(getIH() / this.drawStep));
            }
            this.image.fill(Color.clear);
            this.consTiles((p) -> this.image.set((int)(p.x / this.drawStep), this.image.height - (int)(p.y / this.drawStep) - 1, Color.whiteRgba), this.drawStep);
            if (this.texture != null) {
               this.texture.dispose();
            }
            this.texture = new Texture(this.image);
            this.region = new TextureRegion(this.texture);
         }
      }
   }
   public void onRemove() {
      if (this.image != null) {
         this.image.dispose();
      }
      if (this.texture != null) {
         this.texture.dispose();
      }
   }
   public void onResize() {
      this.graphChanged = true;
   }
   public Expression getExpression(String name) {
      if (name.equals("x")) {
         return this.varx;
      } else if (name.equals("y")) {
         return this.vary;
      } else {
         Expression var = (Expression)this.exps.find((v) -> v.name.equals(name));
         if (var == null) {
            this.exps.add(var = new Expression(name));
            var.parse(1.0F);
         }
         return var;
      }
   }
   public int reParse() {
      this.exps.each((e) -> e.reParse(this));
      this.exp.reParse(this);
      this.strokeexp.reParse(this);
      if (this.exps.count((exp) -> !exp.valid) <= 0 && this.exp.valid && this.strokeexp.valid) {
         this.used.clear();
         for(int i = 0; i < 1000 && this.exps.count((e) -> {
            if (!this.used.contains(e.name) && e.expNames.count((e2) -> !e2.equals(this.varx.name) && !e2.equals(this.vary.name) && !this.used.contains(e2)) == 0) {
               this.used.add(e.name);
               return true;
            } else {
               return false;
            }
         }) != 0; ++i) {
         }
         return this.used.size == this.exps.size ? 1 : -1;
      } else {
         return 0;
      }
   }
   public void addDragableExpressionInput(final Expression e, Label l, final TextField f) {
      f.setText(e.expression);
      f.changed(() -> {
         if (Strings.canParseFloat(f.getText())) {
            e.parse(Strings.parseFloat(f.getText()));
            this.graphChanged = true;
         } else {
            e.parse(f.getText(), this);
            this.onExpressionUpdate(this.expsTable);
         }
      });
      f.update(() -> f.color.set(e.valid ? Color.white : Color.scarlet));
      l.update(() -> l.setColor(e.isVar() ? Color.cyan : Color.acid));
      l.addListener(new ElementGestureListener() {
         public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
            super.pan(event, x, y, deltaX, deltaY);
            if (e.isVar()) {
               GuideSeqImage.guidesImage.cfgPop.cancelScroll();
               e.parse((float)Mathf.floor(e.get() + deltaX / 2.0F * MapFilterExt.step));
               f.setText(String.valueOf(e.get()));
               ExpressionGuide.this.graphChanged = true;
            }
         }
         public void tap(InputEvent event, float x, float y, int count, KeyCode button) {
            super.tap(event, x, y, count, button);
            if (e.isVar()) {
               if (count >= 2) {
                  e.parse(0.0F);
                  f.setText("0");
               }
               ExpressionGuide.this.graphChanged = true;
            }
         }
      });
   }
   }
