package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.persistence.LibraryRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class KomgaUserDaoTest(
  @Autowired private val komgaUserDao: KomgaUserDao,
  @Autowired private val libraryRepository: LibraryRepository
) {

  private val library = makeLibrary()

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library)
  }

  @AfterEach
  fun deleteUsers() {
    komgaUserDao.deleteAll()
    assertThat(komgaUserDao.count()).isEqualTo(0)
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
  }

  @Test
  fun `given a user when saving it then it is persisted`() {
    val now = LocalDateTime.now()
    val user = KomgaUser(
      email = "user@example.org",
      password = "password",
      roleAdmin = false,
      sharedLibrariesIds = setOf(library.id),
      sharedAllLibraries = false
    )

    val created = komgaUserDao.insert(user)

    with(created) {
      assertThat(id).isNotEqualTo(0)
      assertThat(createdDate).isCloseTo(now, offset)
      assertThat(lastModifiedDate).isCloseTo(now, offset)
      assertThat(email).isEqualTo("user@example.org")
      assertThat(password).isEqualTo("password")
      assertThat(roleAdmin).isFalse()
      assertThat(sharedLibrariesIds).containsExactly(library.id)
      assertThat(sharedAllLibraries).isFalse()
    }
  }

  @Test
  fun `given existing user when modifying and saving it then it is persisted`() {
    val user = KomgaUser(
      email = "user@example.org",
      password = "password",
      roleAdmin = false,
      sharedLibrariesIds = setOf(library.id),
      sharedAllLibraries = false
    )

    val created = komgaUserDao.insert(user)

    val modified = created.copy(
      email = "user2@example.org",
      password = "password2",
      roleAdmin = true,
      sharedLibrariesIds = emptySet(),
      sharedAllLibraries = true
    )
    val modifiedDate = LocalDateTime.now()
    komgaUserDao.update(modified)
    val modifiedSaved = komgaUserDao.findByIdOrNull(modified.id)!!

    with(modifiedSaved) {
      assertThat(id).isEqualTo(created.id)
      assertThat(createdDate).isEqualTo(created.createdDate)
      assertThat(lastModifiedDate)
        .isCloseTo(modifiedDate, offset)
        .isNotEqualTo(modified.createdDate)
      assertThat(email).isEqualTo("user2@example.org")
      assertThat(password).isEqualTo("password2")
      assertThat(roleAdmin).isTrue()
      assertThat(sharedLibrariesIds).isEmpty()
      assertThat(sharedAllLibraries).isTrue()
    }
  }

  @Test
  fun `given multiple users when saving then they are persisted`() {
    komgaUserDao.insert(KomgaUser("user1@example.org", "p", false))
    komgaUserDao.insert(KomgaUser("user2@example.org", "p", true))

    val users = komgaUserDao.findAll()

    assertThat(users).hasSize(2)
    assertThat(users.map { it.email }).containsExactlyInAnyOrder(
      "user1@example.org",
      "user2@example.org"
    )
  }

  @Test
  fun `given some users when counting then proper count is returned`() {
    komgaUserDao.insert(KomgaUser("user1@example.org", "p", false))
    komgaUserDao.insert(KomgaUser("user2@example.org", "p", true))

    val count = komgaUserDao.count()

    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `given existing user when finding by id then user is returned`() {
    val existing = komgaUserDao.insert(
      KomgaUser("user1@example.org", "p", false)
    )

    val user = komgaUserDao.findByIdOrNull(existing.id)

    assertThat(user).isNotNull
  }

  @Test
  fun `given non-existent user when finding by id then null is returned`() {
    val user = komgaUserDao.findByIdOrNull(38473)

    assertThat(user).isNull()
  }

  @Test
  fun `given existing user when deleting then user is deleted`() {
    val existing = komgaUserDao.insert(
      KomgaUser("user1@example.org", "p", false)
    )

    komgaUserDao.delete(existing)

    assertThat(komgaUserDao.count()).isEqualTo(0)
  }

  @Test
  fun `given users when checking if exists by email then return true or false`() {
    komgaUserDao.insert(
      KomgaUser("user1@example.org", "p", false)
    )

    val exists = komgaUserDao.existsByEmailIgnoreCase("USER1@EXAMPLE.ORG")
    val notExists = komgaUserDao.existsByEmailIgnoreCase("USER2@EXAMPLE.ORG")

    assertThat(exists).isTrue()
    assertThat(notExists).isFalse()
  }

  @Test
  fun `given users when finding by email then return user`() {
    komgaUserDao.insert(
      KomgaUser("user1@example.org", "p", false)
    )

    val found = komgaUserDao.findByEmailIgnoreCase("USER1@EXAMPLE.ORG")
    val notFound = komgaUserDao.findByEmailIgnoreCase("USER2@EXAMPLE.ORG")

    assertThat(found).isNotNull
    assertThat(found?.email).isEqualTo("user1@example.org")
    assertThat(notFound).isNull()
  }
}
