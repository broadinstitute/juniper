import React from 'react'

import DataChangeRecords from './DataChangeRecords'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen, Screen, waitFor, within } from '@testing-library/react'
import Api, { DataChangeRecord } from '../../api/api'


const assertRowContents = (
  row: Node, ...contains: string[]
) => {
  contains.forEach(shouldBePresent => {
    expect(row).toHaveTextContent(shouldBePresent)
  })
}

const getRows = (screen: Screen): Node[] => {
  const table = screen.getByRole('table')

  // first row group is header
  const tbody = within(table).getAllByRole('rowgroup')[1]
  return within(tbody).getAllByRole('row')
}

test('renders basic data change records', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()

  const dataChangeRecords: DataChangeRecord[] = [
    {
      id: 'dataChangeId1',
      oldValue: 'asdf',
      newValue: 'hjkl',
      createdAt: 200,
      modelName: 'example model 1',
      fieldName: 'example field 1',
      responsibleUserId: 'user1'
    },
    {
      id: 'dataChangeId2',
      oldValue: 'qwer',
      newValue: 'tyuio',
      createdAt: 100,
      modelName: 'example model 2',
      fieldName: 'example field 2',
      responsibleAdminUserId: 'user2'
    }
  ]

  jest.spyOn(Api, 'fetchEnrolleeChangeRecords').mockImplementation(() => Promise.resolve(dataChangeRecords))

  const { RoutedComponent } = setupRouterTest(
    <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext} />)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('example model 1')).toBeInTheDocument()
  })

  const rows = getRows(screen)

  expect(rows).toHaveLength(2)
  assertRowContents(rows[0], 'asdf', 'hjkl', 'example model 1', 'example field 1', 'Participant')
  assertRowContents(rows[1], 'qwer', 'tyuio', 'example model 2', 'example field 2', 'Admin')
})

test('basic object changes', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()

  const dataChangeRecords: DataChangeRecord[] = [
    {
      id: 'dataChangeId1',
      oldValue: '{"changes1": "original text 1", "changes2": "original text 2", "shouldNotChange": "testing"}',
      newValue: '{"changes1": "updated text 1", "changes2": "updated text 2", "shouldNotChange": "testing"}',
      createdAt: 200,
      modelName: 'example model 1',
      responsibleUserId: 'user1'
    }
  ]

  jest.spyOn(Api, 'fetchEnrolleeChangeRecords').mockImplementation(() => Promise.resolve(dataChangeRecords))

  const { RoutedComponent } = setupRouterTest(
    <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext} />)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('changes1', { exact: false })).toBeInTheDocument()
  })

  const rows = getRows(screen)

  expect(rows).toHaveLength(1)
  // creates only 1 row
  assertRowContents(rows[0], 'original text 1', 'updated text 1', 'example model 1', 'changes1', 'Participant',
    'original text 2', 'updated text 2', 'example model 1', 'changes2')
})

test('object deletion changes', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()

  const dataChangeRecords: DataChangeRecord[] = [
    {
      id: 'dataChangeId1',
      oldValue: '{"original": "value"}',
      newValue: '',
      createdAt: 200,
      modelName: 'example model 1',
      responsibleUserId: 'user1'
    }
  ]

  jest.spyOn(Api, 'fetchEnrolleeChangeRecords').mockImplementation(() => Promise.resolve(dataChangeRecords))

  const { RoutedComponent } = setupRouterTest(
    <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext} />)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('original', { exact: false })).toBeInTheDocument()
  })

  const rows = getRows(screen)

  expect(rows).toHaveLength(1)
  // creates 2 rows from 1 data change record
  assertRowContents(rows[0],  'original', 'value', 'example model 1', 'Participant')
})

test('object creation changes', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()

  const dataChangeRecords: DataChangeRecord[] = [
    {
      id: 'dataChangeId1',
      oldValue: '',
      newValue: '{"new": "value"}',
      createdAt: 200,
      modelName: 'example model 1',
      responsibleUserId: 'user1'
    }
  ]

  jest.spyOn(Api, 'fetchEnrolleeChangeRecords').mockImplementation(() => Promise.resolve(dataChangeRecords))

  const { RoutedComponent } = setupRouterTest(
    <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext} />)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('new', { exact: false })).toBeInTheDocument()
  })

  const rows = getRows(screen)

  expect(rows).toHaveLength(1)
  // creates 2 rows from 1 data change record
  assertRowContents(rows[0],  'new', 'value', 'example model 1', 'Participant')
})

test('nested object changes', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()

  const dataChangeRecords: DataChangeRecord[] = [
    {
      id: 'dataChangeId1',
      oldValue: '{"nested": ' +
          '{ "nestedField1": "oldValue1", "doublyNestedObject": {"nestedField2": "oldValue2", "staysSame": "value"}}' +
        '}',
      newValue: '{"nested": ' +
          '{ "nestedField1": "newValue1", "doublyNestedObject": {"nestedField2": "newValue2", "staysSame": "value"}}' +
        '}',
      createdAt: 200,
      modelName: 'example model 1',
      responsibleUserId: 'user1'
    }
  ]

  jest.spyOn(Api, 'fetchEnrolleeChangeRecords').mockImplementation(() => Promise.resolve(dataChangeRecords))

  const { RoutedComponent } = setupRouterTest(
    <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext} />)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('nested.nestedField1', { exact: false })).toBeInTheDocument()
  })

  const rows = getRows(screen)

  expect(rows).toHaveLength(1)
  assertRowContents(rows[0], 'nested.nestedField1', 'oldValue1', 'newValue1', 'example model 1', 'Participant',
    'nested.doublyNestedObject.nestedField2', 'oldValue2', 'newValue2')
})

test('nested object creation', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()

  const dataChangeRecords: DataChangeRecord[] = [
    {
      id: 'dataChangeId1',
      oldValue: '',
      newValue: '{"nested": ' +
        '{"doublyNestedObject": {"newField": "newValue"}}' +
        '}',
      createdAt: 200,
      modelName: 'example model 1',
      responsibleUserId: 'user1'
    }
  ]

  jest.spyOn(Api, 'fetchEnrolleeChangeRecords').mockImplementation(() => Promise.resolve(dataChangeRecords))

  const { RoutedComponent } = setupRouterTest(
    <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext} />)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('nested.doublyNestedObject.newField', { exact: false })).toBeInTheDocument()
  })

  const rows = getRows(screen)

  expect(rows).toHaveLength(1)
  // creates 2 rows from 1 data change record
  assertRowContents(
    rows[0],  'nested.doublyNestedObject.newField', 'newValue', 'example model 1', 'Participant')
})

test('nested object deletion', async () => {
  const enrollee = mockEnrollee()
  const studyEnvContext = mockStudyEnvContext()

  const dataChangeRecords: DataChangeRecord[] = [
    {
      id: 'dataChangeId1',
      oldValue: '{"nested": ' +
        '{"doublyNestedObject": {"oldField": "oldValue"}}' +
        '}',
      newValue: '',
      createdAt: 200,
      modelName: 'example model 1',
      responsibleUserId: 'user1'
    }
  ]

  jest.spyOn(Api, 'fetchEnrolleeChangeRecords').mockImplementation(() => Promise.resolve(dataChangeRecords))

  const { RoutedComponent } = setupRouterTest(
    <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext} />)
  render(RoutedComponent)
  await waitFor(() => {
    expect(screen.getByText('nested.doublyNestedObject.oldField', { exact: false })).toBeInTheDocument()
  })

  const rows = getRows(screen)

  expect(rows).toHaveLength(1)
  // creates 2 rows from 1 data change record
  assertRowContents(
    rows[0],  'nested.doublyNestedObject.oldField', 'oldValue', 'example model 1', 'Participant')
})
