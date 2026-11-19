package starlight.generation;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.maps.filters.FilterOption;
import mindustry.ui.Styles;
import static mindustry.Vars.iconSmall;
public final class RuinFilterOptions {
    private RuinFilterOptions() {
    }
    public static final class CenterModeOption extends FilterOption {
        private final AdvancedRuinFilter filter;
        public CenterModeOption(AdvancedRuinFilter filter) {
            this.filter = filter;
        }
        @Override
        public void build(Table table) {
            table.button(button -> button.add("@filter.option.center-mode." + filter.centerMode)
                    .update(label -> label.setText("@filter.option.center-mode." + filter.centerMode)),
                Styles.flatBordert, () -> {
                    filter.centerMode = filter.centerMode.next();
                    changed.run();
                }).pad(4f).margin(8f);
            table.add("@filter.option.center-mode");
        }
    }
    public static final class GenerationModeOption extends FilterOption {
        private final AdvancedRuinFilter filter;
        public GenerationModeOption(AdvancedRuinFilter filter) {
            this.filter = filter;
        }
        @Override
        public void build(Table table) {
            table.button(button -> button.add("@filter.option.generation-mode." + filter.generationMode)
                    .update(label -> label.setText("@filter.option.generation-mode." + filter.generationMode)),
                Styles.flatBordert, () -> {
                    filter.generationMode = filter.generationMode.next();
                    if (filter.generationMode == AdvancedRuinFilter.GenerationMode.structured
                        && filter.structurePreset == null) {
                        filter.structurePreset = AdvancedRuinFilter.StructurePreset.large;
                    }
                    changed.run();
                }).pad(4f).margin(8f);
            table.add("@filter.option.generation-mode");
        }
    }
    public static final class StructurePresetOption extends FilterOption {
        private final AdvancedRuinFilter filter;
        public StructurePresetOption(AdvancedRuinFilter filter) {
            this.filter = filter;
        }
        @Override
        public void build(Table table) {
            table.button(button -> button.add("@filter.option.structure-preset." + filter.structurePreset)
                    .update(label -> label.setText("@filter.option.structure-preset." + filter.structurePreset)),
                Styles.flatBordert, () -> {
                    filter.structurePreset = filter.structurePreset.next();
                    changed.run();
                }).pad(4f).margin(8f);
            table.add("@filter.option.structure-preset");
        }
    }
    public static final class StepsEditOption extends FilterOption {
        private final AdvancedRuinFilter filter;
        public StepsEditOption(AdvancedRuinFilter filter) {
            this.filter = filter;
        }
        @Override
        public void build(Table table) {
            table.button(button -> button.image(Icon.edit).size(iconSmall),
                () -> new RuinStepsDialog(filter, changed).show()).pad(4f).margin(8f);
            table.add("@btg.filter.option.steps");
        }
    }
}
