import { usePortalLanguage } from './usePortalLanguage'
import { useSearchParams } from 'react-router-dom'
import { useNonNullReactSingleSelect } from 'util/react-select-utils'
import {
  PortalEnvironmentLanguage,
  useI18n
} from '@juniper/ui-core'
import {
  useEffect,
  useRef
} from 'react'

const LANGUAGE_PARAM_KEY = 'lang'


/** hook for supporting a language select dropdown that is controlled by a URL parameter */
export default function useLanguageSelectorFromParam() {
  const { defaultLanguage, supportedLanguages } = usePortalLanguage()

  const [searchParams, setSearchParams] = useSearchParams()
  const selectedLanguageCode = searchParams.get(LANGUAGE_PARAM_KEY) ?? defaultLanguage.languageCode
  const selectedLanguage = supportedLanguages.find(portalLang =>
    portalLang.languageCode === selectedLanguageCode)

  const setSelectedLanguage = (language: PortalEnvironmentLanguage | undefined) => {
    if (!language) {
      return
    }
    setSearchParams(searchParams => {
      searchParams.set(LANGUAGE_PARAM_KEY, language.languageCode)
      return searchParams
    })
  }

  // keep the system text in previews (nav labels, buttons, etc...) in the language being edited. The admin's
  // I18nProvider doesn't persist the language, so this only lasts for the page; on reload the URL param restores it
  const { selectedLanguage: i18nLanguage, changeLanguage } = useI18n()
  const currentLanguageCode = (selectedLanguage ?? defaultLanguage).languageCode
  useEffect(() => {
    if (currentLanguageCode !== i18nLanguage) {
      changeLanguage(currentLanguageCode)
    }
  }, [currentLanguageCode, i18nLanguage])

  // when leaving the editor, go back to the portal default so the rest of the admin UI isn't left in
  // the previewed language
  const restoreLanguage = useRef(() => {})
  restoreLanguage.current = () => changeLanguage(defaultLanguage.languageCode)
  useEffect(() => () => restoreLanguage.current(), [])

  const {
    onChange: languageOnChange, options: languageOptions,
    selectedOption: selectedLanguageOption, selectInputId: selectLanguageInputId
  } =
    useNonNullReactSingleSelect(
      supportedLanguages,
      language => ({ label: language?.languageName, value: language }),
      setSelectedLanguage,
      selectedLanguage
    )

  return {
    languageOnChange, languageOptions, selectedLanguageOption, selectLanguageInputId,
    defaultLanguage, currentLanguage: selectedLanguage ?? defaultLanguage
  }
}
