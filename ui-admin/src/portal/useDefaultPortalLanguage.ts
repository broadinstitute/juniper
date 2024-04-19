import { useContext } from 'react'
import { PortalContext } from './PortalProvider'
import { useStudyEnvParamsFromPath } from '../study/StudyEnvironmentRouter'

/**
 * Returns the default language for the current portal environment.
 */
export function useDefaultLanguage() {
  const { portal } = useContext(PortalContext)
  const studyEnvParams = useStudyEnvParamsFromPath()
  const envName: string | undefined = studyEnvParams.envName

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
