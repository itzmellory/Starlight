package mfe.guides;
import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
public class CurveGuide extends ExpressionGuide {
   public CurveGuide() {
      this.name = Core.bundle.get("guide.curve");
   }
   public void buildContent(Table table) {
      table.table((tline) -> {
         tline.add("C(x,y): ");
         if (this.exp.expression.length() == 0) {
            this.exp.parse("x - y", this);
         }
         tline.field(this.exp.expression, (s) -> {
            this.exp.parse(s, this);
            this.onExpressionUpdate(this.expsTable);
         }).update((f) -> f.color.set(this.exp.valid ? Color.white : Color.scarlet)).growX();
         tline.label(() -> this.centerStroke ? "@guide.curve.neighborhood.center" : "@guide.curve.neighborhood.add");
         tline.row();
         tline.add();
         tline.label(() -> this.exp.expression).height(0.1F).color(Color.clear).padLeft(50.0F);
      }).left();
      table.row();
      table.table((tfill) -> {
         tfill.add("d(x,y)=");
         if (this.strokeexp.expression.length() == 0) {
            this.strokeexp.parse("1", this);
         }
         tfill.field(this.strokeexp.expression, (s) -> {
            this.strokeexp.parse(s, this);
            this.onExpressionUpdate(this.expsTable);
         }).update((f) -> f.color.set(this.strokeexp.valid ? Color.white : Color.scarlet)).growX();
         tfill.row();
         tfill.add();
         tfill.label(() -> this.strokeexp.expression).height(0.1F).color(Color.clear).padLeft(50.0F);
      }).left();
      table.row();
      this.onExpressionUpdate(this.expsTable);
      table.add(this.expsTable).fill();
   }
   public void consTiles(Cons<Vec2> cons, float step) {
      if (!this.exp.valid) {
         this.exp.parse(this.exp.expression, this);
      }
      if (!this.strokeexp.valid) {
         this.strokeexp.parse(this.strokeexp.expression, this);
      }
      if (this.exp.valid && this.strokeexp.valid) {
         for(float x = 0.0F; x < getIW(); x += step) {
            for(float y = 0.0F; y < getIH(); y += step) {
               this.projt2g(this.ptile.set(x, y));
               if (this.polar) {
                  this.varx.parse(Mathf.mod(this.ptile.angleRad() + ((float)Math.PI * 2F), ((float)Math.PI * 2F)));
                  this.vary.parse(this.ptile.len());
               } else {
                  this.varx.parse(this.ptile.x);
                  this.vary.parse(this.ptile.y);
               }
               float dz = this.strokeexp.get();
               if (!Mathf.zero(dz)) {
                  float cz = this.exp.get();
                  if (this.centerStroke && cz <= dz && cz >= -dz) {
                     cons.get(this.pcur.set(x, y));
                  } else if (!this.centerStroke && cz >= 0.0F && cz <= dz) {
                     cons.get(this.pcur.set(x, y));
                  }
               }
            }
         }
      }
   }
}
