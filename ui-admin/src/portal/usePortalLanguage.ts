import { useStudyEnvParamsFromPath } from '../study/StudyEnvironmentRouter'
import { PortalContext } from './PortalProvider'
import { useContext } from 'react'

/**
 * Returns the default language for the current portal environment.
 */
export function usePortalLanguage() {
  const { portal } = useContext(PortalContext)
  const studyEnvParams = useStudyEnvParamsFromPath()
  const envName: string | undefined = studyEnvParams.envName

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
