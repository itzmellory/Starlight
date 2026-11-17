package starlight.mapping.dialogs;
import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.scene.ui.layout.Scl;
import mindustry.content.Planets;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.editor.BannedContentDialog;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Planet;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.type.Category;
import mindustry.ui.dialogs.BaseDialog;
import static arc.Core.settings;
import static mindustry.Vars.*;
public class BetterBannedContentDialog<T extends UnlockableContent> extends BannedContentDialog<T>{
    public boolean isRevealed;
    private final ContentType type;
    private Table selectedTable;
    private Table deselectedTable;
    private ObjectSet<T> contentSet;
    private final Boolf<T> predicate;
    private String search = "";
    private Category selectedCategory;
    private Seq<T> filtered;
    private Seq<UnlockableContent> tabs;
    private UnlockableContent tab = Planets.sun;
    public BetterBannedContentDialog(String title, ContentType type, Boolf<T> predicate){
        super(title, type, predicate);
        this.type = type;
        this.predicate = predicate;
        shown(this::build);
        resized(this::build);
        ObjectSet<UnlockableContent> all = new ObjectSet<>();
        for(Seq<?> group : content.getContentMap()){
            for(Object item : group){
                if(item instanceof UnlockableContent unlockable) all.addAll(unlockable.databaseTabs);
            }
        }
        tabs = all.toSeq().sort();
        tabs.insert(0, Planets.sun);
    }
    @Override
    @SuppressWarnings("unchecked")
    public void show(ObjectSet set){
        contentSet = (ObjectSet<T>)set;
        super.show();
    }
    @Override
    public void build(){
        cont.clear();
        var header = cont.table(table -> {
            table.table(searchRow -> {
                searchRow.label(() -> "@search").padRight(10f);
                var field = searchRow.field(search, value -> {
                    search = value.trim().replaceAll(" +", " ").toLowerCase();
                    rebuild();
                }).get();
                searchRow.button(Icon.cancel, Styles.emptyi, () -> {
                    search = "";
                    field.setText("");
                    rebuild();
                }).size(35f).padLeft(8f);
                searchRow.table(tabTable -> {
                    for(UnlockableContent item : tabs){
                        tabTable.button(item == Planets.sun ? Icon.eyeSmall : item instanceof Planet planet ? Icon.icons.get(planet.icon, Icon.commandRally) : new TextureRegionDrawable(item.uiIcon), Styles.clearNoneTogglei, iconMed, () -> {
                            tab = item;
                            rebuild();
                        }).size(48f).checked(button -> tab == item).tooltip(item == Planets.sun ? "@all" : item.localizedName).with(button -> {
                            button.getStyle().imageUpColor = item instanceof Planet planet ? planet.iconColor : Color.white.cpy();
                        });
                    }
                }).padLeft(8f);
            });
            if(type == ContentType.block){
                table.row();
                table.table(categories -> {
                    categories.defaults().marginRight(4f);
                    for(Category category : Category.values()){
                        categories.button(ui.getIcon(category.name()), Styles.squareTogglei, () -> {
                            selectedCategory = selectedCategory == category ? null : category;
                            rebuild();
                        }).size(42f).update(button -> button.setChecked(selectedCategory == category));
                    }
                }).center();
            }
        });
        cont.row();
        if(!Core.graphics.isPortrait()) header.colspan(2);
        cont.table(table -> {
            table.add(isRevealed ? "@revealed_content" : type == ContentType.block ? "@bannedblocks" : "@bannedunits").color(isRevealed ? Pal.accent : Color.valueOf("f25555")).row();
            table.image().color(isRevealed ? Pal.accent : Color.valueOf("f25555")).height(3f).fillX().padBottom(5f).row();
            table.pane(pane -> selectedTable = pane).grow().row();
            table.button("@addall", Icon.add, () -> {
                contentSet.addAll(filtered);
                rebuild();
            }).fillX().padTop(8f);
        }).grow();
        if(Core.graphics.isPortrait()) cont.row();
        cont.table(table -> {
            table.add(isRevealed ? "@unrevealed_content" : type == ContentType.block ? "@unbannedblocks" : "@unbannedunits").color(isRevealed ? Color.valueOf("f25555") : Pal.accent).row();
            table.image().color(isRevealed ? Color.valueOf("f25555") : Pal.accent).height(3f).fillX().padBottom(5f).row();
            table.pane(pane -> deselectedTable = pane).grow().row();
            table.button("@addall", Icon.add, () -> {
                contentSet.removeAll(filtered);
                rebuild();
            }).fillX().padTop(8f);
        }).grow();
        rebuild();
    }
    private void rebuild(){
        filtered = content.<T>getBy(type).select(predicate);
        if(!search.isEmpty()) filtered.removeAll(item -> !item.localizedName.toLowerCase().contains(search));
        if(type == ContentType.block) filtered.removeAll(item -> selectedCategory != null && ((Block)item).category != selectedCategory);
        filtered.removeAll(item -> tab != Planets.sun && !item.allDatabaseTabs && !item.databaseTabs.contains(tab));
        rebuildTable(selectedTable, true);
        rebuildTable(deselectedTable, false);
    }
    private void rebuildTable(Table table, boolean selected){
        if(table == null) return;
        table.clear();
        int size = settings.getInt("editor_content_buttons_size", 50);
        int columns = Core.graphics.isPortrait() ? Math.max(4, (int)((Core.graphics.getWidth() / Scl.scl() - 100f) / size)) : Math.max(4, (int)((Core.graphics.getWidth() / Scl.scl() - 300f) / size / 2f));
        Seq<T> items = selected ? contentSet.toSeq() : content.<T>getBy(type).copy();
        if(!selected) items.removeAll(contentSet.toSeq());
        items.removeAll(item -> !filtered.contains(item));
        items.sort();
        if(items.isEmpty()){
            table.add("@empty").width(size * columns).center();
            return;
        }
        int index = 0;
        for(T item : items){
            ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNonei);
            button.getStyle().imageUp = new TextureRegionDrawable((TextureRegion)item.uiIcon);
            button.resizeImage(size - 8f);
            button.clicked(() -> {
                if(selected) contentSet.remove(item);
                else contentSet.add(item);
                rebuild();
            });
            table.add(button).size(size).tooltip(item.localizedName);
            if(++index % columns == 0) table.row();
        }
    }
}
