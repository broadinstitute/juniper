import { usePortalLanguage } from './usePortalLanguage'
import { useSearchParams } from 'react-router-dom'
import { useNonNullReactSingleSelect } from 'util/react-select-utils'
import { PortalEnvironmentLanguage } from '@juniper/ui-core'

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
