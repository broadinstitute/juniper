import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import RequestKitModal from './RequestKitModal'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { SIDR_KIT_ATTESTATIONS } from 'study/kits/SidrAttestation'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { KittingSystem } from '@juniper/ui-core'

const contextWithKittingSystem = (kittingSystem: KittingSystem): StudyEnvContextT => {
  const context = mockStudyEnvContext()
  context.currentEnv.studyEnvironmentConfig = { ...context.currentEnv.studyEnvironmentConfig, kittingSystem }
  return context
}

describe('RequestKitModal', () => {
  beforeEach(() => {
    jest.spyOn(Api, 'fetchKitTypes').mockResolvedValue([
      { id: 'kt', name: 'SALIVA', displayName: 'Saliva', description: '' }
    ])
    jest.spyOn(Store, 'addNotification').mockImplementation(() => '')
    jest.spyOn(Api, 'createKitRequest').mockResolvedValue({} as never)
  })

  test('does not ask for attestation for DSM/Pepper studies', async () => {
    render(<RequestKitModal studyEnvContext={contextWithKittingSystem('DSM_PEPPER')} enrolleeShortcode="HDSALK"
      onDismiss={jest.fn()} onSubmit={jest.fn()}/>)
    expect(screen.queryByLabelText('Order acknowledgements')).not.toBeInTheDocument()
    await userEvent.click(screen.getByText('Request Kit'))
    await waitFor(() => expect(Api.createKitRequest).toHaveBeenCalledTimes(1))
    expect(Api.createKitRequest).toHaveBeenCalledWith('portalCode', 'fakeStudy', 'sandbox', 'HDSALK',
      expect.objectContaining({ attestationAccepted: undefined }))
  })

  test('requires the SIDR attestation before requesting a kit', async () => {
    const onSubmit = jest.fn()
    render(<RequestKitModal studyEnvContext={contextWithKittingSystem('SIDR')} enrolleeShortcode="HDSALK"
      onDismiss={jest.fn()} onSubmit={onSubmit}/>)
    SIDR_KIT_ATTESTATIONS.forEach(text => expect(screen.getByText(text)).toBeInTheDocument())
    const submit = screen.getByText('Request Kit')
    expect(submit).toBeDisabled()

    await userEvent.click(screen.getByLabelText('Order acknowledgements'))
    expect(submit).toBeEnabled()
    await userEvent.click(submit)

    await waitFor(() => expect(onSubmit).toHaveBeenCalled())
    expect(Api.createKitRequest).toHaveBeenCalledWith('portalCode', 'fakeStudy', 'sandbox', 'HDSALK',
      expect.objectContaining({ kitType: 'SALIVA', attestationAccepted: true }))
  })
})
