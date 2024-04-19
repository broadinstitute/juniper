import { useContext } from 'react'
import { PortalContext } from './PortalProvider'
import { useParams } from 'react-router-dom'
import { StudyParams } from 'study/StudyRouter'

/**
 * Returns the default language for the current portal environment.
 */
export function usePortalLanguage() {
  const { portal } = useContext(PortalContext)
  const params = useParams<StudyParams>()
  const envName: string | undefined = params.studyEnv

  const defaultLanguage = portal?.portalEnvironments.find(env =>
    env.environmentName === envName)?.portalEnvironmentConfig.defaultLanguage

  const supportedLanguages = portal?.portalEnvironments.find(env =>
    env.environmentName === envName)?.supportedLanguages || []

  const language = supportedLanguages.find(lang => lang.languageCode === defaultLanguage)

  if (!language) {
    return {
      defaultLanguage: {
        languageCode: 'en',
        languageName: 'English'
      },
      supportedLanguages
    }
  }

  return { defaultLanguage: language, supportedLanguages }
}
