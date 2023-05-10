import React, { useState } from 'react'
import Api, { Enrollee } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useUser } from 'user/UserProvider'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { useNavigate } from 'react-router-dom'

/** shows not-commonly-used enrollee functionality */
export default function AdvancedOptions({ enrollee, studyEnvContext }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT}) {
  const [shortcodeConfirm, setShortcodeConfirm] = useState('')
  const { user } = useUser()
  const navigate = useNavigate()
  const canWithdraw = shortcodeConfirm === `withdraw ${enrollee.profile.contactEmail}`
  const doWithdraw = async () => {
    if (!user.superuser) {
      Store.addNotification(failureNotification('you are not authorized to withdraw participants'))
      return
    }
    try {
      await Api.withdrawEnrollee(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, enrollee.shortcode)
      Store.addNotification(successNotification('withdrawal succeeded'))
      navigate(studyEnvContext.currentEnvPath)
    } catch (e) {
      Store.addNotification(failureNotification('withdrawal failed'))
    }
  }
  return <div className="p4">
    <form onSubmit={e => e.preventDefault()}>
      <h3 className="h5">Withdraw enrollee: {enrollee.profile.givenName} {enrollee.profile.familyName}</h3>
      <div>contact email: {enrollee.profile.contactEmail}</div>
      <div className="my-3">
        <label>
          Confirm by typing &quot;withdraw {enrollee.profile.contactEmail}&quot; below.<br/>
          <strong>Withdrawal is permanent!</strong>
          <input type="text" className="form-control" value={shortcodeConfirm}
            onChange={e => setShortcodeConfirm(e.target.value)}/>
        </label>
      </div>
      <button type="button" className="btn btn-primary" onClick={doWithdraw} disabled={!canWithdraw}>Withdraw</button>
    </form>

  </div>
}
