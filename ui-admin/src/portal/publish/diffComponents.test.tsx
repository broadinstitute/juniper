import { render, screen } from '@testing-library/react'
import React from 'react'

import { ConfigChangeListView, renderStudyEnvironmentSurvey } from './diffComponents'
import {
  ListChange,
  VersionedConfigChange
} from 'api/api'
import { StudyEnvironmentSurvey } from '@juniper/ui-core/build/types/study'
import { mockConfiguredSurvey } from '../../test-utils/mocking-utils'

const noopUpdate = (changes: ListChange<StudyEnvironmentSurvey, VersionedConfigChange>) => 1
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
        configChanges: [],
        documentChange: { oldVersion: 1, newVersion: 2, oldStableId: 'foo', newStableId: 'bar', changed: true }
      }],
      removedItems: []
    }

    render(<ConfigChangeListView configChangeList={changeList} selectedChanges={emptyChangeList}
                                 setSelectedChanges={noopUpdate} renderItemSummary={renderStudyEnvironmentSurvey}/>)
    expect(screen.queryByText('Added')).toBeNull()
    expect(screen.queryByText('Removed')).toBeNull()
    expect(screen.getByText('Changed: 1')).toBeTruthy()
  })

  it('shows an added survey', () => {
    const configuredSurvey = mockConfiguredSurvey()
    const changeList: ListChange<StudyEnvironmentSurvey, VersionedConfigChange> = {
      addedItems: [configuredSurvey],
      removedItems: [],
      changedItems: []
    }

    render(<ConfigChangeListView configChangeList={changeList} selectedChanges={emptyChangeList}
                                 setSelectedChanges={noopUpdate} renderItemSummary={renderStudyEnvironmentSurvey}/>)
    expect(screen.queryByText('Changed')).toBeNull()
    expect(screen.queryByText('Removed')).toBeNull()
    expect(screen.getByText('Added: 1')).toBeTruthy()
  })
})
