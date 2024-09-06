import React from 'react'
import {
  Study,
  StudyEnvironmentConfig
} from '@juniper/ui-core'
import InfoPopup from 'components/forms/InfoPopup'
import {
  DocsKey,
  ZendeskLink
} from 'util/zendeskUtils'

export const StudyEnrollmentSettings = (
  {
    study,
    config,
    updateConfig,
    saveConfig
  } : {
    study: Study,
    config: StudyEnvironmentConfig,
    updateConfig: (key: keyof StudyEnvironmentConfig, value: unknown) => void,
    saveConfig:  () => void
  }) => {
  return <div>
    <h2 className="h4">{study.name} study configuration</h2>
    <p>Configure whether participants can access study content, such as surveys and consents.</p>
    <div>
      <label className="form-label">
        password protected <input type="checkbox" checked={config.passwordProtected}
          onChange={e => updateConfig('passwordProtected', e.target.checked)}/>
      </label>
    </div>
    <div>
      <label className="form-label">
        password <input type="text" className="form-control" value={config.password ?? ''}
          onChange={e => updateConfig('password', e.target.value)}/>
      </label>
    </div>
    <div>
      <label className="form-label">
        accepting enrollment <InfoPopup content={`Uncheck this to hide the study from participants who have not
        yet joined and prevent any further enrollments.`}/>
        <input type="checkbox" checked={config.acceptingEnrollment} className="ms-2"
          onChange={e => updateConfig('acceptingEnrollment', e.target.checked)}/>
      </label>
    </div>

    <div>
      <label className="form-label">
        accepting proxy enrollment <InfoPopup content={
          <span>
            Enables enrolling as a proxy on behalf of a dependent.
            Requires extensive changes to your pre-enroll; see the
            <ZendeskLink doc={DocsKey.PROXY_ENROLLMENT}> proxy enrollment documentation </ZendeskLink>
            for more details.
          </span>}/>
        <input type="checkbox" checked={config.acceptingProxyEnrollment} className="ms-2"
          onChange={e => updateConfig('acceptingProxyEnrollment', e.target.checked)}/>
      </label>
    </div>

    <button className="btn btn-primary" onClick={saveConfig}>Save</button>
  </div>
}
