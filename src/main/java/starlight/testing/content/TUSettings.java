package starlight.testing.content;
import arc.Core;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Icon;
import starlight.testing.ui.TUDialogs;
public class TUSettings{
    public static void init(){
        Vars.ui.settings.addCategory("@setting.tu-title", Icon.defense, table -> {
            table.checkPref("tu-instakill", true);
            table.checkPref("tu-despawns", true);
            table.checkPref("tu-permanent", false);
            table.sliderPref("tu-long-press", 2, 1, 12, value -> Strings.autoFixed(value / 4f, 2));
            table.button(Icon.defense, () -> TUDialogs.teamDialog.show(getTeam(), team -> Core.settings.put("tu-default-team", team.id))).get()
                .label(() -> Core.bundle.format("tu-unit-menu.default-team", "[#" + getTeam().color + "]" + TUDialogs.teamDialog.teamName(getTeam()) + "[]"))
                .padLeft(6f).growX();
        });
    }
    public static Team getTeam(){
        return Team.get(Core.settings.getInt("tu-default-team", 1));
    }
}
