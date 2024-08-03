import React, {
  useEffect,
  useState
} from 'react'
import { renderPageHeader } from 'util/pageUtils'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  Alert,
  AlertLevel,
  AlertTrigger,
  ParticipantDashboardAlert,
  PortalEnvironment,
  useI18n
} from '@juniper/ui-core'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import classNames from 'classnames'

/**
 * Renders a preview for a dashboard alert
 */
export const AlertPreview = ({ alert }: { alert: ParticipantDashboardAlert }) => {
  return <Alert
    title={alert.title}
    level={alert.alertType}
    style={{ maxWidth: 768 }}
    detail={alert.detail}
    className={classNames('shadow-sm')}/>
}

/**
 * Renders an editor for a dashboard alert
 */
export const AlertEditor = ({ initial, isReadOnly, updateAlert, onSave }: {
  initial: ParticipantDashboardAlert,
  isReadOnly: boolean
  updateAlert: (alert: ParticipantDashboardAlert) => void
  onSave: () => void
}) => {
  return <div className="card my-4">
    <div className="card-body">
      <div className="form-group">
        <label htmlFor="trigger">Trigger</label>
        <select className="form-control" disabled={true} id="trigger" value={initial.trigger} onChange={e =>
          updateAlert({ ...initial, trigger: e.target.value as AlertTrigger })
        }>
          <option value="NO_ACTIVITIES_REMAIN">NO_ACTIVITIES_REMAIN</option>
        </select>
      </div>
      <div className="form-group pt-2">
        <label htmlFor="title">Title</label>
        <input type="text" disabled={isReadOnly}
          className="form-control" id="title" value={initial.title} onChange={e =>
            updateAlert({ ...initial, title: e.target.value })
          }
        />
      </div>
      <div className="form-group pt-2">
        <label htmlFor="type">Type</label>
        <select className="form-control" disabled={isReadOnly} id="type" value={initial.alertType} onChange={e =>
          updateAlert({ ...initial, alertType: e.target.value as AlertLevel })
        }>
          <option value="PRIMARY">Primary</option>
          <option value="SUCCESS">Success</option>
          <option value="DANGER">Danger</option>
          <option value="WARNING">Warning</option>
          <option value="INFO">Info</option>
        </select>
      </div>
      <div className="form-group pt-2">
        <label htmlFor="detail">Detail</label>
        <textarea className="form-control" disabled={isReadOnly}
          id="detail" rows={4} value={initial.detail} onChange={e =>
            updateAlert({ ...initial, detail: e.target.value })
          }
        />
      </div>
      {!isReadOnly && <button className="btn btn-primary mt-2" onClick={() => onSave()}>Save</button>}
    </div>
  </div>
}

/**
 * Renders a side-by-side editor and preview of a dashboard alert
 */
export const AlertEditorView = ({ initial, isReadOnly, updateAlert, onSave }: {
  initial: ParticipantDashboardAlert,
  isReadOnly: boolean
  updateAlert: (alert: ParticipantDashboardAlert) => void
  onSave: () => void
}) => {
  return <>
    <div className="col-md-4">
      <AlertEditor initial={initial} isReadOnly={isReadOnly} updateAlert={updateAlert} onSave={onSave}/>
    </div>
    <div className="col-md-8 my-4">
      <AlertPreview alert={initial}/>
    </div>
  </>
}

/** shows configuration of alerts for a portal dashboard. currently this lets users manage their alerts
 * at the portal-level. we'll soon add support for configuring these at the study-level.
 * The intended order of precedence will be study > portal > default
 */
export default function DashboardSettings({ portalContext, currentEnv }:
                                      {portalContext: LoadedPortalContextT, currentEnv: PortalEnvironment}) {
  const { i18n } = useI18n()
  const [isLoading, setIsLoading] = useState(false)
  const [dashboardAlerts, setDashboardAlerts] = useState<ParticipantDashboardAlert[]>([])
  const isReadOnly = currentEnv.environmentName !== 'sandbox'

  const updateAlert = (alert: ParticipantDashboardAlert) => {
    setDashboardAlerts(dashboardAlerts.map(a => a.id === alert.id ? alert : a))
  }

  //Currently, we only support editing the 'NO_ACTIVITIES_REMAIN' alert, but once we support
  //editing other alerts, we'll need to add them here.
  const defaultEditableAlerts = [{
    alertType: 'PRIMARY',
    trigger: 'NO_ACTIVITIES_REMAIN',
    title: i18n('hubUpdateNoActivitiesTitle'),
    detail: i18n('hubUpdateNoActivitiesDetail')
  } as ParticipantDashboardAlert]

  useEffect(() => {
    loadAlerts()
  }, [currentEnv])

  const loadAlerts = async () => {
    setIsLoading(true)
    try {
      const alerts = await Api.listPortalEnvAlerts(portalContext.portal.shortcode, currentEnv.environmentName)

      defaultEditableAlerts.forEach(defaultAlert => {
        const matchedAlert = alerts.find(alert => alert.trigger === defaultAlert.trigger)
        if (!matchedAlert) {
          alerts.push(defaultAlert)
        }
      })

      setDashboardAlerts(alerts)
    } catch {
      Store.addNotification(failureNotification('Error loading dashboard alerts'))
    }
    setIsLoading(false)
  }

  const onSave = (alert: ParticipantDashboardAlert) => async () => {
    setIsLoading(true)
    try {
      const response = alert.id === undefined ?
        await Api.createPortalEnvAlert(
          portalContext.portal.shortcode, currentEnv.environmentName, alert.trigger, alert) :
        await Api.updatePortalEnvAlert(
          portalContext.portal.shortcode, currentEnv.environmentName, alert.trigger, alert)

      await response
      await loadAlerts()
      Store.addNotification(successNotification('Dashboard alert successfully updated'))
    } catch {
      Store.addNotification(failureNotification('Error updating dashboard alert'))
    }
    setIsLoading(false)
  }

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Participant Dashboard') }
    <span className="text-muted">
      Configure the alerts that will be shown on the participant dashboard. These changes
      will be applied to all studies in the portal. Styling is approximate and may vary based
      on your portal style settings.
    </span>
    {isLoading && <LoadingSpinner/>}
    {!isLoading && <div>
      {dashboardAlerts.map((alert, index) => {
        return (
          <div key={index} className="row">
            <AlertEditorView initial={alert} isReadOnly={isReadOnly} updateAlert={updateAlert} onSave={onSave(alert)}/>
          </div>
        )
      })}
    </div> }
  </div>
}
