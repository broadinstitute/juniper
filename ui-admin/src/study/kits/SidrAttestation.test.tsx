import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { StudyEnvironmentConfig } from '@juniper/ui-core'
import { mockStudyEnvironmentConfig } from 'test-utils/mocking-utils'
import {
  SIDR_KIT_ATTESTATIONS,
  SIDR_RETURN_ONLY_KIT_ATTESTATIONS,
  useSidrAttestation
} from './SidrAttestation'

const TestForm = ({ config, returnOnly }: { config: StudyEnvironmentConfig, returnOnly?: boolean }) => {
  const { attestationAccepted, canSubmit, resetAttestation, AttestationControl } =
    useSidrAttestation(config, returnOnly)
  return <div>
    {AttestationControl}
    <span data-testid="accepted">{String(attestationAccepted)}</span>
    <button disabled={!canSubmit}>Submit</button>
    <button onClick={resetAttestation}>Reset</button>
  </div>
}

const sidrConfig = (): StudyEnvironmentConfig => ({ ...mockStudyEnvironmentConfig(), kittingSystem: 'SIDR' })

describe('useSidrAttestation', () => {
  test('is not required when the study does not use SIDR', () => {
    render(<TestForm config={{ ...mockStudyEnvironmentConfig(), kittingSystem: 'DSM_PEPPER' }}/>)
    expect(screen.queryByLabelText('Order acknowledgements')).not.toBeInTheDocument()
    expect(screen.getByText('Submit')).toBeEnabled()
    expect(screen.getByTestId('accepted')).toHaveTextContent('undefined')
  })

  test('mailed kits have both attestations', () => {
    render(<TestForm config={sidrConfig()}/>)
    SIDR_KIT_ATTESTATIONS.forEach(text => expect(screen.getByText(text)).toBeInTheDocument())
    expect(SIDR_KIT_ATTESTATIONS).toHaveLength(2)
  })

  test('kits that already have a barcode only have the kit use attestation', () => {
    render(<TestForm config={sidrConfig()} returnOnly={true}/>)
    expect(SIDR_RETURN_ONLY_KIT_ATTESTATIONS).toHaveLength(1)
    expect(screen.getByText(SIDR_RETURN_ONLY_KIT_ATTESTATIONS[0])).toBeInTheDocument()
    expect(screen.queryByText(SIDR_KIT_ATTESTATIONS[1])).not.toBeInTheDocument()
  })

  test('has to be checked to submit, and can be reset', async () => {
    render(<TestForm config={sidrConfig()} returnOnly={true}/>)
    expect(screen.getByText('Submit')).toBeDisabled()
    await userEvent.click(screen.getByLabelText('Order acknowledgements'))
    expect(screen.getByText('Submit')).toBeEnabled()
    expect(screen.getByTestId('accepted')).toHaveTextContent('true')
    await userEvent.click(screen.getByText('Reset'))
    expect(screen.getByText('Submit')).toBeDisabled()
    expect(screen.getByLabelText('Order acknowledgements')).not.toBeChecked()
  })
})
