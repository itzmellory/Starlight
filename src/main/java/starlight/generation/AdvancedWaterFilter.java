package starlight.generation;
import arc.Core;
import arc.util.noise.Simplex;
import java.util.Arrays;
import mindustry.content.Blocks;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import starlight.ui.ModFilterOptions;
public class AdvancedWaterFilter extends GenerateFilter {
   public float waterCoverage = 35.0F;
   public float waterScale = 90.0F;
   public float contourComplexity = 0.55F;
   public float edgeBias = 0.0F;
   public float shoalWidth = 2.0F;
   public float shallowWidth = 5.0F;
   public float depthWarpScale = 0.02F;
   public float depthWarpMag = 3.0F;
   public float shoalFragmentation = 0.0F;
   public float shallowFragmentation = 0.0F;
   public Block shoalFloor;
   public Block shallowFloor;
   public Block deepFloor;
   public boolean naturalCleanup;
   private transient volatile CacheEntry cache;
   public AdvancedWaterFilter() {
      this.shoalFloor = Blocks.sandWater;
      this.shallowFloor = Blocks.water;
      this.deepFloor = Blocks.deepwater;
      this.naturalCleanup = true;
   }
   public String name() {
      return Core.bundle.get("filter.advancedwater.name", "Natural Water");
   }
   public char icon() {
      return '\uf828';
   }
   public FilterOption[] options() {
      this.ensureDefaultFloors();
      return new FilterOption[]{new ModFilterOptions.SliderOption("watercoverage", () -> this.waterCoverage, (v) -> this.waterCoverage = v, 0.0F, 100.0F, 1.0F), new ModFilterOptions.SliderOption("waterscale", () -> this.waterScale, (v) -> this.waterScale = v, 4.0F, 300.0F, 1.0F), new ModFilterOptions.SliderOption("contourcomplexity", () -> this.contourComplexity, (v) -> this.contourComplexity = v, 0.0F, 1.0F, 0.01F), new ModFilterOptions.SliderOption("edgebias", () -> this.edgeBias, (v) -> this.edgeBias = v, -1.0F, 1.0F, 0.01F), new ModFilterOptions.SliderOption("shoalwidth", () -> this.shoalWidth, (v) -> this.shoalWidth = v, 0.0F, 30.0F, 1.0F), new ModFilterOptions.SliderOption("shallowwidth", () -> this.shallowWidth, (v) -> this.shallowWidth = v, 0.0F, 50.0F, 1.0F), new ModFilterOptions.SliderOption("depthwarpscale", () -> this.depthWarpScale, (v) -> this.depthWarpScale = v, 0.005F, 0.2F, 0.005F), new ModFilterOptions.SliderOption("depthwarpmag", () -> this.depthWarpMag, (v) -> this.depthWarpMag = v, 0.0F, 10.0F, 0.5F), new ModFilterOptions.SliderOption("shoalfragmentation", () -> this.shoalFragmentation, (v) -> this.shoalFragmentation = v, 0.0F, 1.0F, 0.01F), new ModFilterOptions.SliderOption("shallowfragmentation", () -> this.shallowFragmentation, (v) -> this.shallowFragmentation = v, 0.0F, 1.0F, 0.01F), new ModFilterOptions.BlockOption("shoalfloor", () -> this.shoalFloor, (v) -> this.shoalFloor = v, ModFilterOptions.floorsOnly), new ModFilterOptions.BlockOption("shallowfloor", () -> this.shallowFloor, (v) -> this.shallowFloor = v, ModFilterOptions.floorsOnly), new ModFilterOptions.BlockOption("deepfloor", () -> this.deepFloor, (v) -> this.deepFloor = v, ModFilterOptions.floorsOnly), new ModFilterOptions.ToggleOption("naturalcleanup", () -> this.naturalCleanup, (v) -> this.naturalCleanup = v)};
   }
   public void apply(GenerateFilter.GenerateInput in) {
      this.ensureDefaultFloors();
      CacheEntry entry = this.ensureCache(in.width, in.height);
      int x = Math.max(0, Math.min(entry.width - 1, in.x));
      int y = Math.max(0, Math.min(entry.height - 1, in.y));
      byte layer = entry.layers[x + y * entry.width];
      if (layer != 0) {
         in.floor = layer == 1 ? this.validFloor(this.shoalFloor, Blocks.sandWater) : (layer == 2 ? this.validFloor(this.shallowFloor, Blocks.water) : this.validFloor(this.deepFloor, Blocks.deepwater));
         if (in.block == Blocks.air && in.overlay != Blocks.spawn) {
            in.overlay = Blocks.air;
         }
      }
   }
   private CacheEntry ensureCache(int w, int h) {
      CacheKey k = new CacheKey(w, h, this.seed, this.waterCoverage, this.waterScale, this.contourComplexity, this.edgeBias, this.shoalWidth, this.shallowWidth, this.depthWarpScale, this.depthWarpMag, this.shoalFragmentation, this.shallowFragmentation, this.naturalCleanup, this.blockName(this.shoalFloor), this.blockName(this.shallowFloor), this.blockName(this.deepFloor));
      CacheEntry c = this.cache;
      if (c != null && c.key.equals(k)) {
         return c;
      } else {
         synchronized(this) {
            c = this.cache;
            if (c != null && c.key.equals(k)) {
               return c;
            } else {
               byte[] l = this.generateLayers(w, h);
               c = new CacheEntry(k, w, h, l);
               this.cache = c;
               return c;
            }
         }
      }
   }
   private void ensureDefaultFloors() {
      if (!(this.shoalFloor instanceof Floor)) {
         this.shoalFloor = Blocks.sandWater;
      }
      if (!(this.shallowFloor instanceof Floor)) {
         this.shallowFloor = Blocks.water;
      }
      if (!(this.deepFloor instanceof Floor)) {
         this.deepFloor = Blocks.deepwater;
      }
   }
   private Block validFloor(Block v, Block f) {
      return v instanceof Floor ? v : f;
   }
   private String blockName(Block b) {
      return b == null ? "" : b.name;
   }
   private float clamp(float v, float min, float max) {
      return !Float.isNaN(v) && !Float.isInfinite(v) ? Math.max(min, Math.min(max, v)) : min;
   }
   private byte[] generateLayers(int w, int h) {
      int area = w * h;
      float cov = this.clamp(this.waterCoverage / 100.0F, 0.0F, 1.0F);
      int tw = Math.max(0, Math.min(area, Math.round((float)area * cov)));
      int pad = Math.max(8, Math.min(64, (int)Math.ceil((double)(Math.max(0.0F, this.shoalWidth) + Math.max(0.0F, this.shallowWidth))) + 4));
      int pw = w + pad * 2;
      int ph = h + pad * 2;
      float[] f = new float[pw * ph];
      float sc = Math.max(4.0F, this.waterScale);
      float cx = this.clamp(this.contourComplexity, 0.0F, 1.0F);
      float eb = this.clamp(this.edgeBias, -1.0F, 1.0F);
      float ws = sc * (1.25F - cx * 0.35F);
      float wm = sc * (0.07F + cx * 0.24F);
      float ds = Math.max(3.0F, sc * (0.22F + cx * 0.18F));
      int oct = 3 + Math.round(cx * 3.0F);
      float per = 0.46F + cx * 0.12F;
      float hm = Math.max(1.0F, (float)Math.min(w, h) * 0.5F);
      for(int py = 0; py < ph; ++py) {
         int y = py - pad;
         for(int px = 0; px < pw; ++px) {
            int x = px - pad;
            float nx = Simplex.noise2d(this.seed + 12081, (double)2.0F, (double)0.55F, (double)(1.0F / ws), (double)((float)x + 10.0F), (double)((float)y + 10.0F)) * 2.0F - 1.0F;
            float ny = Simplex.noise2d(this.seed - 23063, (double)2.0F, (double)0.55F, (double)(1.0F / ws), (double)((float)x + 10.0F), (double)((float)y + 10.0F)) * 2.0F - 1.0F;
            float sx = (float)x + nx * wm;
            float sy = (float)y + ny * wm;
            float base = Simplex.noise2d(this.seed, (double)oct, (double)per, (double)(1.0F / sc), (double)(sx + 10.0F), (double)(sy + 10.0F)) * 2.0F - 1.0F;
            float det = Simplex.noise2d(this.seed + 29003, (double)2.0F, (double)0.5F, (double)(1.0F / ds), (double)(sx + 10.0F), (double)(sy + 10.0F)) * 2.0F - 1.0F;
            float me = (float)Math.min(Math.min(x, w - 1 - x), Math.min(y, h - 1 - y));
            float ei = 1.0F - this.clamp(me / hm, 0.0F, 1.0F);
            f[px + py * pw] = base + det * (0.12F + cx * 0.34F) - eb * ei * 1.15F;
         }
      }
      float[] cv = new float[w * h];
      for(int y = 0; y < h; ++y) {
         System.arraycopy(f, pad + (y + pad) * pw, cv, y * w, w);
      }
      Arrays.sort(cv);
      boolean[] wt = new boolean[pw * ph];
      if (tw >= area) {
         Arrays.fill(wt, true);
      } else if (tw > 0) {
         if (!this.naturalCleanup) {
            float th = cv[Math.max(0, Math.min(cv.length - 1, Math.round((float)cv.length * cov)))];
            for(int i = 0; i < f.length; ++i) {
               wt[i] = f[i] <= th;
            }
         } else {
            int tol = Math.max(1, (int)Math.ceil((double)w * (double)h * 0.03));
            float low = 0.0F;
            float high = 1.0F;
            float frac = cov;
            boolean[] best = null;
            int bestD = Integer.MAX_VALUE;
            for(int a = 0; a < 8; ++a) {
               int idx = Math.max(0, Math.min(cv.length - 1, Math.round((float)cv.length * frac)));
               float th = cv[idx];
               boolean[] cwt = new boolean[f.length];
               for(int i = 0; i < f.length; ++i) {
                  cwt[i] = f[i] <= th;
               }
               this.naturalize(cwt, pw, ph, sc);
               this.fillEnclosed(cwt, pw, pad, w, h);
               int cw = 0;
               for(int y = 0; y < h; ++y) {
                  for(int x = 0; x < w; ++x) {
                     if (cwt[pad + x + (pad + y) * pw]) {
                        ++cw;
                     }
                  }
               }
               int d = Math.abs(cw - tw);
               if (best == null || d < bestD) {
                  best = cwt;
                  bestD = d;
               }
               if (d <= tol) {
                  break;
               }
               if (cw > tw) {
                  high = frac;
               } else {
                  low = frac;
               }
               frac = (low + high) * 0.5F;
            }
            wt = best;
         }
      }
      byte[] pl = this.distanceLayers(wt, pw, ph, this.seed, Math.max(0.0F, this.shoalWidth), Math.max(0.0F, this.shallowWidth), Math.max(0.005F, this.depthWarpScale), Math.max(0.0F, this.depthWarpMag), this.clamp(this.shoalFragmentation, 0.0F, 1.0F), this.clamp(this.shallowFragmentation, 0.0F, 1.0F));
      byte[] l = new byte[area];
      for(int y = 0; y < h; ++y) {
         System.arraycopy(pl, pad + (y + pad) * pw, l, y * w, w);
      }
      return l;
   }
   private void naturalize(boolean[] w, int pw, int ph, float sc) {
      int ms = Math.max(2, Math.min(12, Math.round(sc * 0.08F)));
      this.rmSmall(w, pw, ph, ms);
      this.closeW(w, pw, ph);
      this.openW(w, pw, ph);
      this.smooth(w, pw, ph);
      this.smooth(w, pw, ph);
      this.prune(w, pw, ph, true, 8);
      this.prune(w, pw, ph, false, 8);
      this.prune(w, pw, ph, true, 4);
      this.rmSmall(w, pw, ph, ms);
   }
   private void rmSmall(boolean[] w, int pw, int ph, int ms) {
      boolean[] v = new boolean[w.length];
      int[] q = new int[w.length];
      int[] dx = new int[]{1, -1, 0, 0};
      int[] dy = new int[]{0, 0, 1, -1};
      for(int s = 0; s < w.length; ++s) {
         if (w[s] && !v[s]) {
            int hd = 0;
            int tl = 0;
            q[tl++] = s;
            v[s] = true;
            while(hd < tl) {
               int i = q[hd++];
               int cx = i % pw;
               int cy = i / pw;
               for(int d = 0; d < 4; ++d) {
                  int nx = cx + dx[d];
                  int ny = cy + dy[d];
                  if (nx >= 0 && nx < pw && ny >= 0 && ny < ph) {
                     int ni = nx + ny * pw;
                     if (w[ni] && !v[ni]) {
                        v[ni] = true;
                        q[tl++] = ni;
                     }
                  }
               }
            }
            if (tl < ms) {
               for(int i = 0; i < tl; ++i) {
                  w[q[i]] = false;
               }
            }
         }
      }
   }
   private void fillEnclosed(boolean[] wt, int pw, int pad, int w, int h) {
      boolean[] v = new boolean[w * h];
      int[] q = new int[w * h];
      int[] dx = new int[]{1, -1, 0, 0};
      int[] dy = new int[]{0, 0, 1, -1};
      for(int s = 0; s < v.length; ++s) {
         int sx = s % w;
         int sy = s / w;
         int ps = sx + pad + (sy + pad) * pw;
         if (!wt[ps] && !v[s]) {
            int hd = 0;
            int tl = 0;
            boolean te = false;
            q[tl++] = s;
            v[s] = true;
            while(hd < tl) {
               int i = q[hd++];
               int cx = i % w;
               int cy = i / w;
               if (cx == 0 || cy == 0 || cx == w - 1 || cy == h - 1) {
                  te = true;
               }
               for(int d = 0; d < 4; ++d) {
                  int nx = cx + dx[d];
                  int ny = cy + dy[d];
                  if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                     int ni = nx + ny * w;
                     int pni = nx + pad + (ny + pad) * pw;
                     if (!wt[pni] && !v[ni]) {
                        v[ni] = true;
                        q[tl++] = ni;
                     }
                  }
               }
            }
            if (!te) {
               for(int i = 0; i < tl; ++i) {
                  wt[q[i] % w + pad + (q[i] / w + pad) * pw] = true;
               }
            }
         }
      }
   }
   private void closeW(boolean[] w, int pw, int ph) {
      boolean[] d = this.dilate(w, pw, ph);
      System.arraycopy(this.erode(d, pw, ph), 0, w, 0, w.length);
   }
   private void openW(boolean[] w, int pw, int ph) {
      boolean[] e = this.erode(w, pw, ph);
      System.arraycopy(this.dilate(e, pw, ph), 0, w, 0, w.length);
   }
   private boolean[] dilate(boolean[] w, int pw, int ph) {
      boolean[] r = w.clone();
      int[] dx = new int[]{1, -1, 0, 0};
      int[] dy = new int[]{0, 0, 1, -1};
      for(int y = 0; y < ph; ++y) {
         for(int x = 0; x < pw; ++x) {
            int i = x + y * pw;
            if (!w[i]) {
               for(int d = 0; d < 4; ++d) {
                  int nx = x + dx[d];
                  int ny = y + dy[d];
                  if (nx >= 0 && nx < pw && ny >= 0 && ny < ph && w[nx + ny * pw]) {
                     r[i] = true;
                     break;
                  }
               }
            }
         }
      }
      return r;
   }
   private boolean[] erode(boolean[] w, int pw, int ph) {
      boolean[] r = w.clone();
      int[] dx = new int[]{1, -1, 0, 0};
      int[] dy = new int[]{0, 0, 1, -1};
      for(int y = 0; y < ph; ++y) {
         for(int x = 0; x < pw; ++x) {
            int i = x + y * pw;
            if (w[i]) {
               for(int d = 0; d < 4; ++d) {
                  int nx = x + dx[d];
                  int ny = y + dy[d];
                  if (nx < 0 || nx >= pw || ny < 0 || ny >= ph || !w[nx + ny * pw]) {
                     r[i] = false;
                     break;
                  }
               }
            }
         }
      }
      return r;
   }
   private void smooth(boolean[] w, int pw, int ph) {
      boolean[] n = w.clone();
      for(int y = 0; y < ph; ++y) {
         for(int x = 0; x < pw; ++x) {
            int nb = 0;
            for(int oy = -1; oy <= 1; ++oy) {
               for(int ox = -1; ox <= 1; ++ox) {
                  if (ox != 0 || oy != 0) {
                     int nx = x + ox;
                     int ny = y + oy;
                     if (nx >= 0 && nx < pw && ny >= 0 && ny < ph && w[nx + ny * pw]) {
                        ++nb;
                     }
                  }
               }
            }
            int i = x + y * pw;
            if (w[i] && nb <= 2) {
               n[i] = false;
            }
            if (!w[i] && nb >= 6) {
               n[i] = true;
            }
         }
      }
      System.arraycopy(n, 0, w, 0, w.length);
   }
   private void prune(boolean[] w, int pw, int ph, boolean val, int mp) {
      for(int p = 0; p < mp; ++p) {
         boolean[] n = w.clone();
         boolean ch = false;
         for(int y = 1; y + 1 < ph; ++y) {
            for(int x = 1; x + 1 < pw; ++x) {
               int i = x + y * pw;
               if (w[i] == val) {
                  int m = 0;
                  if (w[i - 1] == val) {
                     ++m;
                  }
                  if (w[i + 1] == val) {
                     ++m;
                  }
                  if (w[i - pw] == val) {
                     ++m;
                  }
                  if (w[i + pw] == val) {
                     ++m;
                  }
                  if (m <= 1) {
                     n[i] = !val;
                     ch = true;
                  }
               }
            }
         }
         System.arraycopy(n, 0, w, 0, w.length);
         if (!ch) {
            break;
         }
      }
   }
   private byte[] distanceLayers(boolean[] w, int pw, int ph, int sd, float shW, float shaW, float ws, float wm, float shF, float shaF) {
      byte[] l = new byte[w.length];
      int[] d = new int[w.length];
      int inf = pw + ph + 2;
      for(int i = 0; i < w.length; ++i) {
         int x = i % pw;
         int y = i / pw;
         d[i] = w[i] ? (x != 0 && y != 0 && x != pw - 1 && y != ph - 1 ? inf : 1) : 0;
      }
      for(int y = 0; y < ph; ++y) {
         for(int x = 0; x < pw; ++x) {
            int i = x + y * pw;
            if (w[i]) {
               if (x > 0) {
                  d[i] = Math.min(d[i], d[i - 1] + 1);
               }
               if (y > 0) {
                  d[i] = Math.min(d[i], d[i - pw] + 1);
               }
            }
         }
      }
      for(int y = ph - 1; y >= 0; --y) {
         for(int x = pw - 1; x >= 0; --x) {
            int i = x + y * pw;
            if (w[i]) {
               if (x + 1 < pw) {
                  d[i] = Math.min(d[i], d[i + 1] + 1);
               }
               if (y + 1 < ph) {
                  d[i] = Math.min(d[i], d[i + pw] + 1);
               }
            }
         }
      }
      int sho = Math.max(1, Math.round(shW));
      int sha = sho + Math.max(0, Math.round(shaW));
      for(int i = 0; i < w.length; ++i) {
         if (w[i]) {
            int bl = d[i] <= sho ? 1 : (d[i] <= sha ? 2 : 3);
            int lr = bl;
            if (wm > 0.001F) {
               int x = i % pw;
               int y = i / pw;
               float wrp = Simplex.noise2d(sd + 777, (double)1.0F, (double)1.0F, (double)(1.0F / ws), (double)((float)x + 10.0F), (double)((float)y + 10.0F)) * 2.0F - 1.0F;
               float sft = (float)d[i] + wrp * wm;
               int tl = sft <= (float)sho ? 1 : (sft <= (float)sha ? 2 : 3);
               lr = bl == 1 ? Math.min(tl, 2) : (bl == 3 ? Math.max(tl, 2) : tl);
            }
            if (lr == 1 && shF > 0.001F && sho > 0) {
               float t = (float)d[i] / (float)sho;
               float kn = Simplex.noise2d(sd + 111, (double)1.0F, (double)1.0F, (double)0.04F, (double)((float)(i % pw) + 10.0F), (double)((float)(i / pw) + 10.0F)) * 2.0F - 1.0F;
               if (kn < t * shF - 1.0F) {
                  lr = 0;
               }
            } else if (lr == 2 && shaF > 0.001F && sha > sho) {
               float t = (float)(d[i] - sho) / (float)(sha - sho);
               float kn = Simplex.noise2d(sd + 222, (double)1.0F, (double)1.0F, (double)0.04F, (double)((float)(i % pw) + 10.0F), (double)((float)(i / pw) + 10.0F)) * 2.0F - 1.0F;
               if (kn < t * shaF - 1.0F) {
                  lr = 1;
               }
            }
            l[i] = (byte)lr;
         }
      }
      return l;
   }
   private static final class CacheEntry {
      final CacheKey key;
      final int width;
      final int height;
      final byte[] layers;
      CacheEntry(CacheKey k, int w, int h, byte[] l) {
         this.key = k;
         this.width = w;
         this.height = h;
         this.layers = l;
      }
   }
   private static final class CacheKey {
      final int w;
      final int h;
      final int s;
      final int cov;
      final int scl;
      final int cx;
      final int eb;
      final int sw;
      final int shw;
      final int dws;
      final int dwm;
      final int sf;
      final int shf;
      final boolean nc;
      final String sfN;
      final String shfN;
      final String dfN;
      CacheKey(int w, int h, int s, float cov, float scl, float cx, float eb, float sw, float shw, float dws, float dwm, float sf, float shf, boolean nc, String sfN, String shfN, String dfN) {
         this.w = w;
         this.h = h;
         this.s = s;
         this.cov = Float.floatToIntBits(cov);
         this.scl = Float.floatToIntBits(scl);
         this.cx = Float.floatToIntBits(cx);
         this.eb = Float.floatToIntBits(eb);
         this.sw = Float.floatToIntBits(sw);
         this.shw = Float.floatToIntBits(shw);
         this.dws = Float.floatToIntBits(dws);
         this.dwm = Float.floatToIntBits(dwm);
         this.sf = Float.floatToIntBits(sf);
         this.shf = Float.floatToIntBits(shf);
         this.nc = nc;
         this.sfN = sfN;
         this.shfN = shfN;
         this.dfN = dfN;
      }
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof CacheKey)) {
            return false;
         } else {
            CacheKey k = (CacheKey)o;
            return this.w == k.w && this.h == k.h && this.s == k.s && this.cov == k.cov && this.scl == k.scl && this.cx == k.cx && this.eb == k.eb && this.sw == k.sw && this.shw == k.shw && this.dws == k.dws && this.dwm == k.dwm && this.sf == k.sf && this.shf == k.shf && this.nc == k.nc && this.sfN.equals(k.sfN) && this.shfN.equals(k.shfN) && this.dfN.equals(k.dfN);
         }
      }
      public int hashCode() {
         return this.w * 31 + this.h * 31 + this.s * 31 + this.cov;
      }
   }
}
