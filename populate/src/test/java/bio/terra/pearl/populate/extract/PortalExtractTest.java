package bio.terra.pearl.populate.extract;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.populate.BasePopulatePortalsTest;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.extract.PortalExtractService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class PortalExtractTest extends BasePopulatePortalsTest {

    @Autowired
    private PortalExtractService portalExtractService;

    @Test
    @Transactional
    public void testExtractDemoPortal() throws Exception {
        baseSeedPopulator.populateRolesAndPermissions();
        // populate a portal, then see if we can extract it, delete it, and repopulate it

        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/demo/portal.json"), true);
        String tmpFileName = "/tmp/demo-%s.zip".formatted(RandomStringUtils.randomAlphanumeric(8));
        File tmpFile = new File(tmpFileName);
        FileOutputStream fos = new FileOutputStream(tmpFile);
        portalExtractService.extract("demo", fos);
        fos.close();

        // we technically don't need this manual delete since the populate below should include a delete, but just to be sure...
        portalService.delete(portal.getId(), Set.of(PortalService.AllowedCascades.STUDY));

        ZipInputStream zis = new ZipInputStream(new FileInputStream(tmpFileName));
        Portal restoredPortal = portalPopulator.populateFromZipFile(zis, true, null);
        PortalEnvironment sandboxPortalEnv = portalEnvironmentService.findOne("demo", EnvironmentName.sandbox).orElseThrow();
        // confirm portal environment properties got copied
        assertThat(participantDashboardAlertDao.findByPortalEnvironmentId(sandboxPortalEnv.getId()), hasSize(1));
        assertThat(portalEnvironmentLanguageService.findByPortalEnvId(sandboxPortalEnv.getId()), hasSize(3));

        // confirm all templates got repopulated
        assertThat(surveyService.findByPortalId(restoredPortal.getId()), hasSize(15));
        assertThat(studyService.findByPortalId(restoredPortal.getId()), hasSize(1));
        assertThat(emailTemplateService.findByPortalId(restoredPortal.getId()), hasSize(8));
        // confirm both the old and current versions of the site content got populated
        assertThat(siteContentService.findByPortalId(restoredPortal.getId()), hasSize(2));

        // confirm the sandbox got configured
        Study study = studyService.findByPortalId(restoredPortal.getId()).get(0);
        StudyEnvironment sandboxEnv = studyEnvironmentService.findByStudy(study.getShortcode(), EnvironmentName.sandbox).orElseThrow();
        assertThat(triggerService.findByStudyEnvironmentId(sandboxEnv.getId()), hasSize(8));
        assertThat(studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(sandboxEnv.getId()), hasSize(1));
    }

}
