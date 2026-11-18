package starlight.testing.dialogs;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.ui.dialogs.*;
import starlight.testing.ui.*;
import static arc.Core.*;
import static mindustry.Vars.*;
public class TeamDialog extends BaseDialog{
    Team curTeam;
    Cons<Team> changed;
    Table all = new Table();
    public TeamDialog(){
        super("@tu-unit-menu.team");
        shouldPause = false;
        addCloseButton();
        shown(this::rebuild);
        onResize(this::rebuild);
        all.margin(20).marginTop(0f);
        cont.pane(all);
    }
    public void show(Team startTeam, Cons<Team> changed){
        curTeam = startTeam;
        this.changed = changed;
        show();
    }
    void rebuild(){
        all.clear();
        all.table(t -> {
            t.add("@tu-unit-menu.teams-id").right().color(Pal.accent);
            TextField tField = TUElements.textField(
                String.valueOf(curTeam.id),
                field -> {
                    if(Strings.canParsePositiveInt(field.getText())){
                        Team selected = Team.get(Strings.parseInt(field.getText()));
                        curTeam = selected;
                        changed.get(selected);
                    }
                },
                () -> String.valueOf(curTeam.id),
                TextFieldFilter.digitsOnly
            );
            t.add(tField).left().padLeft(6);
        }).left().padBottom(6);
        all.row();
        all.add("@tu-unit-menu.teams-main").growX().left().color(Pal.accent);
        all.row();
        all.image().growX().pad(5).padLeft(0).padRight(0).height(3).color(Pal.accent);
        all.row();
        all.table(t -> {
            float iconMul = 2f;
            int cols = (int)Mathf.clamp((graphics.getWidth() - Scl.scl(30)) / Scl.scl(32 + 10) / iconMul, 1, 6);
            int count = 0;
            for(Team team : Team.baseTeams){
                addButton(t, team, 8 * 4 * iconMul);
                if((++count) % cols == 0){
                    t.row();
                }
            }
        });
        all.row();
        all.add("@tu-unit-menu.teams-other").growX().left().color(Pal.accent);
        all.row();
        all.image().growX().pad(5).padLeft(0).padRight(0).height(3).color(Pal.accent);
        all.row();
        all.table(t -> {
            float iconMul = 1.5f;
            int cols = (int)Mathf.clamp((graphics.getWidth() - Scl.scl(30)) / Scl.scl(32 + 10) / iconMul, 1, 22 / iconMul);
            int count = 0;
            for(int i = 6; i < Team.all.length; i++){
                addButton(t, Team.get(i), 8 * 4 * iconMul);
                if((++count) % cols == 0){
                    t.row();
                }
            }
        });
    }
    void addButton(Table t, Team team, float size){
        Table button = new Table();
        button.background(mindustry.gen.Tex.whiteui);
        button.setColor(team.color);
        Label id = new Label(String.valueOf(team.id));
        id.setColor(Color.black);
        id.setFontScale(1f);
        button.add(id).center();
        t.add(button).size(size).pad(3);
        ClickListener listener = new ClickListener();
        button.addListener(listener);
        if(!mobile){
            button.addListener(new HandCursorListener());
            Color lerpColor = team.color.cpy().lerp(Color.white, 0.5f);
            button.update(() -> button.setColor(team.color.cpy().lerp(lerpColor, listener.isOver() ? Mathf.clamp(0.4f * Time.delta) : 0f)));
        }
        button.clicked(() -> {
            curTeam = team;
            changed.get(team);
            hide();
        });
        TUElements.boxTooltip(button, () -> teamName(team));
    }
    public String teamName(Team team){
        return bundle.has("team." + team.name + ".name") ? bundle.get("team." + team.name + ".name") : String.valueOf(team.id);
    }
}
