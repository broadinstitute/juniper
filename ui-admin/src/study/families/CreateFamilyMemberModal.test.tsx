import React from 'react'
import {
  render,
  screen,
  waitFor
} from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import { select } from 'react-select-event'
import {
  mockEnrollee,
  mockFamily,
  mockStudyEnvContext
} from 'test-utils/mocking-utils'
import { CreateFamilyMemberModal } from './CreateFamilyMemberModal'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import {
  Family,
  MockI18nProvider
} from '@juniper/ui-core'

const familyWithTwoMembers = (): Family => {
  const proband = { ...mockEnrollee(), id: 'probandId', shortcode: 'PROBAN' }
  const sibling = { ...mockEnrollee(), id: 'siblingId', shortcode: 'SIBLNG' }
  return { ...mockFamily(), members: [proband, sibling], proband, probandEnrolleeId: proband.id }
}

const renderModal = () => {
  render(<MockI18nProvider>
    <CreateFamilyMemberModal
      family={familyWithTwoMembers()}
      studyEnvContext={mockStudyEnvContext()}
      onCreated={jest.fn()}
      onClose={jest.fn()}
    />
  </MockI18nProvider>)
}

describe('CreateFamilyMemberModal', () => {
  beforeEach(() => {
    jest.spyOn(Store, 'addNotification').mockReturnValue('')
  })

  afterEach(() => {
    jest.restoreAllMocks()
  })

  test('defaults to the proband as the enrollee and the new member as the target', () => {
    renderModal()
    expect(screen.getByText('PROBAN', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('New member')).toBeInTheDocument()
    expect(screen.getByText('Create')).toBeDisabled()
  })

  test('creates the new member as the target of the proband', async () => {
    const createSpy = jest.spyOn(Api, 'createFamilyMember').mockResolvedValue(mockEnrollee())
    renderModal()
    await select(screen.getByLabelText('Relationship'), 'Sister')
    await userEvent.click(screen.getByText('Create'))
    await userEvent.type(screen.getByPlaceholderText('Why are you making this change?'), 'because')
    await userEvent.click(screen.getByText('Save & Complete Change'))

    await waitFor(() => expect(createSpy).toHaveBeenCalledTimes(1))
    expect(createSpy.mock.calls[0][4]).toEqual(expect.objectContaining({
      existingEnrolleeShortcode: 'PROBAN',
      newMemberIsTarget: true,
      familyRelationship: 'Sister'
    }))
  })

  test('moves the new member to the other side when an existing member is chosen as the target', async () => {
    const createSpy = jest.spyOn(Api, 'createFamilyMember').mockResolvedValue(mockEnrollee())
    renderModal()
    await select(screen.getByLabelText('Target'), /SIBLNG/)
    await select(screen.getByLabelText('Relationship'), 'Father')
    await userEvent.click(screen.getByText('Create'))
    await userEvent.type(screen.getByPlaceholderText('Why are you making this change?'), 'because')
    await userEvent.click(screen.getByText('Save & Complete Change'))

    await waitFor(() => expect(createSpy).toHaveBeenCalledTimes(1))
    // the new member is now the enrollee ("new member is the Father of SIBLNG")
    expect(createSpy.mock.calls[0][4]).toEqual(expect.objectContaining({
      existingEnrolleeShortcode: 'SIBLNG',
      newMemberIsTarget: false,
      familyRelationship: 'Father'
    }))
  })
})
