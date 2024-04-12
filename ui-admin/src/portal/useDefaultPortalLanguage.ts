import { useContext } from 'react'
import { PortalContext } from './PortalProvider'
import { useParams } from 'react-router-dom'
import { StudyParams } from 'study/StudyRouter'

/**
 * Returns the default language for the current portal environment.
 */
export function useDefaultLanguage() {
  const { portal } = useContext(PortalContext)
  const params = useParams<StudyParams>()
  const envName: string | undefined = params.studyEnv

  const defaultLanguage = portal?.portalEnvironments.find(env =>
    env.environmentName === envName)?.portalEnvironmentConfig.defaultLanguage

  const language = portal?.portalEnvironments.find(env =>
    env.environmentName === envName)?.supportedLanguages.find(lang => lang.languageCode === defaultLanguage)

  if (!language) {
    return {
      languageCode: 'en',
      languageName: 'English'
    }
  }

  return language
}
