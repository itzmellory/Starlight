package mfe.math;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Expression {
   public String name = "";
   public transient Seq<String> expNames = new Seq();
   protected transient Seq<Object> rpn = new Seq();
   protected static Pattern splitPattern = Pattern.compile(" |(?=[(),])|(?<=[(),])");
   protected static Matcher varMatcher = Pattern.compile("^[a-zA-Z_\\u4e00-\\u9fa5][0-9a-zA-Z_\\u4e00-\\u9fa5]*$").matcher("");
   protected transient FloatSeq stk = new FloatSeq();
   protected transient FloatSeq args = new FloatSeq();
   public transient boolean valid = false;
   public String expression = "";
   public Expression() {
   }
   public Expression(String name) {
      this.name = name;
   }
   public void parse(float value) {
      this.expression = String.valueOf(value);
      this.rpn.clear();
      this.expNames.clear();
      this.rpn.add(value);
      this.valid = true;
   }
   public boolean reParse(ExpressionHandler handler) {
      return this.isVar() ? (this.valid = true) : this.parse(this.expression, handler);
   }
   public boolean parse(String str, ExpressionHandler handler) {
      this.expression = str;
      this.rpn.clear();
      this.expNames.clear();
      int arys = 0;
      int consumes = 0;
      int braceL = 0;
      int braceR = 0;
      Seq<Object> seq = new Seq();
      try {
         String[] sps = splitPattern.split(str);
         for(String sp : sps) {
            boolean matched = false;
            if (sp.equals("(")) {
               seq.add(sp);
               ++braceL;
            } else if (!sp.equals(",") && !sp.equals(")")) {
               for(Ops op : Ops.values()) {
                  if (op.match(sp)) {
                     if (op.op0 != null) {
                        this.rpn.add(op.op0.get());
                        ++arys;
                     } else {
                        while(!seq.isEmpty()) {
                           Object var19 = seq.peek();
                           if (!(var19 instanceof Ops)) {
                              break;
                           }
                           Ops prev = (Ops)var19;
                           if (!(prev.l > op.l)) {
                              break;
                           }
                           this.rpn.add(seq.pop());
                        }
                        seq.add(op);
                     }
                     consumes = (int)((float)consumes + Mathf.maxZero((float)(op.ary - 1)));
                     matched = true;
                     break;
                  }
               }
               if (!matched && !sp.isEmpty()) {
                  if (Strings.canParseFloat(sp)) {
                     this.rpn.add(Float.parseFloat(sp));
                     ++arys;
                  } else if (varMatcher.reset(sp).matches()) {
                     this.rpn.add(handler.getExpression(sp));
                     this.expNames.add(sp);
                     ++arys;
                  }
               }
            } else {
               while(!seq.isEmpty()) {
                  Object top = seq.pop();
                  if (top.equals("(") || top.equals(",")) {
                     break;
                  }
                  this.rpn.add(top);
               }
               if (sp.equals(",")) {
                  seq.add(sp);
               } else {
                  ++braceR;
               }
            }
         }
         while(!seq.isEmpty()) {
            this.rpn.add(seq.pop());
         }
      } catch (Exception err) {
         this.valid = false;
         Log.err("Failure parsing expression: " + str, err);
         return false;
      }
      this.valid = arys - consumes == 1 && braceL == braceR;
      return this.valid;
   }
   public boolean isVar() {
      return this.valid && this.rpn.size == 1;
   }
   public float get() {
      if (!this.valid) {
         return 0.0F;
      } else {
         this.stk.clear();
         for(int i = 0; i < this.rpn.size; ++i) {
            Object obj = this.rpn.get(i);
            if (obj instanceof Expression) {
               Expression exp = (Expression)obj;
               this.stk.add(exp.get());
            }
            if (obj instanceof Float) {
               Float f = (Float)obj;
               this.stk.add(f);
            }
            if (obj instanceof Ops) {
               Ops ops = (Ops)obj;
               this.args.clear();
               for(int ig = 0; ig < ops.ary; ++ig) {
                  this.args.add(this.stk.pop());
               }
               if (ops.op1 != null) {
                  this.stk.add(ops.op1.get(this.args.pop()));
               }
               if (ops.op2 != null) {
                  this.stk.add(ops.op2.get(this.args.pop(), this.args.pop()));
               }
               if (ops.op3 != null) {
                  this.stk.add(ops.op3.get(this.args.pop(), this.args.pop(), this.args.pop()));
               }
            }
         }
         return this.stk.get(this.stk.size - 1);
      }
   }
}
