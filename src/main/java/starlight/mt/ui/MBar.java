package mt.ui;
import arc.Core;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.ui.layout.Scl;
import arc.util.pooling.Pools;
import mindustry.ui.Fonts;
public class MBar extends Element {
   private boolean vertical = false;
   private Floatp fraction;
   private CharSequence name = "";
   private float value;
   private float lastValue;
   private float blink;
   private float outlineRadius;
   private float fontScaleX = 1.0F;
   private float fontScaleY = 1.0F;
   private final Color blinkColor = new Color();
   private final Color outlineColor = new Color();
   public MBar(String name, Color color, Floatp fraction) {
      this.fraction = fraction;
      this.name = Core.bundle.get(name, name);
      this.blinkColor.set(color);
      this.lastValue = this.value = fraction.get();
      this.setColor(color);
   }
   public MBar(Prov<String> name, Prov<Color> color, Floatp fraction) {
      this.fraction = fraction;
      this.blinkColor.set((Color)color.get());
      try {
         this.lastValue = this.value = Mathf.clamp(fraction.get());
      } catch (Exception var5) {
         this.lastValue = this.value = 0.0F;
      }
      this.update(() -> {
         try {
            this.name = (CharSequence)name.get();
            this.setColor((Color)color.get());
         } catch (Exception var4) {
            this.name = "";
         }
      });
   }
   public MBar() {
   }
   public void reset(float value) {
      this.value = this.lastValue = this.blink = value;
   }
   public void set(Prov<String> name, Floatp fraction, Color color) {
      this.fraction = fraction;
      this.lastValue = fraction.get();
      this.blinkColor.set(color);
      this.setColor(color);
      this.update(() -> this.name = (CharSequence)name.get());
   }
   public void set(Prov<String> name, Floatp fraction, Prov<Color> color) {
      this.fraction = fraction;
      this.lastValue = fraction.get();
      this.blinkColor.set((Color)color.get());
      this.update(() -> {
         this.name = (CharSequence)name.get();
         this.setColor((Color)color.get());
      });
   }
   public MBar setVertical(boolean v) {
      this.vertical = v;
      return this;
   }
   public void snap() {
      this.lastValue = this.value = this.fraction.get();
   }
   public MBar outline(Color color, float stroke) {
      this.outlineColor.set(color);
      this.outlineRadius = Scl.scl(stroke);
      return this;
   }
   public void flash() {
      this.blink = 1.0F;
   }
   public MBar blink(Color color) {
      this.blinkColor.set(color);
      return this;
   }
   public void draw() {
      if (this.fraction != null) {
         float computed;
         try {
            computed = Mathf.clamp(this.fraction.get());
         } catch (Exception var8) {
            computed = 0.0F;
         }
         if (this.lastValue > computed) {
            this.blink = 1.0F;
         }
         this.lastValue = computed;
         if (Float.isNaN(this.lastValue)) {
            this.lastValue = 0.0F;
         }
         if (Float.isInfinite(this.lastValue)) {
            this.lastValue = 1.0F;
         }
         if (Float.isNaN(this.value)) {
            this.value = 0.0F;
         }
         if (Float.isInfinite(this.value)) {
            this.value = 1.0F;
         }
         if (Float.isNaN(computed)) {
            computed = 0.0F;
         }
         if (Float.isInfinite(computed)) {
            computed = 1.0F;
         }
         this.blink = Mathf.lerpDelta(this.blink, 0.0F, 0.2F);
         this.value = Mathf.lerpDelta(this.value, computed, 0.15F);
         float innerWidth = this.width - 2.0F * this.outlineRadius;
         float innerHeight = this.height - 2.0F * this.outlineRadius;
         Draw.color(this.outlineColor);
         Draw.alpha(this.parentAlpha);
         Fill.rect(this.centerX(), this.centerY(), this.width, this.height);
         Draw.color(Color.black);
         Draw.alpha(this.parentAlpha);
         Fill.rect(this.centerX(), this.centerY(), innerWidth, innerHeight);
         if (!this.vertical) {
            Draw.color(this.color, this.blinkColor, this.blink);
            Draw.alpha(this.parentAlpha);
            float topWidth = innerWidth * this.value;
            Fill.rect(this.centerX() - innerWidth / 2.0F + topWidth / 2.0F, this.centerY(), topWidth, innerHeight);
         } else {
            Draw.color(this.color, this.blinkColor, this.blink);
            Draw.alpha(this.parentAlpha);
            float topHeight = innerHeight * this.value;
            Fill.rect(this.centerX(), this.centerY() - innerHeight / 2.0F + topHeight / 2.0F, innerWidth, topHeight);
         }
         Draw.color();
         Font font = Fonts.outline;
         float oldScaleX = font.getScaleX();
         float oldScaleY = font.getScaleY();
         font.getData().setScale(this.fontScaleX, this.fontScaleY);
         GlyphLayout lay = (GlyphLayout)Pools.obtain(GlyphLayout.class, GlyphLayout::new);
         lay.setText(font, this.name);
         font.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         font.getCache().clear();
         font.getCache().addText(this.name, this.centerX() - lay.width / 2.0F, this.centerY() + lay.height / 2.0F + 1.0F);
         font.getCache().draw(this.parentAlpha);
         font.getData().setScale(oldScaleX, oldScaleY);
         Pools.free(lay);
      }
   }
   public float centerX() {
      return this.x + this.width / 2.0F;
   }
   public float centerY() {
      return this.y + this.height / 2.0F;
   }
   public MBar setFontScaleX(float scaleX) {
      this.fontScaleX = scaleX;
      return this;
   }
   public MBar setFontScaleY(float scaleY) {
      this.fontScaleY = scaleY;
      return this;
   }
   public MBar setFontScale(float scale) {
      this.fontScaleX = scale;
      this.fontScaleY = scale;
      return this;
   }
   public float getFontScaleX() {
      return this.fontScaleX;
   }
   public float getFontScaleY() {
      return this.fontScaleY;
   }
                            }
