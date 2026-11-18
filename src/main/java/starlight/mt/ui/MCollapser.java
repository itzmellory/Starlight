package mt.ui;
import arc.func.Boolp;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.scene.Action;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.ArcRuntimeException;
import arc.util.Nullable;
public class MCollapser extends WidgetGroup {
   Table table;
   @Nullable
   Boolp collapsedFunc;
   private final CollapseAction collapseAction;
   boolean collapsed;
   boolean autoAnimate;
   boolean horizontal;
   boolean vertical;
   boolean actionRunning;
   float currentHeight;
   float currentWidth;
   float seconds;
   public MCollapser(Cons<Table> cons, boolean collapsed) {
      this(new Table(), collapsed);
      cons.get(this.table);
   }
   public MCollapser(Table table, boolean collapsed) {
      this.collapseAction = new CollapseAction();
      this.vertical = true;
      this.seconds = 0.4F;
      this.table = table;
      this.collapsed = collapsed;
      this.setTransform(true);
      table.setClip(true);
      this.updateTouchable();
      this.addChild(table);
   }
   public MCollapser setDuration(float seconds) {
      this.seconds = seconds;
      return this;
   }
   public MCollapser setCollapsed(Boolp collapsed) {
      this.collapsedFunc = collapsed;
      return this;
   }
   public MCollapser setCollapsed(boolean autoAnimate, Boolp collapsed) {
      this.collapsedFunc = collapsed;
      this.autoAnimate = autoAnimate;
      return this;
   }
   public MCollapser setDirection(boolean horizontal, boolean vertical) {
      this.horizontal = horizontal;
      this.vertical = vertical;
      return this;
   }
   public MCollapser setInterpolation(Interp x, Interp y) {
      this.collapseAction.interpolationHorizontal = x;
      this.collapseAction.interpolationVertical = y;
      return this;
   }
   public MCollapser setInterpolation(Interp interp) {
      this.setInterpolation(interp, interp);
      return this;
   }
   public void toggle() {
      this.setCollapsed(!this.isCollapsed());
   }
   public void toggle(boolean animated) {
      this.setCollapsed(!this.isCollapsed(), animated);
   }
   public void setCollapsed(boolean collapse, boolean withAnimation) {
      this.collapsed = collapse;
      this.updateTouchable();
      if (this.table != null) {
         this.actionRunning = true;
         if (withAnimation) {
            this.addAction(this.collapseAction);
         } else {
            this.currentHeight = this.vertical && collapse ? 0.0F : this.table.getPrefHeight();
            this.currentWidth = this.horizontal && collapse ? 0.0F : this.table.getPrefWidth();
            this.collapsed = collapse;
            this.actionRunning = false;
            this.invalidateHierarchy();
         }
      }
   }
   public void setCollapsed(boolean collapse) {
      this.setCollapsed(collapse, true);
   }
   public boolean isCollapsed() {
      return this.collapsed;
   }
   private void updateTouchable() {
      Touchable touchable1 = this.collapsed ? Touchable.disabled : Touchable.enabled;
      this.touchable = touchable1;
   }
   public void draw() {
      if (this.currentHeight > 1.0F && this.currentWidth > 1.0F) {
         Draw.flush();
         if (this.clipBegin()) {
            super.draw();
            Draw.flush();
            this.clipEnd();
         }
      }
   }
   public void act(float delta) {
      super.act(delta);
      if (this.collapsedFunc != null) {
         boolean col = this.collapsedFunc.get();
         if (col != this.collapsed) {
            this.setCollapsed(col, this.autoAnimate);
         }
      }
   }
   public void layout() {
      if (this.table != null) {
         this.table.setBounds(0.0F, 0.0F, this.getWidth(), this.getHeight());
         if (!this.actionRunning) {
            this.currentHeight = this.vertical && this.collapsed ? 0.0F : this.table.getPrefHeight();
            this.currentWidth = this.horizontal && this.collapsed ? 0.0F : this.table.getPrefWidth();
         }
      }
   }
   public float getPrefHeight() {
      if (this.table == null) {
         return 0.0F;
      } else if (!this.actionRunning) {
         return this.collapsed && this.vertical ? 0.0F : this.table.getPrefHeight();
      } else {
         return this.currentHeight;
      }
   }
   public float getPrefWidth() {
      if (this.table == null) {
         return 0.0F;
      } else if (!this.actionRunning) {
         return this.collapsed && this.horizontal ? 0.0F : this.table.getPrefWidth();
      } else {
         return this.currentWidth;
      }
   }
   public void setTable(Table table) {
      this.table = table;
      this.clearChildren();
      this.addChild(table);
   }
   public float getMinWidth() {
      return 0.0F;
   }
   public float getMinHeight() {
      return 0.0F;
   }
   protected void childrenChanged() {
      super.childrenChanged();
      if (this.getChildren().size > 1) {
         throw new ArcRuntimeException("Only one actor can be added to CollapsibleWidget");
      }
   }
   private class CollapseAction extends Action {
      Interp interpolationHorizontal;
      Interp interpolationVertical;
      float progress;
      CollapseAction() {
         this.interpolationHorizontal = Interp.exp5;
         this.interpolationVertical = Interp.exp5;
         this.progress = 0.0F;
      }
      public boolean act(float delta) {
         if (MCollapser.this.collapsed) {
            this.progress -= delta / MCollapser.this.seconds;
            if (this.progress <= 0.0F) {
               this.progress = 0.0F;
               MCollapser.this.actionRunning = false;
            }
         } else {
            this.progress += delta / MCollapser.this.seconds;
            if (this.progress >= 1.0F) {
               this.progress = 1.0F;
               MCollapser.this.actionRunning = false;
            }
         }
         MCollapser.this.currentHeight = (MCollapser.this.vertical ? this.interpolationVertical.apply(this.progress) : 1.0F) * MCollapser.this.table.getPrefHeight();
         MCollapser.this.currentWidth = (MCollapser.this.horizontal ? this.interpolationVertical.apply(this.progress) : 1.0F) * MCollapser.this.table.getPrefWidth();
         MCollapser.this.invalidateHierarchy();
         return !MCollapser.this.actionRunning;
      }
   }
}
