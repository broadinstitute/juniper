package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.dao.i18n.CoreLanguageTextDao;
import bio.terra.pearl.core.model.i18n.CoreLanguageText;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoreLanguageTextService extends CrudService<CoreLanguageText, CoreLanguageTextDao> {

    private CoreLanguageTextDao coreLanguageTextDao;

    public CoreLanguageTextService(CoreLanguageTextDao coreLanguageTextDao) {
        super(coreLanguageTextDao);
        this.coreLanguageTextDao = coreLanguageTextDao;
    }

    public List<CoreLanguageText> findByLanguage(String language) {
        return coreLanguageTextDao.findByLanguage(language);
    }

}
