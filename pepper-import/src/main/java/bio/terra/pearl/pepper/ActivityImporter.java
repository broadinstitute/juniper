package bio.terra.pearl.pepper;

import org.broadinstitute.ddp.studybuilder.ActivityBuilder;

import java.lang.reflect.Method;
import java.nio.file.Path;

public class ActivityImporter extends ActivityBuilder {
    public ActivityImporter(Path dirPath) {
        super(dirPath, null, null, null, 1L);
    }

    public void buildActivity(Path path) {
        try {
            Method m = getClass().getSuperclass().getDeclaredMethod("readDefinitionConfig", new Class<?>[]{String.class, boolean.class});
            m.setAccessible(true);
            m.invoke(this, new Object[]{path, true});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
