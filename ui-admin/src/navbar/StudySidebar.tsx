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
import React from 'react'
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
import {
  SidebarSection,
  SidebarSectionT
} from 'navbar/SidebarSection'
import { StudySidebarConfig } from 'navbar/AdminSidebar'

/** shows menu options related to the current study */
export const StudySidebar = ({
  study,
  portalList,
  portalShortcode,
  studySidebarConfig,
  isEditingSidebarConfig,
  toggleHiddenItem
}: {
  study: Study,
  portalList: Portal[],
  portalShortcode: string,
  studySidebarConfig: StudySidebarConfig,
  isEditingSidebarConfig: boolean,
  toggleHiddenItem: (key: string) => void
}) => {
  const navigate = useNavigate()
  const user = useUser()
  const portalId = portalList.find(p => p.shortcode === portalShortcode)?.id


  const [hiddenItems, setHiddenItems] = React.useState<string[]>(studySidebarConfig.hidden)
  React.useEffect(() => {
    setHiddenItems(studySidebarConfig.hidden)
  }, [studySidebarConfig])

  const onToggleHiddenItem = (key: string) => {
    if (hiddenItems.includes(key)) {
      setHiddenItems(hiddenItems.filter(i => i !== key))
    } else {
      setHiddenItems([...hiddenItems, key])
    }
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
    <StudySelector
      portalList={portalList}
      selectedShortcode={study.shortcode}
      setSelectedStudy={setSelectedStudy}/>

    {sections.map(section => <SidebarSection
      key={section.key}
      section={section}
      isEditing={isEditingSidebarConfig}
      hiddenItems={hiddenItems}
      toggleHiddenItem={key => {
        toggleHiddenItem(key)
        onToggleHiddenItem(key)
      }}/>)}
  </div>
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
