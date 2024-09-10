import { screen } from '@testing-library/react'
import React from 'react'
import { defaultRenderOpts, mockPortalContext, renderInPortalRouter } from 'test-utils/mocking-utils'
import PortalEnvDiffView, { emptyChangeSet } from './PortalEnvDiffView'

import { PortalEnvironmentChange } from 'api/api'
import { userEvent } from '@testing-library/user-event'


describe('PortalEnvDiff', () => {
  it('handles an empty changeset', () => {
    const { portal } = mockPortalContext()

    renderInPortalRouter(portal, <PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={() => 1}
      sourceEnvName="sourceEnv"
      changeSet={emptyChangeSet}/>)
    expect(screen.queryAllByText('no changes')).toHaveLength(6)
    expect(screen.queryAllByRole('input')).toHaveLength(0)
  })

  it('handles a changeset with one item', async () => {
    const { portal } = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      languageChanges: {
        addedItems: [{
          languageCode: 'es',
          languageName: 'Spanish',
          id: 'es'
        }],
        removedItems: [],
        changedItems: []
      }
    }
    const spyApplyChanges = jest.fn(() => 1)
    renderInPortalRouter(portal, <PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={spyApplyChanges}
      sourceEnvName="sourceEnv"
      changeSet={changeSet}/>, { ...defaultRenderOpts, permissions: ['publish'] })
    expect(screen.queryAllByText('no changes')).toHaveLength(5)
    expect(screen.queryAllByRole('checkbox')).toHaveLength(1)

    // if we save without making any changes, the result should be an empty changeset
    await userEvent.click(screen.getByText(`Publish changes to ${portal.portalEnvironments[0].environmentName}`))
    expect(spyApplyChanges).toHaveBeenCalledTimes(1)
    expect(spyApplyChanges).toHaveBeenCalledWith(emptyChangeSet)

    // if we save after clicking on a language field, we should save with a language change
    await userEvent.click(screen.getByText('Spanish (es)'))
    await userEvent.click(screen.getByText(`Publish changes to ${portal.portalEnvironments[0].environmentName}`))

    expect(spyApplyChanges).toHaveBeenCalledTimes(2)
    expect(spyApplyChanges).toHaveBeenCalledWith(changeSet)
  })

  it('prompts for confirmation when publishing sensitive config changes', async () => {
    const { portal } = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      configChanges: [
        { propertyName: 'password', oldValue: 'secret', newValue: 'moreSecret' }
      ]
    }
    const spyApplyChanges = jest.fn(() => 1)
    renderInPortalRouter(portal, <PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={spyApplyChanges}
      sourceEnvName="sourceEnv"
      changeSet={changeSet}/>, { ...defaultRenderOpts, permissions: ['publish'] })
    expect(screen.queryAllByText('no changes')).toHaveLength(5)
    expect(screen.queryAllByRole('checkbox')).toHaveLength(1)

    // if we save after clicking the password field, we should be prompted for confirmation
    await userEvent.click(screen.getByText('password:'))
    await userEvent.click(screen.getByText(`Publish changes to ${portal.portalEnvironments[0].environmentName}`))

    await screen.getByText('Confirm Publish')

    // at this time, changes should not have been published
    expect(spyApplyChanges).toHaveBeenCalledTimes(0)

    await userEvent.click(screen.getByText('Publish'))

    expect(spyApplyChanges).toHaveBeenCalledTimes(1)
    expect(spyApplyChanges).toHaveBeenCalledWith(changeSet)
  })

  it('hides publish button if no permission', async () => {
    const { portal } = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      configChanges: [
        {
          propertyName: 'password',
          oldValue: 'secret',
          newValue: 'moreSecret'
        }
      ]
    }
    const spyApplyChanges = jest.fn(() => 1)
    renderInPortalRouter(portal, <PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={spyApplyChanges}
      sourceEnvName="sourceEnv"
      changeSet={changeSet}/>, {
      ...defaultRenderOpts,
      permissions: ['somethingElse']
    })
    expect(screen.queryByText(`Publish changes to ${portal.portalEnvironments[0].environmentName}`))
      .not.toBeInTheDocument()
  })

  it('handles changes with siteContent', async () => {
    const { portal } = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      siteContentChange: {
        changed: true,
        newVersion: 2,
        oldVersion: 1,
        newStableId: 'contentId',
        oldStableId: 'contentId'
      }
    }
    const spyApplyChanges = jest.fn(() => 1)
    renderInPortalRouter(portal, <PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={spyApplyChanges}
      sourceEnvName="sourceEnv"
      changeSet={changeSet}/>, {
      ...defaultRenderOpts,
      permissions: ['publish']
    })
    expect(screen.queryAllByText('no changes')).toHaveLength(5)
    expect(screen.queryAllByRole('checkbox')).toHaveLength(1)

    // if we save without making any changes, the result should be an empty changeset
    await userEvent.click(screen.getByText(`Publish changes to ${portal.portalEnvironments[0].environmentName}`))
    expect(spyApplyChanges).toHaveBeenCalledTimes(1)
    expect(spyApplyChanges).toHaveBeenCalledWith(emptyChangeSet)

    // if we save after clicking the password field, we should save with a config change
    await userEvent.click(screen.getByText('contentId v1'))
    await userEvent.click(screen.getByText(`Publish changes to ${portal.portalEnvironments[0].environmentName}`))
    expect(spyApplyChanges).toHaveBeenCalledTimes(2)
    expect(spyApplyChanges).toHaveBeenCalledWith(changeSet)
  })

  it('handles changes with dashboard alerts', async () => {
    const { portal } = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      participantDashboardAlertChanges: [
        {
          trigger: 'NO_ACTIVITIES_REMAIN',
          changes: [
            {
              propertyName: 'title',
              oldValue: 'Old Title',
              newValue: 'New Title'
            },
            {
              propertyName: 'detail',
              oldValue: 'Old message',
              newValue: 'New message'
            }
          ]
        }
      ]
    }
    const spyApplyChanges = jest.fn(() => 1)
    renderInPortalRouter(portal, <PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={spyApplyChanges}
      sourceEnvName="sourceEnv"
      changeSet={changeSet}/>, {
      ...defaultRenderOpts,
      permissions: ['publish']
    })
    const changeCheckboxes = screen.queryAllByRole('checkbox')
    expect(screen.queryAllByText('no changes')).toHaveLength(5)
    expect(changeCheckboxes).toHaveLength(2)

    for (const checkbox of changeCheckboxes) {
      await userEvent.click(checkbox)
    }

    await userEvent.click(screen.getByText(`Publish changes to ${portal.portalEnvironments[0].environmentName}`))

    expect(spyApplyChanges).toHaveBeenCalledTimes(1)
    expect(spyApplyChanges).toHaveBeenCalledWith(changeSet)
  })
})
