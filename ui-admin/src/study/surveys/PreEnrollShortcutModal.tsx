import React, { useState, useEffect } from 'react'
import Api, {
  VersionedForm
} from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button, IconButton } from 'components/forms/Button'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { TextInput } from 'components/forms/TextInput'
import { faClipboard } from '@fortawesome/free-solid-svg-icons'
import { Checkbox } from 'components/forms/Checkbox'
import queryString from 'query-string'
import { useConfig } from 'providers/ConfigProvider'

type ReferralSource = {
    referringSite: string
}

type PreEnrollQueryParams = {
    skipPreEnroll?: boolean
    referralSource?: ReferralSource
    preFilledAnswers?: string
}

/** component for selecting versions of a form */
export default function PreEnrollShortcutModal({
  studyEnvContext, workingForm, onDismiss
}: {
    studyEnvContext: StudyEnvContextT,
    workingForm: VersionedForm,
    onDismiss: () => void
}) {
  const zoneConfig = useConfig()

  const currentPortalEnv = studyEnvContext.portal.portalEnvironments.find(env =>
    env.environmentName === studyEnvContext.currentEnv.environmentName)
  const portalUrl= Api.getParticipantLink(currentPortalEnv!.portalEnvironmentConfig, zoneConfig.participantUiHostname,
    studyEnvContext.portal.shortcode, currentPortalEnv!.environmentName)

  const [shortcutUrl, setShortcutUrl] = useState<string | undefined>(portalUrl)
  const [queryParams, setQueryParams] = useState<PreEnrollQueryParams>({})
  const [skipPreEnroll, setSkipPreEnroll] = useState<boolean>(false)
  const [referralSource, setReferralSource] = useState<ReferralSource>()
  const [preFilledAnswers, setPrefilledAnswers] = useState<string>()

  useEffect(() => {
    const params = {
      ...queryParams,
      referralSource: queryParams.referralSource ? JSON.stringify(queryParams.referralSource) : undefined,
      preFilledAnswers: queryParams.preFilledAnswers ? JSON.stringify(queryParams.preFilledAnswers) : undefined
    }

    const allParamsEmpty = Object.values(params).every(v => v === undefined)
    setShortcutUrl(allParamsEmpty ? portalUrl : `${portalUrl}?${queryString.stringify(params)}`)
  }, [queryParams])

  return <Modal show={true} onHide={onDismiss} size="lg">
    <Modal.Header closeButton>
      <Modal.Title>{workingForm.name} - shortcuts</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form>
        <p>
          Participants can be directed to enroll using a customized URL with the following options:
        </p>
        <Checkbox
          infoContent={'If checked, any participant signing up with this link will bypass the pre-enroll survey.'}
          onClick={() => {
            setSkipPreEnroll(!skipPreEnroll)
            setQueryParams(prev => ({
              ...prev,
              skipPreEnroll: !skipPreEnroll ? true : undefined
            }))
          }} label={'Skip Pre-enroll'} checked={skipPreEnroll}/>
        <div className={'my-3'}>
          <TextInput
            type="text"
            infoContent={
              'If set, any participant signing up with this link will have this ' +
                'site recorded as the referring site in their profile. Use this option if you want to track ' +
                'participant enrollments from a partner website or other source such as a newsletter.'
            }
            label={'Referring Site'}
            value={referralSource?.referringSite} onChange={e => {
              const newReferralSource = e ? { referringSite: e } : undefined
              setReferralSource(newReferralSource)
              setQueryParams(prev => ({
                ...prev,
                referralSource: newReferralSource
              }))
            }}/>
        </div>

        <div className={'my-3'}>
          <TextInput
            type="text"
            infoContent={
              `If set, any participant signing up with this link will have these answers 
               pre-filled in the pre-enroll survey. Go to the "Preview" tab in the survey
                builder and fill out the answers you want to pre-fill. Then, click the
                "Copy answers" button to copy the answers to the clipboard. 
                Finally, paste that value here. `
            }
            label={'Pre-filled Answers'}
            value={preFilledAnswers} onChange={e => {
              setPrefilledAnswers(e)
              setQueryParams(prev => ({
                ...prev,
                preFilledAnswers: e
              }))
            }}/>
        </div>

        <div>
          <span className={'fw-semibold'}>URL</span>
          <div className={'d-flex mb-3'}>
            <TextInput type="text" value={shortcutUrl} onChange={e => setShortcutUrl(e)}/>
            <IconButton
              aria-label={'Copy to clipboard'}
              icon={faClipboard}
              onClick={() => navigator.clipboard.writeText(shortcutUrl || 'error: could not create url')}/>
          </div>
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <Button variant="secondary" onClick={onDismiss}>
        Done
      </Button>
    </Modal.Footer>
  </Modal>
}
