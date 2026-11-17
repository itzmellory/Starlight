package starlight.mapping.dialogs;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.ScrollPane;
import arc.util.Tmp;
import mindustry.game.Rules;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import starlight.mapping.utils.PlanetBackgroundDrawer;
import static arc.Core.input;
import static arc.Core.scene;
import static mindustry.Vars.content;
import static mindustry.Vars.ui;
public class PlanetBackgroundDialog extends BaseDialog{
    private final Rules rules;
    private float zoom = 1f;
    public PlanetBackgroundDialog(Rules rules){
        super("@rules.planetbackground", new DialogStyle(){{
            stageBackground = Styles.none;
            titleFont = Fonts.def;
            titleFontColor = Pal.accent;
        }});
        this.rules = rules;
        dragged((cx, cy) -> {
            if(rules.planetBackground == null || Core.input.getTouches() > 1) return;
            Vec3 pos = rules.planetBackground.camPos;
            float angle = pos.angle(Vec3.Y);
            float speed = 1f - Math.abs(angle - 90f) / 90f;
            pos.rotate(rules.planetBackground.camUp, cx / 9f * speed);
            float amount = Mathf.clamp(angle + cy / 10f, 1f, 179f) - angle;
            pos.rotate(Tmp.v31.set(rules.planetBackground.camUp).rotate(rules.planetBackground.camDir, 90f), amount);
            PlanetBackgroundDrawer.update();
        });
        addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                if(rules.planetBackground == null) return false;
                zoom = Mathf.clamp(zoom + amountY / 10f, rules.planetBackground.planet.minZoom, 50f);
                PlanetBackgroundDrawer.update();
                return true;
            }
        });
        addCaptureListener(new ElementGestureListener(){
            float lastZoom = -1f;
            @Override
            public void zoom(InputEvent event, float initialDistance, float distance){
                if(rules.planetBackground == null) return;
                if(lastZoom < 0f) lastZoom = zoom;
                zoom = Mathf.clamp(initialDistance / distance * lastZoom, rules.planetBackground.planet.minZoom, 50f);
                PlanetBackgroundDrawer.update();
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                lastZoom = zoom;
            }
        });
        addCloseButton();
        buttons.button("@remove_background", Icon.none, () -> ui.showConfirm("@confirm", () -> {
            rules.planetBackground = null;
            build();
        })).get().setDisabled(() -> rules.planetBackground == null);
        buttons.button("@rules.title.planet", Icon.planet, () -> {
            BaseDialog picker = new BaseDialog("@rules.title.planet");
            picker.cont.pane(table -> {
                int i = 0;
                for(Planet planet : content.planets()){
                    table.button(planet.localizedName, Icon.icons.get(planet.icon), Styles.togglet, () -> {
                        if(rules.planetBackground != null) rules.planetBackground.planet = planet;
                        PlanetBackgroundDrawer.update();
                        picker.hide();
                    }).width(220f).height(55f).checked(rules.planetBackground != null && rules.planetBackground.planet == planet).row();
                    if(++i % 3 == 0) table.row();
                }
            });
            picker.addCloseButton();
            picker.show();
        }).get().setDisabled(() -> rules.planetBackground == null);
        shown(this::build);
    }
    @Override
    public PlanetBackgroundDialog show(){
        zoom = rules.planetBackground == null ? 1f : rules.planetBackground.zoom;
        super.show();
        return this;
    }
    private void build(){
        cont.clear();
        if(rules.planetBackground == null){
            cont.add("@empty").row();
            cont.button("@add", Icon.add, () -> {
                rules.planetBackground = new PlanetParams();
                zoom = rules.planetBackground.zoom;
                build();
            }).width(100f);
        }
        PlanetBackgroundDrawer.update();
    }
    @Override
    public void draw(){
        if(rules.planetBackground != null){
            if(scene.getDialog() == this && !scene.hit(input.mouseX(), input.mouseY(), true).isDescendantOf(e -> e instanceof ScrollPane)) scene.setScrollFocus(this);
            rules.planetBackground.zoom = Mathf.lerpDelta(rules.planetBackground.zoom, zoom, 0.4f);
            float size = Math.max(Core.graphics.getWidth(), Core.graphics.getHeight());
            arc.graphics.Texture texture = PlanetBackgroundDrawer.draw(rules.planetBackground);
            if(texture != null) Draw.rect(Draw.wrap(texture), Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f, size, -size);
            Draw.flush();
        }else{
            Draw.color(color.r, color.g, color.b, color.a * parentAlpha);
            Styles.black9.draw(x, y, width, height);
        }
        super.draw();
    }
}
