import { render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { ConfigChangeListView, renderStudyEnvironmentSurvey } from './diffComponents'
import {
  ListChange,
  VersionedConfigChange
} from 'api/api'
import { StudyEnvironmentSurvey } from '@juniper/ui-core'
import { mockConfiguredSurvey, mockSurvey } from 'test-utils/mocking-utils'

const noopUpdate = () => 1
const emptyChangeList: ListChange<StudyEnvironmentSurvey, VersionedConfigChange> = {
  addedItems: [],
  changedItems: [],
  removedItems: []
}

describe('configChangeList', () => {
  it('doesnt list unchanged items', () => {
    const { baseElement } = render(<ConfigChangeListView configChangeList={emptyChangeList}
      selectedChanges={emptyChangeList}
      setSelectedChanges={noopUpdate}
      renderItemSummary={renderStudyEnvironmentSurvey}/>)
    expect(screen.queryByText('Added')).toBeNull()
    expect(screen.queryByText('Removed')).toBeNull()
    expect(screen.queryByText('Changed')).toBeNull()
    expect(baseElement.innerText).toBeUndefined()
  })

  it('shows a changed survey version', () => {
    const changeList: ListChange<StudyEnvironmentSurvey, VersionedConfigChange> = {
      addedItems: [],
      changedItems: [{
        sourceId: 'someGuid',
        destId: 'aotherGuid',
        configChanges: [],
        documentChange: { oldVersion: 1, newVersion: 2, oldStableId: 'foo', newStableId: 'bar', changed: true }
      }],
      removedItems: []
    }

    render(<ConfigChangeListView configChangeList={changeList} selectedChanges={emptyChangeList}
      setSelectedChanges={noopUpdate} renderItemSummary={renderStudyEnvironmentSurvey}/>)
    expect(screen.queryByText('Added')).toBeNull()
    expect(screen.queryByText('Removed')).toBeNull()
    expect(screen.getByText('Changed')).toBeTruthy()
  })

  it('handles added surveys', async () => {
    const configuredSurvey1 = {
      ...mockConfiguredSurvey(),
      id: 'guid1',
      survey: {
        ...mockSurvey(), stableId: 'survey1', name: 'Survey 1'
      }
    }
    const configuredSurvey2 = {
      ...mockConfiguredSurvey(),
      id: 'guid2',
      survey: {
        ...mockSurvey(), stableId: 'survey2', name: 'Survey 2'
      }
    }
    const changeList: ListChange<StudyEnvironmentSurvey, VersionedConfigChange> = {
      addedItems: [configuredSurvey1, configuredSurvey2],
      removedItems: [],
      changedItems: []
    }
    const spySetChanges = jest.fn(() => 1)

    render(<ConfigChangeListView configChangeList={changeList} selectedChanges={emptyChangeList}
      setSelectedChanges={spySetChanges} renderItemSummary={renderStudyEnvironmentSurvey}/>)
    expect(screen.queryByText('Changed')).toBeNull()
    expect(screen.queryByText('Removed')).toBeNull()
    expect(screen.getByText('Added')).toBeTruthy()

    await userEvent.click(screen.getByText('Survey 1'))
    expect(spySetChanges).toHaveBeenCalledTimes(1)
    expect(spySetChanges).toHaveBeenCalledWith({
      ...emptyChangeList,
      addedItems: [configuredSurvey1]
    })
  })
})
