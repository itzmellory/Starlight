package starlight.ui;
import arc.Core;
import arc.Events;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.editor.EditorTool;
import mindustry.editor.MapView;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import starlight.upperuiutils.CustomShapeManager;
import starlight.upperuiutils.LandCopyPast;
import starlight.upperuiutils.TerrainSchematicsDialog;
public class UpperUi{
    private static Table topBar;
    private static MapView lastView;
    private static float lastWidth = -1f;
    private static float lastHeight = -1f;
    public static void init(){
        Events.run(Trigger.update, UpperUi::update);
    }
    private static void update(){
        if(Vars.ui != null && Vars.ui.editor != null && Vars.ui.editor.isShown()){
            MapView view = Vars.ui.editor.getView();
            if(view != null && view.parent != null && view.getWidth() > 0f && view.getHeight() > 0f){
                float[] bounds = getAvailableBounds(view);
                float width = Math.max(1f, bounds[2] - bounds[0]);
                if(topBar == null || topBar.parent != view.parent || lastView != view || Math.abs(lastWidth - width) > 1f || Math.abs(lastHeight - view.getHeight()) > 1f){
                    buildTopBar(view, width);
                }
                positionTopBar(view, bounds);
            }
        }else{
            removeTopBar();
        }
    }
    private static float[] getAvailableBounds(MapView view){
        Vec2 viewBottom = view.localToStageCoordinates(new Vec2(0f, 0f));
        Vec2 viewTop = view.localToStageCoordinates(new Vec2(view.getWidth(), view.getHeight()));
        float left = viewBottom.x;
        float right = viewTop.x;
        float center = (left + right) / 2f;
        Element branch = view;
        for(Group group = view.parent; group != null; group = group.parent){
            for(Element element : group.getChildren()){
                if(element != branch && element != topBar && element.visible && element.getWidth() > 0f && element.getHeight() >= view.getHeight() * 0.35f){
                    Vec2 elementBottom = element.localToStageCoordinates(new Vec2(0f, 0f));
                    Vec2 elementTop = element.localToStageCoordinates(new Vec2(element.getWidth(), element.getHeight()));
                    float overlap = Math.min(viewTop.y, elementTop.y) - Math.max(viewBottom.y, elementBottom.y);
                    if(overlap >= view.getHeight() * 0.3f){
                        float elementCenter = (elementBottom.x + elementTop.x) / 2f;
                        if(elementCenter < center && elementTop.x > left && elementTop.x <= center && elementBottom.x < center) left = Math.max(left, elementTop.x);
                        if(elementCenter > center && elementBottom.x < right && elementBottom.x >= center && elementTop.x > center) right = Math.min(right, elementBottom.x);
                    }
                }
            }
            branch = group;
        }
        Vec2 localBottom = view.parent.stageToLocalCoordinates(new Vec2(left, viewBottom.y));
        Vec2 localTop = view.parent.stageToLocalCoordinates(new Vec2(right, viewTop.y));
        return new float[]{localBottom.x, localBottom.y, localTop.x, localTop.y};
    }
    private static void removeTopBar(){
        if(topBar != null){
            topBar.remove();
            topBar = null;
        }
        lastView = null;
        lastWidth = -1f;
        lastHeight = -1f;
    }
    private static void positionTopBar(MapView view, float[] bounds){
        if(topBar != null && topBar.parent == view.parent){
            topBar.setSize(Math.max(1f, bounds[2] - bounds[0]), topBar.getPrefHeight());
            topBar.setPosition(bounds[0], view.y + view.getHeight() - topBar.getHeight() - 5f);
        }
    }
    private static void buildTopBar(MapView view, float width){
        if(topBar != null) topBar.remove();
        lastView = view;
        lastWidth = width;
        lastHeight = view.getHeight();
        topBar = new Table(Tex.pane);
        topBar.margin(4f);
        topBar.left();
        topBar.touchable = Touchable.childrenOnly;
        Table content = new Table();
        content.left();
        content.defaults().padRight(5f);
        content.table(Tex.button, group -> {
            group.margin(4f);
            group.add("@upper.group.clipboard").color(Pal.accent).fontScale(0.62f).left().row();
            group.table(buttons -> {
                buttons.defaults().size(38f).pad(1f);
                ImageButton copy = (ImageButton)buttons.button(Icon.copy, Styles.squareTogglei, () -> LandCopyPast.toggleMode(LandCopyPast.Mode.copy)).tooltip(Core.bundle.get("upper.copy", "Copy terrain")).get();
                copy.update(() -> copy.setChecked(LandCopyPast.mode == LandCopyPast.Mode.copy));
                ImageButton cut = (ImageButton)buttons.button(Icon.trash, Styles.squareTogglei, () -> LandCopyPast.toggleMode(LandCopyPast.Mode.cut)).tooltip(Core.bundle.get("upper.cut", "Cut terrain")).get();
                cut.update(() -> cut.setChecked(LandCopyPast.mode == LandCopyPast.Mode.cut));
                ImageButton paste = (ImageButton)buttons.button(Icon.paste, Styles.squareTogglei, () -> LandCopyPast.toggleMode(LandCopyPast.Mode.paste)).tooltip(Core.bundle.get("upper.paste", "Paste terrain")).get();
                paste.setDisabled(() -> LandCopyPast.clipboard == null);
                paste.update(() -> paste.setChecked(LandCopyPast.mode == LandCopyPast.Mode.paste));
                buttons.button(Icon.rotate, Styles.flati, LandCopyPast::rotateClipboard).tooltip(Core.bundle.get("upper.rotate", "Rotate")).disabled(b -> LandCopyPast.clipboard == null);
                buttons.button(Icon.flipX, Styles.flati, LandCopyPast::flipX).tooltip(Core.bundle.get("upper.flipx", "Flip X")).disabled(b -> LandCopyPast.clipboard == null);
                buttons.button(Icon.flipY, Styles.flati, LandCopyPast::flipY).tooltip(Core.bundle.get("upper.flipy", "Flip Y")).disabled(b -> LandCopyPast.clipboard == null);
                buttons.button(Icon.save, Styles.flati, TerrainSchematicsDialog::show).tooltip(Core.bundle.get("upper.schematics", "Terrain schematics"));
                buttons.button(Icon.settings, Styles.flati, LandCopyPast::showCopyPasteSettings).tooltip(Core.bundle.get("upper.settings", "Copy/paste options"));
            });
        });
        content.table(Tex.button, group -> {
            group.margin(4f);
            group.add("@upper.group.brush").color(Pal.accent).fontScale(0.62f).left().row();
            group.table(buttons -> {
                buttons.defaults().size(38f, 38f).pad(1f);
                for(CustomShapeManager.BrushShape shape : CustomShapeManager.BrushShape.values()){
                    TextButton button = (TextButton)buttons.button(shape.symbol, Styles.flatTogglet, () -> CustomShapeManager.currentShape = shape).tooltip(shape.title).get();
                    button.update(() -> button.setChecked(CustomShapeManager.currentShape == shape));
                }
            });
        });
        content.table(Tex.button, group -> {
            group.margin(4f);
            group.add("@upper.group.precision").color(Pal.accent).fontScale(0.62f).left().row();
            group.button("45°", Styles.flatTogglet, () -> {
                CustomShapeManager.line45Mode = !CustomShapeManager.line45Mode;
                if(CustomShapeManager.line45Mode){
                    LandCopyPast.setMode(LandCopyPast.Mode.none);
                    view.setTool(EditorTool.line);
                }
            }).size(48f, 38f).tooltip(Core.bundle.get("upper.line45", "Strict 45 degree line")).update(button -> button.setChecked(CustomShapeManager.line45Mode));
        });
        ScrollPane scroll = new ScrollPane(content, Styles.smallPane);
        scroll.setScrollingDisabled(false, true);
        scroll.setFadeScrollBars(false);
        topBar.add(scroll).width(Math.max(1f, width - 8f)).height(58f);
        topBar.pack();
        topBar.setSize(width, topBar.getPrefHeight());
        view.parent.addChild(topBar);
    }
                       }
