package net.rrm.ehour.ui.common.header

import java.util
import javax.json.JsonArray
import javax.servlet.http.HttpServletRequest

import com.richemont.jira.{JiraConst, JiraIssue, JiraService}
import com.richemont.windchill.{WindChillService, WindChillUpdateService}
import net.rrm.ehour.domain.{Activity, User}
import net.rrm.ehour.ui.common.decorator.LoadingSpinnerDecorator
import net.rrm.ehour.ui.common.event.{AjaxEvent, EventPublisher}
import net.rrm.ehour.ui.common.panel.AbstractBasePanel
import net.rrm.ehour.ui.common.session.EhourWebSession
import net.rrm.ehour.ui.common.util.AuthUtil
import net.rrm.ehour.ui.login.page.Logout
import net.rrm.ehour.ui.userprefs.page.UserPreferencePage
import net.rrm.ehour.user.service.UserService
import org.apache.log4j.Logger
import org.apache.wicket.Component
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.ajax.{AbstractAjaxTimerBehavior, AjaxRequestTarget}
import org.apache.wicket.behavior.Behavior
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.link.{BookmarkablePageLink, Link}
import org.apache.wicket.markup.html.panel.Fragment
import org.apache.wicket.model.Model
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.util.time.Duration

object LoggedInAsPanel {
  private val LOGGER: Logger = Logger.getLogger(classOf[HeaderPanel])
}

class LoggedInAsPanel(id: String, showSyncLink: Boolean) extends AbstractBasePanel(id) {
  def this(id: String) {
    this(id, false)
  }

  @SpringBean protected var authUtil: AuthUtil = _

  @SpringBean protected var windChillService: WindChillService = _
  @SpringBean protected var chillUpdateService: WindChillUpdateService = _
  @SpringBean protected var userService: UserService = _
  @SpringBean protected var jiraService: JiraService = _

  override def onInitialize() = {
    super.onInitialize()

    val link = new BookmarkablePageLink("prefsLink", classOf[UserPreferencePage])
    val loggedInUserLabel = new Label("loggedInUser", new Model[String](EhourWebSession.getUser.getFullName))
    link.add(loggedInUserLabel)

    addOrReplace(link)

    addOrReplace(new BookmarkablePageLink("logoffLink", classOf[Logout]))

    addOrReplace(createImpersonatingPanel("impersonatingNotification"))
  }

  private def createImpersonatingPanel(id: String) = {
    if (EhourWebSession.getSession.isImpersonating) {
      val fragment = new Fragment(id, "impersonating", this)
      fragment.add(new Link("stop") {
        def onClick() {
          val session = EhourWebSession.getSession
          session.stopImpersonating()

          val homepage = authUtil.getHomepageForRole(session.getRoles)
          setResponsePage(homepage.homePage, homepage.parameters)
        }
      })

      fragment.add(new Label("name", EhourWebSession.getUser.getFullName))

      fragment
    }
    else {
      new WebMarkupContainer(id).setVisible(false)
    }
  }

  /**
   * JIRA - eHOUR - PJL synchronization
   * added by LLI for Richemont
   *
   * @param visible
   */
  private def addSyncLink(visible: Boolean) {
    val fragment: Fragment = new Fragment("syncFailContainer", "syncFail", this)
    fragment.setOutputMarkupId(true)
    fragment.setOutputMarkupPlaceholderTag(true)
    fragment.setVisible(false)
    add(fragment)

    val link: AjaxLink[Void] = new AjaxLink[Void](("syncLink")) {
      def onClick(target: AjaxRequestTarget) {
        val user: User = EhourWebSession.getUser

        var isJiraSync: Boolean = false
        var isWindSync: Boolean = false

        LoggedInAsPanel.LOGGER.info("\n\n")
        LoggedInAsPanel.LOGGER.info("****************** STEP# 1 : GET ALL ACTIVITIES from HeaderPanel ******************")
        val allAssignedActivitiesByCode: util.Map[String, Activity] = windChillService.getAllAssignedActivitiesByCode(user)
        LoggedInAsPanel.LOGGER.info("\n\n")
        LoggedInAsPanel.LOGGER.info("****************** START JIRA SYNC (get Jira issues) ****************************")
        val isJiraUser: Boolean = userService.isLdapUserMemberOf(user.getUsername, JiraConst.GET_JIRA_ISSUES_FOR_USER_MEMBER_OF)
        LoggedInAsPanel.LOGGER.info("User in " + JiraConst.GET_JIRA_ISSUES_FOR_USER_MEMBER_OF + " createJiraIssuesForUser() for user " + user.getUsername + " = " + isJiraUser)
        var activitiesMasteredByJira: util.Map[JiraIssue, Activity] = null

        try {
          if (isJiraUser) {
            activitiesMasteredByJira = jiraService.createJiraIssuesForUser(allAssignedActivitiesByCode, user.getUsername)
            if (activitiesMasteredByJira == null) {
              isJiraSync = false
            }
            else {
              isJiraSync = true
              LoggedInAsPanel.LOGGER.info("\n\n")
              LoggedInAsPanel.LOGGER.info("****************** Identify missing activity in PJL from JIRA->EHOUR ********************")
              val activitiesPjlToBeCreated: JsonArray = jiraService.identifyMissingPjlActivity(activitiesMasteredByJira)

              val request: HttpServletRequest = getRequest.getContainerRequest.asInstanceOf[HttpServletRequest]
              request.getSession.setAttribute("MissingPjlActivity", activitiesPjlToBeCreated)
            }
          }
          else {
            LoggedInAsPanel.LOGGER.info("User do not exist in " + JiraConst.GET_JIRA_ISSUES_FOR_USER_MEMBER_OF + " : skip createJiraIssuesForUser() for user " + user.getUsername)
            isJiraSync = true
          }
        }
        catch {
          case e: Exception => {
            isJiraSync = false
          }
        }
        LoggedInAsPanel.LOGGER.info("\n\n")
        LoggedInAsPanel.LOGGER.info("****************** START WINDCHILL SYNC (get PJL activities) ************************")
        if (!isEnabled) {
          LoggedInAsPanel.LOGGER.info("WARNING: Windchill sync is disabled")
        }
        else {
          try {
            isWindSync = windChillService.updateDataForUser(allAssignedActivitiesByCode, user.getUsername)
          }
          catch {
            case e: Exception => {
              e.printStackTrace
            }
          }
        }
        allAssignedActivitiesByCode.clear
        if (!isWindSync || !isJiraSync) {
          val container: Component = getSyncFailContainer
          container.setVisible(true)
          removeHiderBehavior(container)
          container.add(new SyncFailureMessageHider())
          target.add(container)
        }
        else {
          EventPublisher.publishAjaxEventToParentChildren(LoggedInAsPanel.this, new AjaxEvent(WindchillSyncEvent.WINDCHILL_SYNCED, target))
        }
        allAssignedActivitiesByCode.clear
      }

      protected def getAjaxCallDecorator= new LoadingSpinnerDecorator
    }

    link.setVisible(visible)
    add(link)
  }



  protected def removeHiderBehavior(container: Component) {
    val behaviors: util.List[_ <: Behavior] = container.getBehaviors
    import scala.collection.JavaConversions._
    for (behavior <- behaviors) {
      if (behavior.isInstanceOf[SyncFailureMessageHider]) {
        container.remove(behavior)
      }
    }
  }

  def getSyncFailContainer: Component = LoggedInAsPanel.this.get("syncFailContainer")

  class SyncFailureMessageHider extends AbstractAjaxTimerBehavior(Duration.seconds(15)) {
    protected def onTimer(target: AjaxRequestTarget) {
      stop(target)
      val container = getSyncFailContainer
      container.setVisible(false)
      target.add(container)
    }
  }
}