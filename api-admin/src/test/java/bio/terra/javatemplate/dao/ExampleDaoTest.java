package bio.terra.javatemplate.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.pearl.api.admin.dao.ExampleDao;
import bio.terra.pearl.api.admin.model.Example;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ExampleDaoTest extends BaseDaoTest {
  @Autowired ExampleDao exampleDao;

  @Test
  void testDifferentUserUpsert() {
    var user1 = new Example("user1", "user1 message");
    exampleDao.upsertExample(user1);
    var user1Actual = exampleDao.getExampleForUser(user1.userId());

    var user2 = new Example("user2", "user2 message");
    exampleDao.upsertExample(user2);
    var user2Actual = exampleDao.getExampleForUser(user2.userId());

    assertTrue(user1Actual.isPresent());
    assertEquals(user1.userId(), user1Actual.get().userId());
    assertEquals(user1.message(), user1Actual.get().message());

    assertTrue(user2Actual.isPresent());
    assertEquals(user2.userId(), user2Actual.get().userId());
    assertEquals(user2.message(), user2Actual.get().message());
  }

  @Test
  void testRepeatedUpsert() {
    var example1 = new Example("testUser", "testMessage");
    exampleDao.upsertExample(example1);
    var firstSave = exampleDao.getExampleForUser(example1.userId());
    assertTrue(firstSave.isPresent());
    assertEquals(example1.userId(), firstSave.get().userId());
    assertEquals(example1.message(), firstSave.get().message());

    var example2 = new Example(example1.userId(), "differentMessage");
    exampleDao.upsertExample(example2);
    var secondSave = exampleDao.getExampleForUser(example1.userId());

    assertTrue(secondSave.isPresent());
    assertEquals(firstSave.get().id(), secondSave.get().id());
    assertEquals(example2.userId(), secondSave.get().userId());
    assertEquals(example2.message(), secondSave.get().message());
  }

  @Test
  void testNotFound() {
    assertTrue(exampleDao.getExampleForUser("testUser").isEmpty());
  }
}
