import {
  DefaultParticipantSearchState,
  ParticipantSearchState,
  toExpression
} from './participantSearchUtils'

describe('toExpression', () => {
  it('should return is subject on default', () => {
    const result = toExpression(DefaultParticipantSearchState)
    expect(result).toEqual('{enrollee.subject} = true')
  })
  it('filters basic search', () => {
    const keywordSearch = { ...DefaultParticipantSearchState, keywordSearch: 'test' }
    const result = toExpression(keywordSearch)
    expect(result).toEqual('(lower({profile.name}) contains lower(\'test\') '
      + 'or lower({profile.contactEmail}) contains lower(\'test\') '
      + 'or lower({enrollee.shortcode}) contains lower(\'test\')) '
      + 'and {enrollee.subject} = true')
  })
  it('filters subject, consented', () => {
    const searchState = { ...DefaultParticipantSearchState, subject: false, consented: true }
    const result = toExpression(searchState)
    expect(result).toEqual('{enrollee.subject} = false and {enrollee.consented} = true')
  })
  it('filters all subjects', () => {
    const searchState = { ...DefaultParticipantSearchState, subject: undefined }
    const result = toExpression(searchState)
    expect(result).toEqual('')
  })
  it('filters tasks', () => {
    const searchState: ParticipantSearchState = {
      ...DefaultParticipantSearchState,
      tasks: [{ task: 'my_task', status: 'COMPLETE' }, { task: 'my_other_task', status: 'CREATED' }]
    }
    const result = toExpression(searchState)
    expect(result).toEqual('{enrollee.subject} = true and '
      + '({task.my_task.status} = \'COMPLETE\' and '
      + '{task.my_other_task.status} = \'CREATED\')')
  })
  it('filters latestKitStatus', () => {
    const searchState: ParticipantSearchState = {
      ...DefaultParticipantSearchState,
      latestKitStatus: ['CREATED', 'ERRORED']
    }
    const result = toExpression(searchState)
    expect(result).toEqual('{enrollee.subject} = true '
      + 'and ({latestKit.status} = \'CREATED\' or {latestKit.status} = \'ERRORED\')')
  })
  it('filters sexAtBirth', () => {
    const searchState: ParticipantSearchState = {
      ...DefaultParticipantSearchState,
      sexAtBirth: ['female', 'preferNotToAnswer']
    }

    const result = toExpression(searchState)
    expect(result).toEqual(
      '{enrollee.subject} = true '
      + 'and ({profile.sexAtBirth} = \'female\' '
      + 'or {profile.sexAtBirth} = \'preferNotToAnswer\')')
  })
  it('filters minAge, maxAge', () => {
    const searchState: ParticipantSearchState = {
      ...DefaultParticipantSearchState,
      minAge: 10,
      maxAge: 20
    }
    const result = toExpression(searchState)
    expect(result).toEqual('{enrollee.subject} = true and {age} >= 10 and {age} <= 20')
  })
  it('filters just minAge/maxAge', () => {
    const minAgeSearchState: ParticipantSearchState = {
      ...DefaultParticipantSearchState,
      minAge: 10
    }
    const minAgeResult = toExpression(minAgeSearchState)
    expect(minAgeResult).toEqual('{enrollee.subject} = true and {age} >= 10')

    const maxAgeSearchState: ParticipantSearchState = {
      ...DefaultParticipantSearchState,
      maxAge: 20
    }
    const maxAgeResult = toExpression(maxAgeSearchState)
    expect(maxAgeResult).toEqual('{enrollee.subject} = true and {age} <= 20')
  })
  it('filters custom', () => {
    const searchState: ParticipantSearchState = {
      ...DefaultParticipantSearchState,
      custom: '{answer.survey.question} contains \'asdf\''
    }
    const result = toExpression(searchState)
    expect(result).toEqual('{enrollee.subject} = true and ({answer.survey.question} contains \'asdf\')')
  })
  it('filters all fields', () => {
    const searchState: ParticipantSearchState = {
      keywordSearch: 'test',
      subject: false,
      consented: true,
      minAge: 10,
      maxAge: 20,
      sexAtBirth: ['female'],
      tasks: [{ task: 'my_task', status: 'COMPLETE' }],
      custom: '{age} != 15',
      latestKitStatus: ['ERRORED']
    }

    const result = toExpression(searchState)
    expect(result).toEqual('(lower({profile.name}) contains lower(\'test\') '
      + 'or lower({profile.contactEmail}) contains lower(\'test\') '
      + 'or lower({enrollee.shortcode}) contains lower(\'test\')) '
      + 'and {enrollee.subject} = false and {enrollee.consented} = true '
      + 'and {age} >= 10 and {age} <= 20 and ({profile.sexAtBirth} = \'female\') '
      + 'and ({task.my_task.status} = \'COMPLETE\') and ({latestKit.status} = \'ERRORED\') '
      + 'and ({age} != 15)')
  })
})
