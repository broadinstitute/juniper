import React from 'react'
import { StudyEnvironmentConfig } from '@juniper/ui-core'
import InfoPopup from 'components/forms/InfoPopup'
import {
  DocsKey,
  ZendeskLink
} from 'util/zendeskUtils'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  userHasPermission,
  useUser
} from 'user/UserProvider'
import { useNonNullReactSingleSelect } from 'util/react-select-utils'
import Select from 'react-select'


export const StudyEnrollmentSettings = (
  {
    studyEnvContext,
    config,
    updateConfig
  } : {
    studyEnvContext: StudyEnvContextT,
    config: StudyEnvironmentConfig,
    updateConfig: (key: keyof StudyEnvironmentConfig, value: unknown) => void
  }) => {
  const { user } = useUser()

  // @ts-ignore
  const {
    onChange: onTimeZoneChange,
    options: timeZoneOptions,
    selectedOption: selectedTimeZone,
    selectInputId: timeZoneSelectId
  } = useNonNullReactSingleSelect<string>(
    // Intl won't be recognized by TypeScript until we upgrade to 5.1+, so do some funky casting for now
    (Intl as unknown as {supportedValuesOf: (key: string) => string[]}).supportedValuesOf?.('timeZone') ?? [],
    timeZone => ({ label: timeZone, value: timeZone }),
    (opt: string) => updateConfig('timeZone', opt),
    config.timeZone)

  return <div>
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
    <div>
      <label className="form-label">
        eligibility rule <input type="text" className="form-control" size={80} value={config.studyEligibilityRule ?? ''}
          onChange={e => updateConfig('studyEligibilityRule', e.target.value)}/>
      </label>
    </div>
    {
      userHasPermission(user, studyEnvContext.portal.id, 'prototype') && (
        <div>
          <label className="form-label">
            enable family linkage <InfoPopup content={'If checked, allows participants to be grouped by families.'}/>
            <input type="checkbox" checked={config.enableFamilyLinkage} className="ms-2"
              onChange={e => updateConfig('enableFamilyLinkage', e.target.checked)}/>
          </label>
        </div>
      )
    }
    <div>
      <label className="form-label" htmlFor={timeZoneSelectId}>
        Study staff time zone <InfoPopup content={
          <span>
            The &quot;home&quot; time zone for the study.
          This only determines times used in data exported from the portal.
            Participants and staff users see things in their own time zone when using the website.
          </span>}/>
      </label>
      <Select options={timeZoneOptions} onChange={onTimeZoneChange}
        value={selectedTimeZone} inputId={timeZoneSelectId}/>
    </div>
  </div>
}
