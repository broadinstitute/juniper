import React, { useState } from 'react'
import Api from 'api/api'
import { participantListPath, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { useNavigate } from 'react-router-dom'
import { doApiLoad } from 'api/api-utils'
import { Button } from 'components/forms/Button'
import { Enrollee } from '@juniper/ui-core'
import { DocsKey, ZendeskLink } from 'util/zendeskUtils'

/** shows not-commonly-used enrollee functionality */
export default function AdvancedOptions({ enrollee, studyEnvContext }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT}) {
  const [shortcodeConfirm, setShortcodeConfirm] = useState('')
  const navigate = useNavigate()
  const withdrawString = `withdraw ${enrollee.profile.givenName} ${enrollee.profile.familyName}`
  const canWithdraw = shortcodeConfirm === withdrawString
  const doWithdraw = async () => {
    doApiLoad(async () => {
      await Api.withdrawEnrollee(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, enrollee.shortcode)
      Store.addNotification(successNotification('withdrawal succeeded'))
      navigate(participantListPath(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName))
    })
  }
  return <div className="p4">
    <form onSubmit={e => e.preventDefault()}>
      <h3 className="h5">Withdraw enrollee: {enrollee.profile.givenName} {enrollee.profile.familyName}</h3>
      <div>contact email: {enrollee.profile.contactEmail}</div>
      <div className="my-3">
        <strong>Withdrawal is permanent!</strong>  Read more about the
        <ZendeskLink doc={DocsKey.WITHDRAWAL}> withdrawal process</ZendeskLink>.
      </div>
      <div className="my-3">
        <label>
          Confirm by typing &quot;{withdrawString}&quot; below.<br/>

          <input type="text" className="form-control" value={shortcodeConfirm}
            onChange={e => setShortcodeConfirm(e.target.value)}/>
        </label>
      </div>
      <Button variant="primary" className="btn btn-primary"
        onClick={doWithdraw} disabled={!canWithdraw}>Withdraw</Button>
    </form>

  </div>
}
