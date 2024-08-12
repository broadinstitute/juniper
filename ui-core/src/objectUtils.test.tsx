import {
  findDifferencesBetweenObjects,
  ObjectDiff
} from './objectUtils'

describe('findDifferencesBetweenObjects', () => {
  it('basic', () => {
    const before = {
      'field1': 'old field1',
      'field2': 'old field2'
    }

    const after = {
      'field2': 'some other value',
      'field3': 'new field'
    }

    const diffs: ObjectDiff[] = findDifferencesBetweenObjects(before, after)

    expect(diffs).toHaveLength(3)

    expect(diffs).toContainEqual({
      fieldName: 'field1',
      oldValue: 'old field1',
      newValue: ''
    })

    expect(diffs).toContainEqual({
      fieldName: 'field2',
      oldValue: 'old field2',
      newValue: 'some other value'
    })

    expect(diffs).toContainEqual({
      fieldName: 'field3',
      oldValue: '',
      newValue: 'new field'
    })
  })

  it('finds updates', () => {
    const before = {
      'field1': 'old',
      'field2': 'another old',
      'nested': {
        'field3': 'another another old',
        'deeply nested': {
          'field4': 'another another another old'
        }
      }
    }

    const after = {
      'field1': 'new',
      'field2': 'another new',
      'nested': {
        'field3': 'another another new',
        'deeply nested': {
          'field4': 'another another another new'
        }
      }
    }

    const diffs: ObjectDiff[] = findDifferencesBetweenObjects(before, after)

    expect(diffs).toHaveLength(4)

    expect(diffs).toContainEqual({
      fieldName: 'field1',
      oldValue: 'old',
      newValue: 'new'
    })

    expect(diffs).toContainEqual({
      fieldName: 'field2',
      oldValue: 'another old',
      newValue: 'another new'
    })

    expect(diffs).toContainEqual({
      fieldName: 'nested.field3',
      oldValue: 'another another old',
      newValue: 'another another new'
    })

    expect(diffs).toContainEqual({
      fieldName: 'nested.deeply nested.field4',
      oldValue: 'another another another old',
      newValue: 'another another another new'
    })
  })

  it('finds deletes', () => {
    const before = {
      'field1': 'old',
      'field2': 'another old',
      'nested': {
        'field3': 'another another old',
        'deeply nested': {
          'field4': 'another another another old'
        }
      }
    }

    const after = {
    }

    const diffs: ObjectDiff[] = findDifferencesBetweenObjects(before, after)

    expect(diffs).toHaveLength(4)

    expect(diffs).toContainEqual({
      fieldName: 'field1',
      oldValue: 'old',
      newValue: ''
    })

    expect(diffs).toContainEqual({
      fieldName: 'field2',
      oldValue: 'another old',
      newValue: ''
    })

    expect(diffs).toContainEqual({
      fieldName: 'nested.field3',
      oldValue: 'another another old',
      newValue: ''
    })

    expect(diffs).toContainEqual({
      fieldName: 'nested.deeply nested.field4',
      oldValue: 'another another another old',
      newValue: ''
    })
  })

  it('finds creates', () => {
    const before = {
    }

    const after = {
      'field1': 'new',
      'field2': 'another new',
      'nested': {
        'field3': 'another another new',
        'deeply nested': {
          'field4': 'another another another new'
        }
      }
    }

    const diffs: ObjectDiff[] = findDifferencesBetweenObjects(before, after)

    expect(diffs).toHaveLength(4)

    expect(diffs).toContainEqual({
      fieldName: 'field1',
      oldValue: '',
      newValue: 'new'
    })

    expect(diffs).toContainEqual({
      fieldName: 'field2',
      oldValue: '',
      newValue: 'another new'
    })

    expect(diffs).toContainEqual({
      fieldName: 'nested.field3',
      oldValue: '',
      newValue: 'another another new'
    })

    expect(diffs).toContainEqual({
      fieldName: 'nested.deeply nested.field4',
      oldValue: '',
      newValue: 'another another another new'
    })
  })
})
