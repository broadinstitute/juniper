import { i18nSurveyText, updateI18nSurveyText } from './juniperSurveyUtils'
import { MOCK_ENGLISH_LANGUAGE, MOCK_SPANISH_LANGUAGE } from '../test-utils/mocking-utils'

describe('getI18nSurveyElement', () => {
  it('should return the value if it is just a string', () => {
    expect(i18nSurveyText('Hello world')).toBe('Hello world')
  })

  it('should return default value if the survey element is internationalized and a language is not specified', () => {
    expect(i18nSurveyText({ default: 'Hello world', es: 'Hola mundo' })).toBe('Hello world')
  })

  it('should return correct value if the survey element is internationalized and a language is specified', () => {
    expect(i18nSurveyText({ default: 'Hello world', es: 'Hola mundo' }, 'es')).toBe('Hola mundo')
  })

  it('should return an empty string if the value is undefined', () => {
    expect(i18nSurveyText(undefined)).toBe('')
  })
})

describe('updateI18nSurveyText', () => {
  it('replaces a string with a string for the simplest case', () => {
    expect(updateI18nSurveyText({
      oldValue: 'blah', valueText: 'blah2',
      languageCode: 'en', supportedLanguages: [MOCK_ENGLISH_LANGUAGE]
    })).toBe('blah2')
  })
  it('replaces a string with an object if multiple languages supported', () => {
    expect(updateI18nSurveyText({
      oldValue: 'blah', valueText: 'blah2',
      languageCode: 'en', supportedLanguages: [MOCK_ENGLISH_LANGUAGE, MOCK_SPANISH_LANGUAGE]
    })).toStrictEqual({ en: 'blah2', es: 'blah' })
  })
  it('replaces a value in an object', () => {
    expect(updateI18nSurveyText({
      oldValue: { en: 'blah', es: 'blah' }, valueText: 'blah2',
      languageCode: 'en', supportedLanguages: [MOCK_ENGLISH_LANGUAGE, MOCK_SPANISH_LANGUAGE]
    })).toStrictEqual({ en: 'blah2', es: 'blah' })
  })
})
