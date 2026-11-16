package starlight.ui;
import arc.Core;
import arc.Events;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.editor.EditorTool;
import mindustry.editor.MapView;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import starlight.upperuiutils.CustomShapeManager;
import starlight.upperuiutils.LandCopyPast;
import starlight.upperuiutils.TerrainSchematicsDialog;
public class UpperUi {
   private static Table topBar;
   private static MapView lastView;
   private static float lastWidth = -1.0F;
   private static float lastHeight = -1.0F;
   public static void init() {
      Events.run(Trigger.update, UpperUi::update);
   }
   private static void update() {
      if (Vars.ui != null && Vars.ui.editor != null && Vars.ui.editor.isShown()) {
         MapView view = Vars.ui.editor.getView();
         if (view != null && view.parent != null && !(view.getWidth() <= 0.0F) && !(view.getHeight() <= 0.0F)) {
            float[] bounds = getAvailableBounds(view);
            float width = Math.max(1.0F, bounds[2] - bounds[0]);
            if (topBar == null || topBar.parent != view.parent || lastView != view || Math.abs(lastWidth - width) > 1.0F || Math.abs(lastHeight - view.getHeight()) > 1.0F) {
               buildTopBar(view, width);
            }
            positionTopBar(view, bounds);
         }
      } else {
         removeTopBar();
      }
   }
   private static float[] getAvailableBounds(MapView view) {
      Vec2 viewBottom = view.localToStageCoordinates(new Vec2(0.0F, 0.0F));
      Vec2 viewTop = view.localToStageCoordinates(new Vec2(view.getWidth(), view.getHeight()));
      float left = viewBottom.x;
      float right = viewTop.x;
      float center = (left + right) / 2.0F;
      Element branch = view;
      for(Group group = view.parent; group != null; group = group.parent) {
         for(Element element : group.getChildren()) {
            if (element != branch && element != topBar && element.visible && !(element.getWidth() <= 0.0F) && !(element.getHeight() < view.getHeight() * 0.35F)) {
               Vec2 elementBottom = element.localToStageCoordinates(new Vec2(0.0F, 0.0F));
               Vec2 elementTop = element.localToStageCoordinates(new Vec2(element.getWidth(), element.getHeight()));
               float overlap = Math.min(viewTop.y, elementTop.y) - Math.max(viewBottom.y, elementBottom.y);
               if (!(overlap < view.getHeight() * 0.3F)) {
                  float elementCenter = (elementBottom.x + elementTop.x) / 2.0F;
                  if (elementCenter < center && elementTop.x > left && elementTop.x <= center && elementBottom.x < center) {
                     left = Math.max(left, elementTop.x);
                  }
                  if (elementCenter > center && elementBottom.x < right && elementBottom.x >= center && elementTop.x > center) {
                     right = Math.min(right, elementBottom.x);
                  }
               }
            }
         }
         branch = group;
      }
      Vec2 localBottom = view.parent.stageToLocalCoordinates(new Vec2(left, viewBottom.y));
      Vec2 localTop = view.parent.stageToLocalCoordinates(new Vec2(right, viewTop.y));
      return new float[]{localBottom.x, localBottom.y, localTop.x, localTop.y};
   }
   private static void removeTopBar() {
      if (topBar != null) {
         topBar.remove();
         topBar = null;
      }
      lastView = null;
      lastWidth = -1.0F;
      lastHeight = -1.0F;
   }
   private static void positionTopBar(MapView view, float[] bounds) {
      if (topBar != null && topBar.parent == view.parent) {
         float width = Math.max(1.0F, bounds[2] - bounds[0]);
         topBar.setSize(width, topBar.getPrefHeight());
         topBar.setPosition(bounds[0], view.y + view.getHeight() - topBar.getHeight() - 4.0F);
      }
   }
   private static void buildTopBar(MapView view, float width) {
      if (topBar != null) {
         topBar.remove();
      }
      lastView = view;
      lastWidth = width;
      lastHeight = view.getHeight();
      Group parent = view.parent;
      topBar = new Table();
      topBar.setFillParent(false);
      topBar.setClip(true);
      topBar.top().left();
      topBar.touchable = Touchable.childrenOnly;
      Table bar = new Table(Tex.button);
      bar.margin(4.0F);
      bar.left();
      bar.defaults().size(42.0F, 42.0F).pad(2.0F);
      ImageButton copyBtn = (ImageButton)bar.button(Icon.copy, Styles.squareTogglei, () -> LandCopyPast.toggleMode(LandCopyPast.Mode.copy)).tooltip(Core.bundle.get("upper.copy", "Copy Terrain")).get();
      copyBtn.update(() -> copyBtn.setChecked(LandCopyPast.mode == LandCopyPast.Mode.copy));
      ImageButton cutBtn = (ImageButton)bar.button(Icon.trash, Styles.squareTogglei, () -> LandCopyPast.toggleMode(LandCopyPast.Mode.cut)).tooltip(Core.bundle.get("upper.cut", "Cut Terrain")).get();
      cutBtn.update(() -> cutBtn.setChecked(LandCopyPast.mode == LandCopyPast.Mode.cut));
      ImageButton pasteBtn = (ImageButton)bar.button(Icon.paste, Styles.squareTogglei, () -> LandCopyPast.toggleMode(LandCopyPast.Mode.paste)).tooltip(Core.bundle.get("upper.paste", "Paste Terrain")).get();
      pasteBtn.setDisabled(() -> LandCopyPast.clipboard == null);
      pasteBtn.update(() -> pasteBtn.setChecked(LandCopyPast.mode == LandCopyPast.Mode.paste));
      bar.button(Icon.rotate, Styles.flati, LandCopyPast::rotateClipboard).tooltip(Core.bundle.get("upper.rotate", "Rotate")).disabled((b) -> LandCopyPast.clipboard == null);
      bar.button(Icon.flipX, Styles.flati, LandCopyPast::flipX).tooltip(Core.bundle.get("upper.flipx", "Flip X")).disabled((b) -> LandCopyPast.clipboard == null);
      bar.button(Icon.flipY, Styles.flati, LandCopyPast::flipY).tooltip(Core.bundle.get("upper.flipy", "Flip Y")).disabled((b) -> LandCopyPast.clipboard == null);
      bar.button(Icon.save, Styles.flati, TerrainSchematicsDialog::show).tooltip(Core.bundle.get("upper.schematics", "Terrain Schematics"));
      bar.button(Icon.settings, Styles.flati, LandCopyPast::showCopyPasteSettings).tooltip(Core.bundle.get("upper.settings", "Copy/Paste Options"));
      bar.image(Tex.whiteui).width(2.0F).height(34.0F).padLeft(4.0F).padRight(4.0F);
      for(CustomShapeManager.BrushShape shape : CustomShapeManager.BrushShape.values()) {
         TextButton shapeButton = (TextButton)bar.button(shape.symbol, Styles.flatTogglet, () -> CustomShapeManager.currentShape = shape).tooltip(shape.title).size(42.0F, 42.0F).get();
         shapeButton.update(() -> shapeButton.setChecked(CustomShapeManager.currentShape == shape));
      }
      bar.image(Tex.whiteui).width(2.0F).height(34.0F).padLeft(4.0F).padRight(4.0F);
      TextButton angleButton = (TextButton)bar.button("45°", Styles.flatTogglet, () -> {
         CustomShapeManager.line45Mode = !CustomShapeManager.line45Mode;
         if (CustomShapeManager.line45Mode) {
            LandCopyPast.setMode(LandCopyPast.Mode.none);
            view.setTool(EditorTool.line);
         }
      }).tooltip(Core.bundle.get("upper.line45", "Strict 45° Line Mode")).size(52.0F, 42.0F).get();
      angleButton.update(() -> angleButton.setChecked(CustomShapeManager.line45Mode));
      bar.image(Tex.whiteui).width(2.0F).height(34.0F).padLeft(4.0F).padRight(4.0F);
      topBar.add(bar).width(width).left();
      topBar.pack();
      topBar.setSize(width, topBar.getPrefHeight());
      parent.addChild(topBar);
   }
}
