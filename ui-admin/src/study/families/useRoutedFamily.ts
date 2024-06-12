import { useState } from 'react'
import {
  useLocation,
  useParams
} from 'react-router-dom'
import { StudyParams } from '../StudyRouter'
import Api from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import { Family } from '@juniper/ui-core'

export type FamilyParams = StudyParams & {
  familyShortcode: string,
}

/** Handles loading a specific family from the server, based on URL params */
export default function useRoutedFamily(studyEnvContext: StudyEnvContextT) {
  const { portal, study, currentEnv } = studyEnvContext
  const params = useParams<FamilyParams>()
  const familyShortcode = params.familyShortcode as string
  const [family, setFamily] = useState<Family>()
  const location = useLocation()

  const { isLoading, reload } = useLoadingEffect(async () => {
    let family = location?.state?.family
    if (!family) {
      // if we haven't already loaded the family, get it from the server
      family = await Api.getFamily(portal.shortcode, study.shortcode,
        currentEnv.environmentName, familyShortcode)
    } else {
      // if we have, clear the state so the family will be reloaded if the page is refreshed
      window.history.replaceState({}, '')
    }

    setFamily(family)
  }, [familyShortcode])

  return { isLoading, family, reload }
}
