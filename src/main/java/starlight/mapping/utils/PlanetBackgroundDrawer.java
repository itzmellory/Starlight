package starlight.mapping.utils;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.gl.FrameBuffer;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.graphics.g3d.PlanetRenderer;
public class PlanetBackgroundDrawer{
    private static FrameBuffer buffer;
    private static final PlanetRenderer renderer = new PlanetRenderer();
    private static boolean changed = true;
    public static void update(){
        changed = true;
    }
    public static Texture draw(PlanetParams params){
        if(params == null) return null;
        int size = Math.max(arc.Core.graphics.getWidth(), arc.Core.graphics.getHeight());
        boolean resized = false;
        if(buffer == null){
            buffer = new FrameBuffer(size, size);
            resized = true;
        }
        if(changed || resized || buffer.resizeCheck(size, size)){
            changed = false;
            buffer.begin(Color.clear);
            params.viewW = size;
            params.viewH = size;
            params.alwaysDrawAtmosphere = true;
            params.drawUi = false;
            renderer.render(params);
            buffer.end();
        }
        return buffer.getTexture();
    }
}
