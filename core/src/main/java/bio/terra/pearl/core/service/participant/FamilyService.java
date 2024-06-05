package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyService extends CrudService<Family, FamilyDao> {
    public static final String FAMILY_SHORTCODE_ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int FAMILY_SHORTCODE_LENGTH = 6;
    private final RandomUtilService randomUtilService;

    public FamilyService(FamilyDao familyDao, RandomUtilService randomUtilService) {
        super(familyDao);
        this.randomUtilService = randomUtilService;
    }

    @Transactional
    public Family create(Family family) {
        if (family.getShortcode() == null) {
            family.setShortcode(generateShortcode());
        }
        Family savedFamily = dao.create(family);
        return savedFamily;
    }

    /**
     * Generate a unique shortcode for a family. Operates exactly like the generateShortcode method in
     * EnrolleeService.
     */
    @Transactional
    public String generateShortcode() {
        int MAX_TRIES = 10;
        String shortcode = null;
        for (int tryNum = 0; tryNum < MAX_TRIES; tryNum++) {
            String possibleShortcode = randomUtilService.generateSecureRandomString(FAMILY_SHORTCODE_LENGTH, FAMILY_SHORTCODE_ALLOWED_CHARS);
            if (dao.findOneByShortcode(possibleShortcode).isEmpty()) {
                shortcode = possibleShortcode;
                break;
            }
        }
        if (shortcode == null) {
            throw new InternalServerException("Unable to generate unique shortcode");
        }
        return shortcode;
    }
}
