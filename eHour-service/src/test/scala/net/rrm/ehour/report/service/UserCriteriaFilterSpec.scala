package net.rrm.ehour.report.service

import net.rrm.ehour.domain.{UserDepartmentObjectMother, UserObjectMother}
import net.rrm.ehour.persistence.user.dao.UserDao
import net.rrm.ehour.report.criteria.UserSelectedCriteria
import net.rrm.ehour.report.service.ReportFilterFixture._
import net.rrm.ehour.util._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class UserCriteriaFilterSpec extends WordSpec with MockitoSugar with Matchers with BeforeAndAfterEach {
  val dao = mock[UserDao]
  val subject = new UserAndDepartmentCriteriaFilter(dao)

  override protected def beforeEach() = reset(dao)

  "User Criteria Filter" must {
    "find all users when no departments are provided" in {
      when(dao.findAll()).thenReturn(toJava(List(pm)))

      val criteria = new UserSelectedCriteria
      criteria.setOnlyActiveUsers(false)
      val (_, users) = subject.getAvailableUsers(criteria)

      users should have size 1

      verify(dao).findAll()
    }

    "find all active users when no departments are provided" in {
      when(dao.findActiveUsers()).thenReturn(toJava(List(pm)))

      val criteria = new UserSelectedCriteria
      val (_, users) = subject.getAvailableUsers(criteria)

      users should have size 1

      verify(dao).findActiveUsers()
    }

    "find all users for given departments" in {
      val userFromOtherDepartment =UserObjectMother.createUser(UserDepartmentObjectMother.createUserDepartment())
      when(dao.findAll()).thenReturn(toJava(List(pm, userFromOtherDepartment)))

      val criteria = new UserSelectedCriteria
      criteria.setDepartments(toJava(List(pm.getUserDepartment)))
      criteria.setOnlyActiveUsers(false)
      val (_, users) = subject.getAvailableUsers(criteria)

      users should have size 1

      verify(dao).findAll()
    }

    "find all active users for given departments" in {
      val userFromOtherDepartment =UserObjectMother.createUser(UserDepartmentObjectMother.createUserDepartment())
      when(dao.findActiveUsers()).thenReturn(toJava(List(pm, userFromOtherDepartment)))

      val criteria = new UserSelectedCriteria
      criteria.setDepartments(toJava(List(department)))
      criteria.setOnlyActiveUsers(true)
      val (_, users) = subject.getAvailableUsers(criteria)

      users should have size 1

      verify(dao).findActiveUsers()
    }

  }
}
