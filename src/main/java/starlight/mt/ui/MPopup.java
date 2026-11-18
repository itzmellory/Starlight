package mt.ui;
import arc.Core;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.scene.Action;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Nullable;
import mindustry.Vars;
import mt.MTStyles;
import mt.MTmps;
public class MPopup extends Table {
   public boolean shown = false;
   public float popupDuration = 0.15F;
   public boolean cancelDrag;
   public void popup(int align) {
      if (!this.shown) {
         Core.scene.add(this);
         this.shown = true;
         this.setTransform(true);
         this.clearActions();
         if (Align.isTop(align)) {
            this.actions(new Action[]{Actions.scaleTo(1.0F, 0.0F), Actions.translateBy(0.0F, this.getPrefHeight())});
            this.actions(new Action[]{Actions.parallel(Actions.scaleTo(1.0F, 1.0F, this.popupDuration, Interp.fade), Actions.translateBy(0.0F, -this.getPrefHeight(), this.popupDuration, Interp.fade), Actions.run(this::keepInScreen))});
         } else if (Align.isBottom(align)) {
            this.actions(new Action[]{Actions.scaleTo(1.0F, 0.0F)});
            this.actions(new Action[]{Actions.parallel(Actions.scaleTo(1.0F, 1.0F, this.popupDuration, Interp.fade), Actions.run(this::keepInScreen))});
         } else if (Align.isLeft(align)) {
            this.actions(new Action[]{Actions.scaleTo(0.0F, 1.0F)});
            this.actions(new Action[]{Actions.parallel(Actions.scaleTo(1.0F, 1.0F, this.popupDuration, Interp.fade), Actions.run(this::keepInScreen))});
         } else if (Align.isRight(align)) {
            this.actions(new Action[]{Actions.scaleTo(0.0F, 1.0F), Actions.translateBy(this.getPrefWidth(), 0.0F)});
            this.actions(new Action[]{Actions.parallel(Actions.scaleTo(1.0F, 1.0F, this.popupDuration, Interp.fade), Actions.translateBy(-this.getPrefWidth(), 0.0F, this.popupDuration, Interp.fade), Actions.run(this::keepInScreen))});
         }
      }
   }
   public void popup() {
      this.popup(8);
   }
   public void hide() {
      if (this.shown) {
         this.shown = false;
         this.actions(new Action[]{Actions.scaleTo(1.0F, 0.0F, this.popupDuration, Interp.fade), Actions.delay(this.popupDuration), Actions.remove()});
      }
   }
   public void snapTo(Element e) {
      e.localToStageCoordinates(MTmps.v1.setZero());
      this.setPositionInScreen(MTmps.v1.x, MTmps.v1.y);
   }
   public void setPositionInScreen(float x, float y) {
      this.setPosition(x, y);
      this.keepInScreen();
   }
   public void keepInScreen() {
      this.keepInStage();
      this.invalidateHierarchy();
      this.pack();
   }
   public void hideWithoutFocusOn(@Nullable Element... elements) {
      boolean hasMouse = false;
      if (elements != null) {
         for(Element e : elements) {
            if (e != null && e.hasMouse()) {
               hasMouse = true;
               break;
            }
         }
      }
      if (!hasMouse) {
         this.hide();
      }
   }
   public void addDragPopupListener(Element e) {
      e.addListener(new InputListener() {
         float fromx;
         float fromy;
         public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            this.fromx = x;
            this.fromy = y;
            return true;
         }
         public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (!MPopup.this.cancelDrag) {
               MPopup.this.localToParentCoordinates(MTmps.v1.set(x, y));
               MPopup.this.setPositionInScreen(MTmps.v1.x - this.fromx, MTmps.v1.y - this.fromy);
            }
         }
      });
   }
   public void addDragMove() {
      this.addDragPopupListener(this);
   }
   public void addCloseButton() {
      this.addCloseButton(32.0F);
   }
   public void addCloseButton(float size) {
      TextButton b = new TextButton("\ue815", MTStyles.textb) {
         {
            this.clicked(() -> MPopup.this.hide());
         }
      };
      b.setStyle(new TextButton.TextButtonStyle(b.getStyle()));
      b.getStyle().up = null;
      b.setSize(size);
      b.update(() -> {
         b.setPosition(this.getWidth() - b.getWidth(), this.getHeight() - b.getHeight());
         b.toFront();
      });
      this.addChild(b);
   }
   public void addInGameVisible() {
      this.visible(() -> !Vars.state.isMenu() && Vars.ui.hudfrag.shown);
   }
}
