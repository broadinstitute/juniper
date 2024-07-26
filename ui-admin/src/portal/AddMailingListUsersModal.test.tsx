import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockPortalContext, mockPortalEnvironment } from '../test-utils/mocking-utils'
import { AddMailingListUsersModal, parseContactCsv } from './AddMailingListUsersModal'
import { userEvent } from '@testing-library/user-event'

describe('AddMailingListUsersModal', () => {
  it('renders a modal with contact fields', () => {
    render(<AddMailingListUsersModal
      portalContext={mockPortalContext()}
      portalEnv={mockPortalEnvironment('sandbox')}
      show={true}
      onClose={jest.fn()}
      reload={jest.fn()}
    />)

    expect(screen.getByText('Add Users')).toBeInTheDocument()
    expect(screen.getByText('Import CSV')).toBeInTheDocument()
    expect(screen.getByText('Name')).toBeInTheDocument()
    expect(screen.getByText('Email Address*')).toBeInTheDocument()
  })

  it('allows the user to Import a CSV file', async () => {
    const reload = jest.fn()
    render(<AddMailingListUsersModal
      portalContext={mockPortalContext()}
      portalEnv={mockPortalEnvironment('sandbox')}
      show={true}
      onClose={jest.fn()}
      reload={reload}
    />)
    const fileInput = screen.getByTestId('fileInput')
    expect(fileInput).toBeInTheDocument()

    const csvContent = 'Jonas Salk,jsalk@test.com\nBasic Done,basic@test.com'
    await userEvent.upload(fileInput, new File([csvContent], 'contacts.csv', { type: 'text/csv' }))

    expect(await screen.findByDisplayValue('Jonas Salk')).toBeInTheDocument()
    expect(await screen.findByDisplayValue('jsalk@test.com')).toBeInTheDocument()
    expect(await screen.findByDisplayValue('Basic Done')).toBeInTheDocument()
    expect(await screen.findByDisplayValue('basic@test.com')).toBeInTheDocument()
  })
})

describe('parseContactCsv', () => {
  it('parses a CSV string into an array of contacts', () => {
    const csvContent = 'Jonas Salk,jsalk@test.com\nBasic Done,basic@test.com\n,noname@test.com'
    const contacts = parseContactCsv(csvContent)

    expect(contacts).toEqual([
      { name: 'Jonas Salk', email: 'jsalk@test.com' },
      { name: 'Basic Done', email: 'basic@test.com' },
      { name: '', email: 'noname@test.com' }
    ])
  })
})
