package starlight.testing.util;
import arc.*;
import arc.scene.ui.layout.*;
import mindustry.game.EventType.*;
import starlight.testing.buttons.*;
import starlight.testing.ui.*;
import static mindustry.Vars.*;
public class Setup{
    static boolean selfInit;
    static Table buttons = new Table().bottom().left();
    public static Table
    team = newTable(),
    death = newTable(),
    sandbox = newTable(),
    status = newTable(),
    units = newTable();
    public static Table newTable(){
        return new Table().bottom().left();
    }
    public static void add(Table table){
        buttons.addChild(table);
    }
    public static void init(){
        TUVars.setDefaults();
        TUDialogs.load();
        TeamChanger.add(team);
        add(team);
        Death.init();
        Death.add(death);
        add(death);
        Sandbox.add(sandbox);
        add(sandbox);
        StatusMenu.add(status);
        add(status);
        SpawnMenu.add(units);
        add(units);
        buttons.visibility = Visibility.buttonVisibility;
        ui.hudGroup.addChild(buttons);
        Events.on(WorldLoadEvent.class, e -> {
            if(!selfInit){
                Table healthUI = placement();
                if(healthUI != null){
                    healthUI.row();
                    Health.healing(healthUI).size(96, 40).color(TUVars.curTeam.color).pad(0).left().padLeft(4);
                    Health.invincibility(healthUI).size(164, 40).color(TUVars.curTeam.color).pad(0).left().padLeft(-20);
                    selfInit = true;
                }
            }
        });
    }
    public static Table placement(){
        Table marker = ui.hudGroup.find("overlaymarker");
        if(marker == null) return null;
        Stack editor = marker.find("waves/editor");
        if(editor == null) return null;
        Table waves = editor.find("waves");
        if(waves == null) return null;
        return waves.find("status");
    }
}
