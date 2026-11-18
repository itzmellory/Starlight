package starlight.testing.ui;
import arc.scene.style.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.ImageButton.*;
import mindustry.gen.*;
import mindustry.ui.*;
import static arc.graphics.Color.*;
public class TUStyles{
    public static Drawable redBack;
    public static ButtonStyle redButtonStyle;
    public static ImageButtonStyle tuImageStyle, tuRedImageStyle, lefti, toggleLefti, righti, toggleRighti, centeri;
    public static void init(){
        redBack = ((TextureRegionDrawable)Tex.whiteui).tint(0.625f, 0f, 0f, 0.8f);
        redButtonStyle = new ButtonStyle(Styles.logict){{
            disabled = redBack;
        }};
        tuImageStyle = new ImageButtonStyle(Styles.logici){{
            down = Styles.flatDown;
            over = Styles.flatOver;
            imageDisabledColor = gray;
            imageUpColor = white;
        }};
        tuRedImageStyle = new ImageButtonStyle(tuImageStyle){{
            disabled = redBack;
        }};
        lefti = new ImageButtonStyle(Styles.defaulti);
        toggleLefti = new ImageButtonStyle(Styles.squareTogglei);
        righti = new ImageButtonStyle(Styles.defaulti);
        toggleRighti = new ImageButtonStyle(Styles.squareTogglei);
        centeri = new ImageButtonStyle(Styles.defaulti);
    }
}
