package bio.terra.javatemplate.dao;

import bio.terra.javatemplate.BaseSpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public abstract class BaseDaoTest extends BaseSpringBootTest {}
