import {
  Portal,
  Study
} from '@juniper/ui-core'
import { useNavigate } from 'react-router-dom'
import {
  studyKitsPath,
  studyParticipantsPath
} from 'portal/PortalRouter'
import StudySelector from './StudySelector'
import React, { useEffect } from 'react'
import {
  adminTasksPath,
  studyEnvDataBrowserPath,
  studyEnvDatasetListViewPath,
  studyEnvExportIntegrationsPath,
  studyEnvFormsPath,
  studyEnvImportPath,
  studyEnvMailingListPath,
  studyEnvMetricsPath,
  studyEnvSiteContentPath,
  studyEnvSiteSettingsPath,
  studyEnvTriggersPath,
  studyEnvWorkflowPath
} from 'study/StudyEnvironmentRouter'
import {
  userHasPermission,
  useUser
} from 'user/UserProvider'
import { studyPublishingPath } from 'study/StudyRouter'
import { portalUsersPath } from 'user/AdminUserRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCheck,
  faPencil
} from '@fortawesome/free-solid-svg-icons'
import {
  SidebarSection,
  SidebarSectionT
} from 'navbar/SidebarSection'

type SidebarConfig = {
  hidden: string[]
}

type SidebarConfigState = { [key: string]: SidebarConfig }

/** shows menu options related to the current study */
export const StudySidebar = ({ study, portalList, portalShortcode }:
                               { study: Study, portalList: Portal[], portalShortcode: string }) => {
  const navigate = useNavigate()
  const user = useUser()
  const portalId = portalList.find(p => p.shortcode === portalShortcode)?.id

  const sidebarConfigState: SidebarConfigState = parseSidebarConfigState(localStorage.getItem('sidebarConfig') || '{}')

  const sidebarConfig: SidebarConfig = sidebarConfigState[study.shortcode] || { hidden: [] }
  const [hiddenItems, setHiddenItems] = React.useState(sidebarConfig.hidden)
  useEffect(() => {
    setHiddenItems(sidebarConfig.hidden)
  }, [sidebarConfig])

  const [isEditing, setIsEditing] = React.useState(false)

  const toggleHiddenItem = (key: string) => {
    if (hiddenItems.includes(key)) {
      sidebarConfig.hidden = hiddenItems.filter(item => item !== key)
    } else {
      sidebarConfig.hidden = [...hiddenItems, key]
    }
    sidebarConfigState[study.shortcode] = sidebarConfig
    localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfigState))
    setHiddenItems(sidebarConfig.hidden)
  }

  /** updates the selected study -- routes to that study's homepage */
  const setSelectedStudy = (portalShortcode: string, studyShortcode: string) => {
    navigate(studyParticipantsPath(portalShortcode, studyShortcode, 'live'))
  }

  const userHasPermissionInPortal = (permission: string) => {
    if (!portalId) {
      return false
    }
    return userHasPermission(user.user, portalId, permission)
  }

  const sections: SidebarSectionT[] = buildStudySidebarSections(userHasPermissionInPortal,
    portalShortcode,
    study.shortcode)

  return <div className="pt-3">
    <div className="d-flex w-100 align-items-baseline">
      <div className="flex-grow-1">
        <StudySelector
          portalList={portalList}
          selectedShortcode={study.shortcode}
          setSelectedStudy={setSelectedStudy}/>

      </div>
      <button
        className="btn btn-secondary btn-sm text-white m-0 ms-2 hover-opacity-50"
        onClick={() => setIsEditing(!isEditing)}
      >
        {isEditing ? <FontAwesomeIcon icon={faCheck}/> : <FontAwesomeIcon icon={faPencil}/>}
      </button>
    </div>
    {sections.map(section => <SidebarSection
      key={section.key}
      section={section}
      isEditing={isEditing}
      hiddenItems={hiddenItems}
      toggleHiddenItem={toggleHiddenItem}/>)}
  </div>
}

const parseSidebarConfigState = (data: string): SidebarConfigState => {
  try {
    return JSON.parse(data)
  } catch (e) {
    return {}
  }
}

const buildStudySidebarSections = (
  userHasPermissionInPortal: (permission: string) => boolean,
  portalShortcode: string,
  studyShortcode: string): SidebarSectionT[] => {
  const sections: SidebarSectionT[] = [
    {
      key: 'research',
      label: 'Research Coordination',
      items: [
        {
          key: 'participants',
          label: 'Participants',
          link: studyParticipantsPath(portalShortcode, studyShortcode, 'live')
        },
        {
          key: 'kits',
          label: 'Kits',
          link: studyKitsPath(portalShortcode, studyShortcode, 'live')
        },
        {
          key: 'tasks',
          label: 'Tasks',
          link: adminTasksPath(portalShortcode, studyShortcode, 'live')
        },
        {
          key: 'import',
          label: 'Import Participants',
          link: studyEnvImportPath(portalShortcode, studyShortcode, 'sandbox')
        },
        {
          key: 'mailingList',
          label: 'Mailing List',
          link: studyEnvMailingListPath({
            portalShortcode,
            studyShortcode,
            envName: 'live'
          })
        }
      ]
    }
  ]

  const analyticsDataSection: SidebarSectionT = {
    key: 'analytics',
    label: 'Analytics & Data',
    items: [
      {
        key: 'metrics',
        label: 'Study Trends',
        link: studyEnvMetricsPath(portalShortcode, studyShortcode, 'live')
      },
      {
        key: 'dataBrowser',
        label: 'Data Export',
        link: studyEnvDataBrowserPath(portalShortcode, studyShortcode, 'live')
      }
    ]
  }

  if (userHasPermissionInPortal('export_integration')) {
    analyticsDataSection.items.push({
      key: 'exportIntegrations',
      label: 'Export Integrations',
      link: studyEnvExportIntegrationsPath({
        portalShortcode,
        studyShortcode,
        envName: 'live'
      })
    })
  }

  if (userHasPermissionInPortal('tdr_export')) {
    analyticsDataSection.items.push({
      key: 'terraDataRepo',
      label: 'Terra Data Repo',
      link: studyEnvDatasetListViewPath(portalShortcode, studyShortcode, 'live')
    })
  }

  sections.push(analyticsDataSection,
    {
      key: 'design',
      label: 'Design & Build',
      items: [
        {
          key: 'siteContent',
          label: 'Website',
          link: studyEnvSiteContentPath({
            portalShortcode,
            studyShortcode,
            envName: 'sandbox'
          })
        },
        {
          key: 'workflow',
          label: 'Participant Flow',
          link: studyEnvWorkflowPath({
            portalShortcode,
            studyShortcode,
            envName: 'sandbox'
          })
        },
        {
          key: 'forms',
          label: 'Forms & Surveys',
          link: studyEnvFormsPath(portalShortcode, studyShortcode, 'sandbox')
        },
        {
          key: 'triggers',
          label: 'Emails & Automation',
          link: studyEnvTriggersPath({
            portalShortcode,
            studyShortcode,
            envName: 'sandbox'
          })
        }
      ]
    },
    {
      key: 'publish',
      label: 'Publish',
      items: [
        {
          key: 'publishContent',
          label: 'Publish Content',
          link: studyPublishingPath(portalShortcode, studyShortcode)
        },
        {
          key: 'siteSettings',
          label: 'Site Settings',
          link: studyEnvSiteSettingsPath(portalShortcode, studyShortcode, 'live')
        }
      ]
    },
    {
      key: 'manage',
      label: 'Manage',
      items: [
        {
          key: 'teamMembers',
          label: 'Team Members',
          link: portalUsersPath({
            portalShortcode,
            studyShortcode,
            envName: 'live'
          })
        }
      ]
    })

  return sections
}
