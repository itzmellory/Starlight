package starlight.mapping;
import arc.Core;
import arc.scene.Element;
import arc.graphics.Color;
import arc.scene.event.EventListener;
import arc.scene.event.VisibilityListener;
import arc.scene.ui.Image;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.DelayedRemovalSeq;
import arc.util.Reflect;
import mindustry.content.Planets;
import mindustry.ctype.ContentType;
import mindustry.world.Block;
import mindustry.type.UnitType;
import mindustry.editor.BannedContentDialog;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.world.meta.Env;
public class RulesDialogMod{
    private final CustomRulesDialog dialog;
    private final Runnable setup = this::setup;
    private final VisibilityListener categoryFix;
    private Rules rules;
    private int currentNumbered;
    private final starlight.mapping.dialogs.BetterBannedContentDialog revealed;
    private final starlight.mapping.dialogs.BetterBannedContentDialog<Block> betterBlocks;
    private final starlight.mapping.dialogs.BetterBannedContentDialog<UnitType> betterUnits;
    private final BannedContentDialog oldBlocks;
    private final BannedContentDialog oldUnits;
    public RulesDialogMod(CustomRulesDialog dialog){
        this.dialog = dialog;
        revealed = new starlight.mapping.dialogs.BetterBannedContentDialog("@rules.revealedblocks", ContentType.block, block -> true);
        revealed.isRevealed = true;
        betterBlocks = new starlight.mapping.dialogs.BetterBannedContentDialog<>("@bannedblocks", ContentType.block, Block::canBeBuilt);
        betterUnits = new starlight.mapping.dialogs.BetterBannedContentDialog<>("@bannedunits", ContentType.unit, unit -> !unit.isHidden());
        oldBlocks = Reflect.get(dialog, "bannedBlocks");
        oldUnits = Reflect.get(dialog, "bannedUnits");
        categoryFix = new VisibilityListener(){
            @Override
            public boolean shown(){
                dialog.categoryNames.clear();
                return false;
            }
        };
    }
    public void enable(){
        dialog.additionalSetup.add(setup);
        ((DelayedRemovalSeq<EventListener>)Reflect.get(Element.class, dialog, "listeners")).insert(0, categoryFix);
        if(Core.settings.getBool("editor_better_content_dialogs", true)){
            Reflect.set(dialog, "bannedBlocks", betterBlocks);
            Reflect.set(dialog, "bannedUnits", betterUnits);
        }
    }
    private void setup(){
        rules = Reflect.get(dialog, "rules");
        dialog.category("miscellaneous");
        if(Core.settings.getBool("editor_hidden_rules", true)) addHiddenRules();
        if(Core.settings.getBool("editor_revealed_blocks", true)) addRevealedBlocks();
        if(Core.settings.getBool("editor_planet_background", true)) addPlanetBackground();
    }
    private void category(String name){
        if(dialog.categoryNames.contains(name)) dialog.current = dialog.categories.get(dialog.categoryNames.indexOf(name));
        else dialog.category(name);
    }
    private boolean matches(String key){
        return Core.bundle.get(key, key).toLowerCase().contains(dialog.ruleSearch);
    }
    private void addHiddenRules(){
        category("waves");
        dialog.check("@rules.hidespawns", value -> Reflect.set(rules, "hideSpawns", value), () -> Reflect.get(rules, "hideSpawns"));
        category("resourcesbuilding");
        dialog.check("@rules.ghostblocks", value -> rules.ghostBlocks = value, () -> rules.ghostBlocks);
        dialog.check("@rules.logicunitbuild", value -> rules.logicUnitBuild = value, () -> rules.logicUnitBuild);
        category("unit");
        dialog.check("@rules.possessionallowed", value -> rules.possessionAllowed = value, () -> rules.possessionAllowed);
        dialog.check("@rules.unitpayloadupdate", value -> rules.unitPayloadUpdate = value, () -> rules.unitPayloadUpdate);
        category("enemy");
        dialog.check("@rules.pvpautopause", value -> rules.pvpAutoPause = value, () -> rules.pvpAutoPause);
        dialog.check("@rules.coredestroyclear", value -> rules.coreDestroyClear = value, () -> rules.coreDestroyClear);
        category("environment");
        dialog.check("@rules.borderdarkness", value -> rules.borderDarkness = value, () -> rules.borderDarkness);
        dialog.check("@rules.disableoutsidearea", value -> rules.disableOutsideArea = value, () -> rules.disableOutsideArea);
        dialog.check("@rules.staticfog", value -> rules.staticFog = value, () -> rules.staticFog);
        colorRule("rules.staticfogcolor", rules.staticColor);
        colorRule("rules.dynamicfogcolor", rules.dynamicColor);
        colorRule("rules.cloudscolor", rules.cloudColor);
        dialog.number("@rules.dragmultiplier", value -> rules.dragMultiplier = value, () -> rules.dragMultiplier);
        if(matches("rules.environmentsettings") && Core.settings.getBool("editor_environment_settings", true)){
            dialog.current.button("@rules.environmentsettings", () -> environmentDialog(rules)).left().width(300f).fillX().row();
        }
        category("teams");
        Table numbered = new Table();
        dialog.numberi("@rules.numberedteam", value -> {
            currentNumbered = value;
            updateNumbered(numbered, Team.get(value));
        }, () -> currentNumbered, 0, 255);
        updateNumbered(numbered, Team.get(currentNumbered));
        if(numbered.hasChildren()) dialog.current.add(numbered).row();
        dialog.numberi("@rules.playerteam", value -> rules.defaultTeam = Team.get(value), () -> rules.defaultTeam.id, 0, 255);
        dialog.numberi("@rules.enemyteam", value -> rules.waveTeam = Team.get(value), () -> rules.waveTeam.id, 0, 255);
        category("miscellaneous");
        dialog.check("@rules.cangameover", value -> rules.canGameOver = value, () -> rules.canGameOver);
        if(matches("rules.modename")) text(dialog.current, "@rules.modename", value -> rules.modeName = value.isEmpty() ? null : value, () -> rules.modeName == null ? "" : rules.modeName);
        if(matches("rules.mission")) text(dialog.current, "@rules.mission", value -> rules.mission = value.isEmpty() ? null : value, () -> rules.mission == null ? "" : rules.mission);
        addBaseTeamRules();
    }
    private void colorRule(String key, Color color){
        if(!matches(key)) return;
        dialog.current.button(table -> {
            table.left();
            table.table(inner -> inner.stack(new Image(Tex.alphaBg), new Image(Tex.whiteui){{
                update(() -> setColor(color));
            }}).grow()).margin(4f).size(50f).padRight(10f);
            table.add("@" + key);
        }, () -> mindustry.Vars.ui.picker.show(color, color::set)).left().width(300f).row();
    }
    private void updateNumbered(Table table, Team team){
        table.clear();
        boolean[] expanded = {false};
        Table previous = dialog.current;
        table.button(team.coloredName(), Icon.downOpen, Styles.togglet, () -> expanded[0] = !expanded[0]).width(260f).height(50f).update(button -> button.setChecked(expanded[0])).left().row();
        table.collapser(inner -> {
            inner.left().defaults().fillX().left().pad(4f);
            dialog.current = inner;
            Rules.TeamRule tr = rules.teams.get(team);
            dialog.number("@rules.blockhealthmultiplier", value -> tr.blockHealthMultiplier = value, () -> tr.blockHealthMultiplier);
            dialog.number("@rules.blockdamagemultiplier", value -> tr.blockDamageMultiplier = value, () -> tr.blockDamageMultiplier);
            dialog.check("@rules.rtsai", value -> tr.rtsAi = value, () -> tr.rtsAi, () -> team != rules.defaultTeam);
            dialog.numberi("@rules.rtsminsquadsize", value -> tr.rtsMinSquad = value, () -> tr.rtsMinSquad, () -> tr.rtsAi, 0, 100);
            dialog.numberi("@rules.rtsmaxsquadsize", value -> tr.rtsMaxSquad = value, () -> tr.rtsMaxSquad, () -> tr.rtsAi, 1, 1000);
            dialog.number("@rules.rtsminattackweight", value -> tr.rtsMinWeight = value, () -> tr.rtsMinWeight, () -> tr.rtsAi);
            dialog.check("@rules.buildai", value -> tr.buildAi = value, () -> tr.buildAi, () -> team != rules.defaultTeam && rules.env != Planets.erekir.defaultEnv && !rules.pvp);
            dialog.number("@rules.buildaitier", false, value -> tr.buildAiTier = value, () -> tr.buildAiTier, () -> tr.buildAi && rules.env != Planets.erekir.defaultEnv && !rules.pvp, 0, 1);
            dialog.check("@rules.infiniteresources", value -> tr.infiniteResources = value, () -> tr.infiniteResources);
            dialog.number("@rules.buildspeedmultiplier", value -> tr.buildSpeedMultiplier = value, () -> tr.buildSpeedMultiplier, 0.001f, 50f);
            dialog.number("@rules.unitdamagemultiplier", value -> tr.unitDamageMultiplier = value, () -> tr.unitDamageMultiplier);
            dialog.number("@rules.unitcrashdamagemultiplier", value -> tr.unitCrashDamageMultiplier = value, () -> tr.unitCrashDamageMultiplier);
            dialog.number("@rules.unitbuildspeedmultiplier", value -> tr.unitBuildSpeedMultiplier = value, () -> tr.unitBuildSpeedMultiplier, 0.001f, 50f);
            dialog.number("@rules.unitcostmultiplier", value -> tr.unitCostMultiplier = value, () -> tr.unitCostMultiplier);
            dialog.number("@rules.unithealthmultiplier", value -> tr.unitHealthMultiplier = value, () -> tr.unitHealthMultiplier);
            dialog.check("@rules.cheat", value -> tr.cheat = value, () -> tr.cheat);
            dialog.check("@rules.coresspawnships", value -> tr.aiCoreSpawn = value, () -> tr.aiCoreSpawn);
            dialog.current = previous;
        }, () -> expanded[0]).growX().row();
    }
    private void addBaseTeamRules(){
        category("teams");
        int[] index = {0};
        dialog.current.getCells().each(cell -> {
            if(index[0] >= Team.baseTeams.length) return;
            if(cell.get() instanceof Table table){
                table.getCells().each(inner -> {
                    if(inner.get() instanceof arc.scene.ui.layout.Collapser){
                        Rules.TeamRule tr = rules.teams.get(Team.baseTeams[index[0]++]);
                        Table old = dialog.current;
                        dialog.current = (Table)Reflect.get(inner.get(), "table");
                        dialog.check("@rules.cheat", tr.cheat, value -> tr.cheat = value);
                        dialog.check("@rules.coresspawnships", tr.aiCoreSpawn, value -> tr.aiCoreSpawn = value);
                        dialog.current = old;
                    }
                });
            }
        });
    }
    private void addRevealedBlocks(){
        category("miscellaneous");
        if(matches("rules.revealedblocks")){
            dialog.ruleInfo(dialog.current.table(table -> table.button("@rules.revealedblocks", Icon.eye, () -> revealed.show(rules.revealedBlocks)).width(300f).left().row()).fillX(), "@rules.revealedblocks");
            dialog.current.row();
        }
    }
    private void addPlanetBackground(){
        category("miscellaneous");
        if(matches("rules.planetbackground")){
            dialog.ruleInfo(dialog.current.table(table -> table.button("@rules.planetbackground", Icon.planet, () -> new starlight.mapping.dialogs.PlanetBackgroundDialog(rules).show()).width(300f).left().row()).fillX(), "@rules.planetbackground");
            dialog.current.row();
        }
    }
    private void text(Table table, String label, arc.func.Cons<String> consumer, arc.func.Prov<String> provider){
        Cell<Table> cell = table.table(inner -> {
            inner.left();
            inner.add(label).left().padRight(5f);
            inner.field(provider.get(), consumer).padRight(100f);
        });
        dialog.ruleInfo(cell, label);
        table.row();
    }
    private void environmentDialog(Rules target){
        BaseDialog popup = new BaseDialog("@rules.title.environment");
        popup.cont.add("@rules.env.warning").color(Pal.accent).center().padBottom(20f).row();
        popup.cont.pane(table -> {
            table.left().defaults().growX().left().pad(5f);
            envCheck(table, "@rules.env.terrestrial", Env.terrestrial, "@rules.env.terrestrial.description", target);
            envCheck(table, "@rules.env.space", Env.space, "@rules.env.space.description", target);
            envCheck(table, "@rules.env.underwater", Env.underwater, "@rules.env.underwater.description", target);
            envCheck(table, "@rules.env.spores", Env.spores, "@rules.env.spores.description", target);
            envCheck(table, "@rules.env.scorching", Env.scorching, "@rules.env.scorching.description", target);
            envCheck(table, "@rules.env.groundOil", Env.groundOil, "@rules.env.groundOil.description", target);
            envCheck(table, "@rules.env.groundWater", Env.groundWater, "@rules.env.groundWater.description", target);
            envCheck(table, "@rules.env.oxygen", Env.oxygen, "@rules.env.oxygen.description", target);
        }).fillX();
        popup.addCloseButton();
        popup.show();
    }
    private void envCheck(Table table, String title, int bit, String description, Rules target){
        CheckBox check = new CheckBox(title);
        check.changed(() -> target.env = check.isChecked() ? target.env | bit : target.env & ~bit);
        check.setChecked((target.env & bit) != 0);
        table.add(check).left().row();
        Cell<Label> cell = table.add(description);
        cell.get().setWidth(600f);
        cell.get().setWrap(true);
        table.row();
    }
          }
