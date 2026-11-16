package starlight.generation;
import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.util.Strings;
import arc.util.noise.Simplex;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import starlight.ui.ModFilterOptions;
public class AdvancedRuinFilter extends GenerateFilter {
   public Block replaceOverlay;
   public CenterMode centerMode;
   public GenerationMode generationMode;
   public StructurePreset structurePreset;
   public Block structureFloor;
   public Block structureMainFloor;
   public Block structureWall;
   public Block structureAccentWall;
   public Block structureDamageFloor;
   public float autoDensity;
   public float minCenterSpacing;
   public RuinStep[] steps;
   public float scl;
   public float threshold;
   public float octaves;
   public float falloff;
   public float tilt;
   public float distortScl;
   public float distortMag;
   private transient IntSeq centers;
   private transient CenterIndex centerIndex;
   private transient TileState[] computed;
   private transient boolean[] floorModified;
   private transient boolean[] blockModified;
   private transient boolean mapComputed;
   private transient int tilesLeft;
   private transient int computedWidth;
   private transient int computedHeight;
   private transient GenerateFilter.GenerateInput computedInput;
   private static transient Method tileMethod;
   public AdvancedRuinFilter() {
      this.replaceOverlay = Blocks.coreZone;
      this.centerMode = AdvancedRuinFilter.CenterMode.auto;
      this.generationMode = AdvancedRuinFilter.GenerationMode.structured;
      this.structurePreset = AdvancedRuinFilter.StructurePreset.large;
      this.structureFloor = Blocks.metalFloor;
      this.structureMainFloor = Blocks.darkPanel1;
      this.structureWall = Blocks.darkMetal;
      this.structureAccentWall = Blocks.duneWall;
      this.structureDamageFloor = Blocks.metalFloorDamaged;
      this.autoDensity = 25.0F;
      this.minCenterSpacing = 24.0F;
      this.steps = AdvancedRuinFilter.RuinStep.defaults();
      this.scl = 40.0F;
      this.threshold = 0.5F;
      this.octaves = 3.0F;
      this.falloff = 0.5F;
      this.tilt = 0.0F;
      this.distortScl = 10.0F;
      this.distortMag = 5.0F;
   }
   public String name() {
      return Core.bundle.get("filter.advancedruin.name", "Advanced Ruins");
   }
   public char icon() {
      return '\uf8a0';
   }
   public FilterOption[] options() {
      this.ensureDefaults();
      ArrayList<FilterOption> o = new ArrayList();
      o.add(new ModFilterOptions.BlockOption("replace", () -> this.replaceOverlay, (b) -> this.replaceOverlay = b, FilterOption.oresFloorsOptional));
      o.add(new CenterModeOption(this));
      o.add(new ModFilterOptions.SliderOption("autodensity", () -> this.autoDensity, (v) -> this.autoDensity = v, 0.0F, 100.0F, 1.0F));
      o.add(new ModFilterOptions.SliderOption("mincenterspacing", () -> this.minCenterSpacing, (v) -> this.minCenterSpacing = v, 0.0F, 256.0F, 1.0F));
      o.add(new GenerationModeOption(this));
      if (this.generationMode == AdvancedRuinFilter.GenerationMode.structured) {
         o.add(new StructurePresetOption(this));
         o.add(new ModFilterOptions.BlockOption("structurefloor", () -> this.structureFloor, (b) -> this.structureFloor = b, FilterOption.floorsOptional));
         o.add(new ModFilterOptions.BlockOption("structuremainfloor", () -> this.structureMainFloor, (b) -> this.structureMainFloor = b, FilterOption.floorsOptional));
         o.add(new ModFilterOptions.BlockOption("structurewall", () -> this.structureWall, (b) -> this.structureWall = b, (b) -> b == Blocks.air || FilterOption.wallsOptional.get(b) && b.isStatic()));
         o.add(new ModFilterOptions.BlockOption("structureaccentwall", () -> this.structureAccentWall, (b) -> this.structureAccentWall = b, (b) -> b == Blocks.air || FilterOption.wallsOptional.get(b) && b.isStatic()));
         o.add(new ModFilterOptions.BlockOption("structuredamagefloor", () -> this.structureDamageFloor, (b) -> this.structureDamageFloor = b, FilterOption.floorsOptional));
      } else {
         o.add(new StepsEditOption(this));
      }
      o.add(new ModFilterOptions.SliderOption("scale", () -> this.scl, (v) -> this.scl = v, 1.0F, 500.0F, 1.0F));
      o.add(new ModFilterOptions.SliderOption("threshold", () -> this.threshold, (v) -> this.threshold = v, 0.0F, 1.0F, 0.01F));
      o.add(new ModFilterOptions.SliderOption("octaves", () -> this.octaves, (v) -> this.octaves = v, 1.0F, 10.0F, 1.0F));
      o.add(new ModFilterOptions.SliderOption("falloff", () -> this.falloff, (v) -> this.falloff = v, 0.0F, 1.0F, 0.01F));
      o.add(new ModFilterOptions.SliderOption("tilt", () -> this.tilt, (v) -> this.tilt = v, -4.0F, 4.0F, 0.01F));
      o.add(new ModFilterOptions.SliderOption("distortscale", () -> this.distortScl, (v) -> this.distortScl = v, 1.0F, 20.0F, 0.1F));
      o.add(new ModFilterOptions.SliderOption("distortmag", () -> this.distortMag, (v) -> this.distortMag = v, 0.0F, 10.0F, 0.1F));
      return (FilterOption[])o.toArray(new FilterOption[0]);
   }
   public void apply(GenerateFilter.GenerateInput in) {
      if (in != null && in.width > 0 && in.height > 0) {
         int size = in.width * in.height;
         if (this.mapComputed && this.computedInput != in) {
            this.resetComputedMap();
         }
         if (this.mapComputed && in.x == 0 && in.y == 0 && this.tilesLeft < size) {
            this.resetComputedMap();
         }
         if (!this.mapComputed || this.computedWidth != in.width || this.computedHeight != in.height) {
            this.ensureDefaults();
            this.computedWidth = in.width;
            this.computedHeight = in.height;
            this.computedInput = in;
            this.rebuildCenters(in);
            this.computeMap(in);
            this.mapComputed = true;
            this.tilesLeft = size;
         }
         this.writeTile(in);
         if (--this.tilesLeft <= 0) {
            this.resetComputedMap();
         }
      }
   }
   private void computeMap(GenerateFilter.GenerateInput in) {
      int w = in.width;
      int h = in.height;
      int size = w * h;
      this.computed = new TileState[size];
      this.floorModified = new boolean[size];
      this.blockModified = new boolean[size];
      for(int i = 0; i < size; ++i) {
         this.computed[i] = this.readState(in, i % w, i / w);
      }
      TileState[] bl = copyStates(this.computed);
      boolean struct = this.usesStructuredMode(w, h);
      if (struct) {
         this.computeStructured(bl, w, h);
      } else if (this.steps != null && this.steps.length > 0 && this.centers != null && this.centers.size > 0) {
         this.centerIndex = new CenterIndex(w, h, this.maxStepRadius());
         for(int i = 0; i < this.centers.size; ++i) {
            this.centerIndex.add(this.centers.get(i) % w, this.centers.get(i) / w);
         }
         for(RuinStep step : this.steps) {
            if (step != null) {
               for(int x = 0; x < w; ++x) {
                  for(int y = 0; y < h; ++y) {
                     if (this.passesNoise(x, y)) {
                        int i = x + y * w;
                        if (this.centerIndex != null && this.centerIndex.hasMatch(x, y, step) && (this.centerMode != AdvancedRuinFilter.CenterMode.auto || this.isAutoProcessable(bl[i]))) {
                           this.applyStep(this.computed[i], bl[i], step, i);
                        }
                     }
                  }
               }
            }
         }
      }
      if (!struct) {
         this.applyDistort(this.computed, bl, w, h);
      }
   }
   private void computeStructured(TileState[] bl, int w, int h) {
      int size = w * h;
      StructuredRuinLayout.Spec sp = AdvancedRuinFilter.StructuredRuinLayout.spec(w, h, this.structurePreset);
      Block[] gf = new Block[size];
      Block[] gw = new Block[size];
      boolean[] gfb = new boolean[size];
      boolean[] gwb = new boolean[size];
      boolean[] cf = new boolean[size];
      boolean[] cw = new boolean[size];
      Block sw = this.structureWallBlock();
      Block aw = this.structureAccentWallBlock();
      Block dw = this.structureDebrisWallBlock();
      for(int ci = 0; ci < this.centers.size; ++ci) {
         int cX = this.centers.get(ci) % w;
         int cY = this.centers.get(ci) / w;
         StructuredRuinLayout.Layout l = AdvancedRuinFilter.StructuredRuinLayout.generate(w, h, cX, cY, this.seed, this.structurePreset);
         for(int ly = 0; ly < l.span; ++ly) {
            for(int lx = 0; lx < l.span; ++lx) {
               int loc = lx + ly * l.span;
               int x = l.originX + lx;
               int y = l.originY + ly;
               if (x >= 0 && y >= 0 && x < w && y < h) {
                  int i = x + y * w;
                  if (this.centerMode != AdvancedRuinFilter.CenterMode.auto || this.isAutoProcessable(bl[i])) {
                     if (l.floor[loc]) {
                        boolean p = l.criticalFloor[loc];
                        Block f = l.mainFloor[loc] ? this.structureMainFloor : (l.panelFloor[loc] ? this.structurePanelFloor(x, y, cX, cY) : this.structureFloor);
                        if (!(f instanceof Floor)) {
                           f = this.structureFloor;
                        }
                        if (!p && this.structuredDamage(x, y, cX, cY, sp.floorDamage, 49)) {
                           f = this.structureDamageFloorVariant(x, y, cX, cY);
                        }
                        if (f instanceof Floor) {
                           if (!gfb[i] || p || !cf[i]) {
                              gf[i] = f;
                              cf[i] = p;
                           }
                           gfb[i] = true;
                           gwb[i] = false;
                           gw[i] = null;
                        }
                     } else if ((l.wall[loc] || l.debrisWall[loc]) && !gfb[i]) {
                        boolean p = l.criticalWall[loc];
                        Block wl = l.mainWall[loc] ? sw : (l.debrisWall[loc] ? dw : aw);
                        if (!p && this.structuredDamage(x, y, cX, cY, sp.wallDamage, 87)) {
                           wl = null;
                        }
                        if (wl != null && wl != Blocks.air) {
                           if (!gwb[i] || p || !cw[i]) {
                              gw[i] = wl;
                              cw[i] = p;
                           }
                           gwb[i] = true;
                        }
                     }
                  }
               }
            }
         }
      }
      for(int i = 0; i < size; ++i) {
         if (gfb[i]) {
            this.computed[i].floor = (Floor)gf[i];
            this.floorModified[i] = true;
         }
         if (gwb[i] && !gfb[i]) {
            this.computed[i].block = gw[i];
            this.blockModified[i] = true;
         }
      }
   }
   private boolean structuredDamage(int x, int y, int cX, int cY, float c, int salt) {
      if (c <= 0.0F) {
         return false;
      } else {
         float s = finiteAtLeast(this.distortScl, 1.0F);
         float m = finiteAtLeast(this.distortMag, 0.0F);
         float wx = (float)x + this.distortionNoise(salt, (float)x, (float)y, s, m / 2.0F);
         float wy = (float)y + this.distortionNoise(salt + 1, (float)x, (float)y, s, m / 2.0F);
         return this.passesNoise(wx, wy) && random01(this.seed ^ salt ^ cX * 1663821211 ^ cY * -2062189835, x, y) < c;
      }
   }
   private Block structureWallBlock() {
      return this.structureWall != Blocks.air && !this.structureWall.isStatic() ? Blocks.darkMetal : this.structureWall;
   }
   private Block structureAccentWallBlock() {
      return this.structureAccentWall != Blocks.air && !this.structureAccentWall.isStatic() ? Blocks.duneWall : this.structureAccentWall;
   }
   private Block structureDebrisWallBlock() {
      if (this.structureWall == Blocks.scrapWall) {
         return this.structureWall;
      } else {
         return this.structureAccentWall == Blocks.scrapWall ? this.structureAccentWall : Blocks.scrapWall;
      }
   }
   private Block structurePanelFloor(int x, int y, int cX, int cY) {
      int v = this.seed ^ x * -1640531527 ^ y * -2048144789 ^ cX * -1028477387 ^ cY * 668265261;
      v ^= v >>> 16;
      switch (v & 3) {
         case 0 -> {
            return Blocks.darkPanel2;
         }
         case 1 -> {
            return Blocks.darkPanel3;
         }
         case 2 -> {
            return Blocks.darkPanel4;
         }
         default -> {
            return Blocks.darkPanel2;
         }
      }
   }
   private Block structureDamageFloorVariant(int x, int y, int cX, int cY) {
      int v = this.seed ^ x * 1663821211 ^ y * -2062189835 ^ cX * -1028477387 ^ cY * 668265261;
      v ^= v >>> 16;
      switch (v & 15) {
         case 5 -> {
            return Blocks.metalFloorDamaged;
         }
         case 6 -> {
            return Blocks.metalFloor2;
         }
         case 7 -> {
            return Blocks.metalFloor3;
         }
         case 8 -> {
            return Blocks.metalFloor4;
         }
         case 9 -> {
            return Blocks.metalFloor5;
         }
         case 10 -> {
            return Blocks.darkPanel5;
         }
         case 11 -> {
            return Blocks.darkPanel6;
         }
         default -> {
            return this.structureDamageFloor;
         }
      }
   }
   private void applyDistort(TileState[] s, TileState[] bl, int w, int h) {
      float sc = finiteAtLeast(this.distortScl, 1.0F);
      float m = finiteAtLeast(this.distortMag, 0.0F);
      if (!(m <= 0.0F)) {
         int size = w * h;
         TileState[] src = copyStates(s);
         TileState[] dst = copyStates(bl);
         boolean[] nf = new boolean[size];
         boolean[] nb = new boolean[size];
         for(int x = 0; x < w; ++x) {
            for(int y = 0; y < h; ++y) {
               int i = x + y * w;
               if (this.floorModified[i] || this.blockModified[i]) {
                  int ox = sampleCoordinate(x, this.distortionNoise(0, (float)x, (float)y, sc, m / 2.0F), w);
                  int oy = sampleCoordinate(y, this.distortionNoise(1, (float)x, (float)y, sc, m / 2.0F), h);
                  int di = ox + oy * w;
                  if (this.floorModified[i]) {
                     dst[di].floor = src[i].floor;
                     nf[di] = true;
                  }
                  if (this.blockModified[i]) {
                     dst[di].block = src[i].block;
                     nb[di] = true;
                  }
               }
            }
         }
         System.arraycopy(dst, 0, s, 0, size);
         this.floorModified = nf;
         this.blockModified = nb;
      }
   }
   private void writeTile(GenerateFilter.GenerateInput in) {
      int x = clampCoordinate(in.x, in.width);
      int y = clampCoordinate(in.y, in.height);
      int i = x + y * in.width;
      if (this.computed != null && (this.floorModified[i] || this.blockModified[i])) {
         TileState st = this.computed[i];
         if (this.floorModified[i] && st.floor != null) {
            in.floor = st.floor;
            Block ov = in.overlay == null ? Blocks.air : in.overlay;
            if (!st.floor.hasSurface() && ov instanceof OreBlock && ov instanceof Floor && ((Floor)ov).needsSurface) {
               in.overlay = Blocks.air;
            }
         }
         if (this.blockModified[i]) {
            Block cb = in.block == null ? Blocks.air : in.block;
            if (!cb.synthetic() && st.block != null && !st.block.synthetic()) {
               in.block = st.block;
            }
         }
      }
   }
   private void rebuildCenters(GenerateFilter.GenerateInput in) {
      if (this.centers == null) {
         this.centers = new IntSeq();
      }
      this.centers.clear();
      if (this.centerMode == AdvancedRuinFilter.CenterMode.auto) {
         this.rebuildAutomaticCenters(in);
      } else {
         Block m = this.replaceOverlay == null ? Blocks.coreZone : this.replaceOverlay;
         if (m != null) {
            for(int x = 0; x < in.width; ++x) {
               for(int y = 0; y < in.height; ++y) {
                  Tile t = readTile(in, x, y);
                  if (t != null && t.overlay() == m) {
                     this.centers.add(x + y * in.width);
                  } else if (t == null && x == in.x && y == in.y && in.overlay == m) {
                     this.centers.add(x + y * in.width);
                  }
               }
            }
         }
      }
   }
   private void rebuildAutomaticCenters(GenerateFilter.GenerateInput in) {
      float d = clamp(this.autoDensity, 0.0F, 100.0F) / 100.0F;
      float spc = finiteAtLeast(this.minCenterSpacing, 0.0F);
      boolean st = this.usesStructuredMode(in.width, in.height);
      StructuredRuinLayout.Spec sp = st ? AdvancedRuinFilter.StructuredRuinLayout.spec(in.width, in.height, this.structurePreset) : null;
      float esp = Math.max(spc, st ? (float)sp.span : 0.0F);
      CenterIndex sel = new CenterIndex(in.width, in.height, (float)Math.max(1, (int)Math.ceil((double)esp)));
      StructureIndex occ = st ? new StructureIndex(in.width, in.height, sp.span) : null;
      int sx = positiveHash(this.seed, 324508639, 0) % in.width;
      int sy = positiveHash(this.seed, 610839776, 1) % in.height;
      boolean singleLarge = st && this.structurePreset == AdvancedRuinFilter.StructurePreset.large;
      for(int ox = 0; ox < in.width; ++ox) {
         int x = (sx + ox) % in.width;
         for(int oy = 0; oy < in.height; ++oy) {
            int y = (sy + oy) % in.height;
            TileState state = this.readState(in, x, y);
            if (this.isAutoCenterCandidate(state) && !(d <= 0.0F) && (!(d < 1.0F) || !(random01(this.seed, x, y) > d)) && (!(esp > 0.0F) || !sel.hasNearby(x, y, esp))) {
               StructuredRuinLayout.Bounds b = null;
               if (st) {
                  b = AdvancedRuinFilter.StructuredRuinLayout.bounds(in.width, in.height, x, y, this.structurePreset);
                  if (!b.contains(in.width, in.height) || occ.conflicts(b)) {
                     continue;
                  }
               }
               this.centers.add(x + y * in.width);
               sel.add(x, y);
               if (st) {
                  occ.add(b);
               }
               if (singleLarge) {
                  return;
               }
            }
         }
      }
   }
   private boolean isAutoCenterCandidate(TileState s) {
      return s.floor != null && !s.floor.isLiquid && s.block != null && !s.block.synthetic();
   }
   private boolean isAutoProcessable(TileState s) {
      return this.isAutoCenterCandidate(s);
   }
   private void applyStep(TileState s, TileState bl, RuinStep st, int i) {
      if (st.preservesFloor()) {
         s.floor = bl.floor;
         this.floorModified[i] = false;
      } else if (st.hasFloor() && st.floor instanceof Floor) {
         s.floor = (Floor)st.floor;
         this.floorModified[i] = true;
      }
      if (st.hasWall() && st.wall != null && st.wall != Blocks.air) {
         s.block = st.wall;
         this.blockModified[i] = true;
      }
   }
   private boolean passesNoise(int x, int y) {
      return this.passesNoise((float)x, (float)y);
   }
   private boolean passesNoise(float x, float y) {
      float sc = finiteAtLeast(this.scl, 1.0F);
      float vl = finiteOr(this.threshold, 0.5F);
      float oc = finiteAtLeast(this.octaves, 1.0F);
      float pe = clamp(finiteOr(this.falloff, 0.5F), 0.0F, 1.0F);
      float sk = finiteOr(this.tilt, 0.0F);
      return this.noise(x, y + x * sk, sc, 1.0F, oc, pe) <= vl;
   }
   private float distortionNoise(int so, float x, float y, float sc, float mg) {
      return Simplex.noise2d(this.seed + so, (double)1.0F, (double)0.0F, (double)(1.0F / sc), (double)(x + 10.0F), (double)(y + 10.0F)) * mg;
   }
   private float maxStepRadius() {
      float m = 0.0F;
      if (this.steps != null) {
         for(RuinStep s : this.steps) {
            if (s != null && Float.isFinite(s.radius)) {
               m = Math.max(m, s.radius);
            }
         }
      }
      return m;
   }
   private TileState readState(GenerateFilter.GenerateInput in, int x, int y) {
      Tile t = readTile(in, x, y);
      if (t != null) {
         return AdvancedRuinFilter.TileState.from(t);
      } else {
         return x == in.x && y == in.y ? AdvancedRuinFilter.TileState.from(in) : AdvancedRuinFilter.TileState.empty();
      }
   }
   private static Tile readTile(GenerateFilter.GenerateInput in, int x, int y) {
      try {
         if (tileMethod != null) {
            return (Tile)tileMethod.invoke(in, (float)x, (float)y);
         }
      } catch (Exception var4) {
      }
      return Vars.world != null && Vars.world.tiles != null ? Vars.world.tile(x, y) : null;
   }
   private void ensureDefaults() {
      if (this.replaceOverlay == null) {
         this.replaceOverlay = Blocks.coreZone;
      }
      if (this.centerMode == null) {
         this.centerMode = AdvancedRuinFilter.CenterMode.auto;
      }
      if (this.generationMode == null) {
         this.generationMode = AdvancedRuinFilter.GenerationMode.structured;
      }
      if (this.structurePreset == null) {
         this.structurePreset = AdvancedRuinFilter.StructurePreset.large;
      }
      if (this.structureFloor == null) {
         this.structureFloor = Blocks.metalFloor;
      }
      if (this.structureMainFloor == null) {
         this.structureMainFloor = Blocks.darkPanel1;
      }
      if (this.structureWall == null) {
         this.structureWall = Blocks.darkMetal;
      }
      if (this.structureAccentWall == null) {
         this.structureAccentWall = Blocks.duneWall;
      }
      if (this.structureDamageFloor == null) {
         this.structureDamageFloor = Blocks.metalFloorDamaged;
      }
      if (this.steps == null) {
         this.steps = AdvancedRuinFilter.RuinStep.defaults();
      }
   }
   private boolean usesStructuredMode(int w, int h) {
      return this.generationMode == AdvancedRuinFilter.GenerationMode.structured && Math.min(w, h) >= 24;
   }
   private void resetComputedMap() {
      this.mapComputed = false;
      this.tilesLeft = 0;
      this.computed = null;
      this.floorModified = null;
      this.blockModified = null;
      this.centerIndex = null;
      this.computedInput = null;
   }
   private static TileState[] copyStates(TileState[] st) {
      TileState[] c = new TileState[st.length];
      for(int i = 0; i < st.length; ++i) {
         c[i] = st[i].copy();
      }
      return c;
   }
   private static int sampleCoordinate(int c, float o, int s) {
      return clampCoordinate((int)((float)c + o), s);
   }
   private static int clampCoordinate(int c, int s) {
      return Math.max(0, Math.min(s - 1, c));
   }
   private static float clamp(float v, float mn, float mx) {
      return Math.max(mn, Math.min(mx, v));
   }
   private static float finiteOr(float v, float fb) {
      return Float.isFinite(v) ? v : fb;
   }
   private static float finiteAtLeast(float v, float mn) {
      return Math.max(mn, finiteOr(v, mn));
   }
   private static int positiveHash(int sd, int slt, int ax) {
      int v = sd ^ slt ^ ax * -1640531527;
      v ^= v >>> 16;
      v *= 2146121005;
      v ^= v >>> 15;
      return v & Integer.MAX_VALUE;
   }
   private static float random01(int sd, int x, int y) {
      int v = sd ^ x * -1640531527;
      v = Integer.rotateLeft(v, 13);
      v ^= y * -2048144789;
      v = Integer.rotateLeft(v, 17);
      v *= -1028477387;
      v ^= v >>> 16;
      return (float)(v & Integer.MAX_VALUE) / (float)Integer.MAX_VALUE;
   }
   static {
      try {
         tileMethod = GenerateFilter.GenerateInput.class.getDeclaredMethod("tile", Float.TYPE, Float.TYPE);
         tileMethod.setAccessible(true);
      } catch (Exception var1) {
      }
   }
   private static class TileState {
      Block block;
      Floor floor;
      Block overlay;
      static TileState from(Tile t) {
         TileState s = new TileState();
         s.block = t.block();
         s.floor = t.floor();
         s.overlay = t.overlay();
         return s;
      }
      static TileState from(GenerateFilter.GenerateInput in) {
         TileState s = new TileState();
         s.block = in.block == null ? Blocks.air : in.block;
         s.floor = in.floor instanceof Floor ? (Floor)in.floor : Blocks.stone.asFloor();
         s.overlay = in.overlay == null ? Blocks.air : in.overlay;
         return s;
      }
      static TileState empty() {
         TileState s = new TileState();
         s.block = Blocks.air;
         s.floor = Blocks.stone.asFloor();
         s.overlay = Blocks.air;
         return s;
      }
      TileState copy() {
         TileState s = new TileState();
         s.block = this.block;
         s.floor = this.floor;
         s.overlay = this.overlay;
         return s;
      }
   }
   private static final class CenterIndex {
      final int w;
      final int cs;
      final int cw;
      final int ch;
      final IntSeq[] b;
      CenterIndex(int wid, int hgt, float r) {
         this.w = wid;
         this.cs = Math.max(1, (int)Math.ceil((double)Math.max(0.0F, r)));
         this.cw = Math.max(1, (wid + this.cs - 1) / this.cs);
         this.ch = Math.max(1, (hgt + this.cs - 1) / this.cs);
         this.b = new IntSeq[this.cw * this.ch];
      }
      void add(int x, int y) {
         int bi = Math.max(0, Math.min(this.cw - 1, x / this.cs)) + Math.max(0, Math.min(this.ch - 1, y / this.cs)) * this.cw;
         if (this.b[bi] == null) {
            this.b[bi] = new IntSeq();
         }
         this.b[bi].add(x + y * this.w);
      }
      boolean hasNearby(int x, int y, float r) {
         if (r <= 0.0F) {
            return false;
         } else {
            int mx = Math.max(0, (int)Math.floor((double)(((float)x - r) / (float)this.cs)));
            int xx = Math.min(this.cw - 1, (int)Math.floor((double)(((float)x + r) / (float)this.cs)));
            int my = Math.max(0, (int)Math.floor((double)(((float)y - r) / (float)this.cs)));
            int xy = Math.min(this.ch - 1, (int)Math.floor((double)(((float)y + r) / (float)this.cs)));
            float sq = r * r;
            for(int bx = mx; bx <= xx; ++bx) {
               for(int by = my; by <= xy; ++by) {
                  IntSeq v = this.b[bx + by * this.cw];
                  if (v != null) {
                     for(int i = 0; i < v.size; ++i) {
                        float dx = (float)(x - v.get(i) % this.w);
                        float dy = (float)(y - v.get(i) / this.w);
                        if (dx * dx + dy * dy < sq) {
                           return true;
                        }
                     }
                  }
               }
            }
            return false;
         }
      }
      boolean hasMatch(int x, int y, RuinStep st) {
         float r = st != null && Float.isFinite(st.radius) ? Math.max(0.0F, st.radius) : 0.0F;
         int mx = Math.max(0, (int)Math.floor((double)(((float)x - r) / (float)this.cs)));
         int xx = Math.min(this.cw - 1, (int)Math.floor((double)(((float)x + r) / (float)this.cs)));
         int my = Math.max(0, (int)Math.floor((double)(((float)y - r) / (float)this.cs)));
         int xy = Math.min(this.ch - 1, (int)Math.floor((double)(((float)y + r) / (float)this.cs)));
         for(int bx = mx; bx <= xx; ++bx) {
            for(int by = my; by <= xy; ++by) {
               IntSeq v = this.b[bx + by * this.cw];
               if (v != null) {
                  for(int i = 0; i < v.size; ++i) {
                     if (st.matches(v.get(i) % this.w, v.get(i) / this.w, x, y)) {
                        return true;
                     }
                  }
               }
            }
         }
         return false;
      }
   }
   private static final class StructureIndex {
      final int cs;
      final int cw;
      final int ch;
      final IntSeq[] b;
      final List<StructuredRuinLayout.Bounds> bd = new ArrayList();
      StructureIndex(int w, int h, int c) {
         this.cs = Math.max(1, c);
         this.cw = Math.max(1, (w + this.cs - 1) / this.cs);
         this.ch = Math.max(1, (h + this.cs - 1) / this.cs);
         this.b = new IntSeq[this.cw * this.ch];
      }
      void add(StructuredRuinLayout.Bounds v) {
         int i = this.bd.size();
         this.bd.add(v);
         for(int x = Math.max(0, v.minX / this.cs); x <= Math.min(this.cw - 1, v.maxX / this.cs); ++x) {
            for(int y = Math.max(0, v.minY / this.cs); y <= Math.min(this.ch - 1, v.maxY / this.cs); ++y) {
               if (this.b[x + y * this.cw] == null) {
                  this.b[x + y * this.cw] = new IntSeq();
               }
               this.b[x + y * this.cw].add(i);
            }
         }
      }
      boolean conflicts(StructuredRuinLayout.Bounds v) {
         for(int x = Math.max(0, v.minX / this.cs); x <= Math.min(this.cw - 1, v.maxX / this.cs); ++x) {
            for(int y = Math.max(0, v.minY / this.cs); y <= Math.min(this.ch - 1, v.maxY / this.cs); ++y) {
               IntSeq bk = this.b[x + y * this.cw];
               if (bk != null) {
                  for(int i = 0; i < bk.size; ++i) {
                     if (v.intersects((StructuredRuinLayout.Bounds)this.bd.get(bk.get(i)))) {
                        return true;
                     }
                  }
               }
            }
         }
         return false;
      }
   }
   public static enum CenterMode {
      overlay,
      auto;
      public CenterMode next() {
         return this == overlay ? auto : overlay;
      }
      private static CenterMode[] $values() {
         return new CenterMode[]{overlay, auto};
      }
      private static CenterMode[] $values$() {
         return new CenterMode[]{overlay, auto};
      }
   }
   public static enum GenerationMode {
      scatter,
      structured;
      public GenerationMode next() {
         return this == scatter ? structured : scatter;
      }
      private static GenerationMode[] $values() {
         return new GenerationMode[]{scatter, structured};
      }
      private static GenerationMode[] $values$() {
         return new GenerationMode[]{scatter, structured};
      }
   }
   public static enum StructurePreset {
      small,
      medium,
      large;
      public StructurePreset next() {
         StructurePreset[] v = values();
         return v[(this.ordinal() + 1) % v.length];
      }
      private static StructurePreset[] $values() {
         return new StructurePreset[]{small, medium, large};
      }
      private static StructurePreset[] $values$() {
         return new StructurePreset[]{small, medium, large};
      }
   }
   public static class CenterModeOption extends FilterOption {
      final AdvancedRuinFilter f;
      public CenterModeOption(AdvancedRuinFilter f) {
         this.f = f;
      }
      public void build(Table t) {
         t.add("@filter.option.centermode").width(160.0F).left().style(Styles.outlineLabel).wrap();
         t.button((b) -> b.label(() -> Core.bundle.get("filter.option.centermode." + this.f.centerMode.name(), this.f.centerMode.name())), Styles.flatt, () -> {
            this.f.centerMode = this.f.centerMode.next();
            if (this.changed != null) {
               this.changed.run();
            }
         }).size(130.0F, 32.0F).right().row();
      }
   }
   public static class GenerationModeOption extends FilterOption {
      final AdvancedRuinFilter f;
      public GenerationModeOption(AdvancedRuinFilter f) {
         this.f = f;
      }
      public void build(Table t) {
         t.add("@filter.option.generationmode").width(160.0F).left().style(Styles.outlineLabel).wrap();
         t.button((b) -> b.label(() -> Core.bundle.get("filter.option.generationmode." + this.f.generationMode.name(), this.f.generationMode.name())), Styles.flatt, () -> {
            this.f.generationMode = this.f.generationMode.next();
            if (this.changed != null) {
               this.changed.run();
            }
         }).size(130.0F, 32.0F).right().row();
      }
   }
   public static class StructurePresetOption extends FilterOption {
      final AdvancedRuinFilter f;
      public StructurePresetOption(AdvancedRuinFilter f) {
         this.f = f;
      }
      public void build(Table t) {
         t.add("@filter.option.structurepreset").width(160.0F).left().style(Styles.outlineLabel).wrap();
         t.button((b) -> b.label(() -> Core.bundle.get("filter.option.structurepreset." + this.f.structurePreset.name(), this.f.structurePreset.name())), Styles.flatt, () -> {
            this.f.structurePreset = this.f.structurePreset.next();
            if (this.changed != null) {
               this.changed.run();
            }
         }).size(130.0F, 32.0F).right().row();
      }
   }
   public static class StepsEditOption extends FilterOption {
      final AdvancedRuinFilter f;
      public StepsEditOption(AdvancedRuinFilter f) {
         this.f = f;
      }
      public void build(Table t) {
         t.button("@filter.option.ruinsteps", Styles.flatt, this::openDialog).colspan(2).width(200.0F).height(36.0F).padTop(2.0F).padBottom(2.0F).row();
      }
      private void openDialog() {
         BaseDialog d = new BaseDialog("@filter.option.ruinsteps");
         Table list = new Table();
         Runnable[] rebuild = new Runnable[1];
         rebuild[0] = () -> {
            list.clearChildren();
            if (this.f.steps == null) {
               this.f.steps = AdvancedRuinFilter.RuinStep.defaults();
            }
            for(int idx = 0; idx < this.f.steps.length; ++idx) {
               RuinStep st = this.f.steps[idx];
               Table row = new Table(Tex.button);
               row.margin(6.0F);
               row.add("#" + (idx + 1)).width(20.0F);
               row.button((b) -> b.image(st.floor == null ? Blocks.air.uiIcon : st.floor.uiIcon).size(24.0F), () -> this.pickBlock(floorsOptional, (b) -> {
                     st.floor = b;
                     rebuild[0].run();
                     if (this.changed != null) {
                        this.changed.run();
                     }
                  })).size(36.0F).pad(2.0F);
               row.button((b) -> b.image(st.wall == null ? Blocks.air.uiIcon : st.wall.uiIcon).size(24.0F), () -> this.pickBlock((b) -> b == Blocks.air || wallsOptional.get(b), (b) -> {
                     st.wall = b == Blocks.air ? null : b;
                     rebuild[0].run();
                     if (this.changed != null) {
                        this.changed.run();
                     }
                  })).size(36.0F).pad(2.0F);
               row.button(st.stepMode.name(), Styles.flatt, () -> {
                  st.stepMode = st.stepMode.next();
                  rebuild[0].run();
                  if (this.changed != null) {
                     this.changed.run();
                  }
               }).size(80.0F, 32.0F).pad(2.0F);
               Slider sl = new Slider(0.0F, 32.0F, 0.5F, false);
               sl.setValue(st.radius);
               Label rl = new Label(Strings.fixed(st.radius, 1));
               sl.moved((v) -> {
                  st.radius = v;
                  rl.setText(Strings.fixed(v, 1));
                  if (this.changed != null) {
                     this.changed.run();
                  }
               });
               row.add(sl).width(70.0F).pad(2.0F);
               row.add(rl).width(30.0F);
               row.button(Icon.trash, Styles.flati, () -> {
                  ArrayList<RuinStep> l = new ArrayList(Arrays.asList(this.f.steps));
                  l.remove(idx);
                  this.f.steps = (RuinStep[])l.toArray(new RuinStep[0]);
                  rebuild[0].run();
                  if (this.changed != null) {
                     this.changed.run();
                  }
               }).size(32.0F);
               list.add(row).growX().pad(2.0F).row();
            }
         };
         rebuild[0].run();
         d.cont.pane(list).size(Math.min((float)Core.graphics.getWidth() * 0.95F, 520.0F), 340.0F).row();
         d.cont.table((bt) -> {
            bt.button("@add", Icon.add, () -> {
               ArrayList<RuinStep> l = new ArrayList(Arrays.asList(this.f.steps));
               l.add(AdvancedRuinFilter.RuinStep.floor(4.0F, Blocks.metalFloor));
               this.f.steps = (RuinStep[])l.toArray(new RuinStep[0]);
               rebuild[0].run();
               if (this.changed != null) {
                  this.changed.run();
               }
            }).size(140.0F, 40.0F).pad(4.0F);
            bt.button("Reset", Icon.refresh, () -> {
               this.f.steps = AdvancedRuinFilter.RuinStep.defaults();
               rebuild[0].run();
               if (this.changed != null) {
                  this.changed.run();
               }
            }).size(140.0F, 40.0F).pad(4.0F);
         });
         d.addCloseButton();
         d.show();
      }
      private void pickBlock(Boolf<Block> filter, Cons<Block> cons) {
         BaseDialog bd = new BaseDialog("");
         bd.cont.pane((t) -> {
            int c = 0;
            for(Block b : Vars.content.blocks()) {
               if (filter.get(b)) {
                  t.button((btn) -> btn.image(b.uiIcon).size(28.0F), Styles.flati, () -> {
                     cons.get(b);
                     bd.hide();
                  }).size(38.0F).pad(2.0F);
                  ++c;
                  if (c % 10 == 0) {
                     t.row();
                  }
               }
            }
         }).size(Math.min((float)Core.graphics.getWidth() * 0.9F, 440.0F), 320.0F);
         bd.addCloseButton();
         bd.show();
      }
   }
   public static class RuinStep implements Serializable {
      public float radius;
      public Block floor;
      public Block wall;
      public StepMode stepMode;
      public RuinStep() {
         this.stepMode = AdvancedRuinFilter.RuinStep.StepMode.chebyshev;
      }
      public RuinStep(float r, Block f, Block w) {
         this.stepMode = AdvancedRuinFilter.RuinStep.StepMode.chebyshev;
         this.radius = r;
         this.floor = f;
         this.wall = w;
      }
      public RuinStep copy() {
         RuinStep s = new RuinStep(this.radius, this.floor, this.wall);
         s.stepMode = this.stepMode;
         return s;
      }
      public boolean hasFloor() {
         return this.floor != null && this.floor != Blocks.air;
      }
      public boolean preservesFloor() {
         return this.floor != null && this.floor == Blocks.air;
      }
      public boolean hasWall() {
         return this.wall != null && this.wall != Blocks.air;
      }
      public boolean matches(int cx, int cy, int x, int y) {
         if (Float.isFinite(this.radius) && !(this.radius < 0.0F)) {
            float dx = (float)(x - cx);
            float dy = (float)(y - cy);
            StepMode m = this.stepMode == null ? AdvancedRuinFilter.RuinStep.StepMode.chebyshev : this.stepMode;
            switch (m) {
               case geometric -> {
                  return Math.sqrt((double)(dx * dx + dy * dy)) <= (double)this.radius;
               }
               case manhattan -> {
                  return Math.abs(dx) + Math.abs(dy) <= this.radius;
               }
               default -> {
                  return Math.max(Math.abs(dx), Math.abs(dy)) <= this.radius;
               }
            }
         } else {
            return false;
         }
      }
      public static RuinStep floor(float r, Block f) {
         return new RuinStep(r, f, (Block)null);
      }
      public static RuinStep wall(float r, Block w) {
         return new RuinStep(r, (Block)null, w);
      }
      public static RuinStep[] defaults() {
         return new RuinStep[]{floor(6.0F, Blocks.metalFloor), floor(1.0F, Blocks.metalFloor3), wall(4.0F, Blocks.darkMetal)};
      }
      public static enum StepMode {
         geometric,
         manhattan,
         chebyshev;
         public StepMode next() {
            StepMode[] v = values();
            return v[(this.ordinal() + 1) % v.length];
         }
         private static StepMode[] $values() {
            return new StepMode[]{geometric, manhattan, chebyshev};
         }
         private static StepMode[] $values$() {
            return new StepMode[]{geometric, manhattan, chebyshev};
         }
      }
   }
   static final class StructuredRuinLayout {
      static Spec spec(int mapWidth, int mapHeight, StructurePreset preset) {
         int shortSide = Math.min(mapWidth, mapHeight);
         StructurePreset safePreset = preset == null ? AdvancedRuinFilter.StructurePreset.large : preset;
         float ratio;
         float wallDamage;
         float floorDamage;
         int minimumSpan;
         int minimumRoomArea;
         int maximumRoomArea;
         int minimumRooms;
         int maximumRooms;
         int corridorWidth;
         int mainCorridorWidth;
         switch (safePreset) {
            case small:
               ratio = 0.22F;
               minimumSpan = 22;
               minimumRoomArea = 32;
               maximumRoomArea = 80;
               minimumRooms = 4;
               maximumRooms = 7;
               corridorWidth = 2;
               mainCorridorWidth = 4;
               wallDamage = 0.2F;
               floorDamage = 0.12F;
               break;
            case medium:
               ratio = 0.34F;
               minimumSpan = 34;
               minimumRoomArea = 64;
               maximumRoomArea = 180;
               minimumRooms = 7;
               maximumRooms = 12;
               corridorWidth = 3;
               mainCorridorWidth = 5;
               wallDamage = 0.24F;
               floorDamage = 0.16F;
               break;
            default:
               ratio = 0.8F;
               minimumSpan = 80;
               minimumRoomArea = 96;
               maximumRoomArea = 320;
               minimumRooms = 12;
               maximumRooms = 18;
               corridorWidth = 3;
               mainCorridorWidth = 7;
               wallDamage = 0.28F;
               floorDamage = 0.2F;
         }
         int targetSpan = Math.round((float)shortSide * ratio);
         int span = Math.max(8, Math.min(shortSide, Math.max(minimumSpan, targetSpan)));
         float roomScale = Math.max(1.0F, (float)span / 96.0F);
         int scaledMin = Math.max(16, Math.round((float)minimumRoomArea * roomScale));
         int scaledMax = Math.max(scaledMin, Math.round((float)maximumRoomArea * roomScale));
         return new Spec(span, scaledMin, scaledMax, minimumRooms, maximumRooms, corridorWidth, mainCorridorWidth, wallDamage, floorDamage);
      }
      static Bounds bounds(int mapWidth, int mapHeight, int centerX, int centerY, StructurePreset preset) {
         Spec s = spec(mapWidth, mapHeight, preset);
         int ox = centerX - s.span / 2;
         int oy = centerY - s.span / 2;
         return new Bounds(ox, oy, ox + s.span - 1, oy + s.span - 1);
      }
      static Layout generate(int mapWidth, int mapHeight, int centerX, int centerY, int seed, StructurePreset preset) {
         Spec spec = spec(mapWidth, mapHeight, preset);
         StructurePreset lp = preset == null ? AdvancedRuinFilter.StructurePreset.large : preset;
         int span = spec.span;
         int ox = centerX - span / 2;
         int oy = centerY - span / 2;
         Random random = new Random(mixSeed(seed, centerX, centerY, lp));
         boolean[] floor = new boolean[span * span];
         boolean[] criticalFloor = new boolean[span * span];
         boolean[] mainFloor = new boolean[span * span];
         List<Room> rooms = new ArrayList();
         List<Room> mainAnchors = new ArrayList();
         Room hall = createRoom(random, spec, span, span / 2, span / 2);
         rooms.add(hall);
         carveRoom(floor, criticalFloor, span, hall, true);
         int roomTarget = spec.minimumRooms + random.nextInt(spec.maximumRooms - spec.minimumRooms + 1);
         int mainRoomTarget = Math.min(roomTarget - 1, lp == AdvancedRuinFilter.StructurePreset.large ? 4 : 3);
         mainAnchors.add(hall);
         if (lp == AdvancedRuinFilter.StructurePreset.large) {
            for(int side = 0; side < 4; ++side) {
               Room p = createPerimeterRoom(random, spec, span, side, rooms);
               if (p != null) {
                  rooms.add(p);
                  carveRoom(floor, criticalFloor, span, p, true);
                  carveRoomConnection(floor, criticalFloor, mainFloor, span, hall, p, spec.mainCorridorWidth, random, true);
                  mainAnchors.add(p);
               }
            }
         }
         for(int idx = rooms.size(); idx < roomTarget; ++idx) {
            boolean mr = idx <= mainRoomTarget;
            Room parent = chooseParent(rooms, mainAnchors, random, mr);
            Room cand = null;
            for(int att = 0; att < 48 && cand == null; ++att) {
               Room nx = createAdjacentRoom(random, spec, span, parent, random.nextInt(4));
               if (nx != null && !overlaps(nx, rooms, 1)) {
                  cand = nx;
               }
            }
            if (cand == null) {
               for(int att = 0; att < 160 && cand == null; ++att) {
                  int x = 2 + random.nextInt(Math.max(1, span - 4));
                  int y = 2 + random.nextInt(Math.max(1, span - 4));
                  Room nx = createRoom(random, spec, span, x, y);
                  if (nx != null && !overlaps(nx, rooms, 1)) {
                     cand = nx;
                  }
               }
            }
            if (cand != null) {
               rooms.add(cand);
               carveRoom(floor, criticalFloor, span, cand, false);
               int w = mr ? Math.max(4, spec.mainCorridorWidth - (idx > 1 ? 1 : 0)) : spec.corridorWidth;
               carveRoomConnection(floor, criticalFloor, mainFloor, span, parent, cand, w, random, mr);
               if (mr) {
                  mainAnchors.add(cand);
               }
            }
         }
         int extra = Math.max(1, rooms.size() / 5);
         for(int c = 0; c < extra; ++c) {
            Room f1 = (Room)rooms.get(1 + random.nextInt(Math.max(1, rooms.size() - 1)));
            Room s2 = (Room)rooms.get(random.nextInt(rooms.size()));
            if (f1 != s2) {
               carveRoomConnection(floor, criticalFloor, mainFloor, span, f1, s2, spec.corridorWidth, random, false);
            }
         }
         if (!roomsConnected(floor, span, rooms)) {
            for(int i = 1; i < rooms.size(); ++i) {
               carveRoomConnection(floor, criticalFloor, mainFloor, span, (Room)rooms.get(i), hall, spec.corridorWidth, random, false);
            }
         }
         collapseNonCriticalFloor(floor, criticalFloor, mainFloor, span, rooms, random, lp);
         boolean[] panelFloor = buildPanelFloors(floor, criticalFloor, mainFloor, span, rooms, random);
         boolean[] wall = buildWalls(floor, span);
         boolean[] mainWall = buildMainWalls(mainFloor, wall, span);
         boolean[] debrisWall = buildDebrisWalls(floor, wall, mainFloor, span, random);
         boolean[] criticalWall = buildCriticalWalls(criticalFloor, wall, span);
         return transform(new Layout(ox, oy, span, floor, wall, mainFloor, mainWall, panelFloor, debrisWall, criticalFloor, criticalWall), random);
      }
      static Room createRoom(Random r, Spec sp, int spn, int cx, int cy) {
         int[] s = roomSize(r, sp, spn);
         return roomAt(r, spn, cx, cy, s[0], s[1]);
      }
      static int[] roomSize(Random r, Spec sp, int spn) {
         int md = Math.max(4, Math.min(spn - 4, Math.max(14, Math.min(48, Math.round((float)spn * 0.18F)))));
         int w = 0;
         int h = 0;
         for(int a = 0; a < 40; ++a) {
            int cw = 4 + r.nextInt(Math.max(1, md - 3));
            int ch = 4 + r.nextInt(Math.max(1, md - 3));
            int ar = cw * ch;
            if (ar >= sp.minimumRoomArea && ar <= sp.maximumRoomArea && cw <= ch * 2 && ch <= cw * 2) {
               w = cw;
               h = ch;
               break;
            }
         }
         if (w == 0) {
            int t = (sp.minimumRoomArea + sp.maximumRoomArea) / 2;
            w = Math.max(4, Math.min(md, (int)Math.sqrt((double)t)));
            h = Math.max(4, Math.min(md, (t + w - 1) / w));
         }
         return new int[]{w, h};
      }
      static Room roomAt(Random r, int spn, int cx, int cy, int w, int h) {
         if (w >= 4 && h >= 4 && w <= spn - 2 && h <= spn - 2) {
            int l = Math.max(1, Math.min(spn - w - 1, cx - w / 2));
            int t = Math.max(1, Math.min(spn - h - 1, cy - h / 2));
            return new Room(l, t, l + w - 1, t + h - 1, r.nextInt(7));
         } else {
            return null;
         }
      }
      static Room createPerimeterRoom(Random r, Spec sp, int spn, int sd, List<Room> rm) {
         for(int a = 0; a < 64; ++a) {
            int[] sz = roomSize(r, sp, spn);
            int w = sz[0];
            int h = sz[1];
            int jt = Math.max(3, spn / 5);
            int of = Math.round((r.nextFloat() * 2.0F - 1.0F) * (float)jt);
            int cx;
            int cy;
            switch (sd) {
               case 0:
                  cx = spn / 2 + of;
                  cy = 1 + h / 2;
                  break;
               case 1:
                  cx = spn - 2 - w / 2;
                  cy = spn / 2 + of;
                  break;
               case 2:
                  cx = spn / 2 + of;
                  cy = spn - 2 - h / 2;
                  break;
               default:
                  cx = 1 + w / 2;
                  cy = spn / 2 + of;
            }
            Room c = roomAt(r, spn, cx, cy, w, h);
            if (c != null && !overlaps(c, rm, 1)) {
               return c;
            }
         }
         return null;
      }
      static Room createAdjacentRoom(Random r, Spec sp, int spn, Room pr, int sd) {
         int[] sz = roomSize(r, sp, spn);
         int w = sz[0];
         int h = sz[1];
         int g = 2 + r.nextInt(3);
         int l;
         int t;
         switch (sd) {
            case 0:
               t = pr.top - g - h;
               int offsetTop = Math.round((r.nextFloat() * 2.0F - 1.0F) * (float)Math.max(2, pr.width() / 3));
               l = pr.centerX() - w / 2 + offsetTop;
               break;
            case 1:
               l = pr.right + g;
               int offsetRight = Math.round((r.nextFloat() * 2.0F - 1.0F) * (float)Math.max(2, pr.height() / 3));
               t = pr.centerY() - h / 2 + offsetRight;
               break;
            case 2:
               t = pr.bottom + g;
               int offsetBottom = Math.round((r.nextFloat() * 2.0F - 1.0F) * (float)Math.max(2, pr.width() / 3));
               l = pr.centerX() - w / 2 + offsetBottom;
               break;
            default:
               l = pr.left - g - w;
               int offsetLeft = Math.round((r.nextFloat() * 2.0F - 1.0F) * (float)Math.max(2, pr.height() / 3));
               t = pr.centerY() - h / 2 + offsetLeft;
         }
         return l >= 1 && t >= 1 && l + w < spn - 1 && t + h < spn - 1 ? new Room(l, t, l + w - 1, t + h - 1, r.nextInt(7)) : null;
      }
      static Room chooseParent(List<Room> rm, List<Room> ma, Random r, boolean mr) {
         if (!mr) {
            return (Room)rm.get(r.nextInt(rm.size()));
         } else {
            return ma.size() > 1 && r.nextFloat() < 0.65F ? (Room)ma.get(ma.size() - 1) : (Room)ma.get(r.nextInt(ma.size()));
         }
      }
      static boolean overlaps(Room c, List<Room> rm, int pad) {
         for(Room r : rm) {
            if (c.right + pad >= r.left && r.right + pad >= c.left && c.bottom + pad >= r.top && r.bottom + pad >= c.top) {
               return true;
            }
         }
         return false;
      }
      static void carveRoom(boolean[] f, boolean[] cf, int spn, Room r, boolean isC) {
         for(int y = r.top; y <= r.bottom; ++y) {
            for(int x = r.left; x <= r.right; ++x) {
               if (r.contains(x, y)) {
                  int i = x + y * spn;
                  f[i] = true;
                  if (isC || r.isCore(x, y)) {
                     cf[i] = true;
                  }
               }
            }
         }
      }
      static void carveRoomConnection(boolean[] f, boolean[] cf, boolean[] mf, int spn, Room f1, Room s2, int w, Random r, boolean mr) {
         int sd;
         if (s2.bottom < f1.top) {
            sd = 0;
         } else if (s2.left > f1.right) {
            sd = 1;
         } else if (s2.top > f1.bottom) {
            sd = 2;
         } else if (s2.right < f1.left) {
            sd = 3;
         } else {
            int dx = s2.centerX() - f1.centerX();
            int dy = s2.centerY() - f1.centerY();
            sd = Math.abs(dx) >= Math.abs(dy) ? (dx >= 0 ? 1 : 3) : (dy >= 0 ? 2 : 0);
         }
         int[] st = doorway(f1, sd, r);
         int[] en = doorway(s2, (sd + 2) % 4, r);
         int dx = en[0] - st[0];
         int dy = en[1] - st[1];
         float sp = 0.35F + r.nextFloat() * 0.3F;
         if (Math.abs(dx) >= Math.abs(dy)) {
            int mx = Math.round((float)st[0] + (float)dx * sp);
            ch(f, cf, mf, spn, st[0], mx, st[1], w, mr);
            cvr(f, cf, mf, spn, st[1], en[1], mx, w, mr);
            ch(f, cf, mf, spn, mx, en[0], en[1], w, mr);
         } else {
            int my = Math.round((float)st[1] + (float)dy * sp);
            cvr(f, cf, mf, spn, st[1], my, st[0], w, mr);
            ch(f, cf, mf, spn, st[0], en[0], my, w, mr);
            cvr(f, cf, mf, spn, my, en[1], en[0], w, mr);
         }
      }
      static int[] doorway(Room rm, int sd, Random r) {
         boolean hz = sd == 0 || sd == 2;
         int mn = hz ? rm.left + 1 : rm.top + 1;
         int mx = hz ? rm.right - 1 : rm.bottom - 1;
         int pr = mn + r.nextInt(Math.max(1, mx - mn + 1));
         for(int of = 0; of <= Math.max(rm.width(), rm.height()); ++of) {
            int[] cd = new int[]{pr - of, pr + of};
            for(int c : cd) {
               if (c >= mn && c <= mx) {
                  int x = hz ? c : (sd == 1 ? rm.right : rm.left);
                  int y = hz ? (sd == 2 ? rm.bottom : rm.top) : c;
                  if (rm.contains(x, y)) {
                     return new int[]{x, y};
                  }
               }
            }
         }
         return new int[]{hz ? rm.centerX() : (sd == 1 ? rm.right : rm.left), hz ? (sd == 2 ? rm.bottom : rm.top) : rm.centerY()};
      }
      static void ch(boolean[] f, boolean[] cf, boolean[] mf, int spn, int fx, int tx, int y, int w, boolean mr) {
         int l = Math.min(fx, tx);
         int rg = Math.max(fx, tx);
         int h = w / 2;
         for(int x = l; x <= rg; ++x) {
            for(int o = -h; o < w - h; ++o) {
               mk(f, cf, mf, spn, x, y + o, mr);
            }
         }
      }
      static void cvr(boolean[] f, boolean[] cf, boolean[] mf, int spn, int fy, int ty, int x, int w, boolean mr) {
         int t = Math.min(fy, ty);
         int b = Math.max(fy, ty);
         int h = w / 2;
         for(int y = t; y <= b; ++y) {
            for(int o = -h; o < w - h; ++o) {
               mk(f, cf, mf, spn, x + o, y, mr);
            }
         }
      }
      static void mk(boolean[] f, boolean[] cf, boolean[] mf, int spn, int x, int y, boolean mr) {
         if (x >= 1 && y >= 1 && x < spn - 1 && y < spn - 1) {
            int i = x + y * spn;
            f[i] = true;
            cf[i] = true;
            if (mr) {
               mf[i] = true;
            }
         }
      }
      static void collapseNonCriticalFloor(boolean[] f, boolean[] cf, boolean[] mf, int spn, List<Room> rm, Random r, StructurePreset p) {
         float c = p == AdvancedRuinFilter.StructurePreset.small ? 0.1F : (p == AdvancedRuinFilter.StructurePreset.medium ? 0.15F : 0.2F);
         int at = spn * (p == AdvancedRuinFilter.StructurePreset.small ? 2 : (p == AdvancedRuinFilter.StructurePreset.medium ? 3 : 5));
         for(int a = 0; a < at; ++a) {
            if (!(r.nextFloat() > c)) {
               int x = 2 + r.nextInt(Math.max(1, spn - 4));
               int y = 2 + r.nextInt(Math.max(1, spn - 4));
               int w = 1 + r.nextInt(p == AdvancedRuinFilter.StructurePreset.small ? 2 : 3);
               int h = 1 + r.nextInt(3);
               boolean tv = false;
               boolean ok = true;
               for(int ly = y; ly < y + h && ly < spn - 1; ++ly) {
                  for(int lx = x; lx < x + w && lx < spn - 1; ++lx) {
                     int i = lx + ly * spn;
                     if (!f[i] || cf[i] || mf[i] || lx > 0 && mf[lx - 1 + ly * spn] || lx + 1 < spn && mf[lx + 1 + ly * spn] || ly > 0 && mf[lx + (ly - 1) * spn] || ly + 1 < spn && mf[lx + (ly + 1) * spn]) {
                        ok = false;
                        break;
                     }
                     if (lx <= 1 || ly <= 1 || lx >= spn - 2 || ly >= spn - 2 || !f[i - 1] || !f[i + 1] || !f[i - spn] || !f[i + spn]) {
                        tv = true;
                     }
                  }
               }
               if (ok && tv) {
                  for(int ly = y; ly < y + h && ly < spn - 1; ++ly) {
                     for(int lx = x; lx < x + w && lx < spn - 1; ++lx) {
                        f[lx + ly * spn] = false;
                        mf[lx + ly * spn] = false;
                     }
                  }
               }
            }
         }
         int st = ((Room)rm.get(0)).centerX() + ((Room)rm.get(0)).centerY() * spn;
         if (!f[st]) {
            for(int i = 0; i < f.length; ++i) {
               if (f[i] && cf[i]) {
                  st = i;
                  break;
               }
            }
         }
         boolean[] v = new boolean[f.length];
         int[] q = new int[f.length];
         int hd = 0;
         int tl = 0;
         if (f[st]) {
            q[tl++] = st;
            v[st] = true;
         }
         while(hd < tl) {
            int cr = q[hd++];
            int x = cr % spn;
            int y = cr / spn;
            if (x > 0 && f[cr - 1] && !v[cr - 1]) {
               v[cr - 1] = true;
               q[tl++] = cr - 1;
            }
            if (x + 1 < spn && f[cr + 1] && !v[cr + 1]) {
               v[cr + 1] = true;
               q[tl++] = cr + 1;
            }
            if (y > 0 && f[cr - spn] && !v[cr - spn]) {
               v[cr - spn] = true;
               q[tl++] = cr - spn;
            }
            if (y + 1 < spn && f[cr + spn] && !v[cr + spn]) {
               v[cr + spn] = true;
               q[tl++] = cr + spn;
            }
         }
         for(int i = 0; i < f.length; ++i) {
            if (f[i] && !v[i]) {
               f[i] = false;
               cf[i] = false;
               mf[i] = false;
            }
         }
      }
      static boolean roomsConnected(boolean[] f, int spn, List<Room> rm) {
         if (rm.isEmpty()) {
            return false;
         } else {
            boolean[] v = new boolean[f.length];
            int[] q = new int[f.length];
            int hd = 0;
            int tl = 0;
            int st = ((Room)rm.get(0)).centerX() + ((Room)rm.get(0)).centerY() * spn;
            if (!f[st]) {
               return false;
            } else {
               q[tl++] = st;
               v[st] = true;
               while(hd < tl) {
                  int cr = q[hd++];
                  int x = cr % spn;
                  int y = cr / spn;
                  if (x > 0 && f[cr - 1] && !v[cr - 1]) {
                     v[cr - 1] = true;
                     q[tl++] = cr - 1;
                  }
                  if (x + 1 < spn && f[cr + 1] && !v[cr + 1]) {
                     v[cr + 1] = true;
                     q[tl++] = cr + 1;
                  }
                  if (y > 0 && f[cr - spn] && !v[cr - spn]) {
                     v[cr - spn] = true;
                     q[tl++] = cr - spn;
                  }
                  if (y + 1 < spn && f[cr + spn] && !v[cr + spn]) {
                     v[cr + spn] = true;
                     q[tl++] = cr + spn;
                  }
               }
               for(Room r : rm) {
                  if (!v[r.centerX() + r.centerY() * spn]) {
                     return false;
                  }
               }
               return true;
            }
         }
      }
      static boolean[] buildPanelFloors(boolean[] f, boolean[] cf, boolean[] mf, int spn, List<Room> rm, Random r) {
         boolean[] p = new boolean[f.length];
         for(int i = 1; i < rm.size(); ++i) {
            Room ro = (Room)rm.get(i);
            int pc = 1 + r.nextInt(2);
            for(int a = 0; a < pc; ++a) {
               int pw = Math.max(2, Math.min(ro.width(), 3 + r.nextInt(5)));
               int ph = Math.max(2, Math.min(ro.height(), 2 + r.nextInt(4)));
               int l = ro.left + r.nextInt(Math.max(1, ro.width() - pw + 1));
               int t = ro.top + r.nextInt(Math.max(1, ro.height() - ph + 1));
               for(int y = t; y < t + ph; ++y) {
                  for(int x = l; x < l + pw; ++x) {
                     int c = x + y * spn;
                     if (f[c] && !mf[c] && (!cf[c] || r.nextBoolean())) {
                        p[c] = true;
                     }
                  }
               }
            }
         }
         return p;
      }
      static boolean[] buildWalls(boolean[] f, int spn) {
         boolean[] w = new boolean[f.length];
         for(int y = 1; y < spn - 1; ++y) {
            for(int x = 1; x < spn - 1; ++x) {
               int i = x + y * spn;
               if (!f[i]) {
                  w[i] = f[i - 1] || f[i + 1] || f[i - spn] || f[i + spn];
               }
            }
         }
         return w;
      }
      static boolean[] buildMainWalls(boolean[] mf, boolean[] w, int spn) {
         boolean[] mw = new boolean[w.length];
         for(int y = 1; y < spn - 1; ++y) {
            for(int x = 1; x < spn - 1; ++x) {
               int i = x + y * spn;
               if (w[i]) {
                  mw[i] = mf[i - 1] || mf[i + 1] || mf[i - spn] || mf[i + spn];
               }
            }
         }
         return mw;
      }
      static boolean[] buildCriticalWalls(boolean[] cf, boolean[] w, int spn) {
         boolean[] cw = new boolean[w.length];
         for(int y = 1; y < spn - 1; ++y) {
            for(int x = 1; x < spn - 1; ++x) {
               int i = x + y * spn;
               if (w[i]) {
                  cw[i] = cf[i - 1] || cf[i + 1] || cf[i - spn] || cf[i + spn];
               }
            }
         }
         return cw;
      }
      static boolean[] buildDebrisWalls(boolean[] f, boolean[] w, boolean[] mf, int spn, Random r) {
         boolean[] d = new boolean[f.length];
         for(int y = 2; y < spn - 2; ++y) {
            for(int x = 2; x < spn - 2; ++x) {
               int i = x + y * spn;
               if (!f[i] && !w[i] && !mf[i - 1] && !mf[i + 1] && !mf[i - spn] && !mf[i + spn] && (w[i - 1] || w[i + 1] || w[i - spn] || w[i + spn]) && r.nextFloat() < 0.24F) {
                  d[i] = true;
               }
            }
         }
         return d;
      }
      static Layout transform(Layout s, Random r) {
         int spn = s.span;
         int rot = r.nextInt(4);
         boolean mir = r.nextBoolean();
         boolean[] tf = new boolean[spn * spn];
         boolean[] twl = new boolean[spn * spn];
         boolean[] tmf = new boolean[spn * spn];
         boolean[] tmw = new boolean[spn * spn];
         boolean[] tpf = new boolean[spn * spn];
         boolean[] tdw = new boolean[spn * spn];
         boolean[] tcf = new boolean[spn * spn];
         boolean[] tcw = new boolean[spn * spn];
         for(int y = 0; y < spn; ++y) {
            for(int x = 0; x < spn; ++x) {
               int tx = x;
               int ty = y;
               if (mir) {
                  tx = spn - 1 - x;
               }
               for(int t = 0; t < rot; ++t) {
                  int nx = spn - 1 - ty;
                  ty = tx;
                  tx = nx;
               }
               int si = x + y * spn;
               int ti = tx + ty * spn;
               tf[ti] = s.floor[si];
               twl[ti] = s.wall[si];
               tmf[ti] = s.mainFloor[si];
               tmw[ti] = s.mainWall[si];
               tpf[ti] = s.panelFloor[si];
               tdw[ti] = s.debrisWall[si];
               tcf[ti] = s.criticalFloor[si];
               tcw[ti] = s.criticalWall[si];
            }
         }
         return new Layout(s.originX, s.originY, spn, tf, twl, tmf, tmw, tpf, tdw, tcf, tcw);
      }
      static long mixSeed(int sd, int cx, int cy, StructurePreset p) {
         long v = (long)sd * -7046029254386353131L;
         v ^= (long)cx * -4417276706812531889L;
         v ^= (long)cy * 1609587929392839161L;
         v ^= (long)(p == null ? 2 : p.ordinal() + 1) * -7723592293110705685L;
         v ^= v >>> 30;
         v *= -4658895280553007687L;
         v ^= v >>> 27;
         v *= -7723592293110705685L;
         return v ^ v >>> 31;
      }
      static final class Spec {
         final int span;
         final int minimumRoomArea;
         final int maximumRoomArea;
         final int minimumRooms;
         final int maximumRooms;
         final int corridorWidth;
         final int mainCorridorWidth;
         final float wallDamage;
         final float floorDamage;
         Spec(int s, int mnA, int mxA, int mnR, int mxR, int cw, int mw, float wd, float fd) {
            this.span = s;
            this.minimumRoomArea = mnA;
            this.maximumRoomArea = mxA;
            this.minimumRooms = mnR;
            this.maximumRooms = mxR;
            this.corridorWidth = cw;
            this.mainCorridorWidth = mw;
            this.wallDamage = wd;
            this.floorDamage = fd;
         }
      }
      static final class Bounds {
         final int minX;
         final int minY;
         final int maxX;
         final int maxY;
         Bounds(int mnX, int mnY, int mxX, int mxY) {
            this.minX = mnX;
            this.minY = mnY;
            this.maxX = mxX;
            this.maxY = mxY;
         }
         boolean contains(int w, int h) {
            return this.minX >= 0 && this.minY >= 0 && this.maxX < w && this.maxY < h;
         }
         boolean intersects(Bounds o) {
            return this.minX <= o.maxX && this.maxX >= o.minX && this.minY <= o.maxY && this.maxY >= o.minY;
         }
      }
      static final class Layout {
         final int originX;
         final int originY;
         final int span;
         final boolean[] floor;
         final boolean[] wall;
         final boolean[] mainFloor;
         final boolean[] mainWall;
         final boolean[] panelFloor;
         final boolean[] debrisWall;
         final boolean[] criticalFloor;
         final boolean[] criticalWall;
         Layout(int ox, int oy, int s, boolean[] f, boolean[] w, boolean[] mf, boolean[] mw, boolean[] pf, boolean[] dw, boolean[] cf, boolean[] cw) {
            this.originX = ox;
            this.originY = oy;
            this.span = s;
            this.floor = f;
            this.wall = w;
            this.mainFloor = mf;
            this.mainWall = mw;
            this.panelFloor = pf;
            this.debrisWall = dw;
            this.criticalFloor = cf;
            this.criticalWall = cw;
         }
      }
      static final class Room {
         final int left;
         final int top;
         final int right;
         final int bottom;
         final int shape;
         Room(int l, int t, int r, int b, int s) {
            this.left = l;
            this.top = t;
            this.right = r;
            this.bottom = b;
            this.shape = s;
         }
         int centerX() {
            return (this.left + this.right) / 2;
         }
         int centerY() {
            return (this.top + this.bottom) / 2;
         }
         int width() {
            return this.right - this.left + 1;
         }
         int height() {
            return this.bottom - this.top + 1;
         }
         boolean isCore(int x, int y) {
            return Math.abs(x - this.centerX()) <= 1 && Math.abs(y - this.centerY()) <= 1;
         }
         boolean contains(int x, int y) {
            int lx = x - this.left;
            int ly = y - this.top;
            int wid = this.width();
            int hgt = this.height();
            if (this.shape != 0 && wid >= 6 && hgt >= 6) {
               boolean tl = lx < 2 && ly < 2;
               boolean tr = lx >= wid - 2 && ly < 2;
               boolean bl = lx < 2 && ly >= hgt - 2;
               boolean br = lx >= wid - 2 && ly >= hgt - 2;
               if (this.shape == 1) {
                  return !tl && !br;
               } else if (this.shape == 2) {
                  return !tr && !bl;
               } else if (this.shape == 3) {
                  return !tl && !tr;
               } else if (this.shape == 4) {
                  return !bl && !br;
               } else if (this.shape == 5) {
                  return (lx != 0 || ly < hgt / 3 || ly > hgt * 2 / 3) && (lx != wid - 1 || ly < hgt / 4 || ly > hgt * 3 / 4);
               } else {
                  return (ly != 0 || lx < wid / 3 || lx > wid * 2 / 3) && (ly != hgt - 1 || lx < wid / 4 || lx > wid * 3 / 4);
               }
            } else {
               return true;
            }
         }
      }
   }
}
