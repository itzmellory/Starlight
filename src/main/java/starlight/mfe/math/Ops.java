package mfe.math;
import arc.math.Mathf;
import arc.util.noise.Simplex;
import mfe.guides.GuideSeqImage;
public enum Ops {
   maph("@maph", () -> (float)GuideSeqImage.guidesImage.imageHeight),
   mapw("@mapw", () -> (float)GuideSeqImage.guidesImage.imageWidth),
   e("e", () -> (float)Math.E),
   pi("pi", () -> (float)Math.PI),
   add("+", (a, b) -> a + b, 1.0F),
   sub("-", (a, b) -> a - b, 1.0F),
   mul("*", (a, b) -> a * b, 2.0F),
   div("/", (a, b) -> a / b, 3.0F),
   pow("^", Mathf::pow, 4.0F),
   log("log", Mathf::log, 5.0F),
   ln("ln", (a) -> Mathf.log(a, (float)Math.E), 5.0F),
   lg("lg", (a) -> Mathf.log(a, 10.0F), 5.0F),
   abs("abs", Math::abs, 5.0F),
   sgn("sgn", (a) -> a == 0.0F ? 0.0F : (a > 0.0F ? 1.0F : -1.0F), 5.0F),
   g0("g0", (a) -> a > 0.0F ? 1.0F : 0.0F, 5.0F),
   geq0("geq0", (a) -> a >= 0.0F ? 1.0F : 0.0F, 5.0F),
   floor("floor", Mathf::floor, 5.0F),
   ceil("ceil", Mathf::ceil, 5.0F),
   round("round", (a) -> (float)Mathf.round(a), 5.0F),
   mod("mod", Mathf::mod, 5.0F),
   max("max", Math::max, 5.0F),
   min("min", Math::min, 5.0F),
   len("len", Mathf::len, 5.0F),
   sin("sin", (a) -> Mathf.sin(a), 5.0F),
   cos("cos", (a) -> Mathf.cos(a), 5.0F),
   tan("tan", (a) -> Mathf.tan(a, 1.0F, 1.0F), 5.0F),
   asin("arcsin", (a) -> (float)Math.asin((double)a), 5.0F),
   acos("arccos", (a) -> (float)Math.acos((double)a), 5.0F),
   atan("arctan", (a) -> (float)Math.atan((double)a), 5.0F),
   noise("noise", (x, y, seed) -> (float)Simplex.raw2d((int)seed, (double)x, (double)y), 5.0F);
   public final String symbol;
   public final float l;
   public final Op3 op3;
   public final Op2 op2;
   public final Op1 op1;
   public final Op0 op0;
   public final int ary;
   private Ops(String symbol, Op0 op) {
      this.symbol = symbol;
      this.l = Float.MAX_VALUE;
      this.op0 = op;
      this.op1 = null;
      this.op2 = null;
      this.op3 = null;
      this.ary = 0;
   }
   private Ops(String symbol, Op1 op, float lvl) {
      this.symbol = symbol;
      this.l = lvl;
      this.op0 = null;
      this.op1 = op;
      this.op2 = null;
      this.op3 = null;
      this.ary = 1;
   }
   private Ops(String symbol, Op2 op, float lvl) {
      this.symbol = symbol;
      this.l = lvl;
      this.op0 = null;
      this.op1 = null;
      this.op2 = op;
      this.op3 = null;
      this.ary = 2;
   }
   private Ops(String symbol, Op3 op, float lvl) {
      this.symbol = symbol;
      this.l = lvl;
      this.op0 = null;
      this.op1 = null;
      this.op2 = null;
      this.op3 = op;
      this.ary = 3;
   }
   public boolean match(String str) {
      return str.equals(this.symbol);
   }
   private static Ops[] $values() {
      return new Ops[]{maph, mapw, e, pi, add, sub, mul, div, pow, log, ln, lg, abs, sgn, g0, geq0, floor, ceil, round, mod, max, min, len, sin, cos, tan, asin, acos, atan, noise};
   }
   public interface Op0 {
      float get();
   }
   public interface Op1 {
      float get(float var1);
   }
   public interface Op2 {
      float get(float var1, float var2);
   }
   public interface Op3 {
      float get(float var1, float var2, float var3);
   }
                       }
