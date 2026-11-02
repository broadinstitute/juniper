package bio.terra.pearl.pepper;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.internal.IOInternalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.broadinstitute.ddp.model.activity.definition.FormActivityDef;

import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CLI importer -- see the "run" method for argument descriptions.
 */
@SpringBootApplication(scanBasePackages = {"bio.terra.pearl.core", "bio.terra.pearl.populate", "bio.terra.pearl.pepper"})
@Slf4j
public class PepperImportCliApp implements CommandLineRunner {

    public static final String ABSOLUTE_SEED_ROOT = "pepper-import/src/main/resources/pepper";
    public static final String OUTPUT_ROOT = "pepper-import/out";

    public static void main(String[] args) {
        SpringApplication.run(PepperImportCliApp.class, args);
    }

    public static String SUBSTITUTIONS_FILE = "substitutions.conf";

    @Autowired
    private ActivityImporter activityImporter;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        log.info("EXECUTING : command line importer");
        if (args.length > 0 && args[0].equals("rgp")) {
            importRgp();
            log.info("COMPLETE : command line importer");
            return;
        }
        // these vars should be read from command line or a conf file eventually
        String studyDir = "atcp";
        String[] formFiles = {"prequal.conf", "registration.conf", "self-consent.conf", "self-consent-edit.conf", "assent.conf",
                "medical-history.conf", "contacting-physician.conf", "review-and-submission.conf", "genome-study.conf", "blood-type.conf", "stay-informed.conf"};

        Config varsCfg = ConfigFactory.parseFile(getFilePath("studies/%s/%s".formatted(studyDir, SUBSTITUTIONS_FILE), ABSOLUTE_SEED_ROOT).toFile());

        for (String formFile : formFiles) {
            Survey survey = activityImporter.parsePepperForm(varsCfg, getFilePath("studies/" + studyDir, ABSOLUTE_SEED_ROOT), Path.of(formFile));
            try {
                Path outFilePath = getFilePath(formFile, OUTPUT_ROOT);
                String surveyString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(survey);
                Files.write(outFilePath, surveyString.getBytes());
            } catch (IOException e) {
                throw new IOInternalException("couldn't write survey", e);
            }
        }
        log.info("COMPLETE : command line importer");
    }


    /**
     * RGP: prequal and enrollment are imported as surveys. The mother/father/sibling/child/relative activities are
     * embedded in enrollment in Pepper, so they're inlined into the enrollment survey rather than imported separately.
     * Run with the argument "rgp".
     */
    private void importRgp() {
        Path studyPath = getFilePath("studies/rgp", ABSOLUTE_SEED_ROOT);
        Config varsCfg = ConfigFactory.parseFile(studyPath.resolve(SUBSTITUTIONS_FILE).toFile());

        // Pepper "patches" that retire picklist options. Everything else in patches/ is already reflected in the
        // .conf files (Puerto Rico state option, deceased-dna show expressions).
        Config v2 = ConfigFactory.parseFile(studyPath.resolve("patches/version2.conf").toFile());
        Map<String, Set<String>> optionsToRemove = Map.of(
                varsCfg.getString("id.q.general_info"), new HashSet<>(Set.of("ENGLISH", v2.getString("optionsToRemove.generalInfo"))), // ENGLISH: RgpPrequalV3
                varsCfg.getString("id.q.relationship"), new HashSet<>(v2.getStringList("optionsToRemove.relationship")),
                varsCfg.getString("id.q.tests"), new HashSet<>(v2.getStringList("optionsToRemove.tests")));
        activityImporter.setOptionsToRemove(optionsToRemove);

        Map<String, FormActivityDef> nested = new HashMap<>();
        for (String code : List.of("mother", "father", "sibling", "child", "relative")) {
            nested.put(code.toUpperCase(), activityImporter.parseActivityDef(varsCfg, studyPath, Path.of(code + ".conf")));
        }
        activityImporter.setNestedActivities(nested);

        Map<String, Map<String, String>> allRenames = new LinkedHashMap<>();
        Map<String, Survey> surveys = new LinkedHashMap<>();
        for (String formFile : List.of("prequal.conf", "enrollment.conf")) {
            SurveyPopDto survey = (SurveyPopDto) activityImporter.convert(varsCfg, activityImporter.parseActivityDef(varsCfg, studyPath, Path.of(formFile)));
            SurveyNormalizer normalizer = new SurveyNormalizer(objectMapper);
            // these questions are flagged in the pepper config as no longer used
            normalizer.removeQuestions(survey.getJsonContent(), Set.of(varsCfg.getString("id.q.ethnicity_categories"), varsCfg.getString("id.q.sick_family")));
            normalizer.normalize(survey.getJsonContent());
            // juniper survey stable ids are camelCase, pepper's are UPPER_SNAKE
            survey.setStableId(SurveyNormalizer.camelCase(survey.getStableId()));
            allRenames.put(survey.getStableId(), new TreeMap<>(normalizer.getRenames()));
            surveys.put(formFile.replace(".conf", ""), survey);
        }

        // Juniper pre-enroll version of the prequal: same content, plus the "qualified" expression that replaces
        // the pepper workflow/event preconditions (all three of these must be checked to proceed to enrollment)
        SurveyPopDto prequal = (SurveyPopDto) surveys.get("prequal");
        SurveyPopDto preEnroll = objectMapper.convertValue(prequal, SurveyPopDto.class);
        preEnroll.setSurveyType(SurveyType.PRE_ENROLL);
        preEnroll.setAutoAssign(false);
        String general = SurveyNormalizer.camelCase(varsCfg.getString("id.q.general_info"));
        ObjectNode qualified = objectMapper.createObjectNode();
        qualified.put("name", "qualified");
        qualified.put("includeIntoResult", true);
        qualified.put("expression", Stream.of("RESIDENT", "RARE_CONDITION", "UNDER_CARE")
                .map(option -> "{%s} contains '%s'".formatted(general, option)).collect(Collectors.joining(" and ")));
        ((ObjectNode) preEnroll.getJsonContent()).putArray("calculatedValues").add(qualified);
        // the prequal is only ever used as the pre-enroll survey in juniper, so it replaces the plain conversion
        surveys.put("prequal", preEnroll);

        try {
            Path outDir = getFilePath("rgp", OUTPUT_ROOT);
            Files.createDirectories(outDir);
            for (Map.Entry<String, Survey> entry : surveys.entrySet()) {
                Files.write(outDir.resolve(entry.getKey() + ".json"), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(entry.getValue()));
            }
            Files.write(outDir.resolve("question-name-mapping.json"), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(allRenames));
        } catch (IOException e) {
            throw new IOInternalException("couldn't write survey", e);
        }
    }

    /**
     * Get the absolute path for a file.
     * depending on whether you are running gradle or spring boot, the root directory could either be
     * the root folder or pepper-import.  So strip out pepper-import if it's there
     */
    public static Path getFilePath(String path, String root) {

        String projectDir = System.getProperty("user.dir").replace("/pepper-import", "");
        String pathName = projectDir + "/" + root + "/" + path;
        return Path.of(pathName);
    }
}
