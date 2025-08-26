import {
  mockPortalContext,
  mockStudyEnvContext,
  mockTrigger
} from 'test-utils/mocking-utils'
import { renderWithRouter } from '@juniper/ui-core'
import React from 'react'
import { ReactNotifications } from 'react-notifications-component'
import { TriggerDesigner } from 'study/notifications/TriggerDesigner'
import {
  screen,
  waitFor
} from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import Api from 'api/api'

describe('TriggerDesigner', () => {
  it('renders', async () => {
    const studyEnvContext = mockStudyEnvContext()
    const trigger = {
      ...mockTrigger()
    }
    const findSpy = jest.spyOn(Api, 'findTrigger')
      .mockImplementation(() => Promise.resolve(trigger))
    const saveSpy = jest.spyOn(Api, 'updateTrigger')
      .mockImplementation(() => Promise.resolve(trigger))

    renderWithRouter(<div>
      <ReactNotifications />
      <TriggerDesigner studyEnvContext={studyEnvContext} portalContext={mockPortalContext()}
        onDelete={jest.fn()}/>
    </div>, [`/${trigger.id}`], ':triggerId')

    await waitFor(() => expect(findSpy).toHaveBeenCalledTimes(1))
    expect(findSpy).toHaveBeenCalledWith(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, trigger.id)

    await userEvent.type(screen.getByLabelText('Subject'), 'blah')
    await userEvent.click(screen.getByText('Save'))

    expect(saveSpy).toHaveBeenCalledTimes(1)
    expect(saveSpy).toHaveBeenCalledWith(studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName,
      studyEnvContext.study.shortcode, trigger.id, {
        ...trigger,
        emailTemplate: {
          ...trigger.emailTemplate,
          id: 'emailTemplate1',
          version: 1,
          localizedEmailTemplates: [{
            subject: 'Mock subjectblah',
            body: 'Mock email message',
            id: undefined,
            language: 'en'
          }]
        }
      })
  })
})
