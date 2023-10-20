// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { Component } from 'react' // dummy import to make this a React module

describe('questionRegexValidation', () => {
  it('handles whole number ranges up to 500', () => {
    const re = new RegExp('^([1-9][0-9]?|[1-4]\\d\\d|500)$')
    expect(re.test('0')).toBe(false)
    expect(re.test('01')).toBe(false)
    expect(re.test('7')).toBe(true)
    expect(re.test('83')).toBe(true)
    expect(re.test('401.2')).toBe(false)
    expect(re.test('499')).toBe(true)
    expect(re.test('500')).toBe(true)
    expect(re.test('501')).toBe(false)
    expect(re.test('1115')).toBe(false)
  })

  it('handles whole number ranges up to 1000', () => {
    const re = new RegExp('^([1-9][0-9]{0,2}|1000)$')
    expect(re.test('0')).toBe(false)
    expect(re.test('01')).toBe(false)
    expect(re.test('7')).toBe(true)
    expect(re.test('83')).toBe(true)
    expect(re.test('999')).toBe(true)
    expect(re.test('1000')).toBe(true)
    expect(re.test('1001')).toBe(false)
    expect(re.test('1115')).toBe(false)
  })

  it('handles half inches as decimals', () => {
    const re = new RegExp('^([0-9]|1[0-1])?(\\.5)?$')
    expect(re.test('')).toBe(true)
    expect(re.test('0')).toBe(true)
    expect(re.test('01')).toBe(false)
    expect(re.test('0.5')).toBe(true)
    expect(re.test('.5')).toBe(true)
    expect(re.test('7')).toBe(true)
    expect(re.test('7.5')).toBe(true)
    expect(re.test('10')).toBe(true)
    expect(re.test('11.5')).toBe(true)
    expect(re.test('11.6')).toBe(false)
    expect(re.test('12')).toBe(false)
    expect(re.test('115')).toBe(false)
  })
})
