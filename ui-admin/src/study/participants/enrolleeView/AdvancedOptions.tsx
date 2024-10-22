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
import { useNonNullReactSingleSelect } from '../../../util/react-select-utils'
import Select from 'react-select'


const withdrawalReasons = [
  { value: 'PARTICIPANT_REQUEST', label: 'Participant request' },
  { value: 'DUPLICATE', label: 'Duplicate of another participant' },
  { value: 'TESTING', label: 'Participant was only created for testing purposes' }
]

/** shows not-commonly-used enrollee functionality */
export default function AdvancedOptions({ enrollee, studyEnvContext }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT}) {
  const [shortcodeConfirm, setShortcodeConfirm] = useState('')
  const navigate = useNavigate()
  const [reason, setReason] = useState('PARTICIPANT_REQUEST')
  const [note, setNote] = useState('')

  const withdrawString = `withdraw ${enrollee.profile.givenName} ${enrollee.profile.familyName}`
  const canWithdraw = shortcodeConfirm === withdrawString

  const { selectInputId, selectedOption, options, onChange } = useNonNullReactSingleSelect(
    withdrawalReasons.map(r => r.value),
    reasonString => withdrawalReasons.find(r => r.value === reasonString)!,
    setReason, reason)

  const doWithdraw = async () => {
    doApiLoad(async () => {
      await Api.withdrawEnrollee(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, enrollee.shortcode, { reason, note })
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
      <div className="my-2">
        <label htmlFor={selectInputId}>Reason for withdrawal:</label>
        <Select inputId={selectInputId} value={selectedOption} onChange={onChange} options={options}/>
      </div>
      <div className="my-2">
        <label htmlFor="withdrawNote">Note:</label>
        <textarea value={note} onChange={e => setNote(e.target.value)}
          className="form-control"/>
      </div>
      <div className="my-4">
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
