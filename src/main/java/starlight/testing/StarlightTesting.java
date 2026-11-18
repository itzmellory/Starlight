package starlight.testing;
import starlight.Starlight;
public final class StarlightTesting{
    private StarlightTesting(){}
    public static boolean disableCampaign(){
        return Starlight.disableTestingInCampaign();
    }
}
