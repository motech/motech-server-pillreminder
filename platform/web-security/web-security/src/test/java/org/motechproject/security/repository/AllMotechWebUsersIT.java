package org.motechproject.security.repository;

import ch.lambdaj.Lambda;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.security.authentication.MotechPasswordEncoder;
import org.motechproject.security.domain.MotechUser;
import org.motechproject.security.domain.MotechUserCouchdbImpl;
import org.motechproject.security.ex.EmailExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.Matchers.equalTo;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/motech/*.xml")
public class AllMotechWebUsersIT {

    @Autowired
    private AllMotechUsers allMotechUsers;

    @Autowired
    private MotechPasswordEncoder passwordEncoder;

    @Test
    public void findByUserName() {
        MotechUser motechUser = new MotechUserCouchdbImpl("testuser", "testpassword", "", "id", asList("ADMIN"), "");
        allMotechUsers.add(motechUser);
        MotechUser testUser = allMotechUsers.findByUserName("testuser");
        assertEquals("testuser", testUser.getUserName());
    }

    @Test
    public void findByUserNameShouldBeCaseInsensitive() {
        String userName = "TestUser";
        allMotechUsers.add(new MotechUserCouchdbImpl(userName, "testpassword", "", "id", asList("ADMIN"), ""));
        assertNotNull(allMotechUsers.findByUserName("TESTUSER"));
    }

    @Test
    public void shouldNotCreateNewAccountIfUserAlreadyExists() {
        String userName = "username";
        allMotechUsers.add(new MotechUserCouchdbImpl(userName, "testpassword", "","id", asList("ADMIN"), ""));
        allMotechUsers.add(new MotechUserCouchdbImpl(userName, "testpassword1", "","id2", asList("ADMIN"), ""));
        MotechUser motechUser = allMotechUsers.findByUserName("userName");
        final List<MotechUserCouchdbImpl> allWebUsers = ((AllMotechUsersCouchdbImpl) allMotechUsers).getAll();
        final int numberOfUsersWithSameUserName = Lambda.select(allWebUsers, HasPropertyWithValue.hasProperty("userName", equalTo(userName))).size();
        assertEquals(1, numberOfUsersWithSameUserName);
        assertEquals("testpassword", motechUser.getPassword());
        assertEquals("id", motechUser.getExternalId());
    }

    @Test
    public void shouldListWebUsersByRole() {
        MotechUser provider1 = new MotechUserCouchdbImpl("provider1", "testpassword", "email1@example.com","id1", asList("PROVIDER"), "");
        MotechUser provider2 = new MotechUserCouchdbImpl("provider2", "testpassword", "email12@example.com","id2", asList("PROVIDER"), "");
        MotechUser cmfAdmin = new MotechUserCouchdbImpl("cmfadmin", "testpassword", "email13@example.com","id3", asList("CMFADMIN"), "");
        MotechUser itAdmin = new MotechUserCouchdbImpl("itadmin", "testpassword", "email4@example.com","id4", asList("ITADMIN"), "");
        allMotechUsers.add(provider1);
        allMotechUsers.add(provider2);
        allMotechUsers.add(cmfAdmin);
        allMotechUsers.add(itAdmin);
        List<? extends MotechUser> providers = allMotechUsers.findByRole("PROVIDER");
        assertEquals(asList("id1", "id2"), extract(providers, on(MotechUser.class).getExternalId()));
    }

    @Test(expected = EmailExistsException.class)
    public void shouldNotAllowDuplicateEmails() {
        allMotechUsers.add(new MotechUserCouchdbImpl("user1", "testpassword", "email1@example.com","id", asList("ADMIN"), ""));
        allMotechUsers.add(new MotechUserCouchdbImpl("user2", "testpassword1", "email1@example.com","id2", asList("ADMIN"), ""));
    }

    @Test
    public void findByUseridShouldReturnNullIfuserNameIsNull() {
        assertNull(null, allMotechUsers.findByUserName(null));
    }

    @Before
    public void setUp() {
        ((AllMotechUsersCouchdbImpl) allMotechUsers).removeAll();
    }

}
