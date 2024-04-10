import { getI18nSurveyElement } from './juniperSurveyUtils'

describe('getI18nSurveyElement', () => {
  it('should return the value if it is just a string', () => {
    expect(getI18nSurveyElement('Hello world')).toBe('Hello world')
  })

  it('should return default value if the survey element is internationalized and a language is not specified', () => {
    expect(getI18nSurveyElement({ default: 'Hello world', es: 'Hola mundo' })).toBe('Hello world')
  })

  it('should return correct value if the survey element is internationalized and a language is specified', () => {
    expect(getI18nSurveyElement({ default: 'Hello world', es: 'Hola mundo' }, 'es')).toBe('Hola mundo')
  })

  it('should return an empty string if the value is undefined', () => {
    expect(getI18nSurveyElement(undefined)).toBe('')
  })
})
