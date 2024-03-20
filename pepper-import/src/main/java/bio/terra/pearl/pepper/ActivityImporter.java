package bio.terra.pearl.pepper;

import bio.terra.pearl.core.model.survey.Survey;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigResolveOptions;
import org.broadinstitute.ddp.model.activity.definition.ActivityDef;
import org.broadinstitute.ddp.studybuilder.ActivityBuilder;
import org.broadinstitute.ddp.util.ConfigUtil;
import org.broadinstitute.ddp.util.GsonUtil;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

public class ActivityImporter extends ActivityBuilder {
    private final Gson gson;
    private final Config varsConfig;
    public ActivityImporter(Path dirPath, Config varsConfig) {
        super(dirPath, null, null, null, 1L);
        this.varsConfig = varsConfig;
        gson = GsonUtil.standardGson();
    }

    public ActivityDef buildActivity(Path path) {
        Path dirPath = PepperImportCliApp.getFilePath("studies/atcp");
        File file = dirPath.resolve(path).toFile();
        if (!file.exists()) {
            throw new RuntimeException("Activity definition file is missing: " + file);
        }

        Config definition = ConfigFactory.parseFile(file)
            // going to resolve first the external global variables that might be used in this configuration
            // using setAllowUnresolved = true so we can do a second pass that will allow us to resolve variables
            // within the configuration
            .resolveWith(varsConfig, ConfigResolveOptions.defaults().setAllowUnresolved(false));
        if (definition.isEmpty()) {
            throw new RuntimeException("Activity definition file is empty: " + file);
        }

        ActivityDef activityDef = gson.fromJson(ConfigUtil.toJson(definition), ActivityDef.class);
        return activityDef;
    }

    public static Survey convert(ActivityDef activityDef) {
        activityDef.get
    }

}
