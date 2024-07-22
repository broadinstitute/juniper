import React from 'react'
import { render, screen } from '@testing-library/react'
import AnswerMappingEditor from './AnswerMappingEditor'
import { assertRowContents, assertRowDoesNotContain, getRows } from 'test-utils/table-utils'
import { userEvent } from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

describe('AnswerMappingEditor', () => {
  test('renders mappings', async () => {
    const { RoutedComponent } = setupRouterTest(<AnswerMappingEditor
      initialAnswerMappings={[{
        id: '',
        questionStableId: 'question1',
        mapType: 'STRING_TO_STRING',
        formatString: '',
        targetType: 'PROFILE',
        surveyId: '',
        errorOnFail: false,
        targetField: 'givenName'
      }]}
      onChange={jest.fn()}
      formContent={{
        pages: [],
        title: ''
      }}
    />)
    render(RoutedComponent)

    const rows = getRows(screen)
    // first row is data, second row is 'add new' row
    expect(rows).toHaveLength(2)

    assertRowContents(rows[0], 'question1', 'Text', 'Profile', 'givenName')
    assertRowDoesNotContain(rows[0], 'STRING_TO_STRING', 'PROFILE')
  })

  test('deletes mapping', async () => {
    const onChange = jest.fn()

    const { RoutedComponent } = setupRouterTest(<AnswerMappingEditor
      initialAnswerMappings={[{
        id: '',
        questionStableId: 'question1',
        mapType: 'STRING_TO_STRING',
        formatString: '',
        targetType: 'PROFILE',
        surveyId: '',
        errorOnFail: false,
        targetField: 'givenName'
      }]}
      onChange={onChange}
      formContent={{
        pages: [],
        title: ''
      }}
    />)
    render(RoutedComponent)

    const rows = getRows(screen)
    // first row is data, second row is 'add new' row
    expect(rows).toHaveLength(2)

    // only one on screen, so we can grab the first one
    const deleteButton = screen.getByLabelText('Delete Answer Mapping')

    // open modal
    await userEvent.click(deleteButton)

    // confirm delete
    await userEvent.click(screen.getByText('Yes'))

    // no answer mappings left
    expect(onChange).toHaveBeenCalledWith([], [])
  })

  test('adds new mapping', async () => {
    const onChange = jest.fn()

    const { RoutedComponent } = setupRouterTest(<AnswerMappingEditor
      initialAnswerMappings={[{
        id: '',
        questionStableId: 'question1',
        mapType: 'STRING_TO_STRING',
        formatString: '',
        targetType: 'PROFILE',
        surveyId: '',
        errorOnFail: false,
        targetField: 'givenName'
      }]}
      onChange={onChange}
      formContent={{
        pages: [],
        title: ''
      }}
    />)
    render(RoutedComponent)

    const rowsBefore = getRows(screen)
    // first row is data, second row is 'add new' row
    expect(rowsBefore).toHaveLength(2)

    const addNewMapping = screen.getByLabelText('Create New Answer Mapping')

    await userEvent.click(addNewMapping)

    // enter details
    await userEvent.type(
      screen.getByLabelText('New Answer Mapping Question ID'),
      'question2{enter}')
    await userEvent.type(
      screen.getByLabelText('New Answer Mapping Target Type'),
      'prof{enter}') // profile
    await userEvent.type(
      screen.getByLabelText('New Answer Mapping Target Field'),
      'familyName{enter}')

    // save
    await userEvent.click(screen.getByLabelText('Accept New Answer Mapping'))

    const rowsAfter = getRows(screen)
    // first row is data, second row is 'add new' row
    expect(rowsAfter).toHaveLength(3)

    assertRowContents(rowsAfter[0], 'question1', 'Text', 'Profile', 'givenName')
    assertRowContents(rowsAfter[1], 'question2', 'Text', 'Profile', 'familyName')
  })
})
