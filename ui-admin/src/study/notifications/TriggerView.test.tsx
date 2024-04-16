import React from 'react'
import { mockTrigger, mockPortalContext, mockStudyEnvContext } from 'test-utils/mocking-utils'
import Api from 'api/api'
import { renderWithRouter } from 'test-utils/router-testing-utils'
import TriggerView from './TriggerView'
import { waitFor, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ReactNotifications } from 'react-notifications-component'
import { usePortalLanguage } from '../../portal/useDefaultPortalLanguage'
import { asMockedFn } from '@juniper/ui-core'


jest.mock('portal/useDefaultPortalLanguage', () => ({
  usePortalLanguage: jest.fn()
}))

test('enables updating of email templates', async () => {
  const portalContext = mockPortalContext()
  asMockedFn(usePortalLanguage).mockReturnValue({
    defaultLanguage: {
      languageCode: 'en',
      languageName: 'English'
    },
    supportedLanguages: [{
      languageCode: 'en',
      languageName: 'English'
    }]
  })

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
    <TriggerView studyEnvContext={studyEnvContext} portalContext={portalContext}
      onDelete={jest.fn()}/>
  </div>, [`/${trigger.id}`], ':configId')

  await waitFor(() => expect(findSpy).toHaveBeenCalledTimes(1))
  expect(findSpy).toHaveBeenCalledWith(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
    studyEnvContext.currentEnv.environmentName, trigger.id)

  await waitFor(() => expect(screen.queryByText('Subject')).toBeInTheDocument())
  await userEvent.type(screen.getByLabelText('Subject'), 'blah')
  await userEvent.click(screen.getByText('Save'))
  expect(saveSpy).toHaveBeenCalledTimes(1)
  expect(saveSpy).toHaveBeenCalledWith(studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName,
    studyEnvContext.study.shortcode, trigger.id, {
      ...trigger,
      emailTemplate: {
        ...trigger.emailTemplate,
        id: undefined,  // confirm id and publishedVersion are cleared
        publishedVersion: undefined,
        version: 2, // confirm version is incremented
        localizedEmailTemplates: [{
          subject: 'Mock subjectblah',
          body: 'Mock email message',
          id: undefined,
          language: 'en'
        }]
      }
    })
})
