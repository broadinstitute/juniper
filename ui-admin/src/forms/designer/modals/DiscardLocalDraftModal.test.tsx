import React from 'react'
import { render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import DiscardLocalDraftModal from './DiscardLocalDraftModal'

describe('DiscardLocalDraftModal', () => {
  test('allows discarding drafts on exit', async () => {
    jest.clearAllMocks()
    //Arrange
    const user = userEvent.setup()
    const FORM_DRAFT_KEY = 'surveyDraft_testForm_12'
    render(<DiscardLocalDraftModal
      formDraftKey={FORM_DRAFT_KEY}
      onExit={() => jest.fn()}
      onSaveDraft={() => jest.fn()}
      onDismiss={() => jest.fn()}
    />)

    //Act
    jest.spyOn(Storage.prototype, 'removeItem')
    const exitAndDiscardButton = screen.getByText('Exit & discard draft')
    await user.click(exitAndDiscardButton)

    //Assert
    expect(localStorage.removeItem).toHaveBeenCalledWith(FORM_DRAFT_KEY)
  })

  test('allows keeping drafts on exit', async () => {
    jest.clearAllMocks()
    //Arrange
    const user = userEvent.setup()
    const FORM_DRAFT_KEY = 'surveyDraft_testForm_12'
    render(<DiscardLocalDraftModal
      formDraftKey={FORM_DRAFT_KEY}
      onExit={() => jest.fn()}
      onSaveDraft={() => jest.fn()}
      onDismiss={() => jest.fn()}
    />)

    //Act
    jest.spyOn(Storage.prototype, 'removeItem')
    const exitAndSaveButton = screen.getByText('Exit & save draft')
    await user.click(exitAndSaveButton)

    //Assert
    expect(localStorage.removeItem).not.toHaveBeenCalled()
  })
})
